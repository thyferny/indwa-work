/**
*
* ClassName HadoopDecisionTrainConfig.java
*
* Version information: 1.00
*
* Sep 18, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;

/**
 * @author john
 *  
 */

public class HadoopDecisionTrainConfig extends AbstractModelTrainerConfig{
	                                               
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.HadoopDecesionTreeVisualizationType";
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	public static final String Constmaximal_depth = "maximal_depth";  //20//integer  >0
	public static final String Constconfidence = "confidence"; // 0.25  //double  0.0000001, 1
	public static final String Constminimal_gain = "minimal_gain";  //0.1d   double >=0
	public static final String Constnumber_of_prepruning_alternatives = "number_of_prepruning_alternatives";  //3 integer >0
	public static final String Constminimal_size_for_split = "minimal_size_for_split";  // 4  integer >=1
	public static final String Constno_pruning = "no_pruning";  //false
	public static final String Constno_pre_pruning = "no_pre_pruning"; //false
 	public static final String Constminimal_leaf_size = "minimal_leaf_size";  //2 integer >=1
 	public static final String ConstcategoryLimit ="categoryLimit";
 	public static final String ConstnumericalGranularity="numericalGranularity";

	static{ 
		//parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(Constmaximal_depth);
 		parameters.add(Constconfidence);
		parameters.add(Constminimal_gain);
		parameters.add(Constnumber_of_prepruning_alternatives);
		parameters.add(Constminimal_size_for_split);
		parameters.add(Constno_pruning);
		parameters.add(Constno_pre_pruning);
 		parameters.add(Constminimal_leaf_size);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstcategoryLimit);
		parameters.add(ConstnumericalGranularity);
		}
	
	public HadoopDecisionTrainConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);

	}
	
	public HadoopDecisionTrainConfig(//String tableName,
			String columnnames,
			String dependentColumn) throws AnalysisException
	{
		setColumnNames(columnnames);
		setParameterNames(parameters);
		setDependentColumn(dependentColumn);

		
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}	 

 

	private String maximal_depth = null;
	private String confidence = null;
	private String minimal_gain = null;
	private String number_of_prepruning_alternatives = null;
	private String minimal_size_for_split = null;
	private String no_pruning = null;
	private String no_pre_pruning = null;
	private String minimal_leaf_size = null;
	private String categoryLimit = null;
	private String numericalGranularity=null;
	
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

	public String getMinimal_gain() {
		return minimal_gain;
	}

	public void setMinimal_gain(String minimalGain) {
		minimal_gain = minimalGain;
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
	

	public void setMinimal_leaf_size(String minimal_leaf_size) {
		this.minimal_leaf_size = minimal_leaf_size;
	}

	public String getMinimal_leaf_size() {
		return minimal_leaf_size;
	}

	public String getCategoryLimit() {
		return categoryLimit;
	}

	public void setCategoryLimit(String categoryLimit) {
		this.categoryLimit = categoryLimit;
	}

	public String getNumericalGranularity() {
		return numericalGranularity;
	}

	public void setNumericalGranularity(String numericalGranularity) {
		this.numericalGranularity = numericalGranularity;
	}
}

