/**
 * ClassName  RecommendationConfig
 *
 * Version information: 1.00
 *
 * Data: 2011-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eason
 * 
 */
public class RecommendationConfig extends DataOperationConfig {


	private String customerTable;
	private String customerIDColumn;
	
	private String customerValueColumn;
	
	private String customerProductColumn;

	private String customerProductCountColumn;

	private String selectionTable;
	private String selectionIDColumn;
	
	private String simThreshold;
	
	private String maxRecords;
	
	private String minProductCount;
	private String scoreThreshold;
	
	private String cohorts;
	private String cohortsAbove;
	private String cohortsBelow;
	
	private String targetCohort;
	
	public static final String ConstCustomerTable = "customerTable";
	public static final String ConstCustomerIDColumn = "customerIDColumn";
	public static final String ConstCustomerValueColumn = "customerValueColumn";
	public static final String ConstCustomerProductColumn = "customerProductColumn";
	public static final String ConstCustomerProductCountColumn = "customerProductCountColumn";
	public static final String ConstSelectionTable = "selectionTable";
	public static final String ConstSelectionIDColumn = "selectionIDColumn";
	public static final String ConstSimThreshold = "simThreshold";
	public static final String ConstMaxRecords = "maxRecords";
	public static final String ConstMinProductCount = "minProductCount";
	public static final String ConstScoreThreshold = "scoreThreshold";
	public static final String ConstCohorts = "cohorts";
	public static final String ConstCohortsAbove = "cohortsAbove";
	public static final String ConstCohortsBelow = "cohortsBelow";	
	public static final String ConstTargetCohort = "targetCohort";
	
	private static final List<String> parameterNames = new ArrayList<String>();


	static {

		parameterNames.add(ConstOutputType);
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstOutputTableStorageParameters);
		parameterNames.add(ConstCustomerTable);
		parameterNames.add(ConstCustomerIDColumn);
		parameterNames.add(ConstCustomerValueColumn);
		parameterNames.add(ConstCustomerProductColumn);
		parameterNames.add(ConstCustomerProductCountColumn);
		parameterNames.add(ConstSelectionTable);
		parameterNames.add(ConstSelectionIDColumn);
		parameterNames.add(ConstSimThreshold);
		parameterNames.add(ConstMaxRecords);
		parameterNames.add(ConstMinProductCount);
		parameterNames.add(ConstScoreThreshold);
		parameterNames.add(ConstCohorts);
		parameterNames.add(ConstCohortsAbove);
		parameterNames.add(ConstCohortsBelow);
	}

	public RecommendationConfig() {
		super();
		setParameterNames(parameterNames);
//		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.RecommendationVisualizationType");
	}

	public String getCustomerTable() {
		return customerTable;
	}

	public void setCustomerTable(String customerTable) {
		this.customerTable = customerTable;
	}

	public String getCustomerIDColumn() {
		return customerIDColumn;
	}

	public void setCustomerIDColumn(String customerIDColumn) {
		this.customerIDColumn = customerIDColumn;
	}

	public String getCustomerValueColumn() {
		return customerValueColumn;
	}

	public void setCustomerValueColumn(String customerValueColumn) {
		this.customerValueColumn = customerValueColumn;
	}

	public String getCustomerProductColumn() {
		return customerProductColumn;
	}

	public void setCustomerProductColumn(String customerProductColumn) {
		this.customerProductColumn = customerProductColumn;
	}

	public String getSelectionTable() {
		return selectionTable;
	}

	public void setSelectionTable(String selectionTable) {
		this.selectionTable = selectionTable;
	}

	public String getSelectionIDColumn() {
		return selectionIDColumn;
	}

	public void setSelectionIDColumn(String selectionIDColumn) {
		this.selectionIDColumn = selectionIDColumn;
	}

	public String getMaxRecords() {
		return maxRecords;
	}

	public void setMaxRecords(String maxRecords) {
		this.maxRecords = maxRecords;
	}

	public String getMinProductCount() {
		return minProductCount;
	}

	public void setMinProductCount(String minProductCount) {
		this.minProductCount = minProductCount;
	}

	public String getScoreThreshold() {
		return scoreThreshold;
	}

	public void setScoreThreshold(String scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
	}

	public String getCohorts() {
		return cohorts;
	}

	public void setCohorts(String cohorts) {
		this.cohorts = cohorts;
	}

	public String getCohortsAbove() {
		return cohortsAbove;
	}

	public void setCohortsAbove(String cohortsAbove) {
		this.cohortsAbove = cohortsAbove;
	}

	public String getCohortsBelow() {
		return cohortsBelow;
	}

	public void setCohortsBelow(String cohortsBelow) {
		this.cohortsBelow = cohortsBelow;
	}

	public String getSimThreshold() {
		return simThreshold;
	}

	public void setSimThreshold(String simThreshold) {
		this.simThreshold = simThreshold;
	}

	public String getCustomerProductCountColumn() {
		return customerProductCountColumn;
	}

	public void setCustomerProductCountColumn(String customerProductCountColumn) {
		this.customerProductCountColumn = customerProductCountColumn;
	}

	public String getTargetCohort() {
		return targetCohort;
	}

	public void setTargetCohort(String targetCohort) {
		this.targetCohort = targetCohort;
	}
}
