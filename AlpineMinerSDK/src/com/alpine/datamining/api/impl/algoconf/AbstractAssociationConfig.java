/**
 * ClassName LogisticRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;



/**
 * @author John Zhao
 * 
 */
public abstract class AbstractAssociationConfig extends
AbstractModelTrainerConfig {

	private String minConfidence;
	private String ruleCriterion;

	/**
	 * @param tableName
	 */
	public AbstractAssociationConfig(
			String ruleMinConfidence, String ruleCriterion) {

		this.minConfidence = ruleMinConfidence;

		this.ruleCriterion = ruleCriterion;
//		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.AssociationTableVisualizationType"+","+
//		"com.alpine.datamining.api.impl.visual.AssociationDBTableVisualizationType");
		setVisualizationTypeClass(
		"com.alpine.datamining.api.impl.visual.AssociationDBTableVisualizationType");

	}

	/**
	 * 
	 */
	public AbstractAssociationConfig() {
		// empty constructor, for reflection use
//		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.AssociationTableVisualizationType"+","+
//		"com.alpine.datamining.api.impl.visual.AssociationDBTableVisualizationType");
		setVisualizationTypeClass(
		"com.alpine.datamining.api.impl.visual.AssociationDBTableVisualizationType");

	}

	public String getMinConfidence() {
		return minConfidence;
	}

	public void setMinConfidence(String ruleMinConfidence) {
		this.minConfidence = ruleMinConfidence;
	}

	public String getRuleCriterion() {
		return ruleCriterion;
	}

	public void setRuleCriterion(String ruleCriterion) {
		this.ruleCriterion = ruleCriterion;
	}

}
