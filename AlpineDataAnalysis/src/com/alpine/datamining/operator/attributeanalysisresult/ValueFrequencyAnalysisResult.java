
package com.alpine.datamining.operator.attributeanalysisresult;

import com.alpine.datamining.utility.Tools;


public class ValueFrequencyAnalysisResult {

	private String columnName;
	private String columnValue;
	private long count;
	private float percentage;

	
	private boolean columnNameNA=false;
	private boolean columnValueNA=false;
	private boolean countNA=false;
	private boolean percentageNA=false;
	
	public ValueFrequencyAnalysisResult() {
	}

	
	public ValueFrequencyAnalysisResult(String columnName, String columnValue,
			int count) {
		this.columnName = columnName;
		this.columnValue = columnValue;
		this.count = count;
	}

	
	public String getColumnName() {
		return columnName;
	}

	
	public String getColumnValue() {
		return columnValue;
	}

	
	public long getCount() {
		return count;
	}

	
	public float getPercentage() {
		return percentage;
	}

	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	
	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}

	
	public void setCount(long count) {
		this.count = count;
	}
	
	public boolean isColumnNameNA() {
		return columnNameNA;
	}

	public void setColumnNameNA(boolean columnNameNA) {
		this.columnNameNA = columnNameNA;
	}

	public boolean isColumnValueNA() {
		return columnValueNA;
	}

	public void setColumnValueNA(boolean columnValueNA) {
		this.columnValueNA = columnValueNA;
	}

	public boolean isCountNA() {
		return countNA;
	}

	public void setCountNA(boolean countNA) {
		this.countNA = countNA;
	}

	public boolean isPercentageNA() {
		return percentageNA;
	}

	public void setPercentageNA(boolean percentageNA) {
		this.percentageNA = percentageNA;
	}
	
	public void setAllNA(boolean NA) {
		this.percentageNA = NA;
		this.countNA = NA;
		this.columnValueNA = NA;
	}

	public boolean isAllNA() {
		return percentageNA&&countNA&&columnValueNA;
	}
	
	public void setPercentage(float percent) {
		this.percentage = percent;
	}

	public String toString() {
		String ret = columnName + " " + columnValue + " " + count + " " + " "
				+ percentage + Tools.getLineSeparator();
		return ret;
	}

}
