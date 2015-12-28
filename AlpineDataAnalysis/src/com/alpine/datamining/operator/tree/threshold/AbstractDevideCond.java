
package com.alpine.datamining.operator.tree.threshold;


public abstract class AbstractDevideCond implements DevideCond {

	
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
