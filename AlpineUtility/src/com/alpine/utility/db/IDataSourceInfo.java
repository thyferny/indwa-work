/**
 * ClassName Resources.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public interface IDataSourceInfo extends Serializable{
	
	public abstract String getBaseSimpleUrl(String hostname, int port, String dbname);
	public abstract String getDBType();
	public abstract String getDBDriver();
	public abstract List<String> getSystemSchema();
	public abstract boolean checkDBConnection(DbConnection dbc) throws Exception;
	public abstract Locale getLocale();
	public abstract void setLocale(Locale locale);
	//tablename already a full name with schema
	public abstract String createSelectSql(String tablename, String limit);
	public abstract String[] deComposeUrl(String url);
}
