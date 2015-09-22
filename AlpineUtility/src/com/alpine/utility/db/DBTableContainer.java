/**
 * ClassName  DBTableContainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author John Zhao
 *
 */
public class DBTableContainer {
	
	List<DBTableMetaInfo> tables;
	
	private DBSchemaMetaInfo schemaMetaInfo;
	
	public List<DBTableMetaInfo> getTables() {
		return tables;
	}
	public void setTables(List<DBTableMetaInfo> tables) {
		this.tables = tables;
	} 
	public List<String> getTableNameList(){
		List<String> names = new ArrayList<String>();
		for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
			DBTableMetaInfo table = (DBTableMetaInfo) iterator.next();
			names.add(table.getTableName());
			
		}
		return names;
	}
	/**
	 * @param tableName
	 * @return
	 */
	public DBTableMetaInfo getDBTableMetaInfo(String tableName) {
		if(tables!=null&&tables.size()>0){
			for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
				DBTableMetaInfo table = (DBTableMetaInfo) iterator.next();
				if(table.getTableName()!=null&&table.getTableName().equals(tableName)){
					return table;
				}
			}
		}
		return null;
	}
	/**
	 * @param shouldRemove
	 */
	public void removeAll(List<String> shouldRemove) {
		List<DBTableMetaInfo> removeList=new ArrayList<DBTableMetaInfo>();
		if(tables!=null&&tables.size()>0){
			for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
				DBTableMetaInfo table = (DBTableMetaInfo) iterator.next();
				if(table.getTableName()!=null&&
						shouldRemove.contains(table.getTableName())){
					removeList.add(table);
					 
				}
			}
			tables.removeAll(removeList);
		}
	 	
	}
	/**
	 * @param shouldAdd
	 * @param schema
	 * @param connName
	 */
	public void addAll(List<String> shouldAdd, String schema, String connName) {
		if(tables==null){
			tables=new ArrayList<DBTableMetaInfo>();
		}
		for (Iterator iterator = shouldAdd.iterator(); iterator.hasNext();) {
			String tableName = (String) iterator.next();
			DBTableMetaInfo table= new DBTableMetaInfo();
			table.setSchemaName(schema);
			table.setTableName(tableName); 
			table.setTableType(DBMetaDataUtil.TABLE_TYPE_TABLE);
			table.setConnectionName(connName);
			tables.add(table);
		}
		
	}
	
	public void removeTable(String tableName) {
		if (tables != null) {
			DBTableMetaInfo found= null;
			for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
				DBTableMetaInfo dbTableMetaInfo = (DBTableMetaInfo) iterator.next();
				if(dbTableMetaInfo.getTableName().equals(tableName)){
					found = dbTableMetaInfo;
					break;
				}
			}
			if(found!=null){
				tables.remove(found) ;
			}
		}

	}
	public DBSchemaMetaInfo getSchemaMetaInfo() {
		return schemaMetaInfo;
	}
	public void setSchemaMetaInfo(DBSchemaMetaInfo schemaMetaInfo) {
		this.schemaMetaInfo = schemaMetaInfo;
	}
}
