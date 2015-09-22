/**
 * ClassName FrequencyAnalysisConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;


/**
 * Eason
 */
public class FrequencyAnalysisConfig extends AbstractAnalyticConfig{
	//dynamic set
 	//ruleCriterion
	
	private static final List<String> parameterNames = new ArrayList<String>();
	static{
 
		parameterNames.add(PARAMETER_COLUMN_NAMES);
	}


	//has no parameters to config...
	//columnNames is dynamic getted from the parent...
	public FrequencyAnalysisConfig(){
		setParameterNames(parameterNames );
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.FrequencyShapeVisualizationType"+","
				+"com.alpine.datamining.api.impl.visual.FrequencyTableVisualizationType");
	}
	
	public FrequencyAnalysisConfig( String columnNames){
		this();
		setColumnNames (columnNames);
 	}
}
