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

import com.alpine.datamining.api.impl.algoconf.ModelNeededConfig;

/**
 * @author John Zhao
 * 
 */
public abstract class AbstractModelTrainerConfig extends AbstractAnalyticConfig
		implements ModelNeededConfig {
	
	public static final String ConstForceRetrain = "forceRetrain";
	public static final String ConstDependentColumn = "dependentColumn";
	
	private String dependentColumn = null;
	private String splitModelGroupByColumn;
	public static final String ConstGroupColumnValue = "groupByTrainerColumn";
	
	public String getSplitModelGroupByColumn() {
		return splitModelGroupByColumn;
	}

	public void setSplitModelGroupByColumn(String splitModelGroupByColumn) {
		this.splitModelGroupByColumn = splitModelGroupByColumn;
	}

	public String getDependentColumn() {
		return dependentColumn;
	}

	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	private String forceRetrain = null;

	public String getForceRetrain() {
		if(forceRetrain==null||forceRetrain.trim().length()==0){
			return "Yes";
		}else{
			return forceRetrain;
		}
	}

	public void setForceRetrain(String forceRetrain) {
		this.forceRetrain = forceRetrain;
	}

	private EngineModel trainedModel;

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
