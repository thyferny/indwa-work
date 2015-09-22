/**
 * ClassName ValueAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
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
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.ValueAnalysisConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAttributeAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueAnalysisResult;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.MultiDBUtilityFactory;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 *
 */
public class ValueAnalysisAnalyzer extends AbstractDBAttributeAnalyzer {
	public static final int INTEGER = DataType.INTEGER;
	private static Logger logger= Logger.getLogger(ValueAnalysisAnalyzer.class);
	
	public static final int REAL = DataType.REAL;
	
    public static final int DATE_TIME = DataType.DATE_TIME;
    
    public static final int DATE = DataType.DATE;
    
    public static final int TIME = DataType.TIME;
    
    public static final int BOOLEAN = DataType.BOOLEAN;
    
    public static final int OTHER = DataType.OTHER;
    
    private HashMap<Integer,String> columnType=new HashMap<Integer,String>();
    
    private String dbType;

    private ArrayList<String> resultColumns = new ArrayList<String>();
    private ValueAnalysisResult valueAnalysisResult = null;

	private ArrayList<String> countDistinctResultColumns = new ArrayList<String>();
	private int columnsStatsCount = 12;
	private int maxColumnCountOracle = 1000;
	private int maxColumnCountGreenplum = 1600;
	private int maxColumnCount = maxColumnCountOracle;
	private int eachTimeColumnsCount = 0;
	private int countDistinctEachTimeColumnsCount = 1;
	private int countDistinctColumnsStatsCount = 1;
//	private int selectLimit = 1000;
	private double distinctPercentThreshold = 0.5;
	public static long countDistinctCountThreshold = 1000;
	private long countAllThreshold = 1000000;

	private ISqlGeneratorMultiDB sqlGenerator;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		try {
			columnType.put(Integer.valueOf(DATE_TIME),"date_time");
			columnType.put(Integer.valueOf(DATE),"date");
			columnType.put(Integer.valueOf(TIME),"time");
			columnType.put(Integer.valueOf(BOOLEAN),"boolean");
			columnType.put(Integer.valueOf(OTHER),"other");
			
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig());

			DatabaseConnection databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();
			String tableName = ((DBTable) dataSet
					.getDBTable()).getTableName();
			dbType = ((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName();
			
			sqlGenerator=SqlGeneratorMultiDBFactory.createConnectionInfo(dbType);
			caculateMaxColumnsCount(dataSet);
			ValueAnalysisConfig config=(ValueAnalysisConfig)source.getAnalyticConfig();
			
			if(AlpineMinerConfig.VALUE_ANALYSIS_EACH_TIME_COLUMNS_COUNT >=1 && AlpineMinerConfig.VALUE_ANALYSIS_EACH_TIME_COLUMNS_COUNT <= maxColumnCount/columnsStatsCount){
				eachTimeColumnsCount = AlpineMinerConfig.VALUE_ANALYSIS_EACH_TIME_COLUMNS_COUNT;
			}else{
				eachTimeColumnsCount = maxColumnCount/columnsStatsCount;
			}

			if(AlpineMinerConfig.VALUE_ANALYSIS_COUNT_DISTINCT_EACH_TIME_COLUMNS_COUNT >=1 && AlpineMinerConfig.VALUE_ANALYSIS_COUNT_DISTINCT_EACH_TIME_COLUMNS_COUNT <= maxColumnCount/countDistinctColumnsStatsCount){
				countDistinctEachTimeColumnsCount = AlpineMinerConfig.VALUE_ANALYSIS_COUNT_DISTINCT_EACH_TIME_COLUMNS_COUNT;
			}
			
//			selectLimit = AlpineMinerConfig.VALUE_ANALYSIS_COUNT_DISTINCT_LIMIT;
			distinctPercentThreshold = AlpineMinerConfig.VALUE_ANALYSIS_COUNT_DISTINCT_THRESHOLD;
			countDistinctCountThreshold = Long.parseLong(ProfileReader.getInstance().getParameter(ProfileUtility.ALG_VA_MAX_DISTINCT));
			countAllThreshold = AlpineMinerConfig.VALUE_ANALYSIS_COUNT_ALL_LIMIT;
			StringBuffer sql = new StringBuffer();
			Statement st = null;
			ResultSet rs = null;
			
			valueAnalysisResult = new ValueAnalysisResult(
					tableName);
			valueAnalysisResult.setDataSet(dataSet);
			List<String> columnNamesList = getSpecifiedColumn(dataSet,config);
			try {
				st = databaseConnection.createStatement(false);
				Iterator<Column> iter = dataSet.getColumns().allColumns();
				int index = 0;
				while (iter.hasNext()) {
					Column att = iter.next();
					if (!columnNamesList.contains(att.getName()))
						continue;
					valueAnalysisResult.addColumnValueAnalysisResult(new ColumnValueAnalysisResult());
					addResultColumns(st, tableName,att, index);
					index++;				}
				int cycle = (resultColumns.size()/columnsStatsCount - 1)/(eachTimeColumnsCount) + 1;
				for ( int i = 0; i < cycle; i++){
					sql = new StringBuffer();
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
					
					logger.debug("ValueAnalysis.apply():sql="+sql);
					rs = st.executeQuery(sql.toString());
					if (rs.next()) {
						for(int k = 0; k < end - start; k++){
							ColumnValueAnalysisResult columnValueAnalysisResult = valueAnalysisResult.getColumnValueAnalysisResult(k + start);
							columnValueAnalysisResult.setColumnName(rs.getString(k * columnsStatsCount + 1));
							columnValueAnalysisResult.setColumnType(rs.getString(k * columnsStatsCount + 2));
							columnValueAnalysisResult.setCount(rs.getLong(k * columnsStatsCount + 3));
							columnValueAnalysisResult.setNullCount(rs.getLong(k * columnsStatsCount + 4));
							columnValueAnalysisResult.setEmptyCount(rs.getLong(k * columnsStatsCount + 5));
							columnValueAnalysisResult.setZeroCount(rs.getLong(k * columnsStatsCount + 6));
							columnValueAnalysisResult.setPositiveValueCount(rs.getLong(k * columnsStatsCount + 7));
							columnValueAnalysisResult.setNegativeValueCount(rs.getLong(k * columnsStatsCount + 8));
							columnValueAnalysisResult.setMin(rs.getDouble(k * columnsStatsCount + 9));
							columnValueAnalysisResult.setMax(rs.getDouble(k * columnsStatsCount + 10));
							columnValueAnalysisResult.setAvg(rs.getDouble(k * columnsStatsCount + 11));
							columnValueAnalysisResult.setDeviation(rs.getDouble(k * columnsStatsCount + 12));
//							valueAnalysisResult.addColumnValueAnalysisResult(columnValueAnalysisResult);
						}
	
					}
				}
				
				cycle = (countDistinctResultColumns.size()/countDistinctColumnsStatsCount - 1)/(countDistinctEachTimeColumnsCount) + 1;
				for ( int i = 0; i < cycle; i++){
					sql = new StringBuffer();
					sql.append("select ");
					int start = i * countDistinctEachTimeColumnsCount;
					int end = 0;
					if (i == cycle - 1){
						end = countDistinctResultColumns.size()/countDistinctColumnsStatsCount;
					}else{
						end = (i+1) * countDistinctEachTimeColumnsCount;
					}
					boolean allNull = true;
					boolean first = true;
					for(int j = start * countDistinctColumnsStatsCount; j < end * countDistinctColumnsStatsCount; j++){
						if (!first){
							sql.append(",");
						}else{
							first = false;
						}
						sql.append(countDistinctResultColumns.get(j));
						if (!countDistinctResultColumns.get(j).trim().equalsIgnoreCase("null")){
							allNull = false;
						}
					}
					sql.append(" from ").append(tableName);
					if(allNull){
						for(int k = 0; k < end - start; k++){
							valueAnalysisResult.getColumnValueAnalysisResult(start+k).setUniqueValueCount(0);
						}
						
					}else{
						logger.debug("ValueAnalysis.apply():sql="+sql);
						rs = st.executeQuery(sql.toString());
						if (rs.next()) {
							for(int k = 0; k < end - start; k++){
								valueAnalysisResult.getColumnValueAnalysisResult(start+k).setUniqueValueCount(rs.getLong(k * countDistinctColumnsStatsCount + 1));
							}
						}
					}
				}


			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
				throw new OperatorException(e.getLocalizedMessage());
			} finally {
				try {
					if(rs!=null)			rs.close();
					if(st!=null)              st.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
			AnalyzerOutPutObject outPut=new AnalyzerOutPutObject(valueAnalysisResult);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
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
	private void caculateMaxColumnsCount(DataSet dataSet){
		if (dbType.equalsIgnoreCase(DataSourceInfoOracle.dBType)){
			maxColumnCount = maxColumnCountOracle;
		}else if (dbType.equalsIgnoreCase(DataSourceInfoPostgres.dBType)
		 || dbType.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)){
			maxColumnCount = maxColumnCountGreenplum;
		}else{
			maxColumnCount = maxColumnCountOracle;
		}
	}
	private List<String> getSpecifiedColumn(DataSet dataSet,ValueAnalysisConfig config) {
		Iterator<Column> it = dataSet.getColumns().allColumns();
		String columnNames = config.getColumnNames();
		List<String> columnNamesList = new ArrayList<String>();
		if (columnNames != null) {
			String[] columnNamesArray = columnNames.split(",");
			for (int i = 0; i < columnNamesArray.length; i++) {
				columnNamesList.add(columnNamesArray[i]);
			}
		} else {
			while (it.hasNext()) {
				columnNamesList.add((it.next()).getName());
			}
		}
		return columnNamesList;
	}

	private ArrayList<String> addResultColumns(Statement st, String tableName, Column att, int i) throws SQLException {
		ColumnValueAnalysisResult columnValueAnalysisResult = valueAnalysisResult.getColumnValueAnalysisResult(i);
		String columnName=StringHandler.doubleQ(att.getName());
		if (att.isNumerical()) {
			String ValueType;
			switch (att.getValueType()) {
			case INTEGER:
				ValueType = "integer";
				break;
			case REAL:
				ValueType = "real";
				break;
			default:
				ValueType = "numeric";
				break;
			}

					resultColumns.add("'"+att.getName()+"'");
					resultColumns.add("'"+ValueType+"'");
					resultColumns.add("count("+columnName+")");
					if(att.getValueType() == INTEGER){
						countDistinctResultColumns.add(getCountDistinct(st, tableName, columnName, columnValueAnalysisResult));
					}else{
						countDistinctResultColumns.add("null");
						columnValueAnalysisResult.setUniqueValueCountNA(true);
					}
					resultColumns.add(" sum(case when "+columnName+" is null then 1 else 0 end)");
					resultColumns.add("null");
					columnValueAnalysisResult.setEmptyCountNA(true);
					resultColumns.add("sum(case when "+columnName+" = 0 then 1 else 0 end)");
					resultColumns.add("sum(case when "+columnName+" > 0  then 1 else 0 end)");
					resultColumns.add("sum( case when "+columnName+" < 0 then 1 else 0 end)" );
					resultColumns.add(" min("+columnName+")" );
					resultColumns.add(" max("+columnName+") " );
					
					resultColumns.add(" avg("+sqlGenerator.castToDouble(columnName)+")" );
					if(!dbType.equals(DataSourceInfoDB2.dBType)){
						resultColumns.add(" stddev("+sqlGenerator.castToDouble(columnName)+") "); 
					}else{
						StringBuilder sb=new StringBuilder();
						sb.append(" stddev(").append(sqlGenerator.castToDouble(columnName)).append(")");
						sb.append("/sqrt(count(").append(sqlGenerator.castToDouble(columnName)).append(")-1)");
						sb.append("*sqrt(count(").append(sqlGenerator.castToDouble(columnName)).append("))");
						resultColumns.add(sb.toString()); 
					}
							
		
		} else if (att.isNominal()&&att.getValueType()!=DATE_TIME&&att.getValueType()!=TIME
				&&att.getValueType()!=DATE&&att.getValueType()!=DATE && att.getValueType() != BOOLEAN&&att.getValueType()!=OTHER) {
			resultColumns.add("'"+att.getName()+"'");
					resultColumns.add("'text'");
					resultColumns.add(" count("+columnName+")");
					countDistinctResultColumns.add(getCountDistinct(st, tableName, columnName, columnValueAnalysisResult));
					resultColumns.add("sum(case when "+columnName+" is null then 1 else 0 end)");
					resultColumns.add("sum(case when "+columnName+" = '' then 1 else 0 end)");
					resultColumns.add("null");
					columnValueAnalysisResult.setZeroCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setPositiveValueCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setNegativeValueCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setMinNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setMaxNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setAvgNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setDeviationNA(true);
		} else if(att.getValueType()!=OTHER){
			resultColumns.add("'"+att.getName()+"'");
					String type =  "'";
					if(columnType.get(att.getValueType())!=null)
					{
						type+=(columnType.get(att.getValueType()));
					}
					else
					{
						type+=(att.getValueType());
					}
					type+="'";
					resultColumns.add(type);
					resultColumns.add(" count("+columnName+")");
					if (att.isNominal()&&att.getValueType()!=DATE_TIME&&att.getValueType()!=TIME
							&&att.getValueType()!=DATE&&att.getValueType()!=DATE){
						countDistinctResultColumns.add(getCountDistinct(st, tableName, columnName, columnValueAnalysisResult));
					}else{
						countDistinctResultColumns.add("null");
						columnValueAnalysisResult.setUniqueValueCountNA(true);
					}
					resultColumns.add( "sum(case when "+columnName+"is null then 1 else 0 end)");
					resultColumns.add("null");
					columnValueAnalysisResult.setEmptyCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setZeroCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setPositiveValueCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setNegativeValueCountNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setMinNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setMaxNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setAvgNA(true);
					resultColumns.add("null");
					columnValueAnalysisResult.setDeviationNA(true);
		}else
		{
			resultColumns.add("'"+att.getName()+"'");
			String type = "'";
			if(columnType.get(att.getValueType())!=null)
			{
				type+=(columnType.get(att.getValueType()));
			}
			else
			{
				type+=(att.getValueType());
			}
			type+="'";
			resultColumns.add(type);
			resultColumns.add("count("+columnName+")");
			countDistinctResultColumns.add("null");
			columnValueAnalysisResult.setUniqueValueCountNA(true);
			resultColumns.add( "null");
			columnValueAnalysisResult.setNullCountNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setEmptyCountNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setZeroCountNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setPositiveValueCountNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setNegativeValueCountNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setMinNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setMaxNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setAvgNA(true);
			resultColumns.add("null");
			columnValueAnalysisResult.setDeviationNA(true);
		}
		return resultColumns;
	}
	private String getCountDistinct(Statement st, String tableName, String columnName, ColumnValueAnalysisResult columnValueAnalysisResult) throws SQLException {
		double percent = 0.0;
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dbType);
		percent = multiDBUtility.getSampleDistinctRatio(st,tableName, columnName,null);
		long count = multiDBUtility.getSampleDistinctCount(st,tableName, columnName,null);
		long countAll = multiDBUtility.getSampleAllCount(st,tableName, columnName,countAllThreshold);
		if((percent < distinctPercentThreshold && count < countDistinctCountThreshold) || countAll < countAllThreshold){
			return " count (distinct "+columnName+")";
		}else{
			columnValueAnalysisResult.setUniqueValueCountNA(true);
			return	"null";
		}
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.VALUE_ANALYSIS_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.VALUE_ANALYSIS_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
