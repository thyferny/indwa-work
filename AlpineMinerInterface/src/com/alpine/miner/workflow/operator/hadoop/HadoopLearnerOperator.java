/**
 * ClassName HadoopLearnerOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-8-17
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionItem;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;

/**
 * @author Jeff Dong
 *
 */
public abstract class HadoopLearnerOperator extends HadoopOperator {

	public HadoopLearnerOperator(List<String> parameterNames) {
		super(parameterNames);
	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=new ArrayList<Object>();
		// get the parent operators
		List<Operator> parentOperatorList=getParentOperatorList();
		for (Operator operator: parentOperatorList) {
			List<Object> parentOperatorOutlutList=operator.getOperatorOutputList();
			if(parentOperatorOutlutList!=null){
				for(Object object:parentOperatorOutlutList){
					if(object instanceof OperatorInputFileInfo){
						continue;
					}
					operatorInputList.add(object);
				}
			}
		}
		return operatorInputList;
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(StringUtil.isEmpty(message) ==true){ 
			message = super.validateStoreResult(precedingOperator);
		}
		return message;
	}
	
	@Override
	public List<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new EngineModel());
		return list;	
	}
	@Override
	public boolean isRunningFlowDirty() {
		OperatorParameter operatorParameter=getOperatorParameter(OperatorParameter.NAME_forceRetrain);
		if(operatorParameter==null){
			return false;
		}
		String forceRetrain=(String)operatorParameter.getValue();
		if(!StringUtil.isEmpty(forceRetrain)
				&&forceRetrain.equals(Resources.YesOpt)){
			return true;
		}
		return false;
	}
	
	protected void validateInteractionColumns(List<String> fieldList, List<String> invalidParameterList, String paraName, InterActionColumnsModel interActionModel) {
		if (!invalidParameterList.contains(paraName)
				&&interActionModel!=null) {
			List<InterActionItem> iaList = interActionModel.getInterActionItems();
			if (iaList == null || iaList.isEmpty())
				return;
			List<InterActionItem> needRemoveList=new ArrayList<InterActionItem>();
			for(InterActionItem item:iaList){
				if(!fieldList.contains(item.getFirstColumn())
						||!fieldList.contains(item.getSecondColumn())){
					needRemoveList.add(item);
				}
			}
			if(iaList.size()==needRemoveList.size()){
				invalidParameterList.add(paraName);
			}
		}	
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_forceRetrain)) {
			return Resources.YesOpt;
		}
		return "";
	}
}
