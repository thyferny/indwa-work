/**
 * ClassName NormalizationDBAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
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
import com.alpine.datamining.api.impl.algoconf.NormalizationConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;
/***
 * 
 * @author John Zhao
 *
 */

public class NormalizationAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(NormalizationAnalyzer.class);
	
	private String[] methods = { "Proportion-Transformation",
			"Range-Transformation", "Z-Transformation","DivideByAverage-Transformation"};

	private ArrayList<String> normalizationColumnList = new ArrayList<String>();

	private HashMap<String, Double> average;
	private HashMap<String, Double> variance;
	private HashMap<String, Double> minimum;
	private HashMap<String, Double> maximum;
	private HashMap<String, Double> sum;
	
	private ArrayList<Column> IllegalColumnList = new ArrayList<Column>();
	
	private	ISqlGeneratorMultiDB sqlGeneratorMultiDB = null;


	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		try {
			DataSet dataSet = getDataSet(
					(DataBaseAnalyticSource) source, source.getAnalyticConfig());
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
			
			sqlGeneratorMultiDB = SqlGeneratorMultiDBFactory.createConnectionInfo(((DBTable) dataSet.
					getDBTable()).getDatabaseConnection().getProperties().getName());
			NormalizationConfig config = (NormalizationConfig) source
					.getAnalyticConfig();

			String columnNames = config.getColumnNames();

			String[] split = columnNames.split(",");

			for (int i = 0; i < split.length; i++) {
				normalizationColumnList.add(split[i].trim());
			}
			setOutputType(config.getOutputType());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setDropIfExist(config.getDropIfExist());

			dropIfExist(dataSet);

			caculateColumn(dataSet, config);
			generateStoragePrameterString((DataBaseAnalyticSource) source);

			applyNormalization(dataSet, config);
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			
			
			ifIllegal(config);

			return outPut;

		} catch (Exception e) {
			 
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}

	}

	private void caculateColumn(DataSet dataSet, NormalizationConfig config)
			throws OperatorException {
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		
		String method=config.getMethod();
		
		
		if (method.equalsIgnoreCase(methods[0])) {
			sum = new HashMap<String, Double>();
		} else if (method.equalsIgnoreCase(methods[1])) {
			minimum = new HashMap<String, Double>();
			maximum = new HashMap<String, Double>();
		} else if (method.equalsIgnoreCase(methods[2])) {
			average = new HashMap<String, Double>();
			variance = new HashMap<String, Double>();
		}else if(method.equalsIgnoreCase(methods[3])){
			average = new HashMap<String, Double>();
		}
		Iterator<Column> i_all = dataSet.getColumns().allColumns();
		while (i_all.hasNext()) {
			Column att = i_all.next();
			if (!att.isNumerical()
					|| !normalizationColumnList.contains(att.getName()))
				continue;
			StringBuilder sql = new StringBuilder("select ");
			String columnName = att.getName();
			String newColumnName = sqlGeneratorMultiDB.castToDouble(StringHandler.doubleQ(columnName));
			if (method.equalsIgnoreCase(methods[0])) {
				sql.append("sum(").append(newColumnName).append(")");
			} else if (method.equalsIgnoreCase(methods[1])) {
				sql.append("min(").append(newColumnName)
						.append("),max(");
				sql.append(newColumnName).append(")");
			} else if (method.equalsIgnoreCase(methods[2])) {
				sql.append("avg(").append(newColumnName)
						.append("),variance(").append(newColumnName).append(
								")");
			}else if(method.equalsIgnoreCase(methods[3])){
				sql.append("avg(").append(newColumnName).append(")");
			}
			sql.append(" from ").append(tableName);

			try {
				logger.debug(
						"NormalizationAnalyzer.caculatecolumn():sql="
								+ sql);
				ResultSet rs = st.executeQuery(sql.toString());
				while (rs.next()) {
					if (method.equalsIgnoreCase(methods[0])) {
						sum.put(columnName, rs.getDouble(1));
						if (rs.getDouble(1) == 0.0) {
							IllegalColumnList.add(att);
						}
					} else if (method.equalsIgnoreCase(methods[1])) {
						minimum.put(columnName, rs.getDouble(1));
						maximum.put(columnName, rs.getDouble(2));
					} else if (method.equalsIgnoreCase(methods[2])) {
						average.put(columnName, rs.getDouble(1));
						variance.put(columnName, rs.getDouble(2));
						if (rs.getDouble(2) == 0.0) {
							IllegalColumnList.add(att);
						}
					}else if(method.equalsIgnoreCase(methods[3])){
						average.put(columnName, rs.getDouble(1));
						if(Double.isNaN(rs.getDouble(1)) || rs.getDouble(1) == 0.0){
							IllegalColumnList.add(att);
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}

	}

	private void applyNormalization(DataSet dataSet,
			NormalizationConfig config) throws OperatorException, AnalysisError {
		String tableName = ((DBTable) dataSet
				.getDBTable()).getTableName();
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
		
		DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
		
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		String method =config.getMethod();
		StringBuilder sql = new StringBuilder();
		StringBuilder selectSql = new StringBuilder();
		StringBuilder insertTable=new StringBuilder();
		sql.append("create ");
		if (getOutputType().equalsIgnoreCase("table")) {
			sql.append(" table ");
		} else {
			sql.append(" view ");
		}
		String outTable=getQuotaedTableName(getOutputSchema(), getOutputTable());
		sql.append(outTable);
		sql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		sql.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));//for oracle
		sql.append(" as ( ");
		selectSql.append(" select ");
		String min = "";
		String max = "";
		if (method.equalsIgnoreCase(methods[1])) {
			min = config.getRangeMin();
			max = config.getRangeMax();
			double min_d = Double.parseDouble(min);
			double max_d = Double.parseDouble(max);
			if (max_d <= min_d) {
				throw new AnalysisError(this,
						AnalysisErrorName.Illegal_parameter,config.getLocale(),
						SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_MAX,config.getLocale()),
						SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_MAXMIN,config.getLocale()));
			}
		}
		
		Iterator<Column> i_all = dataSet.getColumns().allColumns();
		while (i_all.hasNext()) {
			Column att = i_all.next();
			if (!att.isNumerical()
					|| !normalizationColumnList.contains(att.getName())
					|| IllegalColumnList.contains(att)) {
				selectSql.append(StringHandler.doubleQ(att.getName())).append(",");
			} else {
				if (method.equalsIgnoreCase(methods[0])) {
					String a = StringHandler.doubleQ(att.getName());
					String b = sqlGeneratorMultiDB.castToDouble(sum.get(att.getName()).toString());
					selectSql.append(a).append("*(1.0/").append(b).append(") ").append(a)
							.append(",");
				} else if (method.equalsIgnoreCase(methods[1])) {
					String a = StringHandler.doubleQ(att.getName());
					String b = sqlGeneratorMultiDB.castToDouble(minimum.get(att.getName()).toString());// min
					String c = sqlGeneratorMultiDB.castToDouble(maximum.get(att.getName()).toString());// max
					selectSql.append("(").append(a).append("-(").append(b).append(
							"))/(");
					selectSql.append(c).append("-(").append(b).append("))*((")
							.append(max).append(")-(").append(min);
					selectSql.append("))+(").append(min).append(") ").append(a)
							.append(",");
				} else if (method.equalsIgnoreCase(methods[2])) {
					String a = StringHandler.doubleQ(att.getName());
					String b = sqlGeneratorMultiDB.castToDouble(average.get(att.getName()).toString());// mean
					String c = sqlGeneratorMultiDB.castToDouble(variance.get(att.getName()).toString());// Variance
					selectSql.append("(").append(a).append("-(").append(b).append(
							"))/sqrt(");
					selectSql.append(c).append(") ").append(a).append(",");
				}else if(method.equalsIgnoreCase(methods[3])){
					String a = StringHandler.doubleQ(att.getName());
					selectSql.append(a).append("/").append(sqlGeneratorMultiDB.castToDouble(average.get(att.getName()).toString())).append(" ").append(a).append(",");
				}
			}
		}
		selectSql = selectSql.deleteCharAt(selectSql.length() - 1);
		selectSql.append(" from ").append(tableName);
		sql.append(selectSql).append(" )");
		if (getOutputType().equalsIgnoreCase("table")){
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());
			sql.append(getEndingString());//
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outTable));
		}
		try {
			st = databaseConnection.createStatement(false);
			logger.debug(
					"NormalizationAnalyzer.applyNormalization():sql=" + sql);
			st.execute(sql.toString());
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"NormalizationAnalyzer.applyNormalization():refreshTableSql=" + insertTable);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
	
	private void ifIllegal(NormalizationConfig config) throws AnalysisError {
		if (IllegalColumnList.size() != 0) {
			StringBuilder sb_Illegal = new StringBuilder("");
			Iterator<Column> i_illegal=IllegalColumnList.iterator();
			while(i_illegal.hasNext())
			{
				Column att=i_illegal.next();
				sb_Illegal.append(att.getName()).append(",");
			}
			sb_Illegal=sb_Illegal.deleteCharAt(sb_Illegal.length()-1);
			if(config.getMethod().equalsIgnoreCase(methods[0]))
			{
				throw new AnalysisError(this,
						AnalysisErrorName.Illegal_column,config.getLocale(),
						sb_Illegal.toString(),
						SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_Illegal_P,config.getLocale()));
			}
			if(config.getMethod().equalsIgnoreCase(methods[2]))
			{
				throw new AnalysisError(this,
						AnalysisErrorName.Illegal_column,config.getLocale(),
						sb_Illegal.toString(),
						SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_Illegal_Z,config.getLocale()));
			}
			if(config.getMethod().equalsIgnoreCase(methods[3]))
			{
				throw new AnalysisError(this,
						AnalysisErrorName.Illegal_column,config.getLocale(),
						sb_Illegal.toString(),
						SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_Illegal_AVG,config.getLocale()));
			}
		}
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.NORMALIZATION_DESCRIPTION,locale));

		return nodeMetaInfo;
	}

}
