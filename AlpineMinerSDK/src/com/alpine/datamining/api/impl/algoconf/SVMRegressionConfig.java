/**
 * ClassName SVMRegressionConfig
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

public class SVMRegressionConfig extends AbstractSVMConfig{

	 
 
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	private String slambda;
	public final static String ConstSlambda = "slambda";
	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstKernelType);
		parameters.add(ConstDegree);
		parameters.add(ConstGamma);
		parameters.add(ConstEta);
		parameters.add(ConstNu);
		parameters.add(ConstSlambda);
		}

	public SVMRegressionConfig(String columnnames, String dependentColumn){
		this();
		setColumnNames(columnnames);
		setDependentColumn( dependentColumn);
	}
	
	public SVMRegressionConfig(){
		super();
		setParameterNames(parameters);
	}

	public String getSlambda() {
		return slambda;
	}

	public void setSlambda(String slambda) {
		this.slambda = slambda;
	}
}
