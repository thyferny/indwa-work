/**
 * ClassName ValueAnalysisConfig.java
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
public class ValueAnalysisConfig extends AbstractAnalyticConfig{
 
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(PARAMETER_COLUMN_NAMES);
	}
 
	//ValueAnalysis has no configurable parameters...
	public ValueAnalysisConfig(){
		setParameterNames(parameters);
		 setVisualizationTypeClass(
				 "com.alpine.datamining.api.impl.visual.ValueShapeVisualizationType"+","+
				 "com.alpine.datamining.api.impl.visual.ValueNumericVisualizationType"+","+
				 "com.alpine.datamining.api.impl.visual.ValueTableVisualizationType");
	}
	
 
}
