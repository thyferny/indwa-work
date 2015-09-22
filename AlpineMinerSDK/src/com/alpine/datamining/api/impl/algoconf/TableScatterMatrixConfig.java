package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class TableScatterMatrixConfig extends AbstractAnalyticConfig {
	
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.ScatterMatrixImageVisualizationType";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(PARAMETER_COLUMN_NAMES);
	}


	public TableScatterMatrixConfig() {
		super();
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}
	
	public TableScatterMatrixConfig(String columnNames) {
		this();
		setColumnNames(columnNames);
	}

	
}
