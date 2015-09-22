/**
 * ClassName LogisticRegressionConfigGeneral.java
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
import com.alpine.datamining.operator.regressions.AnalysisInterActionColumnsModel;


/**
 * @author John Zhao
 * 
 */
public class LogisticRegressionConfigGeneral extends AbstractModelTrainerConfig{
	
	 
	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.LogisticRegressionTextVisualizationType"+","+
						"com.alpine.datamining.api.impl.visual.LogisticRegressionTextAndTableVisualizationType";
	public static final String SPLIT_MODEL_VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.LogisticRegressionSummaryTableVisualizationType"+","+
			"com.alpine.datamining.api.impl.visual.LogisticRegressionTextAndTableVisualizationType";
	
	public static final String PARAMETER_EPSILON = "epsilon";
	public static final String PARAMETER_GOOD_VALUE = "goodValue";
	public static final String PARAMETER_MAX_GENERATIONS = "max_generations";
	
	private String isStepWise;
	private String stepWiseType;
	private String criterionType;
	private String checkValue;
	
	
	public static final String ConstIsStepWise = "isStepWise";
	public static final String ConstStepWiseType = "stepWiseType";
	public static final String ConstCriterionType = "criterionType";
	public static final String ConstCheckValue = "checkValue";
	public static final String ConstGroupColumnValue = "splitModelGroupByColumn";
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	
	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(PARAMETER_EPSILON);
		parameters.add(PARAMETER_GOOD_VALUE);
		parameters.add(PARAMETER_MAX_GENERATIONS);
		parameters.add(ConstIsStepWise);
		parameters.add(ConstStepWiseType);
		parameters.add(ConstCriterionType);
		parameters.add(ConstCheckValue);
		parameters.add(ConstGroupColumnValue);	
		}

	public String getIsStepWise() {
		return isStepWise;
	}


	public void setIsStepWise(String isStepWise) {
		this.isStepWise = isStepWise;
	}


	public String getStepWiseType() {
		return stepWiseType;
	}


	public void setStepWiseType(String stepWiseType) {
		this.stepWiseType = stepWiseType;
	}


	public String getCriterionType() {
		return criterionType;
	}


	public void setCriterionType(String criterionType) {
		this.criterionType = criterionType;
	}


	public String getCheckValue() {
		return checkValue;
	}


	public void setCheckValue(String checkValue) {
		this.checkValue = checkValue;
	}

	//here UI have not prepare....
	private String add_intercept=null;
	private String max_generations=null;
	private String goodValue = null;
	private String epsilon = null;
	private AnalysisInterActionColumnsModel interActionModel;


	public String getEpsilon() {
		return epsilon;
	}


	public void setEpsilon(String epsilon) {
		this.epsilon = epsilon;
	}
	public String getAdd_intercept() {
		return add_intercept;
	}


	public void setAdd_intercept(String add_intercept) {
		this.add_intercept = add_intercept;
	}

	public String getMax_generations() {
		return max_generations;
	}


	public void setMax_generations(String max_generations) {
		this.max_generations = max_generations;
	}

	public AnalysisInterActionColumnsModel getInterActionModel() {
		return interActionModel;
	}


	public void setInterActionModel(AnalysisInterActionColumnsModel interActionModel) {
		this.interActionModel = interActionModel;
	}


	public LogisticRegressionConfigGeneral(
		String columnames,	
		String dependentColumn){
		this();
		setColumnNames(columnames);
		setDependentColumn(dependentColumn);
	 	}
	
	public LogisticRegressionConfigGeneral(  ){
			setParameterNames(parameters);
			setVisualizationTypeClass(
					VISUALIZATION_TYPE);
		}

	/**
	 * @return the goodValue
	 */
	public String getGoodValue() {
		return goodValue;
	}


	/**
	 * @param goodValue the goodValue to set
	 */
	public void setGoodValue(String goodValue) {
		this.goodValue = goodValue;
	}
	
}
