/**
 * ClassName DataTableVisualizationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import java.util.List;

import com.alpine.datamining.api.impl.AbstractDBTableOutPut;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.TableColumnMetaInfo;

public class DataTableVisualizationOutPut extends TableVisualizationOutPut {

	//sample data...
	private TableEntity table;
	
	private DataBaseInfo dbInfo;
	
	private String schemaName;
	
	//if tableName!=null, means from a real table...
	private String tableName;
	
	List<TableColumnMetaInfo> columns;
	
	public List<TableColumnMetaInfo> getColumns() {
		return columns;
	}
	public void setColumns(List<TableColumnMetaInfo> columns) {
		this.columns = columns;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public DataTableVisualizationOutPut(){
	}
	public DataTableVisualizationOutPut(TableEntity table){
		this.table = table;
	}
	public TableEntity getTableEntity(){
		return table;
	}
	@Override
	public Object getVisualizationObject() {
		return getTableEntity();
	}
	
	public DataBaseInfo getDbInfo() {
		return dbInfo;
	}
	public void setDbInfo(DataBaseInfo dbInfo) {
		this.dbInfo = dbInfo;
	}

	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
 	}
	
	public void fillDBTableInfo(AbstractDBTableOutPut dbTableOutPut){
		dbInfo=dbTableOutPut.getDbInfo();
		setDbInfo(dbInfo);
		setSchemaName(dbTableOutPut.getSchemaName());
		setTableName(dbTableOutPut.getTableName());
		setColumns(dbTableOutPut.getColumns());
		if(table!=null){
			if(null!=dbInfo){
				table.setUrl(dbInfo.getUrl());
				table.setPassword(dbInfo.getPassword());
				table.setSystem(dbInfo.getSystem()); 
				table.setUserName(dbInfo.getUserName());
			}
			table.setSchema( getSchemaName());
			table.setTableName(getTableName()) ;
 			
		}
	}
 
	
//	/**
//	 * 
//	 **/
//
//	public void fillDBInfo(String tableName, DataSet dataSet) {
//		String[] names=tableName.split(".");
//		setTableName(names[1]); 
//		
//		setSchemaName(names[0]); 
//		DBTable db = (DBTable)dataSet.getdataTable();
//		setDbInfo(new DataBaseInfo(Resources.DB_TYPE_POSTSQL,
//				db.getUrl(),db.getUserName(),db.getPassword()));
//	}
}
