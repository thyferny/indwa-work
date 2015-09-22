/**
 * Classname DBTableMetalInfo.java
 *
 * Version information:1.00
 *
 * Data:2010-6-20
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class DBTableMetaInfo {
	String schemaName;
	String tableName;
	String alias;
	String tableType;
	private String connName;
	List<TableColumnMetaInfo> columns=null;
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
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
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	public List<TableColumnMetaInfo> getColumns() {
		return columns;
	}
	public void setColumns(List<TableColumnMetaInfo> columns) {
		this.columns = columns;
	}
 

	/**
	 * @return
	 */
	public void setConnectionName(String name){
		this.connName=name;
	}
	
	public String getConnectionName() {
		return connName;
	}
	/**
	 * @return
	 */
	public List<String> getColumnNames() {
		List<String> res=new ArrayList<String>();
		
		if(columns!=null){
			for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
				TableColumnMetaInfo col = (TableColumnMetaInfo) iterator.next();
				res.add(col.getColumnName() );
			}
		}
		return res;
	} 

}
