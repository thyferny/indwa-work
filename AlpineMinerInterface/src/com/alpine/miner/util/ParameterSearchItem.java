/**
 * ClassName ParameterSearchItem.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.miner.util;

public class ParameterSearchItem {
	String flowName;// full path
	String operatorName;
	String parameterName;
	String parameterValue;

	public ParameterSearchItem(String flowName, String operatorName,
			String parameterName, String parameterValue) {
		super();
		this.flowName = flowName;
		this.operatorName = operatorName;
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	@Override
	public String toString() {
		return "ParameterSearchItem [flowName=" + flowName + ", operatorName="
				+ operatorName + ", parameterName=" + parameterName
				+ ", parameterValue=" + parameterValue + "]";
	}

}
