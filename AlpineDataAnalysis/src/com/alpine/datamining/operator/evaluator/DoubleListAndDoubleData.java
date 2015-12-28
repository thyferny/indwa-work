
package com.alpine.datamining.operator.evaluator;

import java.util.List;


public class DoubleListAndDoubleData extends EvaluatorResultObjectAdapter {
	
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
