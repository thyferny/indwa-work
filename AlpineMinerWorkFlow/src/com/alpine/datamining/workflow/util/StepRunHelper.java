/**
 * ClassName StepRunHelper.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticFlow;
import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticProcess;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AnalyticFlowImpl;
import com.alpine.datamining.api.impl.AnalyticProcessImpl;
import com.alpine.datamining.api.impl.algoconf.AdaboostConfig;
import com.alpine.datamining.api.impl.algoconf.AggregateConfig;
import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.datamining.api.impl.algoconf.FPGrowthConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopAggregaterConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopDataOperationConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopJoinConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPigExecuteConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopUnionConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopVariableConfig;
import com.alpine.datamining.api.impl.algoconf.HistogramAnalysisConfig;
import com.alpine.datamining.api.impl.algoconf.LinearRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.ReplaceNullConfig;
import com.alpine.datamining.api.impl.algoconf.SubFlowConfig;
import com.alpine.datamining.api.impl.algoconf.TableJoinConfig;
import com.alpine.datamining.api.impl.algoconf.TableSetConfig;
import com.alpine.datamining.api.impl.algoconf.VariableConfig;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisFileStructureModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisAggregateFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.aggregate.AnalysisWindowFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.association.AnalysisExpressionModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopjoin.AnalysisHadoopJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.hadoopunion.AnalysisHadoopUnionModel;
import com.alpine.datamining.api.impl.db.attribute.model.histogram.AnalysisColumnBinsModel;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayersModel;
import com.alpine.datamining.api.impl.db.attribute.model.nullreplace.AnalysisNullReplacementModel;
import com.alpine.datamining.api.impl.db.attribute.model.pigexe.AnalysisPigExecutableModel;
import com.alpine.datamining.api.impl.db.attribute.model.tablejoin.AnalysisTableJoinModel;
import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisTableSetModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisDerivedFieldsModel;
import com.alpine.datamining.api.impl.db.attribute.model.variable.AnalysisQuantileFieldsModel;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceModel;
import com.alpine.datamining.operator.regressions.AnalysisInterActionColumnsModel;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.subflow.TableMappingModel;
import com.alpine.utility.file.StringUtil;

public class StepRunHelper {
	private static final Logger itsLogger = Logger.getLogger(StepRunHelper.class);
	private Map<String,AnalyticNode> stepRunNodeMap;
	
	private AnalyticProcess stepRunProcess;
	
	private boolean firstRun=true;
	
	private boolean isInStepRun;
	
	private AnalyticNode runNode;

	private AnalyticProcess subProcessprocess;


	public AnalyticProcess getSubProcessprocess() {
		return subProcessprocess;
	}



	public void setSubProcessprocess(AnalyticProcess subProcessprocess) {
		this.subProcessprocess = subProcessprocess;
	}

	private String flowKey;
	
	public StepRunHelper(String flowKey){
		this.flowKey=flowKey;
	}
	
 
	
	public boolean prepareStepRun(String filePath,String nodeName,   Locale locale, Map<String, AnalyticNode> oldMap){

		if(oldMap == null){
			oldMap = stepRunNodeMap ;
		}
		
		isInStepRun=true;
		//first time read the flow without subflow
		stepRunProcess = MiningUtil.parseXMLFile(filePath,locale,true);
		//for step run catch of context
		if(flowKey!=null){
			stepRunProcess.setFlowFilePath(flowKey) ;
		}
		buildStepRunSubProcess(nodeName, oldMap);
		subProcessprocess = getSubProcess(stepRunProcess,	runNode,  filePath);
		if(flowKey!=null){
			subProcessprocess.setFlowFilePath(flowKey) ;
		}
		return isInStepRun;
	}
	
	
	public boolean prepareStepRun4SubflowOperator(String filePath,String nodeName,  
			 Locale locale,String subfowFilePath,TableMappingModel tableMappingModel){
		Map<String, AnalyticNode>  oldMap=stepRunNodeMap;
		//first parse xml as a full flow
		
		isInStepRun=true;
	 
		//first time read the flow without subflow
		stepRunProcess = MiningUtil.parseXMLFile(filePath,locale,true);
		//for step run catch of context
		if(flowKey!=null){
			stepRunProcess.setFlowFilePath(flowKey) ;
		}
		
		buildStepRunSubProcess(nodeName, oldMap);
		
		List<AnalyticNode> allChildNodesOfSubflow = getAllChildNodesOfSubflow(stepRunProcess,nodeName);
		subProcessprocess = getSubProcess4SubflowNode(stepRunProcess,	allChildNodesOfSubflow,filePath); 
		//then parse the subflow and attachit to the simple flow
		//linkSubFlowProcesstoStepRunSubProcess(subfowFilePath,  tableMappingModel,locale,oldMap,runNode,variableModel);
	// 
				
		return isInStepRun;
	}
	
	private AnalyticProcess getSubProcess4SubflowNode(
			AnalyticProcess stepRunProcess, List<AnalyticNode> allLeafOfSubflow, String fileFlowPath) {
	AnalyticProcess process = createNewSubProcess(stepRunProcess, fileFlowPath); 
		
		AnalyticFlow flow = stepRunProcess.getFlow();
		AnalyticFlow newFlow=getSubFlow4SubflowNode(flow,allLeafOfSubflow);
		process.setFlow(newFlow);

		return process;
		
	 
	}


	private AnalyticFlow getSubFlow4SubflowNode(AnalyticFlow flow,
			List<AnalyticNode> allLeafOfSubflow) {
	 
			AnalyticFlow newFlow= new AnalyticFlowImpl();
			newFlow.setFlowOwnerUser(flow.getFlowOwnerUser()) ; 
			newFlow.setFlowDescription(flow.getFlowDescription()) ;
			newFlow.setVersion(flow.getVersion()) ;
			
			List<AnalyticNode> nodes=new ArrayList<AnalyticNode> ();
			for(int i = 0 ; i <allLeafOfSubflow.size();i++){
				addParentNode(allLeafOfSubflow.get(i),nodes);
			}
			
			newFlow.setAllNodes(nodes);
	 
			return newFlow;
		}
 


	private List<AnalyticNode> getAllChildNodesOfSubflow(
			AnalyticProcess stepRunProcess, String nodeName) {
		List<AnalyticNode> result = new ArrayList<AnalyticNode>();
		if(stepRunProcess!=null&&stepRunProcess.getFlow()!=null){
			List<AnalyticNode> nodes = stepRunProcess.getFlow().getAllNodes();
			for(int i =0 ; i <nodes.size();i++){
				if(nodeName.equals(nodes.get(i).getGroupID())){
					result.add(nodes.get(i)) ;
				}
			}
		}
		 
		return result;
	}


	    

	private void buildStepRunSubProcess(String nodeName,Map<String, AnalyticNode> oldMap) {
		
		if (firstRun) {// first
			firstRun = false;
			stepRunNodeMap = new HashMap<String, AnalyticNode>();
			List<AnalyticNode> nodeList = stepRunProcess.getFlow().getAllNodes();

			for (AnalyticNode node : nodeList) {
				stepRunNodeMap.put(node.getName(), node);
			}
		} else {
			List<AnalyticNode> parentList = new ArrayList<AnalyticNode>();
			stepRunNodeMap = new HashMap<String, AnalyticNode>();
			List<AnalyticNode> nodeList = stepRunProcess.getFlow()	.getAllNodes();

			for (AnalyticNode node : nodeList) {
				stepRunNodeMap.put(node.getName(), node);
			}

			AnalyticNode stepRunNode = stepRunNodeMap.get(nodeName);
			if (stepRunNode != null
					&& stepRunNode.getSource().getAnalyticConfig() instanceof SubFlowConfig == false) {
				addAllParentModelListWithItSelf(stepRunNode, parentList);
			} else {// special for subflow
				addAllParentModelListWithItSelf4SubflowNode(nodeName,parentList, nodeList);
			}

			resetNodeFinished(oldMap, parentList);
		}

		runNode = stepRunNodeMap.get(nodeName);

	}


	public AnalyticNode getRunNode() {
		return runNode;
	}



	public void setRunNode(AnalyticNode runNode) {
		this.runNode = runNode;
	}



	private void addAllParentModelListWithItSelf4SubflowNode(String stepRunNodeName,
			List<AnalyticNode> parentList, List<AnalyticNode> allNodeList) {
		
		if(allNodeList!=null){
			for(int i =0;i<allNodeList.size();i++){ 
				if(stepRunNodeName.equals(allNodeList.get(i).getGroupID())){
					addAllParentModelListWithItSelf(allNodeList.get(i), parentList);//xxxx
				}	 
			}
	 
		}
		
	}


	private void resetNodeFinished(Map<String, AnalyticNode> oldMap,	List<AnalyticNode> parentList) {
		
		Iterator<AnalyticNode> iter=parentList.iterator();
		if(oldMap == null ){
			return ;
		}
		while(iter.hasNext()){
			AnalyticNode node=iter.next();
			if(stepRunNodeMap.containsKey(node.getName())){
				AnalyticNode oldNode=oldMap.get(node.getName());
				AnalyticNode newNode=stepRunNodeMap.get(node.getName());
				if(oldNode==null||!configEquals(newNode.getSource(),oldNode.getSource())
						||isParentsChanged(oldNode,newNode)){
					clearStepRunResult(node);
					itsLogger.debug("startStepRunFlow" +
							"clearStepRunResult=" +node.getName()) ;
				}else if(oldNode.isFinished()){
					newNode.setOutput(oldNode.getOutput());
					newNode.setFinished(true);
					itsLogger.debug("startStepRunFlow" +
							"setFinished= true" +newNode.getName()) ;
 
				}
			}
		}
	}
	

	private boolean isParentsChanged(AnalyticNode oldNode, AnalyticNode newNode) {
		List<AnalyticNode> oldParents = oldNode.getParentNodes();
		List<String> oldParentsName=new ArrayList<String>();
		if(oldParents!=null){
			for(AnalyticNode node:oldParents){
				oldParentsName.add(node.getName());
			}
		}
		List<AnalyticNode> newParents = newNode.getParentNodes();
		List<String> newParentsName=new ArrayList<String>();
		if(newParents!=null){
			for(AnalyticNode node:newParents){
				newParentsName.add(node.getName());
			}
		}
		if(oldParentsName.containsAll(newParentsName)
				&&newParentsName.containsAll(oldParentsName)){
			return false;
		}
		return true;
	}

	public void clearStepRunResult(String nodeName){
		if(StringUtil.isEmpty(nodeName)){
			/**
			 * clear all
			 */
			stepRunProcess=null;		
			stepRunNodeMap=new HashMap<String,AnalyticNode>();

			isInStepRun=false;
		}else	if(stepRunNodeMap!=null&&stepRunNodeMap.get(nodeName)!=null){
			clearStepRunResult(stepRunNodeMap.get(nodeName));
		}else{ //must be a subflow operator
			clearStepRunResult4Subflow( nodeName);
		}
	}
	
	public void clearStepRunResult(AnalyticNode node) {
		
		if(node==null){
			itsLogger.debug("Running") ;
			// clear all
			stepRunProcess=null;		
			stepRunNodeMap=new HashMap<String,AnalyticNode> ();

			isInStepRun=false;
		}else{
			itsLogger.debug("Running") ;
			 
			if(node!=null){
				if(stepRunNodeMap.get(node.getName())==null){//for a subflow
					clearStepRunResult4Subflow( node.getName());
				}else{
					node.setFinished(false);//will cause rerun this node !
					itsLogger.debug("setFinished : false "+node.getName()) ;
					setAllChildrenFinished(node,false);
				}
			} 
		}
	}

	private void clearStepRunResult4Subflow(String groupID) {
		if(stepRunNodeMap!=null&&stepRunNodeMap.values()!=null){
			Iterator<AnalyticNode> it = stepRunNodeMap.values().iterator();
			while(it.hasNext()){
				AnalyticNode node = it.next(); 
				if(groupID.equals( node.getGroupID())){
					node.setFinished(false);//will cause rerun this node !
					itsLogger.debug("setFinished : false "+node.getName()) ;
					setAllChildrenFinished(node,false);
				}
			}
		}
		
	}


	private void setAllChildrenFinished(AnalyticNode node, boolean b) {
	 	
		if(node!=null){
			node.setFinished(b);
			itsLogger.debug("setAllChildrenFinished :"+b+" "+node.getName()) ;
		}else{
			return;
		}
		itsLogger.debug("setAllChildrenFinished :"+b+" "+node.getName()) ;
		if(node.getChildNodes()!=null){
			for (Iterator<AnalyticNode> iterator = node.getChildNodes().iterator(); iterator.hasNext();) {
				AnalyticNode child = iterator.next();
				setAllChildrenFinished(child,b);
			}
		}
	}
	
	private void addAllParentModelListWithItSelf(
			AnalyticNode analyticNode,List<AnalyticNode> list) {
		if(analyticNode!=null){
			if(list.contains(analyticNode)==false){
				list.add(analyticNode);
			}
			
			if(analyticNode.getParentNodes()!=null){
				for (Iterator<AnalyticNode> iterator = analyticNode.getParentNodes().iterator(); iterator.hasNext();) {
					AnalyticNode node =  iterator.next();
					addAllParentModelListWithItSelf(node,list);
				}
			}
		}
	}
	
			
	public static AnalyticProcess getSubProcess(AnalyticProcess stepRunProcess,
			AnalyticNode node, String flowFilePath) {
		AnalyticProcess process = createNewSubProcess(stepRunProcess, flowFilePath); 
		
		AnalyticFlow flow = stepRunProcess.getFlow();
		AnalyticFlow newFlow=getSubFlow(flow,node);
		process.setFlow(newFlow);

		return process;
	}


	private static AnalyticProcess createNewSubProcess(
			AnalyticProcess stepRunProcess, String flowFilePath) {
		AnalyticProcess process= new AnalyticProcessImpl(flowFilePath);
		process.setClientType(stepRunProcess.getClientType());
		process.setSaveResult(stepRunProcess.isSaveResult());
		process.setProcessID( UUID.randomUUID().toString());
		process.setName(stepRunProcess.getName());
		process.setExecuteUserName(stepRunProcess.getExecuteUserName());
		
		process.setExecuteMode(AnalyticProcess.EXE_MODE_INTERACT);
		return process;
	}
	
	private static AnalyticFlow getSubFlow(AnalyticFlow flow, AnalyticNode node) {
		AnalyticFlow newFlow= new AnalyticFlowImpl();
		newFlow.setFlowOwnerUser(flow.getFlowOwnerUser()) ; 
		newFlow.setFlowDescription(flow.getFlowDescription()) ;
		newFlow.setVersion(flow.getVersion()) ;
		
		List<AnalyticNode> nodes=new ArrayList<AnalyticNode> ();
		addParentNode(node,nodes);
		newFlow.setAllNodes(nodes);
 
		return newFlow;
	}
	
	/**
	 * @param node
	 * @param nodes
	 */
	private static void addParentNode(AnalyticNode node, List<AnalyticNode> nodes) {
		if(nodes.contains(node)==false){
			nodes.add(node);
		}
		if(node.getParentNodes()!=null){
			for (Iterator<AnalyticNode> iterator = node.getParentNodes().iterator(); iterator.hasNext();) {
				AnalyticNode analyticNode =  iterator.next();
				addParentNode(analyticNode,nodes);
			}
		}
	}
	 

	private boolean configEquals(AnalyticSource newSource, AnalyticSource oldSource) {
		AnalyticConfiguration newConfig = newSource.getAnalyticConfig();
		AnalyticConfiguration oldConfig = oldSource.getAnalyticConfig();
		
		HashMap<String, String> newValuesMap = newConfig.getValueAsMap();
		
		HashMap<String, String> oldValuesMap = oldConfig.getValueAsMap();
		
		ignoreEmptyHadoopResultName(newConfig, oldConfig, newValuesMap,
				oldValuesMap);
		
		itsLogger.debug("configEquals:newValuesMap=" + newValuesMap.values());
		itsLogger.debug("configEquals:oldValuesMap=" + oldValuesMap.values());
		
		boolean result=newValuesMap.equals(oldValuesMap) ;
		if(result==true){
			//quantile
			if(newConfig instanceof VariableConfig){
				AnalysisQuantileFieldsModel newModel = ((VariableConfig)newConfig).getQuantileModel();
				AnalysisQuantileFieldsModel oldModel = ((VariableConfig)oldConfig).getQuantileModel();
				
				boolean quantileResult = ParameterUtility.nullableEquales(newModel,oldModel);
				
				AnalysisDerivedFieldsModel newDerivedModel = ((VariableConfig)newConfig).getDerivedModel();
				AnalysisDerivedFieldsModel oldDerivedModel = ((VariableConfig)oldConfig).getDerivedModel();
				
				boolean derivedResult = ParameterUtility.nullableEquales(newDerivedModel,oldDerivedModel);
				
				result=quantileResult&&derivedResult;
				
			}else if(newConfig instanceof TableJoinConfig){	//table join
				AnalysisTableJoinModel newModel = ((TableJoinConfig)newConfig).getTableJoinDef();
				AnalysisTableJoinModel oldModel=((TableJoinConfig)oldConfig).getTableJoinDef();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
				
			}else if(newConfig instanceof LinearRegressionConfig){// interaction model...
				 AnalysisInterActionColumnsModel newModel = ((LinearRegressionConfig)newConfig).getInterActionModel();
				 AnalysisInterActionColumnsModel oldModel = ((LinearRegressionConfig)oldConfig).getInterActionModel();
				 result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof 	LogisticRegressionConfigGeneral){// interaction model...
				AnalysisInterActionColumnsModel newModel = ((LogisticRegressionConfigGeneral)newConfig).getInterActionModel();
				AnalysisInterActionColumnsModel oldModel = ((LogisticRegressionConfigGeneral)oldConfig).getInterActionModel();
				 result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof AggregateConfig){
				AnalysisAggregateFieldsModel newModel = ((AggregateConfig)newConfig).getAggregateFieldsModel();
				AnalysisAggregateFieldsModel oldModel = ((AggregateConfig)oldConfig).getAggregateFieldsModel();
				
				boolean aggregateResult = ParameterUtility.nullableEquales(newModel,oldModel);
				
				AnalysisWindowFieldsModel newWinModel = ((AggregateConfig)newConfig).getWindowFieldsModel();
				AnalysisWindowFieldsModel oldWinModel = ((AggregateConfig)oldConfig).getWindowFieldsModel();
				
				boolean winResult = ParameterUtility.nullableEquales(newWinModel,oldWinModel);
				
				result=aggregateResult&&winResult;
			}else if(newConfig instanceof NeuralNetworkConfig){
				AnalysisHiddenLayersModel newModel = ((NeuralNetworkConfig)newConfig).getHiddenLayersModel();
				AnalysisHiddenLayersModel oldModel = ((NeuralNetworkConfig)oldConfig).getHiddenLayersModel();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof FPGrowthConfig){
				AnalysisExpressionModel newModel = ((FPGrowthConfig)newConfig).getExpressionModel();
				AnalysisExpressionModel oldModel = ((FPGrowthConfig)oldConfig).getExpressionModel();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof HistogramAnalysisConfig){
				AnalysisColumnBinsModel newModel = ((HistogramAnalysisConfig)newConfig).getColumnBinModel();
				AnalysisColumnBinsModel oldModel = ((HistogramAnalysisConfig)oldConfig).getColumnBinModel();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof ReplaceNullConfig){
				AnalysisNullReplacementModel newModel = ((ReplaceNullConfig)newConfig).getNullReplacementModel();
				AnalysisNullReplacementModel oldModel = ((ReplaceNullConfig)oldConfig).getNullReplacementModel();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof HadoopReplaceNullConfig){
				AnalysisNullReplacementModel newModel = ((HadoopReplaceNullConfig)newConfig).getNullReplacementModel();
				AnalysisNullReplacementModel oldModel = ((HadoopReplaceNullConfig)oldConfig).getNullReplacementModel();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}
			else if(newConfig instanceof AdaboostConfig){
				AnalysisAdaboostPersistenceModel newModel = ((AdaboostConfig)newConfig).getAdaboostUIModel();
				AnalysisAdaboostPersistenceModel oldModel = ((AdaboostConfig)oldConfig).getAdaboostUIModel();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof 	WeightOfEvidenceConfig){
				AnalysisWOETable newModel = ((WeightOfEvidenceConfig)newConfig).getWOETableInfor();
				AnalysisWOETable oldModel = ((WeightOfEvidenceConfig)oldConfig).getWOETableInfor();
				result = ParameterUtility.nullableEquales(newModel,oldModel);
			}else if(newConfig instanceof 	CustomziedConfig){
				HashMap<String, String> newModel = ((CustomziedConfig)newConfig).getParametersMap();
				HashMap<String, String> oldModel = ((CustomziedConfig)oldConfig).getParametersMap();
				result = newModel.equals(oldModel);
			}else if(newConfig instanceof 	TableSetConfig){
				AnalysisTableSetModel newModel = ((TableSetConfig)newConfig).getTableSetModel();
				AnalysisTableSetModel oldModel = ((TableSetConfig)oldConfig).getTableSetModel();
				result = newModel.equals(oldModel);
			}else if(newConfig instanceof 	HadoopFileSelectorConfig){
				AnalysisFileStructureModel newModel = ((HadoopFileSelectorConfig)newConfig).getHadoopFileStructure();
				AnalysisFileStructureModel oldModel = ((HadoopFileSelectorConfig)oldConfig).getHadoopFileStructure();
				result = newModel.equals(oldModel);
			}
			else if(newConfig instanceof 	CopytoHadoopConfig){
				AnalysisFileStructureModel newModel = ((CopytoHadoopConfig)newConfig).getHadoopFileStructure();
				AnalysisFileStructureModel oldModel = ((CopytoHadoopConfig)oldConfig).getHadoopFileStructure();
				String newIfFileExists = ((CopytoHadoopConfig)newConfig).getIfFileExists();
				String oldIfFileExists = ((CopytoHadoopConfig)oldConfig).getIfFileExists();
				
				result = newModel.equals(oldModel)&&newIfFileExists.equals(oldIfFileExists);
			}
			else if(newConfig instanceof 	HadoopVariableConfig){
				AnalysisDerivedFieldsModel newModel = ((HadoopVariableConfig)newConfig).getDerivedModel();
				AnalysisDerivedFieldsModel oldModel = ((HadoopVariableConfig)oldConfig).getDerivedModel();
				result = newModel.equals(oldModel);
			}else if(newConfig instanceof 	HadoopJoinConfig){
				AnalysisHadoopJoinModel newModel = ((HadoopJoinConfig)newConfig).getJoinModel();
				AnalysisHadoopJoinModel oldModel = ((HadoopJoinConfig)oldConfig).getJoinModel();
				result = newModel.equals(oldModel);
			}
			else if(newConfig instanceof 	HadoopAggregaterConfig){
				  AnalysisAggregateFieldsModel newModel = ((HadoopAggregaterConfig)newConfig).getAggregateFieldsModel();
				 AnalysisAggregateFieldsModel oldModel = ((HadoopAggregaterConfig)oldConfig).getAggregateFieldsModel();
				result = newModel.equals(oldModel);
			}
			else if(newConfig instanceof 	HadoopUnionConfig){
				AnalysisHadoopUnionModel newModel = ((HadoopUnionConfig)newConfig).getUnionModel();
				AnalysisHadoopUnionModel oldModel = ((HadoopUnionConfig)oldConfig).getUnionModel();
				result = newModel.equals(oldModel);
			}
			else if(newConfig instanceof 	SubFlowConfig){
				  HashMap<String, String> newModel = ((SubFlowConfig)newConfig).getTableMapping();
				  HashMap<String, String> oldModel = ((SubFlowConfig)oldConfig).getTableMapping(); 
				result =ParameterUtility.nullableEquales(((SubFlowConfig)newConfig).getSubflowPath(), ((SubFlowConfig)oldConfig).getSubflowPath()) 
					&&ParameterUtility.nullableEquales(((SubFlowConfig)newConfig).getExitOperator(), ((SubFlowConfig)oldConfig).getExitOperator())
					&&hashMapEquals(newModel,oldModel)&&hashMapEquals(((SubFlowConfig)newConfig).getSubflowVariable() ,
						((SubFlowConfig)oldConfig).getSubflowVariable());
			}else if(newConfig instanceof 	HadoopPigExecuteConfig){
				AnalysisFileStructureModel newModel = ((HadoopPigExecuteConfig)newConfig).getHadoopFileStructure();
				AnalysisFileStructureModel oldModel = ((HadoopPigExecuteConfig)oldConfig).getHadoopFileStructure();
				
				boolean fileStructureResult = ParameterUtility.nullableEquales(newModel,oldModel);
				

				AnalysisPigExecutableModel newPigModel = ((HadoopPigExecuteConfig)newConfig).getPigScriptModel();
				AnalysisPigExecutableModel oldPigModel = ((HadoopPigExecuteConfig)oldConfig).getPigScriptModel();
				
				boolean pigExecutableResult = ParameterUtility.nullableEquales(newPigModel,oldPigModel);
				
				result=fileStructureResult&&pigExecutableResult;
			}
	 	}

		itsLogger.debug("configEquals: return: " + result) ;
		return result;
	}


	private void ignoreEmptyHadoopResultName(AnalyticConfiguration newConfig,
			AnalyticConfiguration oldConfig,
			HashMap<String, String> newValuesMap,
			HashMap<String, String> oldValuesMap) {
		if(oldConfig instanceof HadoopDataOperationConfig
				&&newConfig instanceof HadoopDataOperationConfig){
			if(!StringUtil.isEmpty(oldValuesMap.get(HadoopDataOperationConfig.ConstResultsName))
					&&StringUtil.isEmpty(newValuesMap.get(HadoopDataOperationConfig.ConstResultsName))){
				newValuesMap.put(HadoopDataOperationConfig.ConstResultsName, 
				oldValuesMap.get(HadoopDataOperationConfig.ConstResultsName));
			}
		}
	}
	 
	private boolean hashMapEquals(HashMap<String, String> map1,	HashMap<String, String> map2) {
		if(map1==null&&map2!=null){
			return false;
		}else if(map2==null&&map1!=null){
			return false;
		}else if(map1.isEmpty()&&!map2.isEmpty()){
			return false;
		}else if(map2.isEmpty()&&!map1.isEmpty()){
			return false;
		}else{
			Iterator<String> it = map1.keySet().iterator(); 
			while(it.hasNext()){
				String key = it.next();
				if(ParameterUtility.nullableEquales(map1.get(key), map2.get(key))==false){
					return false;
				} 
			}
			
			it = map2.keySet().iterator(); 
			while(it.hasNext()){
				String key = it.next();
				if(ParameterUtility.nullableEquales(map2.get(key), map1.get(key))==false){
					return false;
				} 
			}
		}
		
		return true;
	}


	public boolean hasResult(String nodelName){
		if(stepRunNodeMap!=null&&stepRunNodeMap.containsKey(nodelName)
				&&stepRunNodeMap.get(nodelName).isFinished()){
			return true;
		}else if( hasResult4SubflowNode(nodelName)==true){
			return true;
		}else{
			return false;
		}
	}

	private boolean hasResult4SubflowNode(String nodelName) {
		if(stepRunNodeMap!=null&&stepRunNodeMap.values()!=null){
			Iterator<AnalyticNode> it = stepRunNodeMap.values().iterator();
			while(it.hasNext()){
				AnalyticNode node = it.next(); 
				if(nodelName.equals( node.getGroupID())&&node.isFinished()==true){  
					return true;
				}
			}
		}
		return false;
	}


	public Map<String, AnalyticNode> getStepRunNodeMap() {
		return stepRunNodeMap;
	}

	public boolean isInStepRun() {
		return isInStepRun;
	}

	public void setInStepRun(boolean isInStepRun) {
		this.isInStepRun = isInStepRun;
	}
	
	
}
