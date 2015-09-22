/**
 * ClassName AbstractSVMConfig
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;


/**
 * Eason
 */

public class AbstractSVMConfig extends AbstractModelTrainerConfig{
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.SVMTextAndTableVisualizationType";
	private String kernelType;
	private String degree;
	private String gamma;
	private String eta;
	private String nu;
	public static final String ConstKernelType = "kernelType";
	public static final String ConstDegree = "degree";
	public static final String ConstGamma = "gamma";
	public static final String ConstEta = "eta";
	public static final String ConstNu = "nu";

	public static String[] kernelTypeArray={"dot product","polynomial","gaussian"};
	
	static{ 
		}

	public AbstractSVMConfig(String columnnames, String dependentColumn){
		this();
		setColumnNames(columnnames);
		setDependentColumn( dependentColumn);
	}
	
	public AbstractSVMConfig(){
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public String getKernelType() {
		return kernelType;
	}

	public void setKernelType(String kernelType) {
		this.kernelType = kernelType;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getGamma() {
		return gamma;
	}

	public void setGamma(String gamma) {
		this.gamma = gamma;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

	public String getNu() {
		return nu;
	}

	public void setNu(String nu) {
		this.nu = nu;
	}

}
