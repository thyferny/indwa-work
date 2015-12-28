
package com.alpine.datamining.operator.hadoop.output;

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
