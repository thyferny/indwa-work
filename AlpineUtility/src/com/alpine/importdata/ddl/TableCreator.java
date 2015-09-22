/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * TableCreator.java
 */
package com.alpine.importdata.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import com.alpine.importdata.ddl.impl.*;
import com.alpine.utility.db.*;
import org.apache.log4j.Logger;

import com.alpine.importdata.DatabaseDataType;
import com.alpine.importdata.ddl.ICreateParameter.columnMetaInfo;

/**
 * @author Gary
 * Aug 21, 2012
 */
public abstract class TableCreator {
    private static Logger itsLogger = Logger.getLogger(TableCreator.class);

    private DbConnection connInfo;
	private String userName;
	private String password;
	private String url;
	private String system;
	private Locale locale;
	private String useSSL;
	
	protected TableCreator(DbConnection connInfo){
		this.connInfo = connInfo;
	}
	protected TableCreator(String userName,String password,String url,String system,Locale locale,String useSSL){
		this.userName = userName;
		this.password = password;
		this.url = url;
		this.system = system;
		this.locale = locale;
		this.useSSL = useSSL;
	}
	
	public static TableCreator getInstance(String userName,String password,String url,String system,Locale locale,String useSSL){
		String dbType = system;
		TableCreator creator = null;
		if(DataSourceInfoPostgres.dBType.equals(dbType)){
			creator = new PostgresCreatorImpl(userName, password, url, system, locale , useSSL);
		}else if(DataSourceInfoGreenplum.dBType.equals(dbType)){
            creator = new GreenplumCreatorImpl(userName, password, url, system, locale, useSSL);
        }else if(DataSourceInfoOracle.dBType.equals(dbType)){
			creator = new OracleCreatorImpl(userName, password, url, system, locale, useSSL);
		}else if(DataSourceInfoDB2.dBType.equals(dbType)){
			creator = new DB2CreatorImpl(userName, password, url, system, locale, useSSL);
		}else if(DataSourceInfoNZ.dBType.equals(dbType)){
			creator = new NZCreatorImpl(userName, password, url, system, locale, useSSL);
		}
		return creator;
	}
	
	public static TableCreator getInstance(DbConnection conn){
		String dbType = conn.getDbType();
		TableCreator creator = null;
		if(DataSourceInfoPostgres.dBType.equals(dbType)){
			creator = new PostgresCreatorImpl(conn);
		}else if(DataSourceInfoGreenplum.dBType.equals(dbType)){
            creator = new GreenplumCreatorImpl(conn);
        }else if(DataSourceInfoOracle.dBType.equals(dbType)){
			creator = new OracleCreatorImpl(conn);
		}else if(DataSourceInfoDB2.dBType.equals(dbType)){
			creator = new DB2CreatorImpl(conn);
		}else if(DataSourceInfoNZ.dBType.equals(dbType)){
			creator = new NZCreatorImpl(conn);
		}
		return creator;
	}
	
	public void createTable(ICreateParameter parameters) throws Exception{
		Connection conn = null;
		if(connInfo!=null){
			conn = AlpineUtil.createConnection(connInfo);
		}else{
			conn = AlpineUtil.createConnection(userName,password, url, system, locale ,useSSL);
		}
		Statement stat = null;
		try{
			stat = conn.createStatement();
			stat.executeUpdate(getCreateTableSQL(parameters));
		}catch(SQLException e){
			e.printStackTrace();
            itsLogger.error(e.getMessage(),e);
			throw e;
		}finally{
			stat.close();
			conn.close();
		}
	}
	
	private String getCreateTableSQL(ICreateParameter parameters){
		StringBuilder builder = new StringBuilder("CREATE TABLE ");
		builder.append("\"")
				.append(parameters.getSchemaName())
				.append("\".\"")
				.append(parameters.getTableName())
				.append("\"(");
		for(columnMetaInfo columnMeta : parameters.getColumnMetaList()){
			String columnSQL = buildColumnSQL(columnMeta);
			builder.append(columnSQL)
					.append(",");
		}
		builder.replace(builder.length() - 1, builder.length(), ")");
		return builder.toString();
	}

	private String buildColumnSQL(columnMetaInfo columnInfo) {
        //column names should be case sensitive
		return "\"" + columnInfo.getColumnName() + "\" " + convertDataType(columnInfo.getColumnType());
	}


	/**
	 * @param columnType
	 * @return
	 */
	protected abstract String convertDataType(DatabaseDataType columnType);
}
