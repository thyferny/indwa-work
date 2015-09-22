/**
 * ClassName VariableSelectionConfig
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
import java.util.Locale;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.resources.SDKLanguagePack;


/**
 * Eason
 */
public class VariableSelectionConfig extends AbstractAnalyticConfig{

	private static final List<String> parameterNames = new ArrayList<String>();
	public static final String PARAMETER_DEPENDENT_COLUMN = "dependentColumn";
	private String scoreType = "1";
	public static final String PARAMETER_scoreType = "scoreType";

	static{
 
		parameterNames.add(PARAMETER_COLUMN_NAMES);
		parameterNames.add(PARAMETER_DEPENDENT_COLUMN);
		parameterNames.add(PARAMETER_scoreType);
	}
	private String dependentColumn = null;

	public VariableSelectionConfig(){
		setParameterNames(parameterNames );
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.VariableSelectionTextAndTableVisualizationType");
	}
	
	public VariableSelectionConfig( String columnNames){
		this();
		setColumnNames (columnNames);
 	}

	public String getDependentColumn() {
		return dependentColumn;
	}

	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	public String getScoreType() {
		return scoreType;
	}

	public void setScoreType(String scoreType) {
		this.scoreType = scoreType;
	}
	
	public static String[] getScoreTypeArray(Locale locale){
		return  new String[]{
				SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_SCORETYPE_INFO_GAIN,locale),
				SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO,locale),
				SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN,locale)};
			
	}

}
