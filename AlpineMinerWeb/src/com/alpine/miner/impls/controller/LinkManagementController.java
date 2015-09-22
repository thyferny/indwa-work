/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LinkManagementController.java
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.editworkflow.link.LinkManagement;
import com.alpine.miner.impls.editworkflow.link.LinkModel;
import com.alpine.miner.impls.editworkflow.operator.OperatorManagement;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.web.resource.operator.OperatorPrimaryInfo;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 * Jul 12, 2012
 */
@Controller
@RequestMapping("/main/linkManagement.do")
public class LinkManagementController extends AbstractControler {

	/**
	 * @throws Exception
	 */
	public LinkManagementController() throws Exception {
		super();
	}
	
	@RequestMapping(params = "method=getAvailableSubscribers", method = RequestMethod.POST)
	public void getAvailableSubscribers(String publisherOperatorUid, HttpServletRequest request, HttpServletResponse response) throws IOException{
		FlowInfo flowInfo = ProtocolUtil.getRequest(request, FlowInfo.class);
		OperatorWorkFlow workflow;
		List<UIOperatorModel> availableOperators;
		List<String> availableOperatorUidList;
		try {
			workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale());
			availableOperators = LinkManagement.getInstance().getAvailableSubscriber(workflow, publisherOperatorUid);
		} catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			return;
		}
		availableOperatorUidList = new ArrayList<String>();
		for(UIOperatorModel opModel : availableOperators){
			availableOperatorUidList.add(opModel.getUUID());
		}
		ProtocolUtil.sendResponse(response, availableOperatorUidList);
	}

	@RequestMapping(params = "method=getAvailablePublishers", method = RequestMethod.POST)
	public void getAvailablePublishers(String subscriberOperatorUid, String originalSourceId, HttpServletRequest request, HttpServletResponse response) throws IOException{
		FlowInfo flowInfo = ProtocolUtil.getRequest(request, FlowInfo.class);
		OperatorWorkFlow workflow;
		List<UIOperatorModel> availableOperators;
		List<String> availableOperatorUidList;
		try {
			workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale());
			availableOperators = LinkManagement.getInstance().getAvailablePublisher(workflow, subscriberOperatorUid, originalSourceId);
		} catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			return;
		}
		availableOperatorUidList = new ArrayList<String>();
		for(UIOperatorModel opModel : availableOperators){
			availableOperatorUidList.add(opModel.getUUID());
		}
		ProtocolUtil.sendResponse(response, availableOperatorUidList);
	}

	@RequestMapping(params = "method=connectOperator", method = RequestMethod.POST)
	public void connectOperator(HttpServletRequest request, HttpServletResponse response) throws IOException{
		ConnectOperatorParam connectParam = ProtocolUtil.getRequest(request, ConnectOperatorParam.class);
		FlowInfo flowInfo = connectParam.getFlowInfo();
		OperatorWorkFlow workflow;
		List<OperatorPrimaryInfo> operatorInfoSet;
		String userName = getUserName(request);
		try {
			workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale());
			LinkManagement.getInstance().connectOperator(workflow, connectParam.getSourceOperatorUid(), connectParam.getTargetOperatorUid());
			rmgr.updateFlow(flowInfo, workflow);
		} catch (Exception e) {
			 ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			 return;
		}
		operatorInfoSet = new ArrayList<OperatorPrimaryInfo>();
		for(UIOperatorModel operatorModel : workflow.getChildList()){
			operatorInfoSet.add(OperatorManagement.getInstance().buildOperatorPrimaryInfo(operatorModel, userName));
		}
		ProtocolUtil.sendResponse(response, operatorInfoSet);
	}

	@RequestMapping(params = "method=reconnectOperator", method = RequestMethod.POST)
	public void reconnectOperator(HttpServletRequest request, HttpServletResponse response) throws IOException{
		ConnectOperatorParam connectParam = ProtocolUtil.getRequest(request, ConnectOperatorParam.class);
		FlowInfo flowInfo = connectParam.getFlowInfo();
		OperatorWorkFlow workflow;
		List<OperatorPrimaryInfo> operatorInfoSet;
		String userName = getUserName(request);
		try {
			workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale());
			LinkManagement.getInstance().reconnectOperator(workflow, 
									connectParam.getOriginalSourceOperatorUid(), connectParam.getOriginalTargetOperatorUid(),
									connectParam.getSourceOperatorUid(), connectParam.getTargetOperatorUid());
			rmgr.updateFlow(flowInfo, workflow);
		} catch (Exception e) {
			 ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			 return;
		}
		operatorInfoSet = new ArrayList<OperatorPrimaryInfo>();
		for(UIOperatorModel operatorModel : workflow.getChildList()){
			operatorInfoSet.add(OperatorManagement.getInstance().buildOperatorPrimaryInfo(operatorModel, userName));
		}
		ProtocolUtil.sendResponse(response, operatorInfoSet);
	}

	@RequestMapping(params = "method=deleteLink", method = RequestMethod.POST)
	public void deleteLink(HttpServletRequest request, HttpServletResponse response) throws IOException{
		ConnectOperatorParam param = ProtocolUtil.getRequest(request, ConnectOperatorParam.class);
		OperatorWorkFlow workflow;
		List<OperatorPrimaryInfo> operatorInfoSet;
		String userName = getUserName(request);
		try {
			workflow = ResourceManager.getInstance().getFlowData(param.getFlowInfo(), request.getLocale());
			LinkManagement.getInstance().removeConnection(workflow, new LinkModel(param.getSourceOperatorUid(), param.getTargetOperatorUid()));
			rmgr.updateFlow(param.getFlowInfo(), workflow);
		} catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			return;
		}
		operatorInfoSet = new ArrayList<OperatorPrimaryInfo>();
		for(UIOperatorModel operatorModel : workflow.getChildList()){
			operatorInfoSet.add(OperatorManagement.getInstance().buildOperatorPrimaryInfo(operatorModel, userName));
		}
		ProtocolUtil.sendResponse(response, operatorInfoSet);
	}

	@RequestMapping(params = "method=batchDeleteLink", method = RequestMethod.POST)
	public void batchDeleteLink(HttpServletRequest request, HttpServletResponse response) throws IOException{
		ConnectOperatorParam param = ProtocolUtil.getRequest(request, ConnectOperatorParam.class);
		OperatorWorkFlow workflow;
		List<OperatorPrimaryInfo> operatorInfoSet;
		String userName = getUserName(request);
		try {
			workflow = ResourceManager.getInstance().getFlowData(param.getFlowInfo(), request.getLocale());
			LinkManagement.getInstance().batchRemoveConnections(workflow, param.getConnectionModels());
			rmgr.updateFlow(param.getFlowInfo(), workflow);
		} catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
			return;
		}
		operatorInfoSet = new ArrayList<OperatorPrimaryInfo>();
		for(UIOperatorModel operatorModel : workflow.getChildList()){
			operatorInfoSet.add(OperatorManagement.getInstance().buildOperatorPrimaryInfo(operatorModel, userName));
		}
		ProtocolUtil.sendResponse(response, operatorInfoSet);
	}
	
	public static class ConnectOperatorParam{
		private FlowInfo flowInfo;
		
		//for single delete and reconnect
		private String 	sourceOperatorUid,
						targetOperatorUid;
		
		// for batch delete
		private LinkModel[] connectionModels;
		
		//for reconnect
		private String 	originalSourceOperatorUid,
						originalTargetOperatorUid;
		/**
		 * @return the orignalSourceOperatorUid
		 */
		public String getOriginalSourceOperatorUid() {
			return originalSourceOperatorUid;
		}
		/**
		 * @return the orignalTargetOperatorUid
		 */
		public String getOriginalTargetOperatorUid() {
			return originalTargetOperatorUid;
		}
		public FlowInfo getFlowInfo() {
			return flowInfo;
		}
		public String getSourceOperatorUid() {
			return sourceOperatorUid;
		}
		public String getTargetOperatorUid() {
			return targetOperatorUid;
		}
		public LinkModel[] getConnectionModels() {
			return connectionModels;
		}
	}
}
