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

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;
import com.alpine.datamining.api.impl.EngineModel;

/**
 * @author John Zhao
 * 
 */
public class EvaluatorConfig extends AbstractAnalyticConfig  {

	public static final String ConstDependentColumn = "dependentColumn";
	public static final String ConstColumnValue = "columnValue";
	public static final String ConstUseModel = "useModel";
 
	private String dependentColumn = null;
	private String columnValue=null;
	private String useModel = null;
	// model as input
	List<EngineModel> trainedModels = null;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(ConstDependentColumn);
		parameters.add(ConstColumnValue);
		parameters.add(ConstUseModel);
		
	}
 

	public String getColumnValue() {
		return columnValue;
	}

	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}


	
	/**
	 * @param tableName
	 * also need a table name
	 */
	public EvaluatorConfig( List<EngineModel> trainedModel) { 
		this.trainedModels=trainedModel;
		setParameterNames(parameters);
	}
	
	public EvaluatorConfig(){
		setParameterNames(parameters);
	}


	public List<EngineModel> getTrainedModel() {
		return trainedModels;
	}

	public void setTrainedModel(List<EngineModel> trainedModel) {
		this.trainedModels = trainedModel;
	}

	/**
	 * @return the useModel
	 */
	public String getUseModel() {
		return useModel;
	}

	/**
	 * @param useModel the useModel to set
	 */
	public void setUseModel(String useModel) {
		this.useModel = useModel;
	}



	/**
	 * @return the dependentColumn
	 */
	public String getDependentColumn() {
		return dependentColumn;
	}

	/**
	 * @param dependentColumn the dependentColumn to set
	 */
	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	/**
	 * @param engineModel
	 */
	public void addTrainedModel(EngineModel engineModel) {
		if(trainedModels==null){
			trainedModels=new ArrayList<EngineModel>();
		}
		if(trainedModels.contains(engineModel)==false){
			trainedModels.add(engineModel);
		}
 
		
	}
	
	public void clearTrainedModels() {
		if (trainedModels != null) {
			trainedModels.clear();
		}
	}
	
}
