/**
 * ClassName :ModelInfo.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.web.resource;

import com.alpine.miner.impls.resource.ResourceInfo;

/**
 *  ModelInfo is a pure property file
 * @author zhaoyong
 *
 */
public class FlowResultInfo extends ResourceInfo{
	public static final String START_TIME = "start_time";

	public static final String END_TIME = "end_time";

	public static final String FLOW_NAME = "flowName";
	public static final String RUN_TYPE = "runType";
	
	public static final String RUN_TYPE_SCHEDULER = "Scheduler";
	public static final String RUN_TYPE_MANUAL = "Manual";
	
	public static final String FLOW_FULL_NAME = "flowFullName";

	private String flowName;
	private long endTime;	
	private long startTime; 
	private String runType;
	private String flowFullName;

	public String getRunType() {
		return runType;
	}


	public void setRunType(String runType) {
		this.runType = runType;
	}


	public String getFlowName() {
		return flowName;
	}


	public void setFlowName(String flowname) {
		this.flowName = flowname;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
 	
	public FlowResultInfo(String userName,ResourceType type,String id,long startTime,long endTime,String flowname,String runType){
		super(userName, id, type) ;
		this.startTime=startTime;
		this.endTime=endTime;
		this.flowName=flowname;
		this.runType = runType;
	}

	//this is used for load from file
	public FlowResultInfo() {
		 
	}


	public String getFlowFullName() {
		return flowFullName;
	}


	public void setFlowFullName(String flowFullName) {
		this.flowFullName = flowFullName;
	}
	  
}
