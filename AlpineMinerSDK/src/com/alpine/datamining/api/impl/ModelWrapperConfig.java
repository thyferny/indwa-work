/**
 * ClassName AbstractAnalyticConfiguration.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.algoconf.ModelNeededConfig;

/**
 * @author John Zhao
 * 
 */
public class ModelWrapperConfig extends AbstractAnalyticConfig
		implements ModelNeededConfig {
	  
	private EngineModel trainedModel;
	private String modelFilePath;
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add("modelFilePath");
		 
	}
	public ModelWrapperConfig(){
		setParameterNames(parameters);
	}

	public String getModelFilePath() {
		return modelFilePath;
	}

	public void setModelFilePath(String modelFilePath) {
		this.modelFilePath = modelFilePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.algoconf.ModelNeededConfig#getTrainedModel
	 * ()
	 */
	@Override
	public EngineModel getTrainedModel() {
		return trainedModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.algoconf.ModelNeededConfig#setTrainedModel
	 * (com.alpine.datamining.operator.Model)
	 */
	@Override
	public void setTrainedModel(EngineModel trainedModel) {
		this.trainedModel = trainedModel;

	}

}
