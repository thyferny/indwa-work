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

import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;

/**
 * @author Nihat Hosgur
 *
 */
public class HadoopAggregaterConfig extends HadoopDataOperationConfig {
	
	private AnalysisAggregateFieldsModel aggregateFieldsModel;
	

	public AnalysisAggregateFieldsModel getAggregateFieldsModel() {
		return aggregateFieldsModel;
	}


	public void setAggregateFieldsModel(
			AnalysisAggregateFieldsModel aggregateFieldsModel) {
		this.aggregateFieldsModel = aggregateFieldsModel;
	}


	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}

	
	public HadoopAggregaterConfig() {
		super();
		setParameterNames(parameters);
	}

	
	public HadoopAggregaterConfig(String storeResults, String resultsLocation,
			String resultsName, String override) {
		super(storeResults, resultsLocation, resultsName, override);
		setParameterNames(parameters);
	}


	 
	
}
