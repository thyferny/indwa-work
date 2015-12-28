
package com.alpine.datamining.operator.evaluator;

import java.util.List;


public class DoubleListData extends EvaluatorResultObjectAdapter {

	
	private static final long serialVersionUID = 8984373481848216823L;
	private List<double[]> listData = null;
	
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
