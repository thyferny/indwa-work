package com.alpine.datamining.operator.evaluator;

import com.alpine.datamining.operator.Parameter;

public class EvaluatorParameter implements Parameter {
	private boolean useModel = true;
	private String columnValue;
	public boolean isUseModel() {
		return useModel;
	}
	public void setUseModel(boolean useModel) {
		this.useModel = useModel;
	}
	public String getColumnValue() {
		return columnValue;
	}
	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}
}
