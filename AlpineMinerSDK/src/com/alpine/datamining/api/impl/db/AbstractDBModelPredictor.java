/**
 * ClassName AbstractTrainableAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.datamining.utility.JDBCProperties;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/***
 * 
 * @author John Zhao
 * 
 */

public abstract class AbstractDBModelPredictor extends AbstractDBAnalyzer {
	private static final Logger itsLogger=Logger.getLogger(AbstractDBModelPredictor.class);
	String newTableName = null;
	private AnalysisStorageParameterModel analysisStorageParameterModel = null;
	private boolean appendOnly = false;
	//need model...
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		AnalyticOutPut result = null;
		PredictorConfig config = (PredictorConfig) source.getAnalyticConfig();
		analysisStorageParameterModel=((PredictorConfig)config).getStorageParameters();
		if(analysisStorageParameterModel == null || !analysisStorageParameterModel.isAppendOnly()){
			appendOnly = false;
		}else{
			appendOnly = true;
		}
		result = doPredict((DataBaseAnalyticSource) source,
				config);
		if(appendOnly){
			try {
				setResultDataSetAppendOnly((DataBaseAnalyticSource)source,config, result);
				dropTempTable((DataBaseAnalyticSource)source);
			} catch (OperatorException e) {
				throw new AnalysisException(e);
			}
		}
		return result;
	}

	@Override
	protected DBSource getDataSource(
			DataBaseAnalyticSource para, AnalyticConfiguration config)
			throws OperatorException
			{
		if(!appendOnly){
			return getDataSourceNotAppend(para, config);
		}else{
			return getDataSourceAppendOnly(para, config);
		}
	}

	private DBSource getDataSourceAppendOnly(
			DataBaseAnalyticSource para, AnalyticConfiguration config)
			throws OperatorException
			{
		newTableName = createTempTable(para, config);
		DBSource dataSource = OperatorUtil
					.createOperator(DBSource.class);
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();

		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para
				.getDataBaseInfo().getSystem());
		parameter.setTableName(newTableName);
		parameter.setUrl(para
				.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		dataSource.setParameter(parameter);

			
			fillSpecialDataSource(dataSource, config);
			
			return  dataSource;
	}
	private DBSource getDataSourceNotAppend(
			DataBaseAnalyticSource para, AnalyticConfiguration config)
			throws OperatorException
			{
		String tableName = StringHandler.doubleQ(para.getTableInfo().getTableName());
		String schema =para.getTableInfo().getSchema();
		
	 		
		String OutputSchema= ((PredictorConfig)config).getOutputSchema();
		String OutputTable=((PredictorConfig)config).getOutputTable();
		String DropIfExist=((PredictorConfig)config).getDropIfExist();
//		AnalysisStorageParameterModel analysisStorageParameterModel=((PredictorConfig)config).getStorageParameters();
		newTableName = getQuotaedTableName(OutputSchema, OutputTable);
		Connection conn =null;
		Statement st = null;
		try {
		    conn =para.getConnection();
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		} 
	
		StringBuilder dropSql = new StringBuilder();
		if (DropIfExist.equalsIgnoreCase("yes")) {
			if (para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoOracle.dBType)) {
				int tableExistInt = 0;
				StringBuffer tableExist = new StringBuffer();
				tableExist.append("select count(*) from dba_tables where owner = '").append(OutputSchema).append("' and table_name = '").append(OutputTable).append("'");
				ResultSet rs;
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
							+ tableExist.toString());
					}
					rs = st.executeQuery(tableExist.toString());
					while(rs.next())
					{
						tableExistInt = rs.getInt(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new OperatorException(e1.getLocalizedMessage());
				}
				if (tableExistInt > 0)
				{
					dropSql.append("drop table ");
					dropSql.append(newTableName);
					try {
						if(itsLogger.isDebugEnabled()){
							itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
										+ dropSql.toString());
						}
						st.execute(dropSql.toString());
					} catch (SQLException e) {
						e.printStackTrace();
						itsLogger.error(e);
						throw new OperatorException(e.getLocalizedMessage());
					}
				}
				
			} else if (para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoDB2.dBType)){
				dropSql.append("call PROC_DROPSCHTABLEIFEXISTS( '").append(StringHandler.doubleQ(OutputSchema)).append("','").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
								+ dropSql.toString());
					}
				st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}else  if (para.getDataBaseInfo().getSystem().equals(DataSourceInfoNZ.dBType)){
				dropSql.append("call droptable_if_existsdoubleq('").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					}
					st.execute(dropSql.toString());
					st.close();
					st = conn.createStatement();
				} catch (SQLException e) {
//					e.printStackTrace();
					itsLogger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}

			}else {
				dropSql.append("drop table if exists ");
				dropSql.append(newTableName);
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					}
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
		}
		try {
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(para.getDataSourceType());
			StringBuilder create_sql = new StringBuilder();
			create_sql.append("create table ");
			create_sql.append(newTableName)
			.append(DatabaseUtil.addParallel(para.getDataSourceType()));

			if(schema!=null&&schema.trim().length()>0){
				schema=StringHandler.doubleQ(schema);
				create_sql.append(" as (select *  from ").append(schema).append(".").append(tableName).append(")");
			}	else{
				create_sql.append(" as (select *  from ").append(tableName).append(")");
			}
		    
			create_sql.append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null : analysisStorageParameterModel.getSqlDistributeString()));
			
			DatabaseUtil.alterParallel(conn, para.getDataSourceType());
			
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="+create_sql.toString());
			}
			st.execute(create_sql.toString());
			if(para.getDataSourceType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				schema=StringHandler.doubleQ(schema);
				String insert = "insert into "+newTableName+" select * from "+schema+"."+tableName;
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="+insert);
				}
				st.execute(insert);
			}
			para.getTableInfo().setTableName(StringHandler.doubleQ(OutputTable));
			para.getTableInfo().setSchema(StringHandler.doubleQ(OutputSchema));
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		} 
		
		DBSource dataSource = OperatorUtil
					.createOperator(DBSource.class);
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();

		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para
				.getDataBaseInfo().getSystem());
		parameter.setTableName(newTableName);
		parameter.setUrl(para
				.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		dataSource.setParameter(parameter);

			
			fillSpecialDataSource(dataSource, config);
			
			return  dataSource;
	}
	
	private  DataSet getResultDataSetAppendOnly (DataBaseAnalyticSource source, 
			AnalyticConfiguration config) throws OperatorException
	{
		Connection conenction = source.getConnection();
			//this time source will not connect to the db 
			DBSource  dataSource =  super.getDataSource(source,config);
		 
			JDBCProperties properties = DatabaseUtil.getJDBCProperties().get(
					DatabaseUtil.getDBSystemIndex(((DatabaseSourceParameter)dataSource.getParameter()).getDatabaseSystem()));//AsInt(DBSource.PARAMETER_DATABASE_SYSTEM));
			DatabaseConnection databaseConnection = DatabaseConnection.createDatabaseConnection(conenction,
					source.getDataBaseInfo().getUrl(), properties);
			
			DataSet dataSet=dataSource.createDataSetUsingExitingDBConnection(
						databaseConnection, ((DatabaseSourceParameter)dataSource.getParameter()).getTableName(), //source.getTableInfo().getSchema()	+"."+source.getTableInfo().getTableName(),
						false);
			
			if(dataSet.getColumns().getLabel() != null && dataSet.getColumns().getLabel().isNumerical())
			{
				setNumericalLabelCategory(dataSet.getColumns().getLabel());
			}
			
			handColumns(source, dataSet);
			return dataSet;
	}

	
	private void setResultDataSetAppendOnly(
			DataBaseAnalyticSource para, AnalyticConfiguration config, AnalyticOutPut result)
			throws OperatorException
			{

		String tableName = StringHandler.doubleQ(para.getTableInfo().getTableName());
		String schema =para.getTableInfo().getSchema();
	 		
		String OutputSchema= ((PredictorConfig)config).getOutputSchema();
		String OutputTable=((PredictorConfig)config).getOutputTable();
		String DropIfExist=((PredictorConfig)config).getDropIfExist();
		AnalysisStorageParameterModel analysisStorageParameterModel=((PredictorConfig)config).getStorageParameters();
		String outputTableName = getQuotaedTableName(OutputSchema, OutputTable);
		Connection conn =null;
		Statement st = null;
		try {
		    conn =para.getConnection();
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		} 
	
		StringBuilder dropSql = new StringBuilder();
		if (DropIfExist.equalsIgnoreCase("yes")) {
			if (para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoOracle.dBType)) {
				int tableExistInt = 0;
				StringBuffer tableExist = new StringBuffer();
				tableExist.append("select count(*) from dba_tables where owner = '").append(OutputSchema).append("' and table_name = '").append(OutputTable).append("'");
				ResultSet rs;
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
							+ tableExist.toString());
					}
					rs = st.executeQuery(tableExist.toString());
					while(rs.next())
					{
						tableExistInt = rs.getInt(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new OperatorException(e1.getLocalizedMessage());
				}
				if (tableExistInt > 0)
				{
					dropSql.append("drop table ");
					dropSql.append(outputTableName);
					try {
						if(itsLogger.isDebugEnabled()){
							itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
										+ dropSql.toString());
						}
						st.execute(dropSql.toString());
					} catch (SQLException e) {
						e.printStackTrace();
						itsLogger.error(e);
						throw new OperatorException(e.getLocalizedMessage());
					}
				}
				
			} else if (para.getDataBaseInfo().getSystem().equals(
					DataSourceInfoDB2.dBType)){
				dropSql.append("call PROC_DROPSCHTABLEIFEXISTS( '").append(StringHandler.doubleQ(OutputSchema)).append("','").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
								+ dropSql.toString());
					}
				st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}else  if (para.getDataBaseInfo().getSystem().equals(DataSourceInfoNZ.dBType)){
				dropSql.append("call droptable_if_existsdoubleq('").append(StringHandler.doubleQ(OutputTable)).append("')");
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					}
					st.execute(dropSql.toString());
					st.close();
					st = conn.createStatement();
				} catch (SQLException e) {
//					e.printStackTrace();
					itsLogger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}

			}else {
				dropSql.append("drop table if exists ");
				dropSql.append(outputTableName);
				try {
					if(itsLogger.isDebugEnabled()){
						itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="
									+ dropSql.toString());
					}
					st.execute(dropSql.toString());
				} catch (SQLException e) {
					e.printStackTrace();
					itsLogger.error(e);
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
		}
		try {
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(para.getDataSourceType());
			StringBuilder create_sql = new StringBuilder();
			create_sql.append("create table ");
			create_sql.append(outputTableName)
			.append(sqlGenerator.getStorageString(analysisStorageParameterModel.isAppendOnly(), analysisStorageParameterModel.isColumnarStorage(), analysisStorageParameterModel.isCompression(), analysisStorageParameterModel.getCompressionLevel()))
			.append(DatabaseUtil.addParallel(para.getDataSourceType()));

			create_sql.append(" as (select *  from ").append(newTableName).append(")");
		    
			create_sql.append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null : analysisStorageParameterModel.getSqlDistributeString()));
			
			DatabaseUtil.alterParallel(conn, para.getDataSourceType());
			
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="+create_sql.toString());
			}
			st.execute(create_sql.toString());
			if(para.getDataSourceType().equalsIgnoreCase(DataSourceInfoDB2.dBType)){
				schema=StringHandler.doubleQ(schema);
				String insert = "insert into "+newTableName+" select * from "+schema+"."+tableName;
				if(itsLogger.isDebugEnabled()){
					itsLogger.debug("AbstractDBModelPredictor.getDataSource:sql="+insert);
				}
				st.execute(insert);
			}
			para.getTableInfo().setTableName(StringHandler.doubleQ(OutputTable));
			para.getTableInfo().setSchema(StringHandler.doubleQ(OutputSchema));
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		} 
		
		DBSource dataSource = OperatorUtil
					.createOperator(DBSource.class);
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();

		parameter.setWorkOnDatabase(true);
		parameter.setDatabaseSystem(para
				.getDataBaseInfo().getSystem());
		parameter.setTableName(outputTableName);
		parameter.setUrl(para
				.getDataBaseInfo().getUrl());
		parameter.setUsername(para.getDataBaseInfo().getUserName());
		parameter.setPassword(para.getDataBaseInfo().getPassword());
		dataSource.setParameter(parameter);
		fillSpecialDataSource(dataSource, config);
		DataSet dataSet = getResultDataSetAppendOnly (para, 
					config);
		fillDBInfo((AnalyzerOutPutDataBaseUpdate) result, (DataBaseAnalyticSource)para);
		((AnalyzerOutPutDataBaseUpdate) result).setDataset(dataSet);
	}

	private String createTempTable(
			DataBaseAnalyticSource para, AnalyticConfiguration config)
			throws OperatorException
	{
		String tableName = para.getTableInfo().getTableName();
		String schema = para.getTableInfo().getSchema();
		if (schema != null && schema.trim().length() > 0) {
			schema=StringHandler.doubleQ(schema);
			tableName=StringHandler.doubleQ(tableName);
		}else
		{
			tableName=StringHandler.doubleQ(tableName);
		}
		
		newTableName = StringHandler.doubleQ("alpineeval" + System.currentTimeMillis());

		try {

		    Connection conn =para.getConnection();
		    ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory.createConnectionInfo(para.getDataSourceType());
			Statement st = conn.createStatement();
			StringBuilder create_sql = new StringBuilder();
			create_sql.append(sqlGenerator.generateTempTableString());
			if(schema!=null&&schema.trim().length()>0){
				create_sql.append(newTableName).append(DatabaseUtil.addParallel(para.getDataSourceType()));
				create_sql.append(" as (select *  from ")
					.append(schema).append(".").append(tableName).append(")");
			}else{
				create_sql.append(newTableName).append(DatabaseUtil.addParallel(para.getDataSourceType()));
				create_sql.append(" as (select *  from ")
				.append(tableName).append(")");
			}
			create_sql.append(sqlGenerator.setCreateTableEndingSql(analysisStorageParameterModel == null ? null : analysisStorageParameterModel.getSqlDistributeString()));
			DatabaseUtil.alterParallel(conn, para.getDataSourceType());
			
			itsLogger.debug("AbstractDBModelPredictor.createTempTable:sql="+create_sql.toString());
			st.execute(create_sql.toString());	
			if(para.getDataSourceType().equals(DataSourceInfoDB2.dBType)){
				StringBuffer insert = new StringBuffer();
				insert.append("insert into ").append(newTableName);
				insert.append(" (select *  from ")
					.append(schema).append(".").append(tableName).append(")");
				itsLogger.debug("AbstractDBModelPredictor.createTempTable:sql="+insert.toString());
				st.execute(insert.toString());	
			}
//			para.getTableInfo().setTableName(newTableName);
//			para.getTableInfo().setSchema(null);
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e);
			throw new OperatorException(e.getLocalizedMessage());			
		} 
		return newTableName;
	}
	private void dropTempTable(DataBaseAnalyticSource source) throws OperatorException{
		Statement st = null;
		try{
			st = source.getConnection().createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		if(source.getDataBaseInfo().getSystem().equals(DataSourceInfoOracle.dBType)){
			StringBuffer truncate = new StringBuffer();
			truncate.append("truncate table ").append(newTableName);
			try {
				itsLogger.debug("AbstractDBModelValidator.dropTempTable():sql="
								+ truncate.toString());
				st.execute(truncate.toString());
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(),e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(newTableName);
		try {
			itsLogger.debug("AbstractDBModelValidator.dropTempTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

/** all pridictor are changeing table
	 * @param config 
	 * @return
 * @throws AnalysisException 
 * @throws OperatorException 
	 */
	protected abstract AnalyzerOutPutDataBaseUpdate doPredict(
			DataBaseAnalyticSource source,PredictorConfig config) throws AnalysisException;

//	protected abstract Model train(AnalyticSource source) throws AnalysisException;

	String predictType[];//means the predict type like// ..
	
	
//	String getModelType();.....

}
