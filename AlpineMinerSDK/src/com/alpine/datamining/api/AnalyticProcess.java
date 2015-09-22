/**
 * ClassName AnalysisProcess.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.io.Serializable;

/**
 * It represent an analysis procedure for a flow...
 * @author John Zhao
 *
 */
public interface AnalyticProcess extends Serializable {
	
	public static final String EXE_MODE_AUTO="AUTO";
	//means step by step
	public static final String EXE_MODE_INTERACT="INTE";
	
	
	public static final String CLIENT_TYPE_WEB="WEB";
	//means step by step
	public static final String EXE_MODE_RCP="RCP";
	
	public static final String EXE_MODE_WS="WS";
	
 
	public AnalyticFlow getFlow() ;
	public void setFlow(AnalyticFlow flow) ;
 
	public String getProcessID() ;
	public void setProcessID(String processID);
	/**
	 * @return
	 */
	public String getName();
	public void setName(String name);
	
	public String getClientType() ;
	public void setClientType(String clientType) ;
	
	public boolean isSaveResult() ;
	public void setSaveResult(boolean saveResult) ;
	
	public String getExecuteUserName() ;
	public void setExecuteUserName(String executeUserName) ;
	
	public String getExecuteMode() ;
	public void setExecuteMode(String executeMode);
 
	public String getFlowFilePath() ;
	public void setFlowFilePath(String fowFilePath) ;
	
}
