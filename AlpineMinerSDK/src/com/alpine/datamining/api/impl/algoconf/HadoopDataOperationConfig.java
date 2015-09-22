/**
 * ClassName HadoopDataOperationConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.Map;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

/**
 * @author Jeff Dong
 *
 */
public class HadoopDataOperationConfig extends AbstractAnalyticConfig {
	
	public static final String HD_MULTIOUTPUT_VISUALIZATIONCLASS="com.alpine.datamining.api.impl.visual.HadoopMultiOutputTableVisualizationType";

	public static final String ConstStoreResults ="storeResults";
	public static final String ConstResultsLocation ="resultsLocation";
	public static final String ConstResultsName ="resultsName";
	public static final String ConstOverride ="override";
	
	
	private String storeResults;
	private String resultsLocation;
	private String resultsName;
	private String override;
	private Map<String,Map<String,String>> multiResultNames;
	
	
	public HadoopDataOperationConfig() {
		super();
	}

	public HadoopDataOperationConfig(String storeResults,
			String resultsLocation, String resultsName, String override) {
		super();
		this.storeResults = storeResults;
		this.resultsLocation = resultsLocation;
		this.resultsName = resultsName;
		this.override = override;
	}
	
	public String getStoreResults() {
		return storeResults;
	}
	public void setStoreResults(String storeResults) {
		this.storeResults = storeResults;
	}
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HadoopDataOperationConfig [storeResults=");
		builder.append(storeResults);
		builder.append(", resultsLocation=");
		builder.append(resultsLocation);
		builder.append(", resultsName=");
		builder.append(resultsName);
		builder.append(", override=");
		builder.append(override);
		builder.append("]");
		return builder.toString();
	}
	
	
}
