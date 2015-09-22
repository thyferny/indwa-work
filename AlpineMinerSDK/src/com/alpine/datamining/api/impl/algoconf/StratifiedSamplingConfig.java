/**
 * StratifiedSamplingOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

/**
 * @author Jimmy
 *
 */
public class StratifiedSamplingConfig extends RandomSamplingConfig {

	private String samplingColumn;
	protected static final String ConstSamplingColumn = "samplingColumn";
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstSampleCount);
		parameters.add(ConstSampleSizeType);
		parameters.add(ConstSampleSize);
		parameters.add(ConstRandomSeed);
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTablePrefix);
		parameters.add(ConstKeyColumnList);
		parameters.add(ConstSamplingColumn);
		parameters.add(ConstConsistent);
		parameters.add(ConstDisjoint);
		parameters.add(ConstOutputTableStorageParameters);
	}
	
	public StratifiedSamplingConfig(String outputType, 
			String outputSchema, String outputTablePrefix, String dropIfExist) {
		this();
		this.setOutputType(outputType);
		this.setOutputSchema(outputSchema);
		this.setOutputTable(outputTablePrefix);
		this.setDropIfExist(dropIfExist);
	}
	
	public StratifiedSamplingConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DataOperationSampleVisualizationType");
	}

	public String getSamplingColumn() {
		return samplingColumn;
	}

	public void setSamplingColumn(String samplingColumn) {
		this.samplingColumn = samplingColumn;
	}
}
