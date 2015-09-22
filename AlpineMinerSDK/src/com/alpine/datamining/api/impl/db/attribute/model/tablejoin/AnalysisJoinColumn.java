/**
 * ClassName  JoinColumn.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-27
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.model.tablejoin;


/**
 * @author John Zhao
 *
 */
public class AnalysisJoinColumn{
 
	public  static final String TAG_NAME = "JoinColumnModel";

	String tableAlias;
	String columnName;
	String newColumnName;
	String columnType;
	
	
	/**
	 * @param alias
	 * @param columnName
	 * @param newColumnNam
	 * @param columnType
	 */
	public AnalysisJoinColumn(String alias, String columnName, String newColumnNam,
			String columnType) {

		this.tableAlias = alias;
		this.columnName = columnName;
		this.newColumnName = newColumnNam;
		this.columnType = columnType;
	}
	public String getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getNewColumnName() {
		return newColumnName;
	}
	public void setNewColumnName(String newColumnNam) {
		this.newColumnName = newColumnNam;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}


	public boolean equals(Object obj) {
		AnalysisJoinColumn jModel = (AnalysisJoinColumn) obj;
		if (jModel.getColumnName().equals(columnName)
				&& jModel.getNewColumnName().equals(newColumnName)
				&& jModel.getTableAlias().equals(tableAlias)) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new AnalysisJoinColumn(tableAlias, columnName, newColumnName, columnType);
	}

}