/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LinkManagement.java
 */
package com.alpine.miner.impls.editworkflow.link;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.miner.impls.editworkflow.link.exception.LinkBaseException;
import com.alpine.miner.impls.editworkflow.operator.OperatorCreator;
import com.alpine.miner.impls.editworkflow.operator.OperatorManagement;
import com.alpine.miner.impls.editworkflow.operator.SvdOperatorUtility;
import com.alpine.miner.inter.resources.Resources;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.PredictOperator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.hadoop.*;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionFile;
import com.alpine.miner.workflow.operator.parameter.hadoopunion.HadoopUnionModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosCalculatorOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import org.apache.log4j.Logger;

/**
 * @author Gary
 * Jul 12, 2012
 */
public class LinkManagement {
    private static Logger itsLogger = Logger.getLogger(LinkManagement.class);

    private static LinkManagement INSTANCE = new LinkManagement();
	
	private static final String AVAILABLE = "";
	
	public static LinkManagement getInstance(){
		return INSTANCE;
	}
	
	private LinkManagement(){}
	
	/**
	 * get all of available used to be connect operators
	 * @param workflow
	 * @param operatorUid
	 * @return
	 */
	public List<UIOperatorModel> getAvailableSubscriber(OperatorWorkFlow workflow, String operatorUid) throws LinkBaseException{
		List<UIOperatorModel> availableOperator = new ArrayList<UIOperatorModel>();
		List<UIOperatorModel> operatorList = workflow.getChildList();
		UIOperatorModel currentOperatorModel = OperatorManagement.getInstance().getOperatorModelByUUID(operatorList, operatorUid);
		Operator currentOperator = currentOperatorModel.getOperator();
		
		for(UIOperatorModel operatorModel : operatorList){
			if(currentOperator instanceof SubFlowOperator) {
				if(((SubFlowOperator)currentOperator).hasExitOperator() == false
						&& operatorModel.getOperator() instanceof DbTableOperator == false
						&& operatorModel.getOperator() instanceof SQLExecuteOperator == false){
					continue;
				}
			}
			
			String msg = rebuildOperatorModelBySource(currentOperatorModel, operatorModel, false).getOperator().validateInputLink(currentOperator);
			boolean isLoopConnect = OperatorUtility.checkLoopOperator(currentOperatorModel, operatorModel);
			itsLogger.debug(msg);
			if(AVAILABLE.equals(msg) 
					&& !isLoopConnect 
					&& !currentOperatorModel.getUUID().equals(operatorModel.getUUID()) 
					&& allowSwitch(currentOperatorModel, operatorModel) 
					&& !isConnected(currentOperatorModel, operatorModel)){
				availableOperator.add(operatorModel);
			}
		}
		return availableOperator;
	}
	
	/**
	 * get all of available used to be connected source operators
	 * @param workflow
	 * @param operatorUid
	 * @return
	 */
	public List<UIOperatorModel> getAvailablePublisher(OperatorWorkFlow workflow, String operatorUid, String originalSourceId){
		List<UIOperatorModel> availableOperator = new ArrayList<UIOperatorModel>();
		List<UIOperatorModel> operatorList = workflow.getChildList();
		UIOperatorModel currentOperatorModel = OperatorManagement.getInstance().getOperatorModelByUUID(operatorList, operatorUid);
		UIOperatorModel originalSourceOp = OperatorManagement.getInstance().getOperatorModelByUUID(operatorList, originalSourceId);
		for(UIOperatorModel operatorModel : operatorList){
			if(operatorModel.getOperator() instanceof SubFlowOperator) {
				if(((SubFlowOperator)operatorModel.getOperator()).hasExitOperator() == false
						&& currentOperatorModel.getOperator() instanceof DbTableOperator == false
						&& currentOperatorModel.getOperator() instanceof SQLExecuteOperator == false){
					continue;
				}
			}
			boolean isValid = validateInputLink(originalSourceOp, operatorModel, currentOperatorModel);
			boolean isLoopConnect = OperatorUtility.checkLoopOperator(operatorModel, currentOperatorModel);
			if(isValid
					&& !isLoopConnect 
					&& !currentOperatorModel.getUUID().equals(operatorModel.getUUID()) 
					&& allowSwitch(operatorModel, currentOperatorModel) 
					&& !isConnected(operatorModel, currentOperatorModel)){
				availableOperator.add(operatorModel);
			}
		}
		return availableOperator;
	}
	
	/**
	 * create new connection to connect two Operator.
	 * @param workflow
	 * @param sourceId
	 * @param targetId
	 */
	public void connectOperator(OperatorWorkFlow workflow, String sourceId, String targetId) throws LinkBaseException{
		UIOperatorModel sourceOpModel = OperatorManagement.getInstance().getOperatorModelByUUID(workflow.getChildList(), sourceId);
		UIOperatorModel	targetOpModel = rebuildOperatorModelBySource(sourceOpModel, OperatorManagement.getInstance().getOperatorModelByUUID(workflow.getChildList(), targetId), true);
		validateConnection(sourceOpModel, targetOpModel);
		 	
		
		UIOperatorConnectionModel connectionModel = createConnectionModel(sourceOpModel, targetOpModel);
		workflow.getConnModelList().add(connectionModel);
		afterConnect(sourceOpModel, targetOpModel);
	}

	private void validateConnection(UIOperatorModel sourceOpModel,
			UIOperatorModel targetOpModel) throws LinkBaseException {
		if((sourceOpModel.getOperator() instanceof HadoopFileOperator )
				&&targetOpModel.getOperator() instanceof HadoopUnionOperator){
			OperatorParameter fileParam = ((HadoopFileOperator)sourceOpModel.getOperator()).getOperatorParameter(OperatorParameter.NAME_HD_fileName);
			if(fileParam!=null){
				 String sourceFilePath = fileParam.toString();
				 OperatorParameter  unionParam = targetOpModel.getOperator() .getOperatorParameter(OperatorParameter.NAME_HD_Union_Model);
			if(unionParam!=null &&fileAlreadyExits( sourceFilePath,(HadoopUnionModel)unionParam.getValue())) {
				throw new LinkBaseException("Can not union a hadoop file with itself.");
			}
		}	
		}
		 
		Operator sourceOperator = sourceOpModel.getOperator();
		if(sourceOperator instanceof HadoopUnionOperator ==false
				&&sourceOperator instanceof HadoopDataOperationOperator == true){
		 	OperatorParameter storeParameter = sourceOperator.getOperatorParameter(OperatorParameter.NAME_HD_StoreResults);
		 	if(storeParameter!=null){
		 		if("false".equalsIgnoreCase((String)storeParameter.getValue())){
		 			throw new LinkBaseException("Please check \"store result\" before link \"" +sourceOpModel.getId()
		 					+"\" to hadoop set operator.");
		 		};
		 	}
 		}
	}
	
 
	private boolean fileAlreadyExits(String sourceFilePath,
			HadoopUnionModel model) { 
		if(model!=null&&model.getUnionFiles()!=null){
			List<HadoopUnionFile> files = model.getUnionFiles();

			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
				HadoopUnionFile hadoopUnionFile = (HadoopUnionFile) iterator.next();
				if(hadoopUnionFile.getFile().equals(sourceFilePath)==true){
					return true;
				}
			}
		}
		return false;
	}

	public UIOperatorConnectionModel createConnectionModel(UIOperatorModel sourceOpModel, UIOperatorModel targetOpModel){
		UIOperatorConnectionModel connectionModel = new UIOperatorConnectionModel();
		connectionModel.setSource(sourceOpModel);
		connectionModel.setTarget(targetOpModel);
		connectionModel.attachSource();
		connectionModel.attachTarget();
		return connectionModel;
	}
	
	/**
	 * reconnect operator
	 * @param workflow
	 * @param originalSourceId
	 * @param originalTargetId
	 * @param sourceId
	 * @param targetId
	 * @throws LinkBaseException 
	 */
	public void reconnectOperator(OperatorWorkFlow workflow, 
									String originalSourceId, String originalTargetId, 
									String sourceId, String targetId) throws LinkBaseException{
		UIConnectionModel movedConnection = null;
		for(UIConnectionModel connModel : workflow.getConnModelList()){
			if(connModel.getSource().getUUID().equals(originalSourceId) && connModel.getTarget().getUUID().equals(originalTargetId)){
				movedConnection = connModel;
				break;
			}
		}
		if(movedConnection == null){
			return;
		}
		UIOperatorModel sourceOpModel = OperatorManagement.getInstance().getOperatorModelByUUID(workflow.getChildList(), sourceId);
	    UIOperatorModel targetOpModel = rebuildOperatorModelBySource(sourceOpModel, OperatorManagement.getInstance().getOperatorModelByUUID(workflow.getChildList(), targetId), true);
		
		  validateConnection(sourceOpModel, targetOpModel);
		movedConnection.detachSource();
		movedConnection.detachTarget();
		movedConnection.setSource(sourceOpModel);
		movedConnection.setTarget(targetOpModel);
		movedConnection.attachSource();
		movedConnection.attachTarget();
		afterConnect(sourceOpModel, targetOpModel);
	 
	}
	
	/**
	 * clean connects for giving.
     * @param workflow
	 * @param connectModelSet  e.g. operatorModel.getSourceConnection()
	 */
	public void cleanConnectsByOperator(OperatorWorkFlow workflow, List<UIConnectionModel> connectModelSet){
		List<UIConnectionModel> tmpList = new ArrayList<UIConnectionModel>();
		tmpList.addAll(connectModelSet);
		for(UIConnectionModel connectModel : tmpList){
			connectModel.detachSource();
			connectModel.detachTarget();
			workflow.getConnModelList().remove(connectModel);
		}
	}
	
	public void removeConnection(OperatorWorkFlow workflow, LinkModel link){
		UIConnectionModel removeConnection = null;
		for(UIConnectionModel connModel : workflow.getConnModelList()){
			if(connModel.getSource().getUUID().equals(link.getSourceId()) && connModel.getTarget().getUUID().equals(link.getTargetId())){
				removeConnection = connModel;
				break;
			}
		}
		if(removeConnection != null){
			removeConnection.detachSource();
			removeConnection.detachTarget();
			workflow.getConnModelList().remove(removeConnection);
		}
	}
	
	public void batchRemoveConnections(OperatorWorkFlow workflow, LinkModel[] links){
		for(LinkModel link : links){
			removeConnection(workflow, link);
		}
	}
	
	private boolean allowSwitch(UIOperatorModel sourceOperator, UIOperatorModel targetOperatorModel){
		boolean allowSwitch = true;
		Operator targetOperator = getOutPutOperator(targetOperatorModel);
		if(sourceOperator.getOperator() instanceof HadoopOperator 
				&& !(targetOperator instanceof HadoopOperator)
				&& !(targetOperator instanceof CopyToDBOperator)){
			List<Operator> followupOperators = targetOperator.getChildOperators();
			allowSwitch &= (followupOperators == null || followupOperators.size() == 0);
		}
		return allowSwitch;
	}
	
	private boolean isConnected(UIOperatorModel sourceOperator, UIOperatorModel targetOperatorModel){
		boolean isConnected = false;
		List<UIConnectionModel> targetConnections = sourceOperator.getTargetConnection();
		for(UIConnectionModel targetConnection : targetConnections){
			if(targetConnection.getTarget().equals(targetOperatorModel)){
				isConnected = true;
				break;
			}
		}
		return isConnected;
	}
	
	private UIOperatorModel rebuildOperatorModelBySource(UIOperatorModel sourceOperatorModel, UIOperatorModel targetOperatorModel, boolean replease){
		UIOperatorModel newOperatorModel = targetOperatorModel;
		Operator sourceOperator = getOutPutOperator(sourceOperatorModel);
        //if target already has source connection, that means it needs to stay as db if already db
		if(sourceOperator instanceof HadoopOperator 
				&& Resources.isHadoopOperatorNameExists(targetOperatorModel.getClassName()) 
				&& (targetOperatorModel.getSourceConnection() == null 
						|| targetOperatorModel.getSourceConnection().size() ==0)){
			OperatorWorkFlow workflow = sourceOperatorModel.getOperator().getWorkflow();
			UIOperatorModel newHadoopOperatorModel = OperatorCreator.getInstance().newOperatorModel(
																Resources.getHadoopNewOperatorName(targetOperatorModel.getClassName()), 
																targetOperatorModel.getId(), 
																workflow, 
																targetOperatorModel.getOperator().getUserName(), 
																targetOperatorModel.getOperator().getLocale());
			newHadoopOperatorModel.setUUID(targetOperatorModel.getUUID());
			newHadoopOperatorModel.setPosition(targetOperatorModel.getPosition());
			if(replease){
				List<UIOperatorModel> operatorModelList = workflow.getChildList();
				int targetOpModelIdx = operatorModelList.indexOf(targetOperatorModel);
				operatorModelList.set(targetOpModelIdx, newHadoopOperatorModel);
			}
			newOperatorModel = newHadoopOperatorModel;
		}
		return newOperatorModel;
	}
	
	

	private boolean validateInputLink(UIOperatorModel oldSource,UIOperatorModel source,UIOperatorModel target){
		if (source.getOperator() instanceof SQLExecuteOperator) {
			List<UIOperatorModel> operatorList = OperatorUtility
					.getParentList(source.getOperator().getOperModel());
			if (operatorList == null || operatorList.size() == 0) {
				return target.getOperator() instanceof DbTableOperator;
			} else {
				return validateInputLink(oldSource,operatorList.get(0),target);
			}
		}else{
			List<String> sOutputList = source.getOperator().getOutputClassList();
			List<String> tInputList = target.getOperator().getInputClassList();
			boolean isReady = false;
			if(sOutputList == null || tInputList == null){
				return false;
			}
			for(int i = 0;i < sOutputList.size();i++){
				for(int j = 0;j < tInputList.size();j++){
					if(sOutputList.get(i).equals(tInputList.get(j))){
						isReady = true;
					}
				}
			}
			if(!isReady){
				return false;
			}

			if(target.getOperator() instanceof PredictOperator){
				if(oldSource.getOperator() instanceof LearnerOperator
						&&(source.getOperator() instanceof LearnerOperator)==false){
					return false;
				}
			}
			if(target.getOperator() instanceof HadoopPredictOperator){
				if(oldSource.getOperator() instanceof HadoopLearnerOperator
						&&(source.getOperator() instanceof HadoopLearnerOperator)==false){
					return false;
				}
			}
			if(source.getOperator() instanceof HadoopPredictOperator){
				return false;// logistic and linear pridiction can not connect other operator.
			}
			
			Operator sourceOperator = getOutPutOperator(source);
			if((!(sourceOperator instanceof HadoopFileOperator) 
						&& target.getOperator() instanceof HadoopLearnerOperator)
					|| ((sourceOperator instanceof HadoopDataOperationOperator)
						&& (target.getOperator() instanceof CopyToDBOperator 
							|| target.getOperator() instanceof HadoopKmeansOperator
							|| target.getOperator() instanceof HadoopLinearRegressionOperator
							|| target.getOperator() instanceof HadoopLogisticRegressionOperator
							|| target.getOperator() instanceof HadoopDecisionTreeOperator
							|| target.getOperator() instanceof HadoopPredictOperator
							|| target.getOperator() instanceof HadoopROCOperator
							|| target.getOperator() instanceof HadoopLiftOperator
                            || target.getOperator() instanceof HadoopConfusionOperator
							|| target.getOperator() instanceof HadoopGoodnessOfFitOperator
                            || target.getOperator() instanceof HadoopVariableSelectionAnalysisOperator
                            || target.getOperator() instanceof HadoopNaiveBayesOperator))){
				OperatorParameter opPara = sourceOperator.getOperatorParameter(OperatorParameter.NAME_HD_StoreResults);
				if(opPara != null){
					String opValue = (String)opPara.getValue();
					if(com.alpine.utility.db.Resources.FalseOpt.equals(opValue)){
						return false;
					}
				}
			}
			return validateModel(oldSource,source, target);
		}
	}

	private boolean validateModel(UIOperatorModel oldSource,UIOperatorModel source, UIOperatorModel target) {
		List<Object> list = source.getOperator().getOutputObjectList();
		boolean newModel = false;
		for(Object str:list){
			if(str instanceof EngineModel){
				newModel = true;
			}
		}
		
		boolean hasModel = false;
		
		boolean sameType=false;
		List<Object> oldOutputList = oldSource.getOperator().getOutputObjectList();
		List<Object> newOldOutputList = source.getOperator().getOutputObjectList();
		if(oldOutputList!=null&&oldOutputList.size()!=0
				&&newOldOutputList!=null&&newOldOutputList.size()!=0){
			if(oldOutputList.get(0) instanceof EngineModel
					&&newOldOutputList.get(0) instanceof EngineModel){
				sameType=true;
			}
		}
		
		List<UIOperatorModel> parentList = OperatorUtility.getParentList(target);

		for(UIOperatorModel om:parentList){
			for(Object str:om.getOperator().getOutputObjectList()){
				if(str instanceof EngineModel&&!sameType){
					hasModel = true;
					break;
				}
			}
		}
		
		if(newModel && hasModel){
			return false;
		}
		
		if(OperatorUtility.getParentList(source)!=null
				&&!OperatorUtility.getParentList(source).isEmpty()
				&&OperatorUtility.getParentList(source).size()==1){
			UIOperatorModel opModel=OperatorUtility.getParentList(source).get(0);
			if(opModel.getOperator() instanceof AdaboostOperator){
				return false;
			}
		}
		return true;
	}
	
	//do something after connected operators.
	// We can refine this handler to task chain if there are more things need to do after connect.
	private void afterConnect(UIOperatorModel source, UIOperatorModel target){
		if(source.getOperator() instanceof SVDLanczosOperator
				&& target.getOperator() instanceof SVDLanczosCalculatorOperator){
			SvdOperatorUtility.syncSVDParams2SVDCalculator(source.getOperator(), target.getOperator());
		}
	}
	
	private Operator getOutPutOperator(UIOperatorModel operatorModel){
		Operator operator = operatorModel.getOperator();
		if(operator instanceof SubFlowOperator){
			SubFlowOperator subFlowOperator = (SubFlowOperator) operator;
			operator = subFlowOperator.getExitOperator();
			//if sub-flow's exit operator equals null means User hasn't set up exit operator parameter yet.
			if(operator == null){
				operator = operatorModel.getOperator();
			}
		}
		return operator;
	}
}
