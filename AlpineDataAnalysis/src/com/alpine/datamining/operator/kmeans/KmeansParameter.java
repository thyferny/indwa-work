package com.alpine.datamining.operator.kmeans;


import com.alpine.datamining.operator.Parameter;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;


public class KmeansParameter implements Parameter {
	private int k = 3;
	private int splitNumber = 5;
	private int maxRuns = 10;
	private int maxOptimizationSteps = 10;
	private String resultTableName;
	private String resultSchema;
	private String distance;
	private String dropIfExist = "Yes";
	private String clusterColumnName;
	private boolean useArray = false;
	AnalysisStorageParameterModel analysisStorageParameterModel = null;

	public int getK() {
		return k;
	}
	public void setK(int k) {
		this.k = k;
	}
	public int getSplitNumber() {
		return splitNumber;
	}
	public void setSplitNumber(int splitNumber) {
		this.splitNumber = splitNumber;
	}
	public int getMaxRuns() {
		return maxRuns;
	}
	public void setMaxRuns(int maxRuns) {
		this.maxRuns = maxRuns;
	}
	public int getMaxOptimizationSteps() {
		return maxOptimizationSteps;
	}
	public void setMaxOptimizationSteps(int maxOptimizationSteps) {
		this.maxOptimizationSteps = maxOptimizationSteps;
	}
	public String getResultTableName() {
		return resultTableName;
	}
	public void setResultTableName(String resultTableName) {
		this.resultTableName = resultTableName;
	}
	public String getResultSchema() {
		return resultSchema;
	}
	public void setResultSchema(String resultSchema) {
		this.resultSchema = resultSchema;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getDropIfExist() {
		return dropIfExist;
	}
	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}
	public String getClusterColumnName() {
		return clusterColumnName;
	}
	public void setClusterColumnName(String clusterColumnName) {
		this.clusterColumnName = clusterColumnName;
	}
	public boolean isUseArray() {
		return useArray;
	}
	public void setUseArray(boolean useArray) {
		this.useArray = useArray;
	}
	public AnalysisStorageParameterModel getAnalysisStorageParameterModel() {
		return analysisStorageParameterModel;
	}
	public void setAnalysisStorageParameterModel(
			AnalysisStorageParameterModel analysisStorageParameterModel) {
		this.analysisStorageParameterModel = analysisStorageParameterModel;
	}

}
