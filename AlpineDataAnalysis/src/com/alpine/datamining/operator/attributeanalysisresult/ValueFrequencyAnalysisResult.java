/**
 * ClassName ValueFrequencyAnalysisResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.attributeanalysisresult;

import com.alpine.datamining.utility.Tools;

/**
 * @author Eason
 * 
 */
public class ValueFrequencyAnalysisResult {

	private String columnName;
	private String columnValue;
	private long count;
	private float percentage;

	
	private boolean columnNameNA=false;
	private boolean columnValueNA=false;
	private boolean countNA=false;
	private boolean percentageNA=false;
	/**
	 * constructor
	 */
	public ValueFrequencyAnalysisResult() {
	}

	/**
	 * constructor
	 * 
	 * @param columnName
	 * @param columnValue
	 * @param count
	 */
	public ValueFrequencyAnalysisResult(String columnName, String columnValue,
			int count) {
		this.columnName = columnName;
		this.columnValue = columnValue;
		this.count = count;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the columnValue
	 */
	public String getColumnValue() {
		return columnValue;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @return the percent
	 */
	public float getPercentage() {
		return percentage;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @param columnValue
	 *            the columnValue to set
	 */
	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}

	/**
	 * @param count
	 *            the count to set
	 */
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
	/**
	 * @param percent
	 *            the percent to set
	 */
	public void setPercentage(float percent) {
		this.percentage = percent;
	}

	public String toString() {
		String ret = columnName + " " + columnValue + " " + count + " " + " "
				+ percentage + Tools.getLineSeparator();
		return ret;
	}

}
