/**
 * ClassName DoubleListData.java
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
public class DoubleListData extends EvaluatorResultObjectAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8984373481848216823L;
	private List<double[]> listData = null;
	/**
	 * @param listData
	 */
	public DoubleListData(List<double[]> listData) {
		super();
		this.listData = listData;
	}
	public void setDoubleList(List<double[]> data) {
		this.listData = data;
	}
	public List<double[]> getDoubleList() {
		return listData;
	}
}
