package com.alpine.miner.framework;
/**   
 * ClassName:ColumnInfo.java 
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29
 * 
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.    
 */
public class ColumnInfo {

	private String columnName;
	private String attrName;
	private int dataType;
	private boolean isPrimary;
	private boolean isAutoIncrement;
	private String tableName;
	private String value;
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}
	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}
	public boolean isPrimary() {
		return isPrimary;
	}
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
