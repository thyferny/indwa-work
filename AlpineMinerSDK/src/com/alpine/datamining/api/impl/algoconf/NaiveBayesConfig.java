/**
 * ClassName NaiveBayesConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;


/**
 * Eason
 */

public class NaiveBayesConfig extends AbstractModelTrainerConfig{

	 
 
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.NaiveBayesTableVisualizationType";
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	public static final String ConstCalculateDeviance = "calculateDeviance";

	private String calculateDeviance = "";

	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstCalculateDeviance);
		}

	public NaiveBayesConfig(String columnnames, String dependentColumn){
		this();
		setColumnNames(columnnames);
		setDependentColumn( dependentColumn);
	}
	
	public NaiveBayesConfig(   ){
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public String getCalculateDeviance() {
		return calculateDeviance;
	}

	public void setCalculateDeviance(String calculateDeviance) {
		this.calculateDeviance = calculateDeviance;
	}
 
}
