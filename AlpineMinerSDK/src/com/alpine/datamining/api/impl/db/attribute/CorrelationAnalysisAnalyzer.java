/**
 * ClassName CoorlationAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.CorrelationAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;

/**
 *Eason
 */
public class CorrelationAnalysisAnalyzer extends AbstractDBAttributeAnalyzer{
	private static Logger logger= Logger.getLogger(CorrelationAnalysisAnalyzer.class);
	
	
	ArrayList<String> list = new ArrayList<String>();

	private int columnsStatsCount = 6;
	// eachTimeColumnsCount = 1600/6
	private int eachTimeColumnsCount = 1;	

	private ArrayList<String> XY = new ArrayList<String>();
	private ArrayList<String> resultColumns = new ArrayList<String>(); 
	private String dbType;

	private ISqlGeneratorMultiDB sqlGenerator;
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		DataSet dataSet;
		Statement st = null;
		try {
			dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig());
			eachTimeColumnsCount = caculateEachTimeColumnsCount(dataSet);
			
			Iterator<Column> iter = dataSet.getColumns().allColumns();
			while (iter.hasNext()) {
				Column att = iter.next();
				if (att.isNumerical()){
					list.add(att.getName());
				}
			}
			CorrelationAnalysisConfig config=(CorrelationAnalysisConfig)source.getAnalyticConfig();
			
			dbType=source.getDataSourceType();
			
			sqlGenerator=SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);

			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();

			st = databaseConnection.createStatement(false);
			String tableName = ((DBTable) dataSet
					.getDBTable()).getTableName();
			String columnNames=config.getColumnNames();
			
			String[] columnNamesArray = columnNames.split(",");
			
//			String notNullSql = generateIsNotNullSql(columnNamesArray);
					
			HashMap<String,Double> resultHT =new HashMap<String,Double>();
			
			for(int i=0;i<columnNamesArray.length;i++)
			{
				for(int j=i;j<columnNamesArray.length;j++)
				{
					if (i == 0 && !list.contains(columnNamesArray[j])) {
						logger.error(
								AnalysisErrorName.Non_numeric);			
						throw new AnalysisError(this,AnalysisErrorName.Non_numeric,config.getLocale(),SDKLanguagePack.getMessage(SDKLanguagePack.CORRELATION_NAME,config.getLocale()));
					}
					double r = 0.0;
					if(i==j)
					{
						r=1;
						resultHT.put(columnNamesArray[i]+"/"+columnNamesArray[j], r);
					}
					else
					{
//						getCorrelationString(columnNamesArray[i], columnNamesArray[j],config.getLocale());
						calculatEachCorrelation(st, tableName, columnNamesArray[i], columnNamesArray[j],resultHT);
					}
				}
			}

//			if(columnNamesArray.length > 1)
//			{
//				calculateCorrelation(st, tableName, resultHT,notNullSql);
//			}
			Object[] obj = new Object[2];
			obj[0] = columnNamesArray;
			obj[1] = resultHT;
			 AnalyzerOutPutObject output=new AnalyzerOutPutObject(obj);
			 output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			 return  output;
		} catch (Exception e) {
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		} finally{
			if(st!=null){
				try {
					st.close();
				} catch (SQLException e) {
					logger.error(e );
						throw new AnalysisException(e);
				}
			}
		}
	}
	private void calculatEachCorrelation(Statement st, String tableName,
			String x, String y, HashMap<String, Double> resultHT) throws SQLException {
		
		String xQ=StringHandler.doubleQ(x);
		String yQ=StringHandler.doubleQ(y);
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append("count("+xQ+"*1.0)").append(",");
		sql.append("sum("+sqlGenerator.castToDouble(xQ)+"*"+yQ+")").append(",");
		sql.append("sum("+sqlGenerator.castToDouble(xQ)+")").append(",");
		sql.append("sum("+sqlGenerator.castToDouble(yQ)+")").append(",");
		sql.append("sum("+sqlGenerator.castToDouble(xQ)+"*"+xQ+")").append(",");
		sql.append("sum("+sqlGenerator.castToDouble(yQ)+"*"+yQ+")");
		sql.append(" from ").append(tableName);
		sql.append(" where ").append(xQ).append(" is not null and ");
		sql.append(yQ).append(" is not null");
		
		logger.debug("CorrelationAnalysisAnalyzer.calculateCorrelation():sql="+sql);
		ResultSet rs = st.executeQuery(sql.toString());
		
		while(rs.next()){
			double count = rs.getDouble(1);
			double sum_xmy = rs.getDouble(2);
			double sum_x = rs.getDouble(3);
			double sum_y = rs.getDouble(4);
			double sum_xmx = rs.getDouble(5);
			double sum_ymy = rs.getDouble(6);
			double r = (count * sum_xmy - sum_x * sum_y)
			/ (Math.sqrt(count * sum_xmx - sum_x * sum_x) * Math.sqrt(count
					* sum_ymy - sum_y * sum_y));
			resultHT.put(x+"/"+y, r);
		}
	}
	private void calculateCorrelation(Statement st, String tableName,
			HashMap<String, Double> resultHT, String notNullSql) throws SQLException {
		int cycle = (resultColumns.size()/columnsStatsCount - 1)/(eachTimeColumnsCount) + 1;
		for ( int i = 0; i < cycle; i++){
			StringBuffer sql = new StringBuffer();
			sql.append("select ");
			int start = i * eachTimeColumnsCount;
			int end = 0;
			if (i == cycle - 1){
				end = resultColumns.size()/columnsStatsCount;
			}else{
				end = (i+1) * eachTimeColumnsCount;
			}
			boolean first = true;
			for(int j = start * columnsStatsCount; j < end * columnsStatsCount; j++){
				if (!first){
					sql.append(",");
				}else{
					first = false;
				}
				sql.append(resultColumns.get(j));
			}
			sql.append(" from ").append(tableName);
			sql.append(" where ").append(notNullSql);
			logger.debug("CorrelationAnalysisAnalyzer.calculateCorrelation():sql="+sql);
			ResultSet rs = st.executeQuery(sql.toString());
			if (rs.next()) {
				for(int k = 0; k < end - start; k++){
					double count = rs.getDouble(k * columnsStatsCount + 1);
					double sum_xmy = rs.getDouble(k * columnsStatsCount + 2);
					double sum_x = rs.getDouble(k * columnsStatsCount + 3);
					double sum_y = rs.getDouble(k * columnsStatsCount + 4);
					double sum_xmx = rs.getDouble(k * columnsStatsCount + 5);
					double sum_ymy = rs.getDouble(k * columnsStatsCount + 6);
					double r = (count * sum_xmy - sum_x * sum_y)
					/ (Math.sqrt(count * sum_xmx - sum_x * sum_x) * Math.sqrt(count
							* sum_ymy - sum_y * sum_y));
					resultHT.put(XY.get(start+k), r);
				}
			}
		}
	}
	private void getCorrelationString(String x, String y,Locale locale)
			throws OperatorException, AnalysisError {
		if (x == null || y == null) {
			logger.error(
					AnalysisErrorName.Not_null);
			throw new AnalysisError(this,AnalysisErrorName.Not_null,locale,SDKLanguagePack.getMessage(SDKLanguagePack.CORRELATION_COLUMN,locale));
		}
		
		String xQ=StringHandler.doubleQ(x);
		String yQ=StringHandler.doubleQ(y);

		resultColumns.add("count("+xQ+"*1.0)");
		resultColumns.add("sum("+sqlGenerator.castToDouble(xQ)+"*"+yQ+")");
		resultColumns.add("sum("+sqlGenerator.castToDouble(xQ)+")");
		resultColumns.add("sum("+sqlGenerator.castToDouble(yQ)+")");
		resultColumns.add("sum("+sqlGenerator.castToDouble(xQ)+"*"+xQ+")");
		resultColumns.add("sum("+sqlGenerator.castToDouble(yQ)+"*"+yQ+")");
		
		XY.add(x+"/"+y);
	}
	private int caculateEachTimeColumnsCount(DataSet dataSet){
		int eachTimeColumnsCount = 0;
		if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			eachTimeColumnsCount = 1000/columnsStatsCount;
		}else if (((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoGreenplum.dBType)
		 || ((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equalsIgnoreCase(DataSourceInfoPostgres.dBType)){
			eachTimeColumnsCount = 1600/columnsStatsCount;
		}else{
			eachTimeColumnsCount = 1000/columnsStatsCount;
		}
		return eachTimeColumnsCount;
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.CORRELATION_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.CORRELATION_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
