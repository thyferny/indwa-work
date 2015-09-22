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

public class ClusterKMeansConfig extends AbstractAnalyticConfig {
	private String idColumn;
	private String k;
	private String clusterColumnName;
	private String distance;
	private String outputTable;//String. 
	private String outputSchema;//String. 
	private String dropIfExist;
	private String split_Number;//integer. 1 to +infinite. default:5
	private String add_cluster_attribute;//boolean. default:true
	private String max_runs;//integer. 1 to +infinite. default:10
	private String max_optimization_steps; //integer. 1 to +infinite. default:10
	private String useArray; //
	
	private static final String ConstIdColumn = "idColumn";
	private static final String ConstK = "k";
	private static final String ConstDistance="distance";
	private static final String ConstOutputShemaName="outputSchema";
	private static final String ConstOutputTableName="outputTable";
	private static final String ConstSplitNumber="split_Number";
	private static final String ConstMax_runs="max_runs";
	private static final String ConstMax_optimization_steps="max_optimization_steps";
	private static final String ConstDropIfExist = "dropIfExist";
	private static final String ConstclusterColumnName = "clusterColumnName";
	private static final String ConstUseArray = "useArray";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstIdColumn);
		parameters.add(ConstK);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstDistance);
		parameters.add(ConstOutputShemaName);
		parameters.add(ConstOutputTableName);
		parameters.add(ConstOutputTableStorageParameters);
		parameters.add(ConstclusterColumnName);
		parameters.add(ConstSplitNumber);
		parameters.add(ConstMax_runs);
		parameters.add(ConstMax_optimization_steps);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstUseArray);
	}
	public ClusterKMeansConfig(String columnnames, String id, String k) throws AnalysisException {
		super();
		setColumnNames(columnnames);
		init();
		
		this.idColumn = id;
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
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.KMeansTextVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.KMeansTableVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.KMeansTableDataVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.KMeansTableInfoVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.KMeansTextWarningVisualizationType"+","+
				"com.alpine.datamining.api.impl.visual.KMeansClusterAllVisualizationType"
//				"com.alpine.datamining.api.impl.visual.KMeansClusterScatterVisualizationType"
				);

		
	}

	public ClusterKMeansConfig( )     {
		super();
		init();
//		setParameterNames(parameters);
//		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.KMeansTextVisualizationType"+","+
//		"com.alpine.datamining.api.impl.visual.KMeansTableVisualizationType"+","+
//		"com.alpine.datamining.api.impl.visual.KMeansTableDataVisualizationType");

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

	public String getOutputTable() {
		return outputTable;
	}

	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}

	public String getOutputSchema() {
		return outputSchema;
	}

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

	public String getDropIfExist() {
		return dropIfExist;
	}

	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
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
	public String getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(String id) {
		this.idColumn = id;
	}
	public String getK() {
		return k;
	}
	public void setK(String k) {
		this.k = k;
	}

	public String getUseArray() {
		return useArray;
	}

	public void setUseArray(String useArray) {
		this.useArray = useArray;
	}
}
