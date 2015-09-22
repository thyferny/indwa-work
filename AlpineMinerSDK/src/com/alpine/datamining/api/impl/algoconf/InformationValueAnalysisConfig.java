/**
 * ClassName InformationValueAnalysisConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2011-1-4
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;


/**
 * Eason
 */
public class InformationValueAnalysisConfig extends AbstractAnalyticConfig{
	//dynamic set
 	//ruleCriterion
	
	private static final List<String> parameterNames = new ArrayList<String>();
	public static final String PARAMETER_DEPENDENT_COLUMN = "dependentColumn";
	public static final String PARAMETER_GOOD = "good";

	static{
 
		parameterNames.add(PARAMETER_COLUMN_NAMES);
		parameterNames.add(PARAMETER_DEPENDENT_COLUMN);
		parameterNames.add(PARAMETER_GOOD);
	}
	private String dependentColumn = null;//1,2,3
	private String good = null;


	//has no parameters to config...
	//columnNames is dynamic getted from the parent...
	public InformationValueAnalysisConfig(){
		setParameterNames(parameterNames );
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.InformationValueAnalysisVisualizationType");
	}
	
	public InformationValueAnalysisConfig( String columnNames){
		this();
		setColumnNames (columnNames);
 	}

	public String getDependentColumn() {
		return dependentColumn;
	}

	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	public String getGood() {
		return good;
	}

	public void setGood(String good) {
		this.good = good;
	}

}
