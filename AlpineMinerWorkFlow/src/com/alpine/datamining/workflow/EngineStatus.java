/**
 * ClassName EngineStatus.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.alpine.datamining.api.AnalyticFlowStatus;

/**
 * @author John Zhao
 *
 */
public class EngineStatus {
	
	List<AnalyticFlowStatus> activeFlowStatus;
	List<AnalyticFlowStatus> waitingFlowStatus;
	Properties systemProperties ;
	
	
	public List<AnalyticFlowStatus> getWaitingFlowStatus() {
		return waitingFlowStatus;
	}

	public void setWaitingFlowStatus(List<AnalyticFlowStatus> waitingFlowStatus) {
		this.waitingFlowStatus = waitingFlowStatus;
	}

	public List<AnalyticFlowStatus> getActiveFlowStatus() {
		return activeFlowStatus;
	}

	public void setActiveFlowStatus(List<AnalyticFlowStatus> flowStatus) {
		this.activeFlowStatus = flowStatus;
	}

	public Properties getSystemProperties() {
		return systemProperties;
	}

	public void setSystemProperties(Properties systemProperties) {
		this.systemProperties = systemProperties;
	}
	
	public String toString() {
		String statusStr = "==EngineStatus:\n";
		statusStr = statusStr + "--Active Flow:\n"; 
		for (Iterator it = activeFlowStatus.iterator(); it.hasNext();) {
			AnalyticFlowStatus status = (AnalyticFlowStatus) it.next();
			statusStr = statusStr + status.toString() + "\n";
		}
		statusStr = statusStr + "--Waiting Flow:\n"; 
		for (Iterator it = waitingFlowStatus.iterator(); it.hasNext();) {
			AnalyticFlowStatus status = (AnalyticFlowStatus) it.next();
			statusStr = statusStr + status.toString() + "\n";
		}

		statusStr = statusStr + "=================\n";
		return statusStr;
	}
}
