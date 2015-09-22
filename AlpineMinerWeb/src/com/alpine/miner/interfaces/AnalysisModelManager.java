/**
 * ClassName :AnalysisModelManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.interfaces;

import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.AnalysisModelFileManager;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ModelInfo;

/**
 * @author zhaoyong
 *
 */
public interface AnalysisModelManager {
	public  static final AnalysisModelManager INSTANCE =   AnalysisModelFileManager.getInstance();
	
	 	
	public boolean deleteModel( ModelInfo modelInfo) throws Exception ;
	
	//automatically saved when flow running
	public boolean saveEngineModel( ModelInfo modelInfo, EngineModel engineModel) throws Exception;
	

	

	public 	String getModelVisualization(ModelInfo modelInfo, Locale locale) throws Exception;


	boolean saveModelInFlowFile(String fileName, ModelInfo modelInfo,
			Locale locale) throws Exception;

	//this will be rest service, in UI popup menu, use can replce the model with an trained model
	 
	public FlowInfo replaceModel(FlowInfo flowInfo, String modelName,
			ModelInfo newModel, Locale locale)
			throws Exception;


	List<ModelInfo> getModelInfoList(String parentPath, String flowName,String modelName, Locale locale) throws Exception;


 
}
