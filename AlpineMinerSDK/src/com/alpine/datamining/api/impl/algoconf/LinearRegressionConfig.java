/**
 * ClassName LinearRegressionConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.operator.regressions.AnalysisInterActionColumnsModel;


/**
 * Eason
 */
public class LinearRegressionConfig extends AbstractModelTrainerConfig{

	public static final String VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.LinearRegressionTextVisualizationType"+","+
					"com.alpine.datamining.api.impl.visual.LinearRegressionTableVisualizationType";
	public static final String SPLITMODEL_VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.LinearRegressionSummaryTableVisualizationType"+","+
			"com.alpine.datamining.api.impl.visual.LinearRegressionTableVisualizationType";
	public static final String ResidualPlot_VISUALIZATION_TYPE = "com.alpine.datamining.api.impl.visual.LinearRegressionResidualPlotVisualization"+","+
			"com.alpine.datamining.api.impl.visual.LinearRegressionNormalProbabilityPlotVisualization";

	private final static ArrayList<String> parameters=new  ArrayList<String>();

	private String isStepWise;
	private String stepWiseType;
	private String criterionType;
	private String checkValue;
	private String addResidualPlot;

	public String getCheckValue() {
		return checkValue;
	}

	public void setCheckValue(String checkValue) {
		this.checkValue = checkValue;
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

	public static final String ConstIsStepWise = "isStepWise";
	public static final String ConstStepWiseType = "stepWiseType";
	public static final String ConstCriterionType = "criterionType";
	public static final String ConstCheckValue = "checkValue";
	public static final String ConstGroupColumnValue = "splitModelGroupByColumn";
	public static final String ConstAddResidualPlot = "addResidualPlot";
	
	private AnalysisInterActionColumnsModel interActionModel;
	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstIsStepWise);
		parameters.add(ConstStepWiseType);
		parameters.add(ConstCriterionType);
		parameters.add(ConstCheckValue);
		parameters.add(ConstGroupColumnValue);
		parameters.add(ConstAddResidualPlot);
		}

	public LinearRegressionConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public LinearRegressionConfig(//String tableName,
			String columnNames,String dependentColumn){
		this(); 
		setColumnNames(columnNames);
		setDependentColumn ( dependentColumn);
	
	}

	public AnalysisInterActionColumnsModel getInterActionModel() {
		return interActionModel;
	}

	public void setInterActionModel(AnalysisInterActionColumnsModel interActionModel) {
		this.interActionModel = interActionModel;
	}

	public String getAddResidualPlot() {
		return addResidualPlot;
	}

	public void setAddResidualPlot(String addResidualPlot) {
		this.addResidualPlot = addResidualPlot;
	}
}
