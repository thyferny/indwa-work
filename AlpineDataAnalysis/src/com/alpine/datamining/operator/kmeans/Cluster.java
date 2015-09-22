/**
 * ClassName Cluster.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.kmeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Cluster implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5490123376765480726L;
	private ArrayList<Object> dataIds;
	private String clusterId;
	private Map<String,Long> clustermap=new HashMap<String,Long>();
	
	public Cluster(String clusterId,long l) {
		this.clusterId=clusterId;
		clustermap.put(clusterId,l);
	}

	public Collection<Object> getDataIds() {
		return dataIds;
	}

	public boolean containsDataId(Object id) {
		return dataIds.contains(id);
	}

	public String getClusterId() {
		return clusterId;
	}


	public long getNumberOfData() {
		return clustermap.get(this.clusterId);
	}


	public String toString() {
		return "cluster_" + clusterId;
	}
}
