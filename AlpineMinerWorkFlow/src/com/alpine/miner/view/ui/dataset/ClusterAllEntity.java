package com.alpine.miner.view.ui.dataset;

import java.util.Hashtable;

public class ClusterAllEntity {

	private Hashtable<String, ClusterScatterEntity> entityHt = new Hashtable<String, ClusterScatterEntity>();
	private Object jfreechart;
	private String[] defaultColumn = new String[2];
	private String[] queryColumn;
	private String[] clusters;
	public String[] getClusters() {
		return clusters;
	}
	public void setClusters(String[] clusters) {
		this.clusters = clusters;
	} 
	
	
	
	public String[] getQueryColumn() {
		return queryColumn;
	}
	public void setQueryColumn(String[] queryColumn) {
		this.queryColumn = queryColumn;
	}
	public Hashtable<String, ClusterScatterEntity> getEntityHt() {
		return entityHt;
	}
	public void setEntityHt(Hashtable<String, ClusterScatterEntity> entityHt) {
		this.entityHt = entityHt;
	}
	public Object getJfreechart() {
		return jfreechart;
	}
	public void setJfreechart(Object jfreechart) {
		this.jfreechart = jfreechart;
	}
	public String[] getDefaultColumn() {
		return defaultColumn;
	}
	public void setDefaultColumn(String[] defaultColumn) {
		this.defaultColumn = defaultColumn;
	}
	
	
}
