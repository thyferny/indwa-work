/**
 * ClassName VariableConfig.java
 *
 * Version information:1.00
 *
 * Date:Jun 3, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldsModel;

/**
 * @author Richie Lo
 *
 */
public class HadoopVariableConfig extends HadoopDataOperationConfig {

	private AnalysisDerivedFieldsModel derivedModel;

	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	
	public HadoopVariableConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
	 }
	
	public HadoopVariableConfig() {
		super();
		setParameterNames(parameters);
	}

	public AnalysisDerivedFieldsModel getDerivedModel() {
		return derivedModel;
	}

	public void setDerivedModel(AnalysisDerivedFieldsModel derivedModel) {
		this.derivedModel = derivedModel;
	}
	
	
}
