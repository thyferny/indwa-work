/**
 * ClassName DbTableAnalysisConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;
/**
 * nhosgur
 */
import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class BarChartAnalysisConfig extends AbstractAnalyticConfig {
	private String valueDomain;
	private String scopeDomain;
	private String categoryType;
	public static final String ConstValueDomain = "valueDomain";
	public static final String ConstScopeDomain = "scopeDomain";
	public static final String ConstCategoryType = "categoryType";
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstValueDomain);
		parameters.add(ConstScopeDomain);
		parameters.add(ConstCategoryType);
		 
		
	}
	
	public String getValueDomain() {
		return valueDomain;
	}
	public void setValueDomain(String valueDomain) {
		this.valueDomain = valueDomain;
	}
	public String getScopeDomain() {
		return scopeDomain;
	}
	public void setScopeDomain(String scopeDomain) {
		this.scopeDomain = scopeDomain;
	}
	public String getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}
	
	public BarChartAnalysisConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DbTableAnalysisImageVisualizationType");
	}
	public BarChartAnalysisConfig(String valueDomain,String scopeDomain,String categoryType) {
		this.valueDomain = valueDomain;
		this.scopeDomain = scopeDomain;
		this.categoryType = categoryType;
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DbTableAnalysisImageVisualizationType");
	}
}
