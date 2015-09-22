/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowVariableController
 * Apr 9, 2012
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.flowvariables.FlowVariableException;
import com.alpine.miner.impls.flowvariables.IFlowVariableService;
import com.alpine.miner.impls.flowvariables.model.FlowVariable;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Gary
 *
 */
@Controller
@RequestMapping("/main/flowVariable.do")
public class FlowVariableController extends AbstractControler {

	/**
	 * @throws Exception
	 */
	public FlowVariableController() throws Exception {
		super();
	}

	@RequestMapping(params = "method=getFlowVariables", method = RequestMethod.POST)
	public void getFlowVariables(HttpServletRequest request, HttpServletResponse response) throws IOException{
		FlowInfo flowInfo = ProtocolUtil.getRequest(request, FlowInfo.class);
		try {
			List<FlowVariable>  flowVariables = IFlowVariableService.INSTANCE.getVariableWebModelList(flowInfo, request.getLocale());
			ProtocolUtil.sendResponse(response, flowVariables);
		} catch (FlowVariableException e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}

	@RequestMapping(params = "method=saveVariable", method = RequestMethod.POST)
	public void saveVariable(HttpServletRequest request, HttpServletResponse response) throws IOException{
		FlowVariableForm variableForm = ProtocolUtil.getRequest(request, FlowVariableForm.class);
		try {
			IFlowVariableService.INSTANCE.storeFlowVariableToFlow(variableForm.getFlowVariableSet(), variableForm.getFlowInfo(), request.getLocale());
			OperatorWorkFlow workFlow = ResourceManager.getInstance().getFlowData(variableForm.getFlowInfo(), request.getLocale());
			ProtocolUtil.sendResponse(response, new FlowDTO(variableForm.getFlowInfo(), workFlow, getUserName(request)));
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}
	
	public static class FlowVariableForm{
		private FlowInfo flowInfo;
		private FlowVariable[] flowVariableSet;
		public FlowInfo getFlowInfo() {
			return flowInfo;
		}
		public void setFlowInfo(FlowInfo flowInfo) {
			this.flowInfo = flowInfo;
		}
		public FlowVariable[] getFlowVariableSet() {
			return flowVariableSet;
		}
		public void setFlowVariableSet(FlowVariable[] flowVariableSet) {
			this.flowVariableSet = flowVariableSet;
		}
	}
}
