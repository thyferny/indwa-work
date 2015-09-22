/**
 * ClassName AbstractDevideCond
 *
 * Version information: 1.00
 *
 * Data: 2010-5-5
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.tree.threshold;

/**
 * The abstract super class for all split conditions.
 *
 */
public abstract class AbstractDevideCond implements DevideCond {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7026198558137175949L;
	private String columnName;
	
	public AbstractDevideCond(String columnName) {
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public String toString() {
		return columnName + " " + getRelation() + " " + getReadableValueString();
	}
}
