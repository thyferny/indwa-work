/**
 * ClassName AbstractAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.datamining.utility.JDBCProperties;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;

/**AbstractDBAnalyzer, maybe we will add File analyer later... 
 * @author John Zhao
 *
 */
public abstract class AbstractDBAnalyzer extends AbstractAnalyzer{
	
	private static Logger logger = Logger.getLogger(AbstractDBAnalyzer.class);
	protected DataSet dataSet=null;
	private String appendOnlyString = "";
	private String endingString = ""; 

	protected   DataSet getDataSet (DataBaseAnalyticSource source, 
			AnalyticConfiguration config) throws OperatorException
	{
		Connection conenction = source.getConnection();
		 
		if(dataSet==null){
			//this time source will not connect to the db 
			DBSource  dataSource =  getDataSource(source,config);
		 
			JDBCProperties properties = DatabaseUtil.getJDBCProperties().get(
					DatabaseUtil.getDBSystemIndex(((DatabaseSourceParameter)dataSource.getParameter()).getDatabaseSystem()));//AsInt(DBSource.PARAMETER_DATABASE_SYSTEM));
			DatabaseConnection databaseConnection = DatabaseConnection.createDatabaseConnection(conenction,
					source.getDataBaseInfo().getUrl(), properties);
			
			dataSet=dataSource.createDataSetUsingExitingDBConnection(
						databaseConnection, ((DatabaseSourceParameter)dataSource.getParameter()).getTableName(), //source.getTableInfo().getSchema()	+"."+source.getTableInfo().getTableName(),
						false);
			
			if(dataSet.getColumns().getLabel() != null && dataSet.getColumns().getLabel().isNumerical())
			{
				setNumericalLabelCategory(dataSet.getColumns().getLabel());
			}
			
			handColumns(source, dataSet);
		}
		return dataSet;
	}
	protected void setNumericalLabelCategory(Column label){
	}

	/**
	 * @param para
	 * @param dataSet
	 */
	protected void handColumns(DataBaseAnalyticSource para, DataSet dataSet) {
		if(para.getTableInfo().getColumns()!=null){
			filerColumens(dataSet,para.getTableInfo().getColumnNames());
		}
		

	}
	
 

	/**
	 * @param dataSet
	 * @param columnList
	 */
	protected void filerColumens(DataSet dataSet, List<String> columnList) {
		List<Column > shouldRemove=new ArrayList<Column>();
		
		//filter the attirbutes here
		Columns columns= 		dataSet.getColumns();
		for (Iterator <Column > iterator = columns.iterator(); iterator.hasNext();) {
			Column column = (Column) iterator.next();
			if(columnList.contains(column.getName())==false){
				shouldRemove.add(column );
			}
		}
	 
		for (Iterator <Column > iterator = shouldRemove.iterator(); iterator.hasNext();) {
			Column column = (Column) iterator.next();
			columns.remove(column);
		}
		
		 
	}

	protected DBSource  getDataSource(
			DataBaseAnalyticSource para, AnalyticConfiguration config)
			throws OperatorException
			 {
		String schema = para.getTableInfo().getSchema();
		String tableName = para.getTableInfo().getTableName();
 
		if (schema != null && schema.trim().length() > 0) {
			schema=StringHandler.doubleQ(schema);
			tableName=StringHandler.doubleQ(tableName);
			tableName = schema + "." + tableName;
		}else
		{
			tableName=StringHandler.doubleQ(tableName);
		}
 
		DBSource  dataSource = OperatorUtil
				.createOperator(DBSource.class);
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();
		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para
						.getDataBaseInfo().getSystem());
		parameter.setUrl(para.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		parameter.setTableName(tableName);
		dataSource.setParameter(parameter);
		fillSpecialDataSource(dataSource, config);
		return   dataSource;
	}

	/**
	 * @param dataSource
     * @param config
	 */

	
	//for sub class  should implement this if needed 
	protected    void fillSpecialDataSource(  Operator dataSource,AnalyticConfiguration config) throws OperatorException {
		//for this have nothing to do
	};
	
	//be careful to use this ,only for that analyer will not be used in the flow(say UI popup menu)
	public Connection createConnection(DataBaseAnalyticSource dbSource) throws AnalysisException{
		
		String userName=dbSource.getDataBaseInfo().getUserName();
		String password=dbSource.getDataBaseInfo().getPassword();
		String url=dbSource.getDataBaseInfo().getUrl();
		String system=dbSource.getDataBaseInfo().getSystem();
		String useSSL = dbSource.getDataBaseInfo().getUseSSL();
		Connection connection=null;
		try {
			connection = AlpineUtil.createConnection( userName, password, url, system,AlpineThreadLocal.getLocale(),useSSL);
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		return connection;
		
	}



	/**
	 * @param userName
	 * @param password
	 * @param url
	 * @param system
	 * @return
	 * @throws AnalysisError
	 */


	/**
	 * @param connection
	 * @param sqlString
	 * @param scrollableAndUpdatable
	 * @return
	 * @throws SQLException 
	 */
	protected PreparedStatement createPreparedStatement(Connection connection,
			String sqlString, boolean scrollableAndUpdatable) throws SQLException {
		PreparedStatement statement;
		if (scrollableAndUpdatable)
			statement = connection.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		else
			statement = connection.prepareStatement(sqlString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		return statement;
 
	}
	
	/**
	 * @param source
	 * @param dataTable
	 */
	protected void fillDataTableMetaInfo(AnalyticSource source,
			DataTable dataTable) {
		dataTable.setSchemaName(((DataBaseAnalyticSource)source).getTableInfo().getSchema());
		dataTable.setTableName(((DataBaseAnalyticSource)source).getTableInfo().getTableName());
	}
	protected void fillDBInfo(AnalyzerOutPutDataBaseUpdate result,
			DataBaseAnalyticSource source) {
		DataBaseInfo dbInfo =   source .getDataBaseInfo();
//		
		result.setDbInfo(dbInfo);
		
		String resultTableName=  source.getTableInfo().getTableName();
		String resultschemeName=  source.getTableInfo().getSchema();
		
		result.setTableName(resultTableName);
		result.setSchemaName(resultschemeName);
		
	}
	 //for extension use
	 

	protected void warnTooManyValue(DataSet dataSet, int threshold, Locale locale)
	{
		if(logger.isDebugEnabled()){
			logger.debug("Enter warnTooManyValue");
		}
	
		Columns atts=dataSet.getColumns();
		Iterator<Column> i =atts.iterator();
		StringBuilder sb_column=new StringBuilder();
		while(i.hasNext())
		{
			Column att=i.next();
			if(att.isNumerical())continue;
			if(att.getMapping().size()> threshold)
			{
				sb_column.append(StringHandler.doubleQ(att.getName())).append(",");
			}
		}
		if(sb_column.length()==0 ){
			return;
		}
		sb_column=sb_column.deleteCharAt(sb_column.length()-1);
		String[] temp=SDKLanguagePack.getMessage(SDKLanguagePack.TOO_MANY_VALUES_WARNING,locale).split(";");
		StringBuilder warningMessage=new StringBuilder();
		warningMessage.append(temp[0]).append(sb_column).append(temp[1]).append(threshold);
		warningMessage.append(temp[2]);
		logger.warn(warningMessage.toString());
		if(logger.isDebugEnabled()){
			logger.debug("Exit warnTooManyValue");
		}
	}
	
	protected void setSpecifyColumn(DataSet dataSet,AnalyticConfiguration config)
	{
		String columnNames=config.getColumnNames();
		ArrayList<Column> column_list=new ArrayList<Column>();
		if (!StringUtil.isEmpty(columnNames)){
			String[] columnNamesArray=columnNames.split(",");
			for(String s:columnNamesArray)
			{
				Column att=dataSet.getColumns().get(s);
				column_list.add(att);
			}
		}
		Columns atts=dataSet.getColumns();
		Columns atts_clone=(Columns)atts.clone();
		Iterator<Column> i =atts_clone.iterator();
		while(i.hasNext())
		{
			Column att=i.next();
			if(!column_list.contains(att))
				{
				dataSet.getColumns().remove(att);
				}
			
		}
 
	}

	public AnalyzerOutPutTableObject getResultTableSampleRow(
			DatabaseConnection databaseConnection, DataBaseInfo dbInfo) throws AnalysisException {
		
			return getResultTableSampleRow(databaseConnection, dbInfo, outputSchema, outputTable);
	}
	
	public AnalyzerOutPutTableObject getResultTableSampleRow(
			DatabaseConnection databaseConnection, DataBaseInfo dbInfo,
			String schemaName, String tableName) throws AnalysisException {

		
		AnalyzerOutPutTableObject tableOutput = new AnalyzerOutPutTableObject();
		
		tableOutput.setSchemaName(schemaName);
		tableOutput.setTableName(tableName);
		
		tableOutput.setDbInfo(dbInfo);
		
		return tableOutput;
	}
	public void generateStoragePrameterString(DataBaseAnalyticSource source){
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(source.getDataSourceType());
		AnalysisStorageParameterModel analysisStorageParameterModel = ((AbstractAnalyticConfig)source.getAnalyticConfig()).getStorageParameters();
		if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
			appendOnlyString = " ";
		}else{
			appendOnlyString = sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel());
		}
		endingString = sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null: analysisStorageParameterModel.getSqlDistributeString()); 
	}
	
	protected String generateIsNotNullSql(String[] columnNames) {
		StringBuffer sb_isNotNull=new StringBuffer();
		for(String columnName:columnNames){
			sb_isNotNull.append(StringHandler.doubleQ(columnName)).append(" is not null and ");
		}	
		sb_isNotNull=sb_isNotNull.delete(sb_isNotNull.length()-4, sb_isNotNull.length()-1);
		return sb_isNotNull.toString();
	}
	
	private String outputSchema;
	private String outputTable;

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public String getOutputSchema() {
		return outputSchema;
	}

	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}

	public String getOutputTable() {
		return outputTable;
	}
	public String getAppendOnlyString() {
		return appendOnlyString;
	}

	public void setAppendOnlyString(String appendOnlyString) {
		this.appendOnlyString = appendOnlyString;
	}

	public String getEndingString() {
		return endingString;
	}

	public void setEndingString(String endingString) {
		this.endingString = endingString;
	}
}
