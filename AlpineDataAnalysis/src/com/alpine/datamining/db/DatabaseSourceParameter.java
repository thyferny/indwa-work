
package com.alpine.datamining.db;
import com.alpine.datamining.operator.Parameter;


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
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isWorkOnDatabase() {
		return workOnDatabase;
	}
	
	public void setWorkOnDatabase(boolean workOnDatabase) {
		this.workOnDatabase = workOnDatabase;
	}
	
	public String getDatabaseSystem() {
		return databaseSystem;
	}
	
	public void setDatabaseSystem(String databaseSystem) {
		this.databaseSystem = databaseSystem;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
}
