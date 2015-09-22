/**
 * ClassName ClusterOutputText.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.operator.hadoop.output;
/**
 * @author Jeff Dong
 *
 */
public class ClusterOutputBasicInfo {
	private String clusterColumName;
	private int clusterCount;
	private double avgDistanceMeasurement;
	
	public String getClusterColumName() {
		return clusterColumName;
	}
	public void setClusterColumName(String clusterColumName) {
		this.clusterColumName = clusterColumName;
	}
	public int getClusterCount() {
		return clusterCount;
	}
	public void setClusterCount(int clusterCount) {
		this.clusterCount = clusterCount;
	}
	public double getAvgDistanceMeasurement() {
		return avgDistanceMeasurement;
	}
	public void setAvgDistanceMeasurement(double avgDistanceMeasurement) {
		this.avgDistanceMeasurement = avgDistanceMeasurement;
	}
}
