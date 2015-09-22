/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OperatorManagement.java
 */
package com.alpine.miner.impls.editworkflow.operator;

import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.controller.OperatorManagementController.OperatorParam;
import com.alpine.miner.impls.datasourcemgr.impl.hadoop.IHadoopConnectionFeatcher;
import com.alpine.miner.impls.editworkflow.link.LinkManagement;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.operator.DatabaseOperatorPrimaryInfo;
import com.alpine.miner.impls.web.resource.operator.HadoopOperatorPrimaryInfo;
import com.alpine.miner.impls.web.resource.operator.OperatorPrimaryInfo;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.OperatorPosition;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopFileOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopPredictOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.file.StringUtil;

/**
 * @author Gary
 * Jul 16, 2012
 */
public class OperatorManagement {

	private static final OperatorManagement INSTANCE = new OperatorManagement();
	
	private OperatorManagement(){}
	
	public static OperatorManagement getInstance(){
		return INSTANCE;
	}
	
	/**
	 * create Operator and put it in flow
	 * @param param
	 * @param workFlow
	 * @param userName
	 * @param locale
	 * @return
	 */
	public UIOperatorModel fillOperatorToWorkflow(OperatorParam param, OperatorWorkFlow workFlow, String userName, Locale locale, FlowInfo flowInfo){
		// create new operator property, for dragging a operator to workflow.
        UIOperatorModel opModel = 
        		OperatorCreator.getInstance().newOperatorModel(
        				param.getOperatorClass(), 
        				param.getName(), 
        				workFlow, 
        				userName, 
        				locale,
        				flowInfo);
        
		opModel.setUUID(param.getUuid());
		int x = param.getX(),
			y = param.getY();
		opModel.setPosition(new OperatorPosition(x, y, x, y));
		workFlow.addChild(opModel);
		Operator operator = opModel.getOperator();
        if(param.isHasDefaultVal()){
            if(DbTableOperator.class.getSimpleName().equals(param.getOperatorClass())){
            	operator.getOperatorParameter(OperatorParameter.NAME_dBConnectionName).setValue(param.getConnectionName());
            	operator.getOperatorParameter(OperatorParameter.NAME_schemaName).setValue(param.getSchemaName());
            	operator.getOperatorParameter(OperatorParameter.NAME_tableName).setValue(param.getEntityName());
            }else if(HadoopFileOperator.class.getSimpleName().equals(param.getOperatorClass())){
            	operator.getOperatorParameter(OperatorParameter.NAME_HD_connetionName).setValue(param.getConnectionName());
            	operator.getOperatorParameter(OperatorParameter.NAME_HD_fileName).setValue(param.getFilePath());
            }
        } //note: filling default values moved to operator creator function
        if(operator.getOperatorParameterList() == null){
        	return opModel;
        }
        for(OperatorParameter parameter : operator.getOperatorParameterList()){
        	String defaultVal = operator.getOperatorParameterDefaultValue(parameter.getName());
            if(!StringUtil.isEmpty(defaultVal)){
            	parameter.setValue(defaultVal);
            }
        }
        return opModel;
	}
	
	/**
	 * build operator primary info
	 * @param opModel
	 * @param currentUser
	 * @return
	 */
	public OperatorPrimaryInfo buildOperatorPrimaryInfo(UIOperatorModel opModel, String currentUser){
		OperatorPrimaryInfo info = null;
		Operator operator = opModel.getOperator();
		if(operator instanceof HadoopOperator 
				|| (operator instanceof SubFlowOperator 
						&& ((SubFlowOperator)operator).getExitOperator() instanceof HadoopOperator)){//hadoop operator
			Operator hadoopOperator = operator;
			if(operator instanceof SubFlowOperator){
				hadoopOperator = ((SubFlowOperator)operator).getExitOperator();
			}
			HadoopOperatorPrimaryInfo hadoopInfo = new HadoopOperatorPrimaryInfo();
			info = hadoopInfo;
			hadoopInfo.setOperatorType(OperatorPrimaryInfo.HADOOP_TYPE);
			String connectionName = OperatorUtility.getHadoopConnectionName(hadoopOperator.getOperModel());
			hadoopInfo.setOutputHadoopFilePath(OperatorUtility.getHadoopFilePath(hadoopOperator.getOperModel()));
			
			hadoopInfo.setConnectionName(connectionName == null ? "" : IHadoopConnectionFeatcher.INSTANCE.getHadoopEntityInfo(currentUser, connectionName).getKey());
			OperatorParameter storeResult = ParameterUtility.getParameterByName(hadoopOperator, OperatorParameter.NAME_HD_StoreResults);
			if(storeResult != null){
				hadoopInfo.setStoreResult(Boolean.parseBoolean((String) storeResult.getValue()));
			}
			if(operator instanceof HadoopPredictOperator){
				hadoopInfo.setStoreResult(true);
			}
		}else{
			DatabaseOperatorPrimaryInfo dbInfo = new DatabaseOperatorPrimaryInfo();
			info = dbInfo;
			dbInfo.fillOPUIDBInfo(operator);
			dbInfo.setOperatorType(OperatorPrimaryInfo.DATABASE_TYPE);
			dbInfo.setHasDbTableInfo(dbInfo.hasDBTableInfo(opModel));
		}
		info.setValid(operator.isVaild(operator.getWorkflow().getParentVariableModel()));			
		info.setUid(opModel.getUUID());
		info.setName(opModel.getId());
		info.setClassname(opModel.getClassName());
		info.setX(opModel.getPosition().getStartX());
		info.setY(opModel.getPosition().getStartY());
		String description = (String) ParameterUtility.getParameterValue(operator, OperatorParameter.NAME_note);
		if(description != null){
			info.setDescription(description);
		}
		
		return info;
	}
	
	/**
	 * remove an operator from work flow
	 * @param workFlow
	 * @param operatorUid
	 */
	public void removeOperator(OperatorWorkFlow workFlow, String operatorUid){
		UIOperatorModel operatorModel = getOperatorModelByUUID(workFlow.getChildList(), operatorUid);
		LinkManagement.getInstance().cleanConnectsByOperator(workFlow, operatorModel.getSourceConnection());
		LinkManagement.getInstance().cleanConnectsByOperator(workFlow, operatorModel.getTargetConnection());
		List<UIOperatorModel> operatorSet = workFlow.getChildList();
		for(int i = 0;i < operatorSet.size();i++){
			UIOperatorModel op = operatorSet.get(i);
			if(op.getUUID().equals(operatorUid)){
				operatorSet.remove(i);
				break;
			}
		}
	}
	
	public void renameOperator(OperatorWorkFlow workFlow, String operatorUid, String newName){
		UIOperatorModel operatorModel = getOperatorModelByUUID(workFlow.getChildList(), operatorUid);
		operatorModel.setId(newName);
	}
	
	public UIOperatorModel getOperatorModelByUUID(List<UIOperatorModel> operatorList, String uuid){
		for(UIOperatorModel operatorModel : operatorList){
			if(operatorModel.getUUID().equals(uuid)){
				return operatorModel;
			}
		}
		throw new NullPointerException("cannot find Operator by " + uuid);
	}
}
