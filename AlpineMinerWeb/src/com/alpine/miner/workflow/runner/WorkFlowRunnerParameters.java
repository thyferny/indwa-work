/**
 * ClassName AbstractWorkFlowRunnerParameters.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.runner;

import java.util.List;

import com.alpine.datamining.api.AnalyticProcessListener;

public class WorkFlowRunnerParameters {
	
	String filePath;
	
	List<AnalyticProcessListener> listeners;

	public WorkFlowRunnerParameters(String filePath,
			List<AnalyticProcessListener> listeners) {
		this.filePath = filePath;
		this.listeners = listeners;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public List<AnalyticProcessListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<AnalyticProcessListener> listeners) {
		this.listeners = listeners;
	}

}
