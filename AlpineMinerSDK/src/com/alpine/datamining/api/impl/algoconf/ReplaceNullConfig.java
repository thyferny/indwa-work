/**
 * ClassName ReplaceNullConfig
 *
 * Version information:1.00
 *
 * Date:Jun 1, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;

/**
 *
 */
public class ReplaceNullConfig extends DataOperationConfig {

	private AnalysisNullReplacementModel nullReplacementModel;

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);
		
	}
	public ReplaceNullConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
 	}
	public ReplaceNullConfig(){
		super();
		setParameterNames(parameters);
	}
	public AnalysisNullReplacementModel getNullReplacementModel() {
		return nullReplacementModel;
	}
	public void setNullReplacementModel(AnalysisNullReplacementModel nullReplacementModel) {
		this.nullReplacementModel = nullReplacementModel;
	}
}
