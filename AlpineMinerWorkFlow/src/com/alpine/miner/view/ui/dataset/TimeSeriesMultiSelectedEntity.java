/**
 * ClassName MultiSelectedEntity.java
 *
 * Version information: 1.00
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.view.ui.dataset;

import java.util.HashMap;
import java.util.List;

public class TimeSeriesMultiSelectedEntity {
	
	private Object chart;
	private List<String> availableValueList;
	private String idColumn;
	private String valueColumn;
	private boolean isIdColumnDate;
	
	private HashMap<String,List<Object>> idColumnList;
	private HashMap<String,HashMap<Object,Double>> idValueMap;
	
	public Object getChart() {
		return chart;
	}
	public void setChart(Object chart) {
		this.chart = chart;
	}
	public List<String> getAvailableValueList() {
		return availableValueList;
	}
	public void setAvailableValueList(List<String> availableValueList) {
		this.availableValueList = availableValueList;
	}
	public String getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	public String getValueColumn() {
		return valueColumn;
	}
	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}
	public boolean isIdColumnDate() {
		return isIdColumnDate;
	}
	public void setIdColumnDate(boolean isIdColumnDate) {
		this.isIdColumnDate = isIdColumnDate;
	}
	public HashMap<String, List<Object>> getIdColumnList() {
		return idColumnList;
	}
	public void setIdColumnList(HashMap<String, List<Object>> idColumnList) {
		this.idColumnList = idColumnList;
	}
	public HashMap<String, HashMap<Object, Double>> getIdValueMap() {
		return idValueMap;
	}
	public void setIdValueMap(HashMap<String, HashMap<Object, Double>> idValueMap) {
		this.idValueMap = idValueMap;
	}

}
