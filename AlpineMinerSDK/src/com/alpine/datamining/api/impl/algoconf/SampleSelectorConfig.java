/**
 * ClassName SampleSelectorConfig.java
 *
 * Version information:1.00
 *
 * Date:Jun 9, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

/**
 * @author Richie Lo
 *
 */
public class SampleSelectorConfig extends AbstractAnalyticConfig {

	private String selectedTable;
	
	private static final String ConstSelectedTable = "selectedTable";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstSelectedTable);		
	}
	
	public SampleSelectorConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DataOperationTableVisualizationType");
	}

	public void setSelectedTable(String selectedTable) {
		this.selectedTable = selectedTable;
	}
	
	public String getSelectedTable() {
		return selectedTable;
	}

}
