/**
 * ClassName SQLAnalysisConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class SQLAnalysisConfig extends AbstractAnalyticConfig {
	
	private String sqlClause = null;
	private String dbConnectionName = null;
	private String url;
	private String password;
	private String userName;
	private String system;
	private String tableType;
	
	public static final String ConstSqlClause = "sqlClause";
	public static final String ConstDbConnectionName = "dbConnectionName";
	
	public static final String   Const_URL="url";
	public static final String  Const_PASSWORD="password";
	public static final String  Const_USERNAME="userName";
	public static final String  Const_SYSTEM="system";
	public final static String  Const_USESSL="useSSL";
 	public static final String  ConstTableType="tableType";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstSqlClause);
		parameters.add(ConstDbConnectionName);
		parameters.add(Const_URL);
		parameters.add(Const_PASSWORD);
		parameters.add(Const_USERNAME);
		parameters.add(Const_SYSTEM);
		parameters.add(Const_USESSL) ;
 		parameters.add(ConstTableType);
	}
	
	private String useSSL;
	public String getUseSSL() {
		return useSSL;
	}

	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
	}
	
	public SQLAnalysisConfig() {
		setParameterNames(parameters);
	}
	public String getSqlClause() {
		return sqlClause;
	}
	public void setSqlClause(String sqlClause) {
		this.sqlClause = sqlClause;
	}
	public String getDbConnectionName() {
		return dbConnectionName;
	}
	public void setDbConnectionName(String dbConnectionName) {
		this.dbConnectionName = dbConnectionName;
	}
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
		this.system = system;
	}
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}


}
