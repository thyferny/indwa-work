/**
 * ClassName CorrelationAnalysisConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;


/**
 * Eason
 */
public class CorrelationAnalysisConfig extends AbstractAnalyticConfig{
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(PARAMETER_COLUMN_NAMES);
	}

	public CorrelationAnalysisConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.CorrelationTextVisualizationType");
	}
	public CorrelationAnalysisConfig(String columnX, String columnY){
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.CorrelationTextVisualizationType");
	}
}
