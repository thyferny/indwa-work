/**
 * ClassName HadoopRowFilterConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-6-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

/**
 * @author Jeff Dong
 *
 */
public class HadoopRowFilterConfig extends HadoopDataOperationConfig {
	
	private String filterCondition;
	
	private static final String ConstFilterCondition = "filterCondition";

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{
		parameters.add(ConstFilterCondition);
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}

	
	public HadoopRowFilterConfig() {
		super();
		setParameterNames(parameters);
	}

	
	public HadoopRowFilterConfig(String storeResults, String resultsLocation,
			String resultsName, String override,String filterCondition) {
		super(storeResults, resultsLocation, resultsName, override);
		this.filterCondition = filterCondition;
		setParameterNames(parameters);
	}


	public String getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}
	
}
