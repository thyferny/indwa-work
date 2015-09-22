/**
 * ClassName DataAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.List;


/**
 * @author John Zhao
 * 
 *anlyzer .modeltrainer. predictor
 */
public interface DataAnalyzer {
 	//some operator may need refining to return model
	public AnalyticOutPut doAnalysis(AnalyticSource source ) 	throws AnalysisException;
	
//	public List<Object> getResourceConnections() ;
	
 	public String getVersion();
 	
 	public AnalyticSource getAnalyticSource();
 	
 	public void setAnalyticSource(AnalyticSource source);
 	
 	public AnalyticOutPut getOutPut();
 	
 	public void setOutPut(AnalyticOutPut outPut) ;
 	
 	public String getName();
 	
 	public void setName(String name);

 	public void setListeners(List<AnalyticProcessListener> listeners);
 
 	public void addListener(AnalyticProcessListener listener);

	public void setContext(AnalyticContext context);

	public void setUUID(String id); 
	
	public String getUUID ();

	
	public void setFlowRunUUID(String id); 
	
	public String getFlowRunUUID ();

	
	public void stop();
 	
 	
}
