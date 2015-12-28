
package com.alpine.datamining.operator.hadoop.output;

import java.util.List;
import java.util.Map;


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
