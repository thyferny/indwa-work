/**
 * ClassName LogisticRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.EngineModel;

/**
 * @author John Zhao
 * 
 */
public class PredictorConfig extends AbstractAnalyticConfig implements
		ModelNeededConfig {

	public static final String ConstOutputTable = "outputTable";
	public static final String ConstOutputSchema = "outputSchema";
	public static final String ConstDropIfExist = "dropIfExist";

	private String outputSchema;
	private String outputTable;
	private String dropIfExist;
	
	protected final static ArrayList<String> parameters = new ArrayList<String>();
	static{
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputTableStorageParameters);
	}
	
	public PredictorConfig(EngineModel trainedModel) {
		this.trainedModel = trainedModel;
	}

	public PredictorConfig() {
		setParameterNames(parameters);
	}
	
	
	
	public String getOutputSchema() {
		return outputSchema;
	}

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public String getOutputTable() {
		return outputTable;
	}

	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}

	public String getDropIfExist() {
		return dropIfExist;
	}

	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}

	

	// model as input
	protected EngineModel trainedModel;

	public EngineModel getTrainedModel() {
		return trainedModel;
	}

	public void setTrainedModel(EngineModel trainedModel) {
		this.trainedModel = trainedModel;
	}

}
