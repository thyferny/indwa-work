package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

public class HadoopColumnFilterConfig extends HadoopDataOperationConfig {

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	
	public HadoopColumnFilterConfig() {
		super();
		setParameterNames(parameters);
	}
}
