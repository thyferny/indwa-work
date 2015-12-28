
package com.alpine.datamining.operator.hadoop.output;

import java.util.List;
import java.util.Map;


public class ClusterOutputModel {
	
	private List<String> columnNames;
	private List<String> columnTypes;
	private ClusterOutputBasicInfo outputText;
	private ClusterOutputProfiles outputProfiles;
	private List<String[]> dataSampleContents;
	private Map<String,Map<String,Double>> centroidsContents;
	private boolean isStable;
	private Map<String,Map<String,List<Double>>> outputScatters;
	public List<String> getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
	public List<String> getColumnTypes() {
		return columnTypes;
	}
	public void setColumnTypes(List<String> columnTypes) {
		this.columnTypes = columnTypes;
	}
	public ClusterOutputBasicInfo getOutputText() {
		return outputText;
	}
	public void setOutputText(ClusterOutputBasicInfo outputText) {
		this.outputText = outputText;
	}
	public ClusterOutputProfiles getOutputProfiles() {
		return outputProfiles;
	}
	public void setOutputProfiles(ClusterOutputProfiles outputProfiles) {
		this.outputProfiles = outputProfiles;
	}
	public List<String[]> getDataSampleContents() {
		return dataSampleContents;
	}
	public void setDataSampleContents(List<String[]> dataSampleContents) {
		this.dataSampleContents = dataSampleContents;
	}
	public Map<String,Map<String, Double>> getCentroidsContents() {
		return centroidsContents;
	}
	public void setCentroidsContents(Map<String,Map<String, Double>> centroidsContents) {
		this.centroidsContents = centroidsContents;
	}
	public boolean isStable() {
		return isStable;
	}
	public void setStable(boolean isStable) {
		this.isStable = isStable;
	}
	public Map<String,Map<String, List<Double>>> getOutputScatters() {
		return outputScatters;
	}
	public void setOutputScatters(Map<String,Map<String, List<Double>>> outputScatters) {
		this.outputScatters = outputScatters;
	}
}
