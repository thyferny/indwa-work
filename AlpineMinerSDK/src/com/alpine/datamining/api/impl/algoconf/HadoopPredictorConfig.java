/**
 * 

* ClassName HadoopPredictorConfig.java
*
* Version information: 1.00
*
* Date: 2012-8-22
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Shawn
 *
 *  
 */

public class HadoopPredictorConfig extends PredictorConfig {
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	public static final String ConstResultsLocation ="resultsLocation";
	public static final String ConstResultsName ="resultsName";
	public static final String ConstOverride ="override";
	
	static{ 
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}

    private String resultsLocation;
    private String resultsName;
    private String override;
    
    
    public HadoopPredictorConfig() {
		setParameterNames(parameters);
	}

	private Map<String,Map<String,String>> multiResultNames;

	private String modelFile;
    public String getResultsLocation() {
        return resultsLocation;
    }
    public void setResultsLocation(String resultsLocation) {
        this.resultsLocation = resultsLocation;
    }
    public String getResultsName() {
        return resultsName;
    }
    public void setResultsName(String resultsName) {
        this.resultsName = resultsName;
    }
    public String getOverride() {
        return override;
    }
    public void setOverride(String override) {
        this.override = override;
    }
    public Map<String, Map<String, String>> getMultiResultNames() {
        return multiResultNames;
    }
    public void setMultiResultNames(
            Map<String, Map<String, String>> multiResultNames) {
        this.multiResultNames = multiResultNames;
    }
    
    public void setModelFile(String fileName) {
    	this.modelFile = fileName;
    }
    
    public String getModelFile() {
    	return this.modelFile;
    }
}
