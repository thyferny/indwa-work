/**
 * ClassName  DBTableSelectorCongif.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.ArrayList;

/**
 * @author John Zhao
 *
 */
public class DBTableSelectorConfig extends AbstractAnalyticConfig {
	
	public final static String ConstDbConnectionName = "dbConnectionName";
	public final static String ConstSchemaName = "schemaName";
	public final static String ConstTableName = "tableName";
	
	public final static String   Const_URL="url";
	public final static String  Const_PASSWORD="password";
	public final static String  Const_USERNAME="userName";
	public final static String  Const_SYSTEM="system";
	public final static String  Const_USESSL="useSSL";
 	public final static String  ConstTableType="tableType";
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		if(system.equals("0"))
			system="PostgreSQL";
		this.system = system;
	}

	private String url;
	private String useSSL;
	public String getUseSSL() {
		return useSSL;
	}

	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
	}

	private String password;
	private String userName;
	private String system;
	private String tableType;
	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstDbConnectionName);
		parameters.add(ConstSchemaName);
		parameters.add(ConstTableName);
		parameters.add(Const_URL);
		parameters.add(Const_PASSWORD);
		parameters.add(Const_USERNAME);
		parameters.add( Const_SYSTEM);
		parameters.add(Const_USESSL);
 		parameters.add( ConstTableType);
	}
	
	public DBTableSelectorConfig(String dbConnectionName,
			String schemaName,String tableName){
		super( );
		this.dbConnectionName=dbConnectionName;
		this.schemaName=schemaName;
		this.dbConnectionName=dbConnectionName;
		setParameterNames(parameters);
	}
	
	public DBTableSelectorConfig(){
		setParameterNames(parameters);
	}
	
	private String dbConnectionName;
	private String schemaName;
	private String tableName;
	public String getDbConnectionName() {
		return dbConnectionName;
	}
	public void setDbConnectionName(String dbConnectionName) {
		this.dbConnectionName = dbConnectionName;
	}
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
