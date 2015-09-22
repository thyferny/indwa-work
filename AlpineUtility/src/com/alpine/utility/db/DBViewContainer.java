/**
 * ClassName  DBViewContainer.java
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
public class DBViewContainer extends DBTableContainer {
	public List<String> getViewNameList(){
		 
		return super.getTableNameList();
	}
	
	/**
	 * @param shouldRemove
	 */
	public void removeAll(List<String> shouldRemove) {
		List<DBViewMetaInfo> removeList=new ArrayList<DBViewMetaInfo>();
		if(tables!=null&&tables.size()>0){
			for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
				DBViewMetaInfo view = (DBViewMetaInfo) iterator.next();
				if(view.getTableName()!=null&&
						shouldRemove.contains(view.getTableName())){
					removeList.add(view);
					 
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
			DBViewMetaInfo table= new DBViewMetaInfo();
			table.setSchemaName(schema);
			table.setTableName(tableName); 
			table.setTableType(DBMetaDataUtil.TABLE_TYPE_VIEW);
			table.setConnectionName(connName);
			tables.add(table);
		}
		
	}
}
