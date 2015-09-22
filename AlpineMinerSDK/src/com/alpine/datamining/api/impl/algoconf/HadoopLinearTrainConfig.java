/**
 * 

* ClassName HadoopLirTrainConfig.java
*
* Version information: 1.00
*
* Date: 2012-8-20
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.operator.regressions.AnalysisInterActionColumnsModel;
/**
 * @author Shawn
 *
 *  
 */

public class HadoopLinearTrainConfig extends AbstractModelTrainerConfig{

	
	public static final String VISUALIZATION_TYPE ="com.alpine.datamining.api.impl.visual.HadoopLinearRegressionTextVisualizationType"+","+
	"com.alpine.datamining.api.impl.visual.HadoopLinearRegressionFileVisualizationType"+","+
	"com.alpine.datamining.api.impl.visual.HadoopLinearRegressionResidualPlotVisualization"+","+
	"com.alpine.datamining.api.impl.visual.HadoopLinearRegressionNormalProbabilityPlotVisualization";

	
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();

 
	
	
	private AnalysisInterActionColumnsModel interActionModel;
	static{ 
		parameters.add(ConstForceRetrain);
		parameters.add(ConstDependentColumn);
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstGroupColumnValue);
		}

	public HadoopLinearTrainConfig(){
		setParameterNames(parameters);
		setVisualizationTypeClass(VISUALIZATION_TYPE);
	}

	public HadoopLinearTrainConfig(//String tableName,
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
}
