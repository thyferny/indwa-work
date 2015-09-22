/**
 * Classname TableColumns.java
 *
 * Version information:1.00
 *
 * Data:2010-6-20
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.db;

/**
 * @author Administrator
 * 
 */
public class TableColumnMetaInfo {

	String columnName;
	String columnsType;
	int length;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @param string
	 * @param string2
	 */
	public TableColumnMetaInfo(String columnName, String columnsType) {
		this.columnsType = columnsType;
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnsType() {
		return columnsType;
	}

	public void setColumnsType(String columnsType) {
		this.columnsType = columnsType;
	}

}
