/**
 * ClassName :AnalysisModelFileManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.web.resource;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.impl.AnalyticNodeImpl;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AdaBoostAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.miner.impls.result.OutPutVisualAdapter;
import com.alpine.miner.impls.result.OutPutVisualAdapterFactory;
import com.alpine.miner.inter.resources.Resources;
import com.alpine.miner.interfaces.AnalysisModelManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.utils.JSONUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.model.impl.UIOperatorModelImpl;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.reader.FlowMigrator;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;

/**
 * @author zhaoyong
 *
 */
public class AnalysisModelFileManager implements AnalysisModelManager {
	private static final String CLASS_NAME_MODELOPERATOR = "ModelOperator"; 

	private static AnalysisModelFileManager instance = null; 
	
	Persistence persistence =   FilePersistence.INSTANCE;

	private AnalysisModelFileManager(){
		 
	} 
	
	 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.AnalysisModelManager#getModelInfoList(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ModelInfo> getModelInfoList(String parentPath, String flowName,
			String modelName,Locale locale) throws Exception {	 
		return persistence.getModelInfoList(parentPath, flowName, modelName,  locale);
	 
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.AnalysisModelManager#deleteModel(java.lang.String, com.alpine.miner.impls.resource.ModelInfo)
	 */
	@Override
	public boolean deleteModel(  ModelInfo modelInfo) throws Exception {
		return persistence.deleteModel( modelInfo.getCreateUser(),  modelInfo) ;
	}

	
 	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.AnalysisModelManager#saveEngineModel(java.lang.String, java.lang.String, java.lang.String, com.alpine.datamining.api.impl.EngineModel)
	 */
	@Override
	public boolean saveEngineModel(ModelInfo modelInfo, EngineModel engineModel) throws Exception {
		return persistence.createEngineModel( modelInfo.getCreateUser(),modelInfo,   engineModel);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.AnalysisModelManager#replaceModel(java.lang.String, java.lang.String, java.lang.String, com.alpine.miner.impls.resource.ModelInfo)
	 */
	@Override
	public FlowInfo replaceModel(FlowInfo flowInfo, String modelName, ModelInfo newModel, Locale locale) throws Exception {
		EngineModel newEngineModel = getEngineModel( newModel) ;
		ResourceManager rmgr = ResourceManager.getInstance();
		OperatorWorkFlow workFlow = rmgr.getFlowData(flowInfo, locale);
		List<UIOperatorModel> uiOperators = workFlow.getChildList();
		for (Iterator iterator2 = uiOperators.iterator(); iterator2
				.hasNext();) {
			UIOperatorModel uiOperatorModel = (UIOperatorModel) iterator2
					.next();
			//in the UI,id is name
			if(uiOperatorModel.getId().equals(modelName)){
				ModelOperator modelOperator=(ModelOperator)uiOperatorModel.getOperator();
				modelOperator.setModel(newEngineModel);
			 //only do this the engine model will be saved
				//this isvery useful 
				 Resources.retrainHash.put(modelOperator.getOperModel().getUUID(), newEngineModel) ;
//				XMLWorkFlowSaver workflowSaver=new XMLWorkFlowSaver() ;
//				String fileName = persistence.generateResourceKey(flowInfo) + ".afm";
//				workflowSaver.doSave(fileName, workFlow, flowInfo.getModifiedUser(), false) ;
				
				//reset the  flow info to clear the temp path
//				persistence.storeFlowInfo(persistence.generateResourceKey(flowInfo) +
//						FilePersistence.INF, flowInfo) ;
			 		
				return flowInfo;
			}
		}
		return null;
	}

 
	private EngineModel getEngineModel(  ModelInfo modelInfo) throws Exception {
		return persistence.getEngineModel(  modelInfo.getCreateUser(),   modelInfo) ;
	}



	public static AnalysisModelManager getInstance() {
		
		if(instance==null){
			instance = new AnalysisModelFileManager();
		}
		return instance;
	}



	@Override
	public boolean saveModelInFlowFile(String fileName, ModelInfo modelInfo, Locale locale) throws Exception {
		OperatorWorkFlow workFlow = new OperatorWorkFlow();
		workFlow.setUserName(modelInfo.getCreateUser()) ;
		workFlow.setVersion(FlowMigrator.Version_3) ;
		workFlow.setDescription(modelInfo.getComments()) ;
		UIOperatorModel uiom = new UIOperatorModelImpl();
		uiom.setClassName(CLASS_NAME_MODELOPERATOR) ;
		uiom.setId(modelInfo.getModelName()); 
		OperatorPosition operatorPosition= new OperatorPosition(100,100); 
		uiom.setPosition(operatorPosition) ;
		uiom.setUUID(System.currentTimeMillis()+"") ; 
		
		uiom.initiateOperator(locale);
		uiom.getOperator().setOperModel(uiom) ;
		EngineModel model = getEngineModel(modelInfo);  
		((ModelOperator)uiom.getOperator()).setModel(model) ;
		workFlow.addChild(uiom) ;
		
		XMLWorkFlowSaver workflowSaver=new XMLWorkFlowSaver() ;
		 
		workflowSaver.doSave(fileName, workFlow,modelInfo.getCreateUser(),false) ; 
		
		return true;
	}



	@Override
	public String getModelVisualization(ModelInfo modelInfo, Locale locale) throws Exception { 
		
		EngineModel model = getEngineModel(modelInfo);
		
		if(model==null){
			return null;
		}
		AnalyzerOutPutTrainModel output=null;
		if(model.getModelType()!=null&&model.getModelType().equals(EngineModel.MPDE_TYPE_ADABOOST)){
			output=new AdaBoostAnalyzerOutPutTrainModel(model.getModel());
		}else{
			output=new AnalyzerOutPutTrainModel(model);
		}

		
 
		AnalyticNode node = new AnalyticNodeImpl();
		node.setName(modelInfo.getModelName());
		
		output.setAnalyticNode(node);
	
		OutPutVisualAdapter adapter = OutPutVisualAdapterFactory.getInstance().getAdapter(output); 
		if(adapter ==null){
				return null;
		}else{
			VisualizationModel vModel = adapter.toVisualModel(output, locale);
			return JSONUtility.toJSONString(vModel,locale);
			
		}
	}

 
}
