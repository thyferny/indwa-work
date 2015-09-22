/**
 * ClassName DataTable.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.db.TableColumnMetaInfo;


/**
 * data table output...
 * @author John Zhao
 * 
 */
public class DataTable {
	
	String schemaName;
 	
 	String tableName;
	
	List<TableColumnMetaInfo> columns;

	List<DataRow> rows;
	
	public int size(){
		if(rows!=null){
			return rows.size();
		}
		else{
			return 0;
		}
	}
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<TableColumnMetaInfo> getColumns() {
		return columns;
	}

	public void setColumns(List<TableColumnMetaInfo> columns) {
		this.columns = columns;
	}

	public List<DataRow> getRows() {
		return rows;
	}

	public void setRows(List<DataRow> rows) {
		this.rows = rows;
	}
	
	public void appendRow(DataRow row) {
		if(rows==null){
			rows= new ArrayList<DataRow>();
		}
		rows.add(row);
	}
	/**
	 * @return
	 */
	public String[] getColumnNameString() {
		if(columns!=null){
			String[] res= new String[columns.size()];
			for (int i = 0; i < res.length; i++) {
				res[i]=columns.get(i).getColumnName();
			}
			return res;
		}else{
			return new String[0];
		}
	}
	
	public String[] getColumnTypeString() {
		if(columns!=null){
			String[] res= new String[columns.size()];
			for (int i = 0; i < res.length; i++) {
				res[i]=columns.get(i).getColumnsType();
			}
			return res;
		}else{
			return new String[0];
		}
	}
	
  
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
}
