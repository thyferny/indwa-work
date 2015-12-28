
package com.alpine.datamining.operator.regressions;

import com.alpine.datamining.operator.Parameter;

public class LogisticRegressionParameter implements Parameter {
	private boolean addInercept = true;
	private int maxGenerations = 25;
	private String goodValue;
	private double epsilon = 0.00000001;
	private String columnNames;
	private AnalysisInterActionColumnsModel analysisInterActionModel  = null;
	private boolean isGroupBy = false;
	private String groupByColumn;
	
	public String getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

	public AnalysisInterActionColumnsModel getAnalysisInterAtionModel() {
		return analysisInterActionModel;
	}

	public void setAnalysisInteractionModel(AnalysisInterActionColumnsModel analysisInterActionModel) {
		this.analysisInterActionModel = analysisInterActionModel;
	}

	public boolean isAddInercept() {
		return addInercept;
	}
	public void setAddInercept(boolean addInercept) {
		this.addInercept = addInercept;
	}
	public int getMaxGenerations() {
		return maxGenerations;
	}
	public void setMaxGenerations(int maxGenerations) {
		this.maxGenerations = maxGenerations;
	}
	public String getGoodValue() {
		return goodValue;
	}
	public void setGoodValue(String goodValue) {
		this.goodValue = goodValue;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	public boolean isGroupBy() {
		return isGroupBy;
	}
	public void setGroupBy(boolean isGroupBy) {
		this.isGroupBy = isGroupBy;
	}
	public String getGroupByColumn() {
		return groupByColumn;
	}
	public void setGroupByColumn(String groupByColumn) {
		this.groupByColumn = groupByColumn;
	} 
}
