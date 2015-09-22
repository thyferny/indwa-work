/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowVariableServiceImpl
 * Apr 10, 2012
 */
package com.alpine.miner.impls.flowvariables.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.flowvariables.FlowVariableException;
import com.alpine.miner.impls.flowvariables.IFlowVariableService;
import com.alpine.miner.impls.flowvariables.model.FlowVariable;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;

/**
 * @author Gary
 *
 */
public class FlowVariableServiceImpl implements IFlowVariableService {

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.flowvariables.IFlowVariableService#getVariableWebModel(com.alpine.miner.impls.web.resource.FlowInfo)
	 */
	@Override
	public List<FlowVariable> getVariableWebModelList(FlowInfo flowInfo, Locale locale) throws FlowVariableException {
		OperatorWorkFlow workFlow = readWorkFlow(flowInfo, locale);
		List<FlowVariable> variableModelList = new ArrayList<FlowVariable>();
		variableModelList.add(new FlowVariable(workFlow.getVariableModelList().get(0), flowInfo.getId()));//current flow variable info
		variableModelList.addAll(getSubFlowVariables(workFlow));
		return variableModelList;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.flowvariables.IFlowVariableService#storeFlowVariableToFlow(com.alpine.miner.impls.flowvariables.model.FlowVariable, com.alpine.miner.impls.web.resource.FlowInfo)
	 */
	@Override
	public void storeFlowVariableToFlow(FlowVariable[] flowVariable,
			FlowInfo flowInfo, Locale locale) throws FlowVariableException {
		OperatorWorkFlow workFlow = readWorkFlow(flowInfo, locale);
		for(FlowVariable fv : flowVariable){
			VariableModel model = fv.getModel();
			if(fv.getFlowName().equals(flowInfo.getId())){// current flow
				workFlow.setVariableModelList(Arrays.asList(model));
			}else{//sub flow
				List<UIOperatorModel> operatorModelList = workFlow.getChildList();
				for(UIOperatorModel operator : operatorModelList){
					if(operator.getOperator() instanceof SubFlowOperator && operator.getId().equals(fv.getFlowName())){
						SubFlowOperator subFlowOperator = (SubFlowOperator)operator.getOperator();
						subFlowOperator.setVariableModel(model);
					}
				}
			}
		}

		try {
			ResourceManager.getInstance().updateFlow(flowInfo, workFlow);
		} catch (Exception e) {
			throw new FlowVariableException(e);
		}
	}
	
	private List<FlowVariable> getSubFlowVariables(OperatorWorkFlow workFlow){
		List<FlowVariable> subFlowModelList = new ArrayList<FlowVariable>();
		List<UIOperatorModel> operatorModelList = workFlow.getChildList();
		for(UIOperatorModel operator : operatorModelList){
			if(operator.getOperator() instanceof SubFlowOperator){
				SubFlowOperator subFlowOperator = (SubFlowOperator)operator.getOperator();
				subFlowModelList.add(new FlowVariable(subFlowOperator.getVariableModel(), operator.getId()));
			}
		}
		return subFlowModelList;
	}
	//get workflow from cache
	private OperatorWorkFlow readWorkFlow(FlowInfo flowInfo, Locale locale) throws FlowVariableException{
		try {
			return ResourceManager.getInstance().getFlowData(flowInfo, locale);
		} catch (OperationFailedException e) {
			throw new FlowVariableException(e);
		}
	}
 
}
