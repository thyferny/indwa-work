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
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseUtil;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;

/***
 * 
 * @author John Zhao
 * 
 */

public abstract class AbstractDBModelValidator extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(AbstractDBModelValidator.class);
	
	String userName = null;
	String password = null;
	String dbSystem = null;
	String newTableName = null;
	
	protected abstract AnalyticOutPut doValidate(DataBaseAnalyticSource source,
			EvaluatorConfig config) throws AnalysisException;

	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		
		EvaluatorConfig config = (EvaluatorConfig) source.getAnalyticConfig();
		AnalyticOutPut output=null;
		try {
			createTempTable((DataBaseAnalyticSource )source, config);
			output = doValidate((DataBaseAnalyticSource) source, config);
			dropTempTable((DataBaseAnalyticSource )source, config);
			
		} catch (AnalysisException e) {
			throw e;
		} catch (Exception e) {
			throw new AnalysisException(e.getLocalizedMessage());
		} 
		return output;
	}
	protected void setNumericalLabelCategory(Column label){
		if(label.isNumerical())
		{
			((NumericColumn)label).setCategory(true);
		}
	}
	
	private void createTempTable(
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
			if(para.getDataSourceType().equals(DataSourceInfoNZ.dBType)){
				create_sql = new StringBuilder(" create temp table ");
			}
			if(schema!=null&&schema.trim().length()>0){
				create_sql.append(newTableName).append(DatabaseUtil.addParallel(para.getDataSourceType()));
				create_sql.append(" as (select *  from ")
					.append(schema).append(".").append(tableName).append(")");
			}else{
				create_sql.append(newTableName).append(DatabaseUtil.addParallel(para.getDataSourceType()));
				create_sql.append(" as (select *  from ")
				.append(tableName).append(")");
			}
			create_sql.append(sqlGenerator.setCreateTableEndingSql(null));
			DatabaseUtil.alterParallel(conn, para.getDataSourceType());
			
			logger.debug("AbstractDBModelValidator.createTempTable:sql="+create_sql.toString());
			st.execute(create_sql.toString());	
			if(para.getDataSourceType().equals(DataSourceInfoDB2.dBType)){
				StringBuffer insert = new StringBuffer();
				insert.append("insert into ").append(newTableName);
				insert.append(" (select *  from ")
					.append(schema).append(".").append(tableName).append(")");
				logger.debug("AbstractDBModelValidator.createTempTable:sql="+insert.toString());
				st.execute(insert.toString());	
			}
			para.getTableInfo().setTableName(newTableName);
			para.getTableInfo().setSchema(null);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());			
		} 
		
		
	}
	private void dropTempTable(DataBaseAnalyticSource source,
			EvaluatorConfig config) throws OperatorException{
		Statement st = null;
		try{
			st = source.getConnection().createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		if(source.getDataBaseInfo().getSystem().equals(DataSourceInfoOracle.dBType)){
			StringBuffer truncate = new StringBuffer();
			truncate.append("truncate table ").append(newTableName);
			try {
				logger.debug("AbstractDBModelValidator.dropTempTable():sql="
								+ truncate.toString());
				st.execute(truncate.toString());
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(newTableName);
		try {
			logger.debug("AbstractDBModelValidator.dropTempTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}


}