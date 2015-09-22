/**
 * ClassName AnalyticResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.List;

/**
 * @author John Zhao
 *
 */
public interface AnalyticResult {

	public AnalyticFlowMetaInfo getAnalyticMetaInfo();
	
	public void setAnalyticMetaInfo(AnalyticFlowMetaInfo metaInfo);

	public abstract List<AnalyticOutPut> getOutPuts();

	public abstract void setOutPuts(List<AnalyticOutPut> outPuts);
	
	//like a session ID
	public String getProcessID();
	public void setProcessID(String processID);

}