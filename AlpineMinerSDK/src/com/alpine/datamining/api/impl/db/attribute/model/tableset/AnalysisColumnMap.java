/**
 * ClassName  ColumnMap.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-19
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.tableset;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.ModelUtility;

/**
 * @author zhaoyong,Jeff Dong
 *
 */
public class AnalysisColumnMap {
 
	private String tableName ="";
	private String schemaName ="";
	private List<String> tableColumns = new ArrayList<String> (); 
  
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

	public List<String> getTableColumns() {
		return tableColumns;
	}

	public void setTableColumns(List<String> tableColumns) {
		this.tableColumns = tableColumns;
	}

	public AnalysisColumnMap(String tableName, List<String> tableColumns) {
		super();
		this.tableName = tableName;
		this.tableColumns = tableColumns;
	}

	@Override
	public AnalysisColumnMap clone() throws CloneNotSupportedException {
		AnalysisColumnMap clone = new AnalysisColumnMap( );
		clone.setTableName(getTableName()) ;
		clone.setSchemaName(getSchemaName());
		
		List<String> newTableColumns = new ArrayList<String>();
		for(String column:tableColumns){
			newTableColumns.add(column);
		}
		clone.setTableColumns(newTableColumns) ;
		return clone;
	 
	}

	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		out.append("schemaName =" +schemaName+"\n");
		out.append("tableName =" +tableName+"\n");
		out.append("tableColumns =" +tableColumns.toArray()+"\n");
	 
		return out.toString();
	}
	 

	/**
	 * 
	 */
	public AnalysisColumnMap() {
		 
	} 
	  
	 public boolean equals(Object obj) {
		
		 if((obj instanceof AnalysisColumnMap)==false){
			 return false;
		 }
		 
		 AnalysisColumnMap target=(AnalysisColumnMap )obj;
		 
		 return ModelUtility.nullableEquales(tableName, target.getTableName())
	 		&& ModelUtility.nullableEquales(schemaName, target.getSchemaName())
	 		&& ModelUtility.equalsWithOrder(tableColumns, target.getTableColumns());
 
		 
	 }
 


}