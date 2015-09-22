/**
 * ClassName WorkFlowUtil
 *
 * Version information: 1.00
 *
 * Data: 2012-4-8
 *
 * COPYRIGHT   2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPredictOperator;
import com.alpine.miner.workflow.operator.clustering.EMClusteringPredictOperator;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.decisiontree.TreePredictOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.field.BarChartAnalysisOperator;
import com.alpine.miner.workflow.operator.field.BoxAndWhiskerOperator;
import com.alpine.miner.workflow.operator.field.ScatterMatrixOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopBoxAndWiskerOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopFileOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopLinearRegressionPredictOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopLogisticRegressionPredictOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionPredictOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.naivebayes.NaiveBayesPredictOperator;
import com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkPredictOperator;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.plda.PLDATrainerOperator;
import com.alpine.miner.workflow.operator.sampling.RandomSamplingOperator;
import com.alpine.miner.workflow.operator.sampling.StratifiedSamplingOperator;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import com.alpine.miner.workflow.operator.svm.SVMPredictOperator;
import com.alpine.miner.workflow.operator.timeseries.TimeSeriesPredictOperator;
import com.alpine.miner.workflow.reader.AbstractReaderParameters;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.FileUtility;


/**
 * @author zhao yong
 *
 */

public class WorkFlowUtil {
	public static HashMap<String,String> getLeafOperatorNameIDMap(String filePath,
			Locale locale, ResourceType resourceType, String userName) throws Exception{
		
		List<UIOperatorModel> childList = getUIOperatorModels(filePath, locale,	resourceType, userName);
		return getLeafOperatorNameIDMap(childList);
		
	}

	public static HashMap<String, String> getLeafOperatorNameIDMap(
			List<UIOperatorModel> childList) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		if(childList!=null){
			for(int  i = 0 ;i<childList.size();i++){
				Operator operator = childList.get(i).getOperator();
				if((operator.getChildOperators()==null||
						operator.getChildOperators().size()==0)
						&&operator.getParentOperators()!=null
						&&operator.getParentOperators().size()>0){
					if(	operator instanceof HadoopOperator ){
							if( isHadoopOperatorOK(operator)==true
							&&hasOutPutTable(operator)==true){
						resultMap.put(childList.get(i).getId(),childList.get(i).getUUID());
							}
					}
					else if(operator instanceof SQLExecuteOperator==false
							&&operator instanceof DbTableOperator==false
							&&operator instanceof PLDATrainerOperator==false
							&&operator instanceof SVDLanczosOperator==false
							&&operator instanceof RandomSamplingOperator ==false
							&&operator instanceof StratifiedSamplingOperator ==false
							&&operator instanceof ScatterMatrixOperator ==false
							&&operator instanceof TreePredictOperator  ==false
							&&operator instanceof AdaboostPredictOperator ==false  
							&&operator instanceof LogisticRegressionOperator ==false
							&&operator instanceof NaiveBayesPredictOperator  ==false
							&&operator instanceof NeuralNetworkPredictOperator ==false
							&&operator instanceof SVMPredictOperator ==false
							&&operator instanceof LogisticRegressionPredictOperator==false
							&&operator instanceof TimeSeriesPredictOperator ==false
							&&operator instanceof EMClusteringPredictOperator == false
							&&hasOutPutTable(operator)==true){ 
						resultMap.put(childList.get(i).getId(),childList.get(i).getUUID());
					}
				}
			}
		}
		
		return resultMap;
	}

	private static boolean isHadoopOperatorOK(Operator operator) {
		if(operator instanceof HadoopLogisticRegressionPredictOperator
				||operator instanceof HadoopLinearRegressionPredictOperator){
			return false;
		}else if(ParameterUtility.getParameterByName(operator, OperatorParameter.NAME_HD_StoreResults)!=null
				&&ParameterUtility.getParameterByName(operator, OperatorParameter.NAME_HD_StoreResults).getValue()!=null){
			String storeResult = ParameterUtility.getParameterByName(operator, OperatorParameter.NAME_HD_StoreResults).getValue().toString();
			if(storeResult.equalsIgnoreCase("false")){
				return false;
			}
		}
		return true;
	}

	public static List<UIOperatorModel> getUIOperatorModels(String filePath,
			Locale locale, ResourceType resourceType, String userName)
			throws Exception {
		XMLWorkFlowReader reader = new XMLWorkFlowReader();
		AbstractReaderParameters para = new XMLFileReaderParameters(filePath, userName, resourceType);
		OperatorWorkFlow workflow = reader.doRead(para, locale);
		List<UIOperatorModel> childList = workflow.getChildList();
		return childList;
	}
	
	private static boolean hasOutPutTable(Operator operator) {
		List<Object> list = operator.getOperatorOutputList();
		if(list!=null){
			for(int i = 0 ;i <list.size();i++){
				if(list.get(i) instanceof OperatorInputTableInfo) {
					return true;
				}else if(list.get(i) instanceof OperatorInputFileInfo) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<OperatorInputTableInfo> getSubFlowInputTableSets(String filePath,
					Locale locale, ResourceType resourceType, String userName) throws Exception{
		List<OperatorInputTableInfo> infos = new ArrayList<OperatorInputTableInfo> ();
		List<UIOperatorModel> childList = getUIOperatorModels(filePath, locale,
				resourceType, userName);
		if(childList!=null){
			for(int  i = 0 ;i<childList.size();i++){
				Operator operator = childList.get(i).getOperator();
				if(operator instanceof DbTableOperator){
					infos.add(createOperatorInputTableInfo((DbTableOperator)operator));
				}else if(operator instanceof HadoopFileOperator){
					
					OperatorInputTableInfo fileInfo = SubFlowOperator.createOperatorInputTableInfo((HadoopFileOperator)operator); 
					infos.add(fileInfo);

				}
				 
			}
		}
		
		return infos;
		
	}

	

	private static OperatorInputTableInfo createOperatorInputTableInfo(
			DbTableOperator operator) {
		if(operator!=null&&operator.getOperatorOutputList()!=null&&operator.getOperatorOutputList().size()>0){
			return  (OperatorInputTableInfo)operator.getOperatorOutputList().get(0)	 ;
		}
		return null;
	}
	
	
	public static OperatorWorkFlow getWorkflow(String filePath,
			Locale locale, ResourceType resourceType, String userName) throws Exception {
		XMLWorkFlowReader reader = new XMLWorkFlowReader();
		AbstractReaderParameters para = new XMLFileReaderParameters(filePath, userName, resourceType);
		OperatorWorkFlow workflow = reader.doRead(para, locale);
	 
		return workflow;
	}


	public static List<SubFlowOperator> findSubflowOperators(	OperatorWorkFlow subflowWorkFlow) {
		if(subflowWorkFlow!=null&&subflowWorkFlow.getChildList()!=null){
			List<SubFlowOperator> results = new ArrayList<SubFlowOperator> ();
			
			List<UIOperatorModel> list = subflowWorkFlow.getChildList();
			for(int i = 0; i<list.size();i++){
				UIOperatorModel uiModel = list.get(i);
				if(uiModel.getOperator() instanceof SubFlowOperator){
					results.add((SubFlowOperator)uiModel.getOperator());
				}
			}
			  
			return results;
		}
		return null;
	}

	public static EngineModel loadModelFromFile(String modelFilePath) throws Exception   {
		 XMLWorkFlowReader reader = new XMLWorkFlowReader();
		 
		try {
			OperatorWorkFlow	owf = reader .doRead(new XMLFileReaderParameters(modelFilePath,System.getProperty("user.name"),ResourceType.Personal),Locale.getDefault());
		
		 
			for(UIOperatorModel om:owf.getChildList()){
				return ((ModelOperator)om.getOperator()).getModel();
				 
			}
		} catch (Exception e) {
			//this . a pure object file
			
			String objectStr = FileUtility.readFiletoString(new File(modelFilePath)).toString(); 
			Object modelObj = AlpineUtil.stringToObject(objectStr);
			if(modelObj instanceof EngineModel){
				return (EngineModel)modelObj;
			}
			throw new Exception("Can not load model :"+modelFilePath);
		}
		return null;
	}
	
	
}
