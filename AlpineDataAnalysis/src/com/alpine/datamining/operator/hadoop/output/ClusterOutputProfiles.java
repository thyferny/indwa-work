/**
 * ClassName ClusterOutputProfiles.java
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
public class ClusterOutputProfiles {
	
	private long totalRowCounts;//count
	private Map<String,List<KmeansValueRange>> columnRangeMap;
	private List<ClusterRangeInfo> clusterRangeInfo;//each cluster count
	
	public long getTotalRowCounts() {
		return totalRowCounts;
	}
	public void setTotalRowCounts(long totalRowCounts) {
		this.totalRowCounts = totalRowCounts;
	}
	public Map<String, List<KmeansValueRange>> getColumnRangeMap() {
		return columnRangeMap;
	}
	public void setColumnRangeMap(Map<String, List<KmeansValueRange>> columnRangeMap) {
		this.columnRangeMap = columnRangeMap;
	}
	public List<ClusterRangeInfo> getClusterRangeInfo() {
		return clusterRangeInfo;
	}
	public void setClusterRangeInfo(List<ClusterRangeInfo> clusterRangeInfo) {
		this.clusterRangeInfo = clusterRangeInfo;
	}
}
