/**
 * ClassName ClusterRangeInfo.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.operator.hadoop.output;

import java.util.List;
import java.util.Map;

/**
 * @author Jeff Dong
 *
 */
public class ClusterRangeInfo {
	//column -> list of range
	private Map<String,List<Long>> columnRangeRowCountMap;
	private String clusterName;
	private long clusterRowCounts;
	  
	public Map<String, List<Long>> getColumnRangeRowCountMap() {
		return columnRangeRowCountMap;
	}
	public void setColumnRangeRowCountMap(
			Map<String, List<Long>> columnRangeRowCountMap) {
		this.columnRangeRowCountMap = columnRangeRowCountMap;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public long getClusterRowCounts() {
		return clusterRowCounts;
	}
	public void setClusterRowCounts(long clusterRowCounts) {
		this.clusterRowCounts = clusterRowCounts;
	}

}
