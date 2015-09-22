/**
 * ClassName SVMNoveltyDetectionConfig
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;


/**
 * Eason
 */

public class SVMNoveltyDetectionConfig extends AbstractSVMConfig{

	 
 
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstKernelType);
		parameters.add(ConstDegree);
		parameters.add(ConstGamma);
		parameters.add(ConstEta);
		parameters.add(ConstNu);
		}

	public SVMNoveltyDetectionConfig(String columnnames, String dependentColumn){
		this();
		setColumnNames(columnnames);
	}
	
	public SVMNoveltyDetectionConfig(){
		super();
		setParameterNames(parameters);
	}
}
