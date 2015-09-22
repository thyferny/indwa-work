/**
 * ClassName AggregateConfig.java
 *
 * Version information:1.00
 *
 * Date:Jun 4, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowFieldsModel;

/**
 * @author Richie Lo
 *
 */
public class AggregateConfig extends DataOperationConfig {
	
	private AnalysisAggregateFieldsModel aggregateFieldsModel;
	
	private AnalysisWindowFieldsModel windowFieldsModel;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);

	}
	
	public AggregateConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
		}
	
	public AggregateConfig( ) {
		super();
		setParameterNames(parameters);
	}

	public AnalysisAggregateFieldsModel getAggregateFieldsModel() {
		return aggregateFieldsModel;
	}

	public void setAggregateFieldsModel(AnalysisAggregateFieldsModel aggregateFieldsModel) {
		this.aggregateFieldsModel = aggregateFieldsModel;
	}

	public AnalysisWindowFieldsModel getWindowFieldsModel() {
		return windowFieldsModel;
	}

	public void setWindowFieldsModel(AnalysisWindowFieldsModel windowFieldsModel) {
		this.windowFieldsModel = windowFieldsModel;
	}


	

}
