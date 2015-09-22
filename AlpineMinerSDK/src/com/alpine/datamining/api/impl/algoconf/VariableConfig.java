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
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileFieldsModel;

/**
 * @author Richie Lo
 *
 */
public class VariableConfig extends DataOperationConfig {

	private AnalysisQuantileFieldsModel quantModel; //becarefull not the same as param name

	private AnalysisDerivedFieldsModel derivedModel;

	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);
	}

	public VariableConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
	 }
	
	public VariableConfig() {
		super();
		setParameterNames(parameters);
	}
	
	public AnalysisQuantileFieldsModel getQuantileModel() {
		return quantModel;
	}

	public void setQuantileModel(AnalysisQuantileFieldsModel quantileModel) {
		this.quantModel = quantileModel;
	}

	public AnalysisDerivedFieldsModel getDerivedModel() {
		return derivedModel;
	}

	public void setDerivedModel(AnalysisDerivedFieldsModel derivedModel) {
		this.derivedModel = derivedModel;
	}
	
	
}
