/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WOECalculateParam
 * Jan 9, 2012
 */
package com.alpine.miner.impls.woe;

import com.alpine.miner.impls.web.resource.FlowInfo;

/**
 * @author Gary
 *
 */
public class WOECalculateParam {

	private FlowInfo flowInfo;
	private WoeCalculateElement[] calculateElements;
	
	private String dependentColumn;
	
	private String goodValue;
	
	private String columnNames;
	
	private String operatorUUID;
	
	public FlowInfo getFlowInfo() {
		return flowInfo;
	}
	public void setFlowInfo(FlowInfo flowInfo) {
		this.flowInfo = flowInfo;
	}
	public WoeCalculateElement[] getCalculateElements() {
		return calculateElements;
	}
	public void setCalculateElements(WoeCalculateElement[] calculateElements) {
		this.calculateElements = calculateElements;
	}
	public String getGoodValue() {
		return goodValue;
	}
	public void setGoodValue(String goodValue) {
		this.goodValue = goodValue;
	}
	public String getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}
	public String getDependentColumn() {
		return dependentColumn;
	}
	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}
	public String getOperatorUUID() {
		return operatorUUID;
	}
	public void setOperatorUUID(String operatorUUID) {
		this.operatorUUID = operatorUUID;
	}
}
