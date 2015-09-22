/**
 * ClassName VerificationOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.model.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;

/**
 * @author zhao yong
 *
 */
public abstract class VerificationOperator extends AbstractOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_columnValue,
			OperatorParameter.NAME_useModel
	});
	
	public VerificationOperator(List<String> parameterNames) {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addInputClass(EngineModel.MPDE_TYPE_LOR);
		addInputClass(EngineModel.MPDE_TYPE_LR_SPLITMODEL);
		addInputClass(EngineModel.MPDE_TYPE_NB);
		addInputClass(EngineModel.MPDE_TYPE_NEU);
		addInputClass(EngineModel.MPDE_TYPE_TREE_CLASSIFICATION);
		addInputClass(EngineModel.MPDE_TYPE_TREE_REGRESSION);
		addInputClass(EngineModel.MPDE_TYPE_SVM_C);
		addInputClass(EngineModel.MPDE_TYPE_ADABOOST);
		addInputClass(EngineModel.MPDE_TYPE_TREE_RANDOM_FOREST);
	}
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_useModel)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_columnValue)){
				validateNull(invalidParameterList, paraName, paraValue);
			}		
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}
	}
	@Override
	public List<Object> getOperatorOutputList() {
		return null;
	}
	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputTableInfo> dbList = new ArrayList<OperatorInputTableInfo>();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof OperatorInputTableInfo){
					dbList.add((OperatorInputTableInfo)obj);
				}else if(obj instanceof EngineModel){
					modelList.add((EngineModel)obj);
				}
			}
		}

		String userModel=(String)getOperatorParameter(OperatorParameter.NAME_useModel).getValue();
		if (userModel != null && userModel.equalsIgnoreCase("true")) {
			if ((dbList.size() == 1) && (modelList.size() > 0)) {
				return true;
			} else {
				return false;
			}
		} else {
			if ((dbList.size() == 1)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_useModel)){
			return Resources.TrueOpt;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
