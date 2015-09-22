/**
 * ClassName ClusterKMeansConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-18
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class HadoopKMeansConfig extends AbstractAnalyticConfig {
	private String k;
	private String clusterColumnName;
	private String distance;
	private String split_Number;//integer. 1 to +infinite. default:5
	private String add_cluster_attribute;//boolean. default:true
	private String max_runs;//integer. 1 to +infinite. default:10
	private String max_optimization_steps; //integer. 1 to +infinite. default:10
	
	private String storeResults;
	private String resultsLocation;
	private String resultsName;
	private String override;
	private String idColumn;
	
	private static final String ConstIdColumn = "idColumn";
	private static final String ConstK = "k";
	private static final String ConstDistance="distance";
	private static final String ConstSplitNumber="split_Number";
	private static final String ConstMax_runs="max_runs";
	private static final String ConstMax_optimization_steps="max_optimization_steps";
	private static final String ConstclusterColumnName = "clusterColumnName";
	private static final String ConstStoreResults ="storeResults";
	private static final String ConstResultsLocation ="resultsLocation";
	private static final String ConstResultsName ="resultsName";
	private static final String ConstOverride ="override";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstIdColumn);
		parameters.add(ConstK);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstDistance);
		parameters.add(ConstclusterColumnName);
		parameters.add(ConstSplitNumber);
		parameters.add(ConstMax_runs);
		parameters.add(ConstMax_optimization_steps);
		parameters.add(ConstStoreResults);
		parameters.add(ConstResultsLocation);
		parameters.add(ConstResultsName);
		parameters.add(ConstOverride);	
	}
	public HadoopKMeansConfig(String columnnames, String k) throws AnalysisException {
		super();
		setColumnNames(columnnames);
		init();
		this.k = k;
		}
	
	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	/**
	 * 
	 */
	private void init() {
		setParameterNames(parameters);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.HadoopKmeansTextVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.HadoopKmeansProfilesVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.HadoopKmeansTableDataVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.HadoopKmeansWarningVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.HadoopKmeansCentroidsVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.HadoopKmeansClusterVisualizationType"
				);

		
	}

	public HadoopKMeansConfig(){
		super();
		init();
	}
	
	public String getSplit_Number() {
		return split_Number;
	}

	public void setSplit_Number(String splitNumber) {
		split_Number = splitNumber;
	}
 
	public String getClusterColumnName() {
		return clusterColumnName;
	}

	public void setClusterColumnName(String clusterColumnName) {
		this.clusterColumnName = clusterColumnName;
	}
	
	public String getAdd_cluster_attribute() {
		return add_cluster_attribute;
	}
	public void setAdd_cluster_attribute(String addClusterAttribute) {
		add_cluster_attribute = addClusterAttribute;
	}
	public String getMax_runs() {
		return max_runs;
	}
	public void setMax_runs(String maxRuns) {
		max_runs = maxRuns;
	}
	public String getMax_optimization_steps() {
		return max_optimization_steps;
	}
	public void setMax_optimization_steps(String maxOptimizationSteps) {
		max_optimization_steps = maxOptimizationSteps;
	}
	public String getK() {
		return k;
	}
	public void setK(String k) {
		this.k = k;
	}

	public String getStoreResults() {
		return storeResults;
	}

	public void setStoreResults(String storeResults) {
		this.storeResults = storeResults;
	}

	public String getResultsLocation() {
		return resultsLocation;
	}

	public void setResultsLocation(String resultsLocation) {
		this.resultsLocation = resultsLocation;
	}

	public String getResultsName() {
		return resultsName;
	}

	public void setResultsName(String resultsName) {
		this.resultsName = resultsName;
	}

	public String getOverride() {
		return override;
	}

	public void setOverride(String override) {
		this.override = override;
	}

	public String getIdColumn() {
		return this.idColumn;
	}
	
	public void setIdColumn(String id) {
		this.idColumn = id;
	}
	
	
}
