/**
 * ClassName DatabaseSourceParameter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;
import com.alpine.datamining.operator.Parameter;

/**
 * An DataIterator iterates over a sequence of data. 
 * @author eason
 */
public class DatabaseSourceParameter implements Parameter {
	
	private String label;
	private String id;
	private boolean workOnDatabase;
	private String databaseSystem;
	private String url;
	private String username;
	private String password;
	private String tableName;
	private String query = null;
	/**
	 * @return dependent column
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * set  dependent column
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}
	/**
	 * set id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return whether work on database
	 */
	public boolean isWorkOnDatabase() {
		return workOnDatabase;
	}
	/**
	 * set whether work on database
	 * @param workOnDatabase
	 */
	public void setWorkOnDatabase(boolean workOnDatabase) {
		this.workOnDatabase = workOnDatabase;
	}
	/**
	 * @return Database System
	 */
	public String getDatabaseSystem() {
		return databaseSystem;
	}
	/** 
	 * set databaseSystem
	 * @param databaseSystem
	 */
	public void setDatabaseSystem(String databaseSystem) {
		this.databaseSystem = databaseSystem;
	}
	/**
	 * @return url of database connection
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url of database connection
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return Username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * set user name
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return Password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * set Password
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return TableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * @return query
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * set query
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}
}
