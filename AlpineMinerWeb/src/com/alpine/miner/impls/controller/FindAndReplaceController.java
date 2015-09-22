/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FindAndReplaceController.java
 */
package com.alpine.miner.impls.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence;
import com.alpine.miner.impls.categorymanager.model.FlowBasisInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.FindAndReplaceParamSearchObj;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ReplaceObj;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.util.ParameterSearchItem;
import com.alpine.miner.util.SearchReplaceUtil;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;

/**
 * @author Will
 * Jan 21, 2013
 */
@Controller
@RequestMapping("/main/findAndReplace.do")
public class FindAndReplaceController extends AbstractControler {

	/**
	 * @throws Exception
	 */
	public FindAndReplaceController() throws Exception {
		super();
	}

    @RequestMapping(params = "method=getFindAndReplaceQueryResult", method = RequestMethod.POST)
    public void getFindAndReplaceQueryResult(HttpServletRequest request,
                                             HttpServletResponse response, ModelMap model) throws IOException{
        String userName = getUserName(request);
        FindAndReplaceParamSearchObj searchObj = ProtocolUtil.getRequest(request, FindAndReplaceParamSearchObj.class);
        try {
            String scope = searchObj.getSearchScope();
            List<ParameterSearchItem> searchItems = new ArrayList<ParameterSearchItem>();
            if("all".equalsIgnoreCase(scope)){
                List<FlowBasisInfo> allFlowList = IFlowCategoryPersistence.INSTANCE.getAllFlowInfo(userName);

                FlowInfo currentOpenflowInfo = searchObj.getFlowInfo();
                if(null!=allFlowList){
                    for (Iterator<FlowBasisInfo> iterator = allFlowList.iterator(); iterator
                            .hasNext();) {
                        FlowBasisInfo flowBasisInfo = iterator
                                .next();
                        try {
                            FlowInfo flowInfo = flowBasisInfo.getInfo();
                            List<ParameterSearchItem> tempSearchItems = null;
                            if(null!=flowInfo && (currentOpenflowInfo==null || flowInfo.getId().equals(currentOpenflowInfo.getId())==false)){
                                String flowPath = Persistence.INSTANCE.generateResourceKey(flowInfo)+Resources.AFM;
                                String flowNameWithPath = getFloaNameWithPath(flowInfo, userName);
                                tempSearchItems = SearchReplaceUtil.searchInFlow(searchObj.getParamterName(), searchObj.getParameterValue(), flowNameWithPath,flowPath, searchObj.isIgnoreCase());

                            }else if(currentOpenflowInfo!=null && flowInfo.getId().equals(currentOpenflowInfo.getId())==true){
                                String flowNameWithPath = getFloaNameWithPath(currentOpenflowInfo, userName);
                                OperatorWorkFlow workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale());
                                searchItems = SearchReplaceUtil.searchInOperators(searchObj.getParamterName(), searchObj.getParameterValue(),flowNameWithPath,workflow.getChildList(), searchObj.isIgnoreCase());

                            }

                            if(null!=tempSearchItems){
                                searchItems.addAll(tempSearchItems);
                            }
                        } catch (Exception e) {

                        }
                    }

                }

            }else{//search current flow
                FlowInfo flowInfo = searchObj.getFlowInfo();
                String flowNameWithPath = getFloaNameWithPath(flowInfo, userName);
                OperatorWorkFlow workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale());
                searchItems = SearchReplaceUtil.searchInOperators(searchObj.getParamterName(), searchObj.getParameterValue(),flowNameWithPath,workflow.getChildList(), searchObj.isIgnoreCase());
            }

            ProtocolUtil.sendResponse(response, searchItems);
        } catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale());
        }
    }
    /*
      * return string flowname with username and category name.
      * */
    private String getFloaNameWithPath(FlowInfo flowInfo,String userName){
        String[] categories = flowInfo.getCategories();
        String flowNameWithPath = "";
        if(null!=categories && categories.length==1){
            flowNameWithPath = categories[0]+File.separator+flowInfo.getId();
        }else{
            flowNameWithPath = userName+File.separator+flowInfo.getId();
        }
        return flowNameWithPath;
    }
    private boolean isSelectFlowPath(List<ReplaceObj> replaceList,
                                     String flowNameWithPath, HashMap<String, String> replaceMap){
        boolean rtnValue= false;
        if(null!=flowNameWithPath && null!=replaceList && replaceList.size()>0){
            for (Iterator<ReplaceObj> iterator = replaceList.iterator(); iterator.hasNext();) {
                ReplaceObj replaceObj = iterator.next();
                if(null!=replaceObj && null!=replaceObj.getFlowPath()){
                    if(flowNameWithPath.trim().equals(replaceObj.getFlowPath().trim())){
                        String key = replaceObj.getOperName();
                        String value = replaceObj.getParameterName();
                        replaceMap.put(key, value);
                    }
                }

            }
        }
        if(null!=replaceMap && replaceMap.size()>0){
            rtnValue = true;
        }
        return rtnValue;
    }
    @RequestMapping(params = "method=replaceFindAndReplaceQueryResult", method = RequestMethod.POST)
    public void replaceFindAndReplaceQueryResult(HttpServletRequest request,
                                                 HttpServletResponse response, ModelMap model)throws IOException{
        FindAndReplaceParamSearchObj replaceObjs = ProtocolUtil.getRequest(request, FindAndReplaceParamSearchObj.class);
        String userName = getUserName(request);
        List<ReplaceObj> replaceList = replaceObjs.getReplaceList();
        //Set<String> replacePathSet = replacePathMap.keySet();
        if(null!=replaceObjs){
            try {
                String replaceScope = replaceObjs.getSearchScope();
                if(null!=replaceScope && "all".equalsIgnoreCase(replaceScope)){
                    List<FlowBasisInfo> allFlowList = IFlowCategoryPersistence.INSTANCE.getAllFlowInfo(userName);

                    FlowInfo currentOpenflowInfo = replaceObjs.getFlowInfo();
                    if(null!=allFlowList){
                        int num = 0;
                        for (Iterator<FlowBasisInfo> iterator = allFlowList.iterator(); iterator
                                .hasNext();) {
                            FlowBasisInfo flowBasisInfo = iterator
                                    .next();
                            try {

                                FlowInfo flowInfo = flowBasisInfo.getInfo();
                                if(null!=flowInfo && (currentOpenflowInfo==null || flowInfo.getId().equals(currentOpenflowInfo.getId())==false)){
                                    String flowPath = Persistence.INSTANCE.generateResourceKey(flowInfo)+Resources.AFM;
                                    //decide which flow to replace
                                    String flowNameWithPath = getFloaNameWithPath(flowInfo,userName);
                                    boolean canReplace = false;
                                    HashMap<String, String> replaceMap = new HashMap<String, String>();
                                    canReplace = isSelectFlowPath(replaceList,flowNameWithPath,replaceMap);
                                    if(canReplace==true){
                                        num =num + SearchReplaceUtil.replaceParameterValue(flowPath, replaceMap, replaceObjs.getReplaceValue());
                                    }

                                }else if(currentOpenflowInfo!=null && flowInfo.getId().equals(currentOpenflowInfo.getId())==true){
                                 
                                    String flowNameWithPath = getFloaNameWithPath(currentOpenflowInfo,userName);
                                    boolean canReplace = false;
                                    HashMap<String, String> replaceMap = new HashMap<String, String>();
                                    canReplace = isSelectFlowPath(replaceList,flowNameWithPath,replaceMap);
                                    if(canReplace==true){
                                       //OperatorWorkFlow workflow = ResourceManager.getInstance().getFlowData(currentOpenflowInfo, request.getLocale()) ;
                                       OperatorWorkFlow workflow = rmgr.getFlowData(currentOpenflowInfo,request.getLocale());
                                       num =num + SearchReplaceUtil.replaceParameterValue(workflow.getChildList(), replaceMap, replaceObjs.getReplaceValue());
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }
                        ProtocolUtil.sendResponse(response, "{'replaceNum':"+num+",'message':'"+VisualNLS.getMessage(VisualNLS.REPLACE_SUCCESS, request.getLocale())+"<br />"+VisualNLS.getMessage(VisualNLS.REPLACE_OPERATOR_NUMBER, request.getLocale())+num+"'}");
                    }

                }else{//replace current 
                    FlowInfo flowInfo  = replaceObjs.getFlowInfo();
                    HashMap<String, String> replaceMap = new HashMap<String, String>();
                    if(null!=replaceList){
                        for (Iterator<ReplaceObj> iterator = replaceList.iterator(); iterator
                                .hasNext();) {
                            ReplaceObj replaceObj = iterator
                                    .next();
                            replaceMap.put(replaceObj.getOperName(), replaceObj.getParameterName());
                        }
                    }
                    OperatorWorkFlow workflow = ResourceManager.getInstance().getFlowData(flowInfo, request.getLocale()) ;
                    int num = SearchReplaceUtil.replaceParameterValue(workflow.getChildList(), replaceMap, replaceObjs.getReplaceValue());
                    try {
            			rmgr.updateFlow(flowInfo, workflow);
            		} catch (Exception e) {
            			generateErrorDTO(response, e, request.getLocale());
            			return;
            		}
                    ProtocolUtil.sendResponse(response, "{'replaceNum':"+num+",'message':'"+VisualNLS.getMessage(VisualNLS.REPLACE_SUCCESS, request.getLocale())+"<br />"+VisualNLS.getMessage(VisualNLS.REPLACE_OPERATOR_NUMBER, request.getLocale())+num+"'}");
                }
            } catch (Exception e) {
                generateErrorDTO(response, e,request.getLocale());
            }

        }
    }
    
    @RequestMapping(params = "method=validateReplaceValueOperator", method = RequestMethod.POST)
    public void validateReplaceValueOperator(HttpServletRequest request,
                                             HttpServletResponse response, ModelMap model)throws IOException{
        FlowInfo flowInfo =  ProtocolUtil.getRequest(request, FlowInfo.class);
        OperatorWorkFlow workFlow = null;
        try {
            workFlow = ResourceManager.getInstance().getFlowData(flowInfo,request.getLocale());
            Map<String, Boolean> operatorValidMap = new HashMap<String, Boolean>();
            for(UIOperatorModel op : workFlow.getChildList()){
                boolean isValid = op.getOperator().isVaild(workFlow.getVariableModelList().get(0));
                operatorValidMap.put(op.getUUID(), isValid);
            }
            ProtocolUtil.sendResponse(response, operatorValidMap);
        } catch (OperationFailedException e) {
            generateErrorDTO(response, e,request.getLocale());
        }
    }
}
