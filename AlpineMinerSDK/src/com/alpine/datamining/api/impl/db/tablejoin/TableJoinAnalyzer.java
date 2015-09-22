/**
 * ClassName TableAnalysisAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.tablejoin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.TableJoinConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.DataOperationAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinColumn;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinCondition;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisJoinTable;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisTableJoinModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.AnalysisErrorName;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 * 
 */
public class TableJoinAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(TableJoinAnalyzer.class);
	public static final String Alpine_ID = "alpine_ID";
	private DatabaseConnection databaseConnection;
	private AnalysisTableJoinModel tableJoinDef;
	private String outputTableName;
	private ISqlGeneratorMultiDB sqlGenerator;
	private StringBuilder insertTable=new StringBuilder();
	private static final String MAP_SEPERATOR = ".";

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		DataTable dataTable = new DataTable();
		fillDataTableMetaInfo(source, dataTable);
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			TableJoinConfig config = (TableJoinConfig) source
					.getAnalyticConfig();

			DataSet dataSet = getDataSet(
					(DataBaseAnalyticSource) source, source.getAnalyticConfig());
			databaseConnection = ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection();

			setOutputTable(config.getOutputTable());
			setOutputSchema(config.getOutputSchema());
			setDropIfExist(config.getDropIfExist());
			setOutputType(config.getOutputType());
			tableJoinDef=config.getTableJoinDef();
			generateStoragePrameterString((DataBaseAnalyticSource) source);

			sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(source.getDataSourceType());
			dropIfExist(dataSet);
			
			DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle

			outputTableName = getQuotaedTableName(getOutputSchema(),
					getOutputTable());

			if (config.getTableJoinDef() == null) {
				throw new AnalysisError(this,
						AnalysisErrorName.Empty_TABLE_JOIN_DEFINITION,config.getLocale());
			}
			String sql = createJoinSql(config);
			logger.debug(
					"TableJoinAnalyzer.doAnalysis():sql=" + sql);
			Statement st = databaseConnection.createStatement(false);

			st.execute(sql);
			
			if(insertTable.length()>0){
				try {
					logger.debug(
							"TableJoinAnalyzer.doAnalysis():insertTableSql=" + insertTable);
					st.execute(insertTable.toString());
				} catch (Exception e) {
					if(e.getLocalizedMessage().startsWith("DB2 SQL Error: SQLCODE=-420, SQLSTATE=22018")){
						throw new AnalysisError(this,AnalysisErrorName.JOIN_DIFFERENT_TYPE,config.getLocale());
					}else if(e.getLocalizedMessage().startsWith("DB2 SQL Error: SQLCODE=-203, SQLSTATE=42702")){
						throw new AnalysisError(this,AnalysisErrorName.JOIN_MORE_THAN_ONCE,config.getLocale());
					}else{
						throw e;
					}
				}finally{
					st.close();
				}
			}
			
			// here get tge result-----
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			
			
			return outPut;
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof AnalysisException) {
				throw (AnalysisException) e;
			} else if(e instanceof SQLException){
				if(((SQLException)e).getSQLState().equals("72000")//Oracle
						||((SQLException)e).getLocalizedMessage().startsWith("ERROR:  pg_atoi:")){
					throw new AnalysisError(this,AnalysisErrorName.JOIN_DIFFERENT_TYPE,AlpineThreadLocal.getLocale());
				}else if(e.getLocalizedMessage().startsWith("DB2 SQL Error: SQLCODE=-203, SQLSTATE=42702")){
					throw new AnalysisError(this,AnalysisErrorName.JOIN_MORE_THAN_ONCE,AlpineThreadLocal.getLocale());
				}else {
					throw new AnalysisException(e);
				}				
			}else {
				throw new AnalysisException(e);
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}
		}
	}

	public String createJoinSql(TableJoinConfig config)
			throws AnalysisException {

		String outType = getOutputType();
		String createSequenceID = config.getCreateSequenceID();// Yes No

		List<AnalysisJoinColumn> joinColumns = tableJoinDef.getJoinColumns();
		String columnList = buildColumnsList(createSequenceID, joinColumns);

		// ------------------buildFromClause-------------------
		List<AnalysisJoinTable> joinTables = tableJoinDef.getJoinTables();
		List<AnalysisJoinCondition> joinConditions = tableJoinDef
				.getJoinConditions();
		String fromClause = createFromClause(joinTables, joinConditions);

		StringBuilder sb = new StringBuilder("create ");
		StringBuilder selectSql = new StringBuilder();
		sb.append(outType).append(" ").append(outputTableName);
		sb.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");

		sb.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));
		sb.append(" as (");
		selectSql.append(" select ").append(columnList).append(" from ").append(
				fromClause);
		sb.append(selectSql).append(" )");
		if(outType.equalsIgnoreCase("table")){
		    sb.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(), outputTableName));
		}
		return sb.toString();
	}

	private String createFromClause(List<AnalysisJoinTable> joinTables,
			List<AnalysisJoinCondition> joinConditions) {

		StringBuffer sb = new StringBuffer();

		// no condition....
		if (joinConditions.size() == 0) {
			for (int i = 0; i < joinTables.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				AnalysisJoinTable joinTable = joinTables.get(i);

//				sb.append(String.format("%s.%s %s", StringHandler.doubleQ(joinTable.getSchema()),
//						StringHandler.doubleQ(joinTable.getTable()), StringHandler.doubleQ(joinTable.getAlias())));
				sb.append(StringHandler.doubleQ(joinTable.getSchema())+"."+StringHandler.doubleQ(joinTable.getTable())+" "+StringHandler.doubleQ(joinTable.getAlias()));
			}
		} else {// ......
			List<String> conditionList=new ArrayList<String>();
			String lastAndOr=null;
			String lastConditionString=null;
			for(int i = 0; i < joinConditions.size(); i++){
				AnalysisJoinCondition joinCondition = joinConditions.get(i);
				String joinType=joinCondition.getJoinType();
				String condition=joinCondition.getCondition();
				String andOr=joinCondition.getAndOr();
				String column1=joinCondition.getColumn1();
				String column2=joinCondition.getColumn2();
				if(!StringUtil.isEmpty(joinType)){
					if(!StringUtil.isEmpty(lastConditionString)){
						conditionList.add(lastConditionString);
					}
					String conditionString=null;
					if(isStartWithTableAlias(column1)&&isStartWithTableAlias(column2)){
						conditionString=StringHandler.doubleQ(column1.substring(0, column1.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column1.substring(column1.indexOf(MAP_SEPERATOR)+1,column1.length()))+" "+condition+" "+
						StringHandler.doubleQ(column2.substring(0, column2.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column2.substring(column2.indexOf(MAP_SEPERATOR)+1,column2.length()));
					}else if(isStartWithTableAlias(column1)){
						conditionString=StringHandler.doubleQ(column1.substring(0, column1.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column1.substring(column1.indexOf(MAP_SEPERATOR)+1,column1.length()))+" "+condition+" "+column2;
					}else if(isStartWithTableAlias(column2)){
						conditionString=column1+" "+condition+" "+
						StringHandler.doubleQ(column2.substring(0, column2.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column2.substring(column2.indexOf(MAP_SEPERATOR)+1,column2.length()));
					}else{
						conditionString=column1+" "+condition+" "+column2;
					}			
					lastAndOr=andOr;
					lastConditionString=conditionString;
				}else{
					String conditionString=null;
					if(isStartWithTableAlias(column1)&&isStartWithTableAlias(column1)){
						conditionString=StringHandler.doubleQ(column1.substring(0, column1.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column1.substring(column1.indexOf(MAP_SEPERATOR)+1,column1.length()))+" "+condition+" "+
						StringHandler.doubleQ(column2.substring(0, column2.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column2.substring(column2.indexOf(MAP_SEPERATOR)+1,column2.length()));
					}else if(isStartWithTableAlias(column1)){
						conditionString=StringHandler.doubleQ(column1.substring(0, column1.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column1.substring(column1.indexOf(MAP_SEPERATOR)+1,column1.length()))+" "+condition+" "+column2;
					}else if(isStartWithTableAlias(column2)){
						conditionString=column1+" "+condition+" "+
						StringHandler.doubleQ(column2.substring(0, column2.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
						StringHandler.doubleQ(column2.substring(column2.indexOf(MAP_SEPERATOR)+1,column2.length()));
					}else{
						conditionString=column1+" "+condition+" "+column2;
					}
					lastConditionString=lastConditionString+" "+lastAndOr+" "+conditionString;
					lastAndOr=andOr;
				}
			}
			if(!StringUtil.isEmpty(lastConditionString)){
				conditionList.add(lastConditionString);
			}
			
			for (int i = 0,j=0; i < joinConditions.size();i++){
				AnalysisJoinCondition joinCondition = joinConditions.get(i);
				String joinType=joinCondition.getJoinType();

				if(StringUtil.isEmpty(joinType)){
					continue;
				}
				String leftTable=joinCondition.getTableAlias1();
				String rightTable=joinCondition.getTableAlias2();
				String column1=joinCondition.getColumn1();
				String column2=joinCondition.getColumn2();
				
				if(isStartWithTableAlias(column1)){
					column1=StringHandler.doubleQ(column1.substring(0, column1.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
					StringHandler.doubleQ(column1.substring(column1.indexOf(MAP_SEPERATOR)+1,column1.length()));
				}
				if(isStartWithTableAlias(column2)){
					column2=StringHandler.doubleQ(column2.substring(0, column2.indexOf(MAP_SEPERATOR)))+MAP_SEPERATOR+
					StringHandler.doubleQ(column2.substring(column2.indexOf(MAP_SEPERATOR)+1,column2.length()));
				}
					String secondFullTable=getSchemaTable(joinTables,
							rightTable);
					if(i==0){
						String firstFullTable=getSchemaTable(joinTables,
								leftTable);
						sb.append(firstFullTable).append(" ").append(StringHandler.doubleQ(leftTable));
					}
					sb.append(" ").append(joinType).append(" ");
					sb.append(secondFullTable).append(" ").append(StringHandler.doubleQ(rightTable));
					
					if(!joinType.equalsIgnoreCase("CROSS JOIN")){
						sb.append(" on (");
						sb.append(conditionList.get(j)).append(")");
					}	
					
					j++;
			}
		}

		return sb.toString();
	}

	private boolean isStartWithTableAlias(String expression){
		if(StringUtil.isEmpty(expression)){
			return false;
		}
		boolean isStartWith=false;
		List<AnalysisJoinTable>  joinTables=tableJoinDef.getJoinTables();
		List<String> allTableAlias=new ArrayList<String>();
		if(joinTables!=null){
			for(AnalysisJoinTable joinTable:joinTables){
				allTableAlias.add(joinTable.getAlias());
			}
		}
		for(String s:allTableAlias){
			if(expression.startsWith(s)){
				isStartWith=true;
				break;
			}
		}
		return isStartWith;
	}
	
	private String getSchemaTable(List<AnalysisJoinTable> joinTables, String alias) {
		String schemaTable = null;
		for (Iterator<AnalysisJoinTable> iterator = joinTables.iterator(); iterator
				.hasNext();) {
			AnalysisJoinTable joinTableModel = iterator.next();
			if (joinTableModel.getAlias().equals(alias)) {
				schemaTable = StringHandler.doubleQ(joinTableModel.getSchema())
						+ "."
						+ StringHandler.doubleQ(joinTableModel.getTable());
				break;
			}
		}

		return schemaTable;
	}

	private String buildColumnsList(String createSequenceID,
			List<AnalysisJoinColumn> joinColumns) {
		StringBuilder columnList = new StringBuilder("");

		for (Iterator<AnalysisJoinColumn> iterator = joinColumns.iterator(); iterator
				.hasNext();) {
			AnalysisJoinColumn joinColumnModel = iterator.next();

			if (columnList.length()!=0) {
				columnList.append(",");
			}
			columnList.append(StringHandler.doubleQ(joinColumnModel.getTableAlias())).append(".");
			columnList.append(StringHandler.doubleQ(joinColumnModel.getColumnName())).append(" as ");
			columnList.append(StringHandler.doubleQ(joinColumnModel.getNewColumnName()));
		}

		if (createSequenceID.equals(Resources.YesOpt)) {
			columnList.append(",").append(sqlGenerator.rownumberOverByNull()).append(" as ").append(StringHandler.doubleQ(Alpine_ID));
		}
		return columnList.toString();
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.TABLE_JOIN_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.TABLE_JOIN_DESCRIPTION,locale));

		return nodeMetaInfo;
	}

}
