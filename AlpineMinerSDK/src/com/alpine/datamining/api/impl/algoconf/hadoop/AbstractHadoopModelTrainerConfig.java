/**
 * ClassName AbstractHadoopModelTrainerConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-25
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.ModelNeededConfig;

/**
 * @author Eason
 * 
 */
public abstract class AbstractHadoopModelTrainerConfig extends AbstractHadoopAnalyticConfig
		implements ModelNeededConfig {
	
	public static final String ConstForceRetrain = "forceRetrain";
	public static final String ConstDependentColumn = "dependentColumn";
	
	private String dependentColumn = null;

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
