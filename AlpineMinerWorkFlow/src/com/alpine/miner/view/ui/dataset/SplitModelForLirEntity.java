/**
 * ClassName SplitModelForLirEntity.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.view.ui.dataset;

import java.util.List;
import java.util.Map;

/**
 * @author Jeff Dong
 *
 */
public class SplitModelForLirEntity {
	
	private String dependentColumn;
	
	private TableEntity summaryTable;
	
	private List<TableEntity> groupTable;
	
	private Map<String,List<double[]>> groupResiduals;
	
	private Map<String,Double> groupSValues;

	public String getDependentColumn() {
		return dependentColumn;
	}

	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	public TableEntity getSummaryTable() {
		return summaryTable;
	}

	public void setSummaryTable(TableEntity summaryTable) {
		this.summaryTable = summaryTable;
	}

	public List<TableEntity> getGroupTable() {
		return groupTable;
	}

	public void setGroupTable(List<TableEntity> groupTable) {
		this.groupTable = groupTable;
	}

	public Map<String, List<double[]>> getGroupResiduals() {
		return groupResiduals;
	}

	public void setGroupResiduals(Map<String, List<double[]>> groupResiduals) {
		this.groupResiduals = groupResiduals;
	}

	public Map<String, Double> getGroupSValues() {
		return groupSValues;
	}

	public void setGroupSValues(Map<String, Double> groupSValues) {
		this.groupSValues = groupSValues;
	}



}
