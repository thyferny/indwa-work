/**
 * 
 */
package com.alpine.miner.impls.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.miner.impls.controller.forms.FlowItemsReplicationForm;
import com.alpine.miner.impls.editworkflow.flow.ConnectionItemInfo;
import com.alpine.miner.impls.editworkflow.flow.CopyItems;
import com.alpine.miner.impls.editworkflow.flow.CopyService;
import com.alpine.miner.impls.editworkflow.operator.OperatorManagement;
import com.alpine.miner.impls.editworkflow.operator.frequentmgr.IOperatorFrequentManagement;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.web.resource.operator.OperatorPrimaryInfo;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.model.impl.UIOperatorConnectionModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;

/**
 * ClassName: OperatorManagementController.java
 * <p/>
 * Data: 2012-7-7
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
@Controller
@RequestMapping("/main/operatorManagement.do")
public class OperatorManagementController extends AbstractControler {
    private static final Logger itsLogger=Logger.getLogger(OperatorManagementController.class);

    /**
	 * @throws Exception
	 */
	public OperatorManagementController() throws Exception {
		super();
	}

	@RequestMapping(params = "method=addOperator", method = RequestMethod.POST)
	public void addOperator(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String user = getUserName(request);
        PropertyEditForm paramForm = ProtocolUtil.getRequest(request, PropertyEditForm.class);
        OperatorWorkFlow workFlow;
        UIOperatorModel newOperatorModel;
		try {
			workFlow = ResourceManager.getInstance().getFlowData(paramForm.getFlowInfo(), request.getLocale());
		} catch (OperationFailedException e) {
			 ProtocolUtil.sendResponse(response, new ErrorDTO(1, e.getMessage()));//FLOW_NOT_FOUND
			 return;
		}
		newOperatorModel = OperatorManagement.getInstance().fillOperatorToWorkflow(paramForm.getOperatorParam(), workFlow, user, request.getLocale(), paramForm.getFlowInfo());
        try {
			rmgr.updateFlow(paramForm.getFlowInfo(), workFlow);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
        IOperatorFrequentManagement.INSTANCE.increaseFrequent(newOperatorModel.getClassName(), user);//increase use frequence.
        ProtocolUtil.sendResponse(response, OperatorManagement.getInstance().buildOperatorPrimaryInfo(newOperatorModel, user));
	}

	@RequestMapping(params = "method=removeOperator", method = RequestMethod.POST)
	public void removeOperator(HttpServletRequest request, HttpServletResponse response) throws IOException{
		RemoveOperatorForm param = ProtocolUtil.getRequest(request, RemoveOperatorForm.class);
		String userName = getUserName(request);
        OperatorWorkFlow workFlow;
        List<OperatorPrimaryInfo> operatorSummarySet;
		try {
			workFlow = ResourceManager.getInstance().getFlowData(param.getFlowInfo(), request.getLocale());
		} catch (OperationFailedException e) {
			 ProtocolUtil.sendResponse(response, new ErrorDTO(1, e.getMessage()));//FLOW_NOT_FOUND
			 return;
		}
		for(String operatorUid : param.getOperatorUids()){
			OperatorManagement.getInstance().removeOperator(workFlow, operatorUid);
		}
        try {
			rmgr.updateFlow(param.getFlowInfo(), workFlow);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
        operatorSummarySet = new ArrayList<OperatorPrimaryInfo>();
        for(UIOperatorModel op : workFlow.getChildList()){
        	OperatorPrimaryInfo operatorPrimary = OperatorManagement.getInstance().buildOperatorPrimaryInfo(op, userName);
        	operatorSummarySet.add(operatorPrimary);
        }
        ProtocolUtil.sendResponse(response, operatorSummarySet);
	}

	@RequestMapping(params = "method=getOperatorFrequence", method = RequestMethod.GET)
	public void getOperatorFrequence(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String userName = getUserName(request);
		List<String> operatorFrequence = IOperatorFrequentManagement.INSTANCE.getFrequentOperatorNameList(6, userName);
        ProtocolUtil.sendResponse(response, operatorFrequence);
	}
	
    @RequestMapping(params = "method=copyOperators", method = RequestMethod.POST)
    public void copyOperators(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	String userName = getUserName(request);
    	FlowItemsReplicationForm form = ProtocolUtil.getRequest(request, FlowItemsReplicationForm.class);
    	OperatorWorkFlow workFlow,
    					 sourceWorkFlow;
    	CopyItems copyItems;
 		try {
 			workFlow = ResourceManager.getInstance().getFlowData(form.getFlowInfo(), request.getLocale());
 			if(form.getCopiedFlowInfo().getKey().equals(form.getFlowInfo().getKey())){// copy/paste in a same flow.
 	 			sourceWorkFlow = ResourceManager.getInstance().getFlowData(form.getCopiedFlowInfo(), request.getLocale());
 			}else{// copy/paste in different flow.
 				sourceWorkFlow = Persistence.INSTANCE.readWorkFlow(form.getCopiedFlowInfo(), request.getLocale());
 			}
 		} catch (OperationFailedException e) {
 			 ProtocolUtil.sendResponse(response, new ErrorDTO(1, e.getMessage()));//FLOW_NOT_FOUND
 			 return;
 		}
    	try {
    		copyItems = CopyService.getInstance().copyFlowItems(sourceWorkFlow, workFlow,
    				form.getCopyOperatorSet(), form.getOffset(),form.getFlowInfo());
		} catch (Exception e) {
			itsLogger.error(e.getMessage(), e);
			e.printStackTrace();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString("copy_paste_error_copy_err");
			super.generateErrorDTO(response, msg, request.getLocale());
			return;
		}
        try {
			rmgr.updateFlow(form.getFlowInfo(), workFlow);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
    	List<OperatorPrimaryInfo> operatorSummarySet = new ArrayList<OperatorPrimaryInfo>();
    	List<ConnectionItemInfo> connInfoSet = new ArrayList<ConnectionItemInfo>();
    	FlowItemsReplicationForm returnData = new FlowItemsReplicationForm();
    	returnData.setOperatorPrimaryInfoSet(operatorSummarySet);
    	returnData.setConnectionInfoSet(connInfoSet);
    	Set<String> operatorUidChecker = new HashSet<String>();
    	for(UIOperatorModel opModel : copyItems.getCopyOperatorSet()){
    		fillOperatorPrimaryInfo(opModel, operatorSummarySet, operatorUidChecker, userName);
    	}
    	
        for(UIOperatorConnectionModel connModel : copyItems.getCopyConnectionSet()){
        	connInfoSet.add(new ConnectionItemInfo(connModel.getSource().getUUID(), 
        											connModel.getTarget().getUUID(),
        											connModel.getSource().getPosition().getStartX(),
        											connModel.getSource().getPosition().getStartY(),
        											connModel.getTarget().getPosition().getStartX(),
        											connModel.getTarget().getPosition().getStartY()));
        };
        Collections.sort(returnData.getOperatorPrimaryInfoSet(), new Comparator<OperatorPrimaryInfo>(){
			@Override
			public int compare(OperatorPrimaryInfo o1, OperatorPrimaryInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
        });
        ProtocolUtil.sendResponse(response, JSONObject.fromObject(returnData).toString());
    }

    @RequestMapping(params = "method=renameOperator", method = RequestMethod.POST)
    public void renameOperator(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	PropertyEditForm form = ProtocolUtil.getRequest(request, PropertyEditForm.class);
    	OperatorWorkFlow workFlow;
    	try {
			workFlow = ResourceManager.getInstance().getFlowData(form.getFlowInfo(), request.getLocale());
		} catch (OperationFailedException e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
    	OperatorManagement.getInstance().renameOperator(workFlow, form.getOperatorParam().getUuid(), form.getOperatorParam().getName());
        try {
			rmgr.updateFlow(form.getFlowInfo(), workFlow);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
    	returnSuccess(response);
    }
    
    private void fillOperatorPrimaryInfo(UIOperatorModel opModel, List<OperatorPrimaryInfo> operatorSummarySet, Set<String> operatorUidChecker, String userName){
    	if(operatorUidChecker.contains(opModel.getUUID())){
    		return;
    	}
    	operatorUidChecker.add(opModel.getUUID());
    	OperatorPrimaryInfo operatorPrimary = OperatorManagement.getInstance().buildOperatorPrimaryInfo(opModel, userName);
    	operatorSummarySet.add(operatorPrimary);
    }
	
	public static class RemoveOperatorForm{
		private FlowInfo flowInfo;
		private String[] operatorUids;
		public FlowInfo getFlowInfo() {
			return flowInfo;
		}
		public String[] getOperatorUids() {
			return operatorUids;
		}
	}

    public static class PropertyEditForm{
    	private FlowInfo flowInfo;
    	private OperatorParam operatorParam;
		/**
		 * @return the flowInfo
		 */
		public FlowInfo getFlowInfo() {
			return flowInfo;
		}
		/**
		 * @return the operatorParam
		 */
		public OperatorParam getOperatorParam() {
			return operatorParam;
		}
    }
    public static class OperatorParam{
    	private boolean hasDefaultVal;

    	private int x,y;
    	
		private String 	operatorClass,
						uuid,
						name,
    					connectionName,
    					
    					//hadoop fields
    					filePath,
    					
    					//database fields
    					entityName,//table or view name
    					schemaName;

    	public String getFilePath() {
			return filePath;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the x
		 */
		public int getX() {
			return x;
		}
		/**
		 * @return the y
		 */
		public int getY() {
			return y;
		}
		/**
		 * @return the uuid
		 */
		public String getUuid() {
			return uuid;
		}
		/**
		 * @return the hasDefaultVal
		 */
		public boolean isHasDefaultVal() {
			return hasDefaultVal;
		}
		/**
		 * @return the operatorName
		 */
		public String getOperatorClass() {
			return operatorClass;
		}

		/**
		 * @return the connectionKey
		 */
		public String getConnectionName() {
			return connectionName;
		}

		/**
		 * @return the entityName
		 */
		public String getEntityName() {
			return entityName;
		}

		/**
		 * @return the schemaName
		 */
		public String getSchemaName() {
			return schemaName;
		}
    }
}
