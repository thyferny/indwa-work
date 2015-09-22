/**
 * ClassName AnalysisProcess.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import com.alpine.datamining.api.AnalyticFlow;
import com.alpine.datamining.api.AnalyticProcess;

/**
 * @author John Zhao
 *
 */
public class AnalyticProcessImpl implements AnalyticProcess{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6463146695656351416L;
	
	AnalyticFlow flow;
	String fowFilePath = null;

	String  processID;

	private String name;
	
	 
	String clientType; //web rcp or sth...
	
	//all output must be Serializable 
	boolean saveResult;
	
	String executeUserName;//will enable security later...

	String executeMode; //interactive(step by step), or automaticlly 
	
	
	public AnalyticProcessImpl(String fowFilePath){
		this.fowFilePath=fowFilePath;
	}
	
	public AnalyticFlow getFlow() {
		return flow;
	}
	
	public void setFlow(AnalyticFlow flow) {
		this.flow = flow;
	}
 
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcess#getProcessID()
	 */
	@Override
	public String getProcessID() {
		// TODO Auto-generated method stub
		return processID;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcess#setProcessID(java.lang.String)
	 */
	@Override
	public void setProcessID(String processID) {
		// TODO Auto-generated method stub
		 this.processID= processID;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcess#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticProcess#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name=name;
		
	}
	public String getClientType() {
		return clientType;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	public boolean isSaveResult() {
		return saveResult;
	}
	public void setSaveResult(boolean saveResult) {
		this.saveResult = saveResult;
	}
	public String getExecuteUserName() {
		return executeUserName;
	}
	public void setExecuteUserName(String executeUserName) {
		this.executeUserName = executeUserName;
	}
	public String getExecuteMode() {
		return executeMode;
	}
	public void setExecuteMode(String executeMode) {
		this.executeMode = executeMode;
	}
	
	public String getFlowFilePath() {
		return fowFilePath;
	}
	public void setFlowFilePath(String fowFilePath) {
		this.fowFilePath = fowFilePath;
	}

}
