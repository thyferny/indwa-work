/**
 * TableBoxAndWhiskerConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

/**
 * @author Jimmy
 *
 */
public class TableBoxAndWhiskerConfig extends AbstractAnalyticConfig {
	
	private static final String PARAMETER_VALUEDOMAIN_COLUMN = "analysisValueDomain";
	private static final String PARAMETER_TYPEDOMAIN_COLUMN = "typeDomain";
	private static final String PARAMETER_SERIESDOMAIN_COLUMN = "seriesDomain";
    private static final String PARAMETER_USEAPPROXIMATION = "useApproximation";

	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(PARAMETER_VALUEDOMAIN_COLUMN);
		parameters.add(PARAMETER_TYPEDOMAIN_COLUMN);
		parameters.add(PARAMETER_SERIESDOMAIN_COLUMN);
        parameters.add(PARAMETER_USEAPPROXIMATION);
	}
	
	public TableBoxAndWhiskerConfig(){
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.BoxAndWhiskerImageVisualizationType");
		setParameterNames(parameters);
	}
    public TableBoxAndWhiskerConfig(String valueDomain,String seriesDomain,String typeDomain){
        this(valueDomain,seriesDomain,typeDomain,"true");
    }

	public TableBoxAndWhiskerConfig(String valueDomain,String seriesDomain,String typeDomain, String useApproximation){
		setAnalysisValueDomain(valueDomain);
		setSeriesDomain(seriesDomain);
		setTypeDomain(typeDomain);
        setUseApproximation(useApproximation);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.BoxAndWhiskerImageVisualizationType");
		setParameterNames(parameters);
	}
	private String valueDomain;
	private String seriesDomain;
	private String typeDomain;
    private boolean useApproximation = true;
	
	
	public String getAnalysisValueDomain() {
		return valueDomain;
	}
	public void setAnalysisValueDomain(String valueDomain) {
		this.valueDomain = valueDomain;
	}
	public String getSeriesDomain() {
		return seriesDomain;
	}
	public void setSeriesDomain(String seriesDomain) {
		this.seriesDomain = seriesDomain;
	}
	public String getTypeDomain() {
		return typeDomain;
	}
	public void setTypeDomain(String typeDomain) {
		this.typeDomain = typeDomain;
	}
    public boolean getUseApproximation()
    {
        return useApproximation;
    }
    public void setUseApproximation(String useApproximation)
    {
       //System.out.println("userApr: " + useApproximation);
        if (useApproximation.equalsIgnoreCase("Yes") ||useApproximation.equalsIgnoreCase("True") )
        {
            this.useApproximation = true;
        } else
        {
            this.useApproximation = false;
        }
    }

}
