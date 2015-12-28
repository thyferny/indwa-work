
package com.alpine.datamining.operator.hadoop.output;

import java.util.List;
import java.util.Map;


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
