/**
 * 

* ClassName HadoopLirPredictConfig.java
*
* Version information: 1.00
*
* Date: 2012-8-21
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.algoconf;

import java.util.Map;

/**
 * @author Shawn
 *
 *  
 */

public class HadoopLinearPredictConfig {

	private String resultsLocation;
	private String resultsName;
	private String override;
	private Map<String,Map<String,String>> multiResultNames;
	public String getResultsLocation() {
		return resultsLocation;
	}
	public void setResultsLocation(String resultsLocation) {
		this.resultsLocation = resultsLocation;
	}
	public String getResultsName() {
		return resultsName;
	}
	public void setResultsName(String resultsName) {
		this.resultsName = resultsName;
	}
	public String getOverride() {
		return override;
	}
	public void setOverride(String override) {
		this.override = override;
	}
	public Map<String, Map<String, String>> getMultiResultNames() {
		return multiResultNames;
	}
	public void setMultiResultNames(
			Map<String, Map<String, String>> multiResultNames) {
		this.multiResultNames = multiResultNames;
	}

}
