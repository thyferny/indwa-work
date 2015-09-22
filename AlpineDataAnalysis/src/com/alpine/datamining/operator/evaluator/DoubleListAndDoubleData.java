/**
 * ClassName DoubleListAndDoubleData.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.evaluator;
/*
 * return ROCAUCData to UI to show the ROC curve and AUC value;
 */
import java.util.List;

/***
 * 
 * @author Eason
 *
 */
public class DoubleListAndDoubleData extends EvaluatorResultObjectAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2277493730307376091L;
	private double doubleData = 0.0;
	private List<double[]> listData = null;
	public void setDouble(double auc) {
		this.doubleData = auc;
	}
	public double getDouble() {
		return doubleData;
	}
	public void setDoubleList(List<double[]> data) {
		this.listData = data;
	}
	public List<double[]> getDoubleList() {
		return listData;
	}
}
