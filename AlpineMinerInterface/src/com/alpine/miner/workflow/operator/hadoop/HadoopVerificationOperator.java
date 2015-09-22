/**
 * ClassName HadoopVerificationOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-19
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public abstract class HadoopVerificationOperator extends HadoopOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_columnValue
			//,OperatorParameter.NAME_useModel
	});
	
	public HadoopVerificationOperator(List<String> parameterNames) {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_LOR);
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_TREE_CLASSIFICATION);
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
		List<OperatorInputFileInfo> fileList = new ArrayList<OperatorInputFileInfo>();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof OperatorInputFileInfo){
					fileList.add((OperatorInputFileInfo)obj);
				}else if(obj instanceof EngineModel){
					modelList.add((EngineModel)obj);
				}
			}
		}
		if ((fileList.size() == 1) && (modelList.size() > 0)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(StringUtil.isEmpty(message) ==true){ 
			message = super.validateStoreResult(precedingOperator);
		}
		return message;
	}
}
