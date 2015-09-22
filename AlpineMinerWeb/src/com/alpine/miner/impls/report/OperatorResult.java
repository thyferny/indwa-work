/**
 * ClassName :OperatorReport.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report;



/**
 * @author zhaoyong
 * 
 */
public class OperatorResult {

	String[][] nodeMetaInfo = null;
	String[][] operatorInput = null; 
	



	OperatorOutput operatorOutput = null;
	private String name = null; 

	
	public String[][] getOperatorInput() {
		return operatorInput;
	}

	public void setOperatorInput(String[][] operatorInput) {
		this.operatorInput = operatorInput;
	}
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param nodeMetaInfo
	 * @param reportOutPut
	 */
	public OperatorResult(String[][] nodeMetaInfo,
			OperatorOutput operatorOutput,String name) {
		this.name=name;
		this.nodeMetaInfo = nodeMetaInfo;
		this.operatorOutput = operatorOutput;
	}

	public OperatorOutput getOperatorOutput() {
		return operatorOutput;
	}

	public void setOperatorOutput(OperatorOutput operatorOutput) {
		this.operatorOutput = operatorOutput;
	}

	public String[][] getNodeMetaInfo() {
		return nodeMetaInfo;
	}

	public void setNodeMetaInfo(String[][] nodeMetaInfo) {
		this.nodeMetaInfo = nodeMetaInfo;
	}

	/**
	 * @return
	 */
	public String getName() {
		
		return name;
	}

 
}
