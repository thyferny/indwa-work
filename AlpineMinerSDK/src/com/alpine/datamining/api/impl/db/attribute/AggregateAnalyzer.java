/**
 * ClassName AggregateAnalyzer.java
 *
 * Version information:1.00
 *
 * Date:Jun 4, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.db.attribute;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.AggregateConfig;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowField;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowFieldsModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Richie Lo
 *
 */
public class AggregateAnalyzer extends DataOperationAnalyzer {
	private static Logger logger= Logger.getLogger(AggregateAnalyzer.class);
	
	private AnalysisAggregateFieldsModel aggregateFieldsModel;
	
	private AnalysisWindowFieldsModel windowFieldsModel;
	
	private ISqlGeneratorMultiDB sqlGenerator;

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException  {
 
 
		try {
			DataSet dataSet = getDataSet((DataBaseAnalyticSource) source,  source
					.getAnalyticConfig()) ;
			DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
	
			AggregateConfig config = (AggregateConfig) source.getAnalyticConfig();
			
			sqlGenerator = SqlGeneratorMultiDBFactory
			.createConnectionInfo(databaseConnection.getProperties()
					.getName());
			
			setInputSchema(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
			setInputTable(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
			
			setAggregateFieldsModel(config.getAggregateFieldsModel());
			setWindowFieldsModel(config.getWindowFieldsModel());
			
			setOutputType(config.getOutputType());
			setOutputSchema(config.getOutputSchema());
			setOutputTable(config.getOutputTable());
			setDropIfExist(config.getDropIfExist());
			generateStoragePrameterString((DataBaseAnalyticSource) source);
			performOperation(databaseConnection,dataSet);	
			
			DataBaseInfo dbInfo = ((DataBaseAnalyticSource)source).getDataBaseInfo();
			AnalyzerOutPutTableObject outPut=getResultTableSampleRow(databaseConnection, dbInfo);
			outPut.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			outPut.setDbInfo(dbInfo);
			outPut.setSchemaName(getOutputSchema());
			outPut.setTableName(getOutputTable());
			return outPut;
		} catch ( Exception e) {
			e.printStackTrace();
			logger.error(e );
			if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}		
	 
		 
	}

	/**
	 * @param databaseConnection
	 * @throws OperatorException 
	 */
	private void performOperation(DatabaseConnection databaseConnection,DataSet dataSet) throws AnalysisException, OperatorException  {
		String fieldList = buildFieldList();
		String aggregateFieldList = buildAggregateFieldList();
		String windowFieldList = buildWindowFieldList();
		String groupByList = buildGroupByList();
		String colList = buildColList(fieldList,aggregateFieldList,windowFieldList);
		
		String outputTableName = getQuotaedTableName(getOutputSchema(),getOutputTable());
		
		String inputTableName= getQuotaedTableName(getInputSchema(),getInputTable());
	 
		dropIfExist(dataSet);
		
		DatabaseUtil.alterParallel(databaseConnection,getOutputType());//for oracle
						
		StringBuilder createSql = new StringBuilder("create ");
		StringBuilder insertTable = new StringBuilder();
		StringBuilder selectSql = new StringBuilder();
		createSql.append(getOutputType()).append(" ").append(outputTableName);
		createSql.append(getOutputType().equalsIgnoreCase(Resources.TableType)? getAppendOnlyString() : "");
		createSql.append(DatabaseUtil.addParallel(databaseConnection,getOutputType()));//for oracle
		createSql.append(" as ( ");
		selectSql.append(" select ");
		selectSql.append(colList).append(" from ").append(inputTableName).append(" ").append(groupByList);
		createSql.append(selectSql).append(" )");
		if(getOutputType().equalsIgnoreCase(Resources.TableType)){
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(databaseConnection.getProperties().getName());
			createSql.append(getEndingString());
			insertTable.append(sqlGenerator.insertTable(selectSql.toString(),outputTableName));
		}

		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			logger.debug("AggregateAnalyzer.performOperation():sql="+createSql);
			st.executeUpdate(createSql.toString());
			if(insertTable.length()>0){
				st.execute(insertTable.toString());
				logger.debug(
						"AggregateAnalyzer.performOperation():refreshTableSql=" + insertTable);
			}
			
		} catch (SQLException e) {
			logger.error(e);
			throw new OperatorException(e);
		} finally {
			try {
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}		
	}

	private String buildFieldList() {
		StringBuffer buffer = new StringBuffer();
		
		if(getAggregateFieldsModel()!=null
				&&getAggregateFieldsModel().getGroupByFieldList()!=null
				&&!getAggregateFieldsModel().getGroupByFieldList().isEmpty()){
			for(String s:getAggregateFieldsModel().getGroupByFieldList()){
				buffer.append(StringHandler.doubleQ(s)).append(",");
			}
			if(buffer.length()>0){
				buffer=buffer.deleteCharAt(buffer.length()-1);
			}
		}else if(getAggregateFieldsModel()!=null
				&&getAggregateFieldsModel().getParentFieldList()!=null
				&&!getAggregateFieldsModel().getParentFieldList().isEmpty()){
			for(String s:getAggregateFieldsModel().getParentFieldList()){
				buffer.append(StringHandler.doubleQ(s)).append(",");
			}
			if(buffer.length()>0){
				buffer=buffer.deleteCharAt(buffer.length()-1);
			}
		}	
		return buffer.toString();
	}

	private String buildAggregateFieldList() {
		StringBuffer buffer = new StringBuffer();
		if(getAggregateFieldsModel()!=null
				&&getAggregateFieldsModel().getAggregateFieldList()!=null
				&&!getAggregateFieldsModel().getAggregateFieldList().isEmpty()
				&&getAggregateFieldsModel().getGroupByFieldList()!=null
				&&!getAggregateFieldsModel().getGroupByFieldList().isEmpty()){
			for(AnalysisAggregateField field:getAggregateFieldsModel().getAggregateFieldList()){
				if(field.getAlias().contains("(\"")&&field.getAlias().contains("\")")){	
					field.setAlias(field.getAlias().substring(0, field.getAlias().indexOf("(\""))+"("+
					field.getAlias().substring(field.getAlias().indexOf("(\"")+2,field.getAlias().indexOf("\")"))+")");
				}
				buffer.append("(").append(field.getAggregateExpression()).
				append(") AS ").append(StringHandler.doubleQ(field.getAlias())).append(",");
			}
			if(buffer.length()>0){
				buffer=buffer.deleteCharAt(buffer.length()-1);
			}
		}else{
			return "";
		}
		return buffer.toString();
	}

	private String buildWindowFieldList() {
		StringBuffer buffer = new StringBuffer();
		
		if(getWindowFieldsModel()!=null
				&&getWindowFieldsModel().getWindowFieldList()!=null
				&&!getWindowFieldsModel().getWindowFieldList().isEmpty()){
			for(AnalysisWindowField windowField:getWindowFieldsModel().getWindowFieldList()){
				String overClause;
				if (windowField.getWindowSpecification().equals("null")) {
					overClause = "OVER ()";
				} else {
					overClause = "OVER ("+windowField.getWindowSpecification()+")";
				}
				StringBuffer winFunSql=new StringBuffer();
				winFunSql.append(windowField.getWindowFunction()).append(" ").append(overClause);
				String sqlClause = sqlGenerator.getCastDataType(winFunSql.toString(),windowField.getDataType());
				buffer.append(sqlClause).append(" AS \"").append(windowField.getResultColumn().trim()).append("\"  ,");
			}
			if(buffer.length()>0){
				buffer=buffer.deleteCharAt(buffer.length()-1);
			}
		}else{
			return "";
		}
		return buffer.toString();
	}
	

	private String buildColList(String fieldList, String aggregateFieldList,
			String windowFieldList) {
		StringBuffer buffer = new StringBuffer();
		if (fieldList!=null && !fieldList.trim().isEmpty()) {
			buffer.append(fieldList);
		}
		if (aggregateFieldList!=null && !aggregateFieldList.trim().isEmpty()) {
			if (buffer.length()>0) {
				buffer.append(", ");
			}
			buffer.append(aggregateFieldList);
		}
		if (windowFieldList!=null && !windowFieldList.trim().isEmpty()) {
			if (buffer.length()>0) {
				buffer.append(", ");
			}
			buffer.append(windowFieldList);
		}
		return buffer.toString();
	}

	private String buildGroupByList() {
		StringBuffer buffer = new StringBuffer();
		
		if(getAggregateFieldsModel()!=null
				&&getAggregateFieldsModel().getGroupByFieldList()!=null
				&&!getAggregateFieldsModel().getGroupByFieldList().isEmpty()){
			buffer.append("GROUP BY ");
			for(String s:getAggregateFieldsModel().getGroupByFieldList()){
				buffer.append(StringHandler.doubleQ(s)).append(",");
			}
			if(buffer.length()>0){
				buffer=buffer.deleteCharAt(buffer.length()-1);
			}
		}else{
			return "";
		}	
		return buffer.toString();
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.AGGREGATE_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.AGGREGATE_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

	public AnalysisAggregateFieldsModel getAggregateFieldsModel() {
		return aggregateFieldsModel;
	}

	public void setAggregateFieldsModel(AnalysisAggregateFieldsModel aggregateFieldsModel) {
		this.aggregateFieldsModel = aggregateFieldsModel;
	}

	public AnalysisWindowFieldsModel getWindowFieldsModel() {
		return windowFieldsModel;
	}

	public void setWindowFieldsModel(AnalysisWindowFieldsModel windowFieldsModel) {
		this.windowFieldsModel = windowFieldsModel;
	}


}
