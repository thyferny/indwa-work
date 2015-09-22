/**
 * ClassName NormalizationConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;


/**
 * @author Jeff Dong
 * 
 */
public class HadoopNormalizationConfig extends HadoopDataOperationConfig {
	public static final String ConstMethod = "method";
	public static final String ConstRangeMin = "rangeMin";
	public static final String ConstRangeMax = "rangeMax";

	private String rangeMin=null;
	private String rangeMax=null;
	private String method = null;
	private String columnNames=null;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstMethod);
		parameters.add(ConstRangeMin);
		parameters.add(ConstRangeMax);
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
		parameters.add(PARAMETER_COLUMN_NAMES);
		
	}
	public String getColumnNames() {
		return columnNames;
	}



	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

	public String getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}



	public String getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(String rangeMax) {
		this.rangeMax = rangeMax;
	}

	
	public HadoopNormalizationConfig(){
		super();
		setParameterNames(parameters);
	}

	public HadoopNormalizationConfig(String outputType, String outputSchema,
			String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);	
	}
 
 

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

 

}
