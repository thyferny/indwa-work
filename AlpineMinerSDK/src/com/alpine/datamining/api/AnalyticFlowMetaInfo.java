/**
  * ClassName AnalyticMetaInfo.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.Date;
import java.util.Properties;

import com.alpine.utility.db.Resources;

/**
 * @author John Zhao
 * 
 */
public class AnalyticFlowMetaInfo {

	private static final String APP_NAME = "Alpine Miner "+ Resources.minerEdition;
	private Properties analyticServerConfig;
	private String executeUserName;
	
	public String getExecuteUserName() {
		return executeUserName;
	}

	public void setExecuteUserName(String executeUserName) {
		this.executeUserName = executeUserName;
	}

	public String getFlowFileName() {
		return flowFileName;
	}

	public void setFlowFileName(String flowFileName) {
		this.flowFileName = flowFileName;
	}

	public String getFlowFilePath() {
		return flowFilePath;
	}

	public void setFlowFilePath(String flowFilePath) {
		this.flowFilePath = flowFilePath;
	}

	public String getFlowOwnerUser() {
		return flowOwnerUser;
	}

	public void setFlowOwnerUser(String flowOwnerUser) {
		this.flowOwnerUser = flowOwnerUser;
	}

	public String getFlowDescription() {
		return flowDescription;
	}

	public void setFlowDescription(String flowDescription) {
		this.flowDescription = flowDescription;
	}

	private String flowFileName;
	private String flowFilePath;
	private String flowOwnerUser;
	private String flowDescription;
	private Date endTime;
	
	private Date startTime;

	// source info is in each analyzer's source object and configuration

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

 

	// <Application name="alpine Intelligent Miner" version="9.1"/>

	public String getAnalyticApplicationName() {
		return APP_NAME;
	}

	public String getCopyRightInfo() {
		return Resources.coptRight;

	}

	// some special info for extension
	public String getExtendedInfo() {
		return "";
	}

	public String getAnalyticApplicationVersion() {
		return Resources.minerEdition;
	}

	// <Application name="IBM DB2 Intelligent Miner" version="9.1"/>

	public Properties getAnalyticServerConfig() {
		return analyticServerConfig;
	}

	public void setAnalyticServerConfig(Properties analyticServerConfig) {
		this.analyticServerConfig = analyticServerConfig;
	}
}
