/**
 * ClassName HadoopPredictorConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-20
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.ModelNeededConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.AbstractHadoopAnalyticConfig;

/**
 * @author Eason
 * 
 */
public class HadoopPredictorConfig extends AbstractHadoopAnalyticConfig implements
		ModelNeededConfig {

	public static final String ConstOutputDir = "outputDir";
	public static final String ConstDropIfExist = "dropIfExist";

	private String dropIfExist;
	private String outputDir;
	protected EngineModel trainedModel;

	protected final static ArrayList<String> parameters = new ArrayList<String>();
	static{
		parameters.add(ConstOutputDir);
		parameters.add(ConstDropIfExist);
	}
	public HadoopPredictorConfig() {
		setParameterNames(parameters);
	}

	public String getDropIfExist() {
		return dropIfExist;
	}

	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}
	public HadoopPredictorConfig(EngineModel trainedModel) {
		this.trainedModel = trainedModel;
	}


	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}


	public EngineModel getTrainedModel() {
		return trainedModel;
	}

	public void setTrainedModel(EngineModel trainedModel) {
		this.trainedModel = trainedModel;
	}

}
