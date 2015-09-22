package com.alpine.datamining.operator.fpgrowth;

import com.alpine.datamining.operator.Parameter;

public class FPGrowthParameter implements Parameter {
	private int tableSizeThreshold = 10000000;
	private double support = 0.1;
	private String positiveValue;
	private String columnName;
	private boolean useArray = false;
	private String expression;
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public int getTableSizeThreshold() {
		return tableSizeThreshold;
	}
	public void setTableSizeThreshold(int tableSizeThreshold) {
		this.tableSizeThreshold = tableSizeThreshold;
	}
	public double getSupport() {
		return support;
	}
	public void setSupport(double support) {
		this.support = support;
	}
	public String getPositiveValue() {
		return positiveValue;
	}
	public void setPositiveValue(String positiveValue) {
		this.positiveValue = positiveValue;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public boolean isUseArray() {
		return useArray;
	}
	public void setUseArray(boolean useArray) {
		this.useArray = useArray;
	}
}
