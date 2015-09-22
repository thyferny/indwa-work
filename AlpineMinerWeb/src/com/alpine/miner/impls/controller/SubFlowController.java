/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * SubFlowController.java
 */
package com.alpine.miner.impls.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.datamining.workflow.util.WorkFlowUtil;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.utility.file.StringUtil;

/**
 * @author Gary
 * Jan 21, 2013
 */
@Controller
@RequestMapping("/main/operator/subflowHandle.do")
public class SubFlowController extends AbstractControler {

	/**
	 * @throws Exception
	 */
	public SubFlowController() throws Exception {
		super();
	}

    @RequestMapping(params = "method=getSubFlowExitOperatorInfo", method = RequestMethod.POST)
    public void getSubFlowExitOperatorInfo(String subflowRealName,String newSubflowName,String parentFlowName,String subflowOperatorUUID,HttpServletRequest request,
                                           HttpServletResponse response, ModelMap model) throws IOException {
        String userName = getUserName(request);
        FlowInfo currentFlowInfo = ProtocolUtil.getRequest(request,FlowInfo.class);
        String parentFlowTempPath = "";
        String subflowTempPath = "";
        if(null!=currentFlowInfo){
            parentFlowTempPath = getFlowPath(currentFlowInfo);
            int i = parentFlowTempPath.lastIndexOf(File.separator);
            if(i!=-1){
                subflowTempPath = parentFlowTempPath.substring(0, i)+File.separator+subflowRealName+Resources.AFM;
            }
        }
        try {
            String subflowFilePath = IFlowCategoryPersistence.INSTANCE.getFlowAbsolutelyPath(newSubflowName);
//            String parentFlowPath = IFlowCategoryPersistence.INSTANCE.getFlowAbsolutelyPath(parentFlowName);

            OperatorWorkFlow workFlow = null;
            VariableModel subFlowVariableModel  =null;
            Map<String,Object> returnMap = new HashMap<String, Object>();

            if(new File(subflowFilePath).exists()==true){
                if(new File(subflowTempPath).exists()==false){
                    FileUtils.copyFile(new File(subflowFilePath), new File(subflowTempPath));
                }

                Set<String>  readedSubFlowNames = new HashSet<String>();
                workFlow = rmgr.getFlowData(currentFlowInfo, request.getLocale());//WorkFlowUtil.getWorkflow(parentFlowTempPath+Resources.AFM, request.getLocale(), ResourceType.Personal, userName);
                if(workFlow!=null){
                    try {
                        List<UIOperatorModel> list = workFlow.getChildList();
                        for (Iterator<UIOperatorModel> iterator = list.iterator(); iterator.hasNext();) {
                            UIOperatorModel uiOperatorModel = iterator
                                    .next();
                            if(uiOperatorModel.getUUID().equals(subflowOperatorUUID)){
                                String oldValue = (String)ParameterUtility.getParameterValue(uiOperatorModel.getOperator(),OperatorParameter.NAME_subflowPath);
                                //avoid the init onchange
                                OperatorWorkFlow subFlowWorkflow = recursiveReadWithSubFlow((SubFlowOperator) uiOperatorModel.getOperator(), subflowTempPath, readedSubFlowNames, currentFlowInfo.getId(),currentFlowInfo);
                                ((SubFlowOperator) uiOperatorModel.getOperator()).setSubWorkflow(subFlowWorkflow) ;
                                if(subflowRealName.equals(oldValue)==false){
                                    ((SubFlowOperator) uiOperatorModel.getOperator()).setVariableModel(subFlowWorkflow.getVariableModelList().get(0)) ;
                                }
                                subFlowVariableModel = ((SubFlowOperator) uiOperatorModel.getOperator()).getVariableModel();

                            }
                        }
                       /*
                       if(null!=list && list.size()==0 && workFlow.getVariableModelList().size()==1){
                            subFlowVariableModel = workFlow.getVariableModelList().get(0);
                        }
                        */
                    } catch (Exception e) {
                        if(null!=e.getMessage() && e.getMessage().indexOf("LanguagePack.Recursive_subflow_found")!=-1){
                            returnMap.put("errorId", e.getMessage());
                            returnMap.put("errorMessage", ErrorNLS.getMessage(ErrorNLS.SUBFLOW_CYCLE,request.getLocale()));
                        }else{
                            generateErrorDTO(response, e,request.getLocale());
                            return;
                        }
                    }
                }
            }

            Map<String,String> exitOperatorMap = WorkFlowUtil.getLeafOperatorNameIDMap(subflowFilePath, request.getLocale(), com.alpine.miner.impls.resource.ResourceInfo.ResourceType.Personal, userName);

            //tempflow variable
            //OperatorWorkFlow selectedSubFlow = WorkFlowUtil.getWorkflow(subflowTempPath, request.getLocale(), ResourceType.Personal, userName);

            Map<String,String> subFlowVariableMap = null;


            if(subFlowVariableModel!=null){
                Iterator<Entry<String,String>> itor = subFlowVariableModel.getIterator();
                if(null!=itor){
                    subFlowVariableMap = new HashMap<String, String>();
                    while (itor.hasNext()) {
                        Entry<String,String> entry =  itor.next();
                        subFlowVariableMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }



            returnMap.put("exitOperatorMap", exitOperatorMap);
            returnMap.put("subFlowVariableMap", subFlowVariableMap);

            ProtocolUtil.sendResponse(response, returnMap);
        } catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale());
        }

    }

	@RequestMapping(params = "method=getSubFlowTablesInfo", method = RequestMethod.GET)
    public void getSubFlowTablesInfo(String flowBasisKey,HttpServletRequest request,
                                     HttpServletResponse response, ModelMap model) throws IOException {
        String userName = getUserName(request);
        try {
            String filePath = IFlowCategoryPersistence.INSTANCE.getFlowAbsolutelyPath(flowBasisKey);
            List<OperatorInputTableInfo> list= WorkFlowUtil.getSubFlowInputTableSets(filePath, request.getLocale(), com.alpine.miner.impls.resource.ResourceInfo.ResourceType.Personal, userName);
            ProtocolUtil.sendResponse(response, list);
        } catch (Exception e) {
            generateErrorDTO(response, e,request.getLocale());
        }

    }
	
    @RequestMapping(params = "method=getSubFlowInfo", method = RequestMethod.GET)
    public void getSubFlowInfo(String flowCategory,String subflowPathValue,HttpServletRequest request,HttpServletResponse response, ModelMap model)throws IOException{
        //param current flow-category subflowname
        //if category==null then category =  getUserName(request);
        String userName = getUserName(request);

        List<FlowInfo> flowInfoList = FilePersistence.INSTANCE.loadFlowInfo(flowCategory);
        if(null!=flowInfoList){
            FlowInfo flowInfo =null;
            for (Iterator iterator = flowInfoList.iterator(); iterator
                    .hasNext();) {
                flowInfo = (FlowInfo) iterator.next();
                if(subflowPathValue.equals(flowInfo.getId())==true){
                    break;
                }

            }
            ProtocolUtil.sendResponse(response, flowInfo);
        }
    }
	
	//for subflow and search/replace is ok
    private String getFlowPath(FlowInfo info) {
    	return FilePersistence.INSTANCE.generateResourceKey(info);
	}

    private OperatorWorkFlow recursiveReadWithSubFlow(SubFlowOperator startOperator, String subFlowRealPath,
                                                      Set<String>  readedSubFlowNames, String flowName,FlowInfo flowInfo)
            throws Exception {
        readedSubFlowNames.add(flowName);
        OperatorWorkFlow subflowWorkFlow = WorkFlowUtil.getWorkflow( subFlowRealPath, Locale.getDefault(),ResourceType.Personal, System.getProperty("user.name"));

        List<SubFlowOperator> subFlowOperators =WorkFlowUtil.findSubflowOperators(subflowWorkFlow);
        if(subFlowOperators!=null&&subFlowOperators.size()>0){
            for(int j = 0 ; j<subFlowOperators.size();j++){
                SubFlowOperator subFlowOperator =  subFlowOperators.get(j);
                String subFlowName = (String) ParameterUtility.getParameterValue(subFlowOperator, OperatorParameter.NAME_subflowPath);
                if(readedSubFlowNames.contains(subFlowName)==true){
                    throw new Exception("LanguagePack.Recursive_subflow_found"+subFlowName);
                }
                if(subFlowName!=null){
                    //String realPath=IFlowCategoryPersistence.INSTANCE.getFlowAbsolutelyPath(subFlowName); //getSubFlowRealPath(subFlowName);
                    flowInfo.setId(subFlowName);
                    String realPath = Persistence.INSTANCE.generateResourceKey(flowInfo)+Resources.AFM;
                    boolean isExistsFlowFile = new File(realPath).exists();
                    if((StringUtil.isEmpty(realPath)==false) && isExistsFlowFile==true){
                        OperatorWorkFlow subSubFlow = recursiveReadWithSubFlow( subFlowOperator,realPath,readedSubFlowNames,subFlowName,flowInfo);
                        subFlowOperator.setWorkflow(subSubFlow);

                        //not found remove the name
                        readedSubFlowNames.remove(subFlowOperator.getWorkflow().getName()) ;
//			      if(subSubFlow!=null){
//			       subFlowOperator.setSubFlowModels(subSubFlow.getChildList());
//			       
//			      }
                    }
                }
            }
        }
        readedSubFlowNames.remove(startOperator.getWorkflow().getName()) ;
        return subflowWorkFlow;
    }
}
