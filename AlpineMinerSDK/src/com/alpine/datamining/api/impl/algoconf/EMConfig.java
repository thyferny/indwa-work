/**
 * 

* ClassName EMConfig.java
*
* Version information: 1.00
*
* Data: Apr 25, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;

/**
 * @author Shawn
 *
 */
public class EMConfig extends AbstractModelTrainerConfig{
	
	private final static List<String> parameters=new  ArrayList<String>();
	
	public static final String VISUALIZATION_TYPE ="";
	
	public static final String ConstClusterNumber="clusterNumber";
	public static final String ConstEPSILON = "epsilon";
	public static final String ConstMAXITERATIONNUMBER = "maxIterationNumber";
	public static final String ConstInitClusterSize="initClusterSize";
	
	
	private String initClusterSize=null;
	
	static{ 
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstClusterNumber);
		parameters.add(ConstEPSILON);
		parameters.add(ConstMAXITERATIONNUMBER);
		parameters.add(ConstInitClusterSize);
	}
	
	public EMConfig() {
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	private String clusterNumber=null;
	private String maxIterationNumber=null;
	private String epsilon = null;
	
	public String getMaxIterationNumber() {
		return maxIterationNumber;
	}

	public void setMaxIterationNumber(String maxIterationNumber) {
		this.maxIterationNumber = maxIterationNumber;
	}

	public String getClusterNumber() {
		return clusterNumber;
	}

	public void setClusterNumber(String clusterNumber) {
		this.clusterNumber = clusterNumber;
	}

	public String getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(String epsilon) {
		this.epsilon = epsilon;
	}
	

	public String getInitClusterSize() {
		return initClusterSize;
	}

	public void setInitClusterSize(String initClusterSize) {
		this.initClusterSize = initClusterSize;
	}
	
}
