/**
 * ClassName DecisionTreeConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;


public class CartConfig extends AbstractModelTrainerConfig {
	
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.CartTreeVisualizationType";
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	public static final String Constmaximal_depth = "maximal_depth";
	public static final String Constconfidence = "confidence";
//	public static final String Constminimal_gain = "minimal_gain";
	public static final String Constnumber_of_prepruning_alternatives = "number_of_prepruning_alternatives";
	public static final String Constminimal_size_for_split = "minimal_size_for_split";
	public static final String Constno_pruning = "no_pruning";
	public static final String Constno_pre_pruning = "no_pre_pruning";
	public static final String Constsize_threshold_load_data = "size_threshold_load_data";
	public static final String Constminimal_leaf_size = "minimal_leaf_size";

	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(Constmaximal_depth);
		parameters.add(Constconfidence);
//		parameters.add(Constminimal_gain);
		parameters.add(Constnumber_of_prepruning_alternatives);
		parameters.add(Constminimal_size_for_split);
		parameters.add(Constno_pruning);
		parameters.add(Constno_pre_pruning);
		parameters.add(Constsize_threshold_load_data);
		parameters.add(Constminimal_leaf_size);
		parameters.add(PARAMETER_COLUMN_NAMES);
		}
	
	public CartConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);

	}
 
	private String maximal_depth = null;
	private String confidence = null; // for tree not for regression
//	private String minimal_gain = null;
	private String number_of_prepruning_alternatives = null;
	private String minimal_size_for_split = null;
	private String no_pruning = null;// for tree not for regression
	private String no_pre_pruning = null;
	private String size_threshold_load_data = null;
	private String isChiSqaure="false";
	private boolean forWoe=false;
	public boolean isForWoe() {
		return forWoe;
	}

	public void setForWoe(boolean forWoe) {
		this.forWoe = forWoe;
	}

	public String getIsChiSqaure() {
		return isChiSqaure;
	}

	public void setIsChiSqaure(String isChiSqare) {
		this.isChiSqaure = isChiSqare;
	}

	public String getSize_threshold_load_data() {
		return size_threshold_load_data;
	}

	private String minimal_leaf_size = null;

	public String getMaximal_depth() {
		return maximal_depth;
	}

	public void setMaximal_depth(String maximalDepth) {
		maximal_depth = maximalDepth;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}



	public String getNumber_of_prepruning_alternatives() {
		return number_of_prepruning_alternatives;
	}

	public void setNumber_of_prepruning_alternatives(
			String numberOfPrepruningAlternatives) {
		number_of_prepruning_alternatives = numberOfPrepruningAlternatives;
	}

	public String getMinimal_size_for_split() {
		return minimal_size_for_split;
	}

	public void setMinimal_size_for_split(String minimalSizeForSplit) {
		minimal_size_for_split = minimalSizeForSplit;
	}

	public String getNo_pruning() {
		return no_pruning;
	}

	public void setNo_pruning(String noPruning) {
		no_pruning = noPruning;
	}

	public String getNo_pre_pruning() {
		return no_pre_pruning;
	}

	public void setNo_pre_pruning(String noPrePruning) {
		no_pre_pruning = noPrePruning;
	}
 

	/**
	 * @param size_threshold_load_data the size_threshold_load_data to set
	 */
	public void setSize_threshold_load_data(String sizeThresholdLoadData) {
		this.size_threshold_load_data = sizeThresholdLoadData;
	}

	public void setMinimal_leaf_size(String minimal_leaf_size) {
		this.minimal_leaf_size = minimal_leaf_size;
	}

	public String getMinimal_leaf_size() {
		return minimal_leaf_size;
	}


}
