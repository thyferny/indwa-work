/**
 * ClassName :FlowReport.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report;

import java.util.List;

/**
 * @author zhaoyong
 *
 */
//this have to be send to web client...
public class FlowResult {
	//[label][value]
	String[][] flowMetaInfo;

	/**
	 * @param flowMetaInfo
	 * @param operatorReports
	 */
	public FlowResult(String[][] flowMetaInfo,
			List<OperatorResult> operatorReports) {
		super();
		this.flowMetaInfo = flowMetaInfo;
		this.operatorResults = operatorReports;
	}
	List<OperatorResult> operatorResults;
	private String isIE ="" ;
	
	public void setIE(String isIE) {
		this.isIE = isIE;
	}
	public String[][] getFlowMetaInfo() {
		return flowMetaInfo;
	}
	public void setFlowMetaInfo(String[][] flowMetaInfo) {
		this.flowMetaInfo = flowMetaInfo;
	}
	public List<OperatorResult> getOperatorResults() {
		return operatorResults;
	}
	public void setOperatorResults(List<OperatorResult> operatorResults) {
		this.operatorResults = operatorResults;
	}
	public String isIE() { 
		return isIE;
	}
 
}
