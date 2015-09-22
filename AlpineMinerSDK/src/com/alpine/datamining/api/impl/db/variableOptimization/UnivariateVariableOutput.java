/**
 * ClassName UnivariateVariableOutput.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.variableOptimization;

import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
/** 
 * Jeff
 */

public class UnivariateVariableOutput extends AbstractAnalyzerOutPut {

	private static final long serialVersionUID = -2179120274198857008L;

	/**
	 * 
	 */

	private String name;
	private Map<String,Double> pValueMap;
	public UnivariateVariableOutput(Map<String, Double> pValueMap) {
		super();
		this.pValueMap = pValueMap;
	}
	public Map<String, Double> getpValueMap() {
		return pValueMap;
	}
	public void setpValueMap(HashMap<String, Double> pValueMap) {
		this.pValueMap = pValueMap;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


}
