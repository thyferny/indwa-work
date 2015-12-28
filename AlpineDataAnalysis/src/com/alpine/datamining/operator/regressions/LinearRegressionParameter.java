
package com.alpine.datamining.operator.regressions;

import com.alpine.datamining.operator.Parameter;

public class LinearRegressionParameter implements Parameter {
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

	public AnalysisInterActionColumnsModel getAnalysisInterActionModel() {
		return analysisInterActionModel;
	}

	public void setAnalysisInterActionModel(AnalysisInterActionColumnsModel analysisInterActionModel) {
		this.analysisInterActionModel = analysisInterActionModel;
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
