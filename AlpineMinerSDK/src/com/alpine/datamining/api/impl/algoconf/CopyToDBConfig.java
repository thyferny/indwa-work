/**
 * ClassName CopyToDBConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-23
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

/**
 * @author Jeff Dong
 *
 */
public class CopyToDBConfig extends DataOperationConfig {
	
	public final static String ConstDbConnectionName = "dbConnectionName";
	public final static String ConstSchemaName = "schemaName";
	public final static String ConstTableName = "copyToTableName";
	public final static String ConstIfDataExists = "ifDataExists";
	
	public final static String  Const_URL="url";
	public final static String  Const_PASSWORD="password";
	public final static String  Const_USERNAME="userName";
	public final static String  Const_SYSTEM="system";
	public final static String  Const_USESSL="useSSL";
 	public final static String  ConstTableType="tableType";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{ 
		parameters.add(ConstDbConnectionName);
		parameters.add(ConstSchemaName);
		parameters.add(ConstTableName);
		parameters.add(ConstIfDataExists);
	}
	
	private String dbConnectionName;
	private String schemaName;
	private String copyToTableName;
	private String ifDataExists;
	
	private String url;
	private String useSSL;
	private String password;
	private String userName;
	private String system;
	private String tableType;

	public CopyToDBConfig(){
		super();
		setParameterNames(parameters);
	}

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

	public String getCopyToTableName() {
		return copyToTableName;
	}

	public void setCopyToTableName(String copyToTableName) {
		this.copyToTableName = copyToTableName;
	}

	public String getIfDataExists() {
		return ifDataExists;
	}

	public void setIfDataExists(String ifDataExists) {
		this.ifDataExists = ifDataExists;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUseSSL() {
		return useSSL;
	}

	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
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
