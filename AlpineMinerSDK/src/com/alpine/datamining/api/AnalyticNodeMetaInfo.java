/**
 * ClassName  AnalyticNodeMetaInfo.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.Date;
import java.util.Properties;

/**
 * @author John Zhao
 *
 */
public class AnalyticNodeMetaInfo {

	private String algorithmName;
	 	
	private String algorithmDescription;
	
	private String name;
	
	private Date endTime;

	private Date startTime;
	
	private Properties properties;
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public String getAlgorithmDescription() {
		return algorithmDescription;
	}

	public void setAlgorithmDescription(String algorithmDescription) {
		this.algorithmDescription = algorithmDescription;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AnalyticNodeMetaInfo [algorithmName=");
		builder.append(algorithmName);
		builder.append(", algorithmDescription=");
		builder.append(algorithmDescription);
		builder.append(", name=");
		builder.append(name);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", startTime=");
		builder.append(startTime);
		return builder.toString();
	}

    public String getElapsedTime()
    {
        if (getEndTime() == null || getStartTime() == null) return "N/A";
        return Long.toString(Math.round((getEndTime().getTime() - getStartTime().getTime())/1000));

    }
	
	//AnalyticSource analyticSource;//table and input...
}
