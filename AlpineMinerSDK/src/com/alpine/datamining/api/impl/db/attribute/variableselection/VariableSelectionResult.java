/**
 * ClassName VariableSelectionResult.java
 *
 * Version information: 1.00
 *
 * Data: 2011-1-4
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.attribute.variableselection;

import java.io.Serializable;

import com.alpine.datamining.utility.Tools;

/**
 * @author Eason
 * 
 */
public class VariableSelectionResult implements Serializable {

	/**
	 */
	private static final long serialVersionUID = 1L;
	private String[] columnNames;

	private double[] scores;
	private double thresholdCategory;
	private double thresholdNumber;

	public VariableSelectionResult(String[] columnNames, double[] scores,double thresholdCategory,double thresholdNumber) {
		this.columnNames = columnNames;
		this.scores = scores;
		this.thresholdCategory = thresholdCategory;
		this.thresholdNumber = thresholdNumber;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}

	public double getThresholdCategory() {
		return thresholdCategory;
	}

	public void setThresholdCategory(double thresholdCategory) {
		this.thresholdCategory = thresholdCategory;
	}

	public double getThresholdNumber() {
		return thresholdNumber;
	}

	public void setThresholdNumber(double thresholdNumber) {
		this.thresholdNumber = thresholdNumber;
	}

	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("threshold Category: ").append(thresholdCategory).append(Tools.getLineSeparator());
		buf.append("threshold Number: ").append(thresholdNumber).append(Tools.getLineSeparator());
		for (int i = 0; i < columnNames.length; i++)
		{
			{
				buf.append(columnNames[i]).append(":").append(scores[i]).append(Tools.getLineSeparator());
			}
		}
		return buf.toString();
	}
}
