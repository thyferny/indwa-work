/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowRunnerController.java
 */
package com.alpine.miner.impls.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.pig.builtin.AlpinePigConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alpine.datamining.workflow.AlpineAnalyticEngine;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.framework.DataStore;
import com.alpine.miner.framework.RequestUtil;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import com.alpine.miner.impls.web.resource.WebWorkFlowStepRunner;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.runner.FlowRunningHelper;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;

/**
 * @author Gary
 * Jan 21, 2013
 */
@Controller
@RequestMapping("/main/flowRunner.do")
public class FlowRunnerController extends FlowController{
    private static Logger itsLogger = Logger.getLogger(FlowRunnerController.class);
    //this is necessary for the client side
    private static final String STR_NULL = "null";
    private static final String COLUMN_OUTPUT = "output";
    private static final String COLUMN_ERRMESSAGE = "errMessage";
    private static final String COLUMN_LOGMESSAGE = "logmessage";
    private static final String COLUMN_UUID = "uuid";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_OPERATORNAME = "operatorname";
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private static final String ALP_EXTRA_MSG_SEP = "<br/>"; 

	/**
	 * @throws Exception
	 */
	public FlowRunnerController() throws Exception {
		super();
	}

	@RequestMapping(params = "method=runFlow", method = RequestMethod.POST)
    public void runFlow(String uuid, String user, HttpServletRequest request,
                        HttpServletResponse response, ModelMap model) throws IOException{

        if (checkUser(user, request, response) == false) {
            return  ;
        }
        
         try {
            FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
            if (info == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(9999,BAD_REQUEST));
            }

            OperatorWorkFlow flow = rmgr.getFlowData(info,request.getLocale());
            if (flow == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(FLOW_NOT_FOUND));
                return  ;
            }

            RowInfo resultInfo = initResultInfo(user,uuid,request.getLocale());
            
            String filePath = getTmpFlowPath4FlowRun(info,request.getLocale(),user);
            String flowFullName = Persistence.INSTANCE.getFlowFullName(info);
            
	        HadoopConstants.Flow_Call_Back_URL =  "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
            		
//            String callBackURL = "http://localhost:8080/AlpineMinerWeb"; 
			WebWorkFlowRunner runner = new WebWorkFlowRunner(filePath,   resultInfo, uuid,  flowFullName,request.getLocale(),info);


            Map<String, WebWorkFlowRunner> userMap = FlowRunningHelper.getInstance().getUserRunnerMap(user, true);
            userMap.put(uuid, runner);

            runner.runWorkFlow();
            DataStore ds = new DataStore();
            ds.setRow(resultInfo);
            String str = RequestUtil.toJson(ds);
            ProtocolUtil.sendResponse(response, str);

        } catch (Exception e) {

            generateErrorDTO(response, e,request.getLocale());
        }
    }

	@RequestMapping(params = "method=stopFlow", method = RequestMethod.POST)
	public void stopFlow(String uuid, String user, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException {

		if (checkUser(user, request, response) == false) {
			return;
		}

		Map<String, WebWorkFlowRunner> userMap = FlowRunningHelper.getInstance().getUserRunnerMap(user, false);
		if (userMap != null) {
			WebWorkFlowRunner runner = userMap.remove(uuid);
			// for runner
			if (runner != null) {
				try {
					runner.stopWorkFlow();

				} catch (Exception e) {
					e.printStackTrace();
					generateErrorDTO(response, e, request.getLocale());
					return;
				} 
//				finally {
//					runnerMap.remove(uuid);// wrong code, it remove nothing from runnerMap, so remove it.
//
//				}
			}
		}
		// for step run
		WebWorkFlowStepRunner stepRunner = FlowRunningHelper.STEP_RUNNER_MAP.remove(user);
		FlowRunningHelper.STEP_RUNNER_MAP_FOR_SYSTEM_UPDATE.remove(user);
		if (stepRunner != null) {
			try {
				AlpineAnalyticEngine.getInstance().stopAnalysisProcess(
						stepRunner.getProcessID());
			} catch (Exception e) {

				generateErrorDTO(response, e, request.getLocale());
			}
		}
	}

    @RequestMapping(params = "method=stepRunFlow", method = RequestMethod.POST)
    public void stepRunFlow(String uuid, String user, String operatorUUID,
                            HttpServletRequest request,
                            HttpServletResponse response, ModelMap model) throws IOException {

        if (checkUser(user, request, response) == false) {
            return  ;
        }
	        HadoopConstants.Flow_Call_Back_URL =  "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
	      
	        //delete old property result if step run
	    	if(FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.containsKey(uuid)) {
	    		FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.remove(uuid);
	    	}

	        try {
            FlowInfo info = ProtocolUtil.getRequest(request, FlowInfo.class);
            
            if (info == null) {
                ProtocolUtil.sendResponse(response, new ErrorDTO(9999,BAD_REQUEST));
                return;
            }
            OperatorWorkFlow flow = rmgr.getFlowData(info,request.getLocale());
            if (flow == null) {
                ProtocolUtil.sendResponse(response,
                        new ErrorDTO(FLOW_NOT_FOUND));
                return  ;
            }
    		
            String filePath = getTmpFlowPath4FlowRun(info,request.getLocale(),user);
            String flowFullName = Persistence.INSTANCE.getFlowFullName(info);
            RowInfo resultInfo = initResultInfo(user,uuid,request.getLocale());

            WebWorkFlowStepRunner stepRunner = FlowRunningHelper.STEP_RUNNER_MAP.get(user);
            if (stepRunner == null) {
            	stepRunner = new WebWorkFlowStepRunner(filePath,  
                        resultInfo, uuid,  flowFullName,
                        request.getLocale(),info);
            	FlowRunningHelper.STEP_RUNNER_MAP.put(user, stepRunner);
                //for systemupdate
            	FlowRunningHelper.STEP_RUNNER_MAP_FOR_SYSTEM_UPDATE.put(user,info.getId());
            }else{
            	//We have to update RowInfo to stepRuner if stepRunner already exist.
            	stepRunner.setRow(resultInfo);
            	stepRunner.setUuid(uuid);//fix Pivotal 40505523: Running log will be hang on "Analytic Flow started running......"
            }
            stepRunner.stepRunworkFlow(filePath, operatorUUID,stepRunner.getContext());
            DataStore ds = new DataStore();
            ds.setRow(resultInfo);
            String str = RequestUtil.toJson(ds);
            ProtocolUtil.sendResponse(response, str);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale()) ;
        }

    }

    // need synchronize ...
    @RequestMapping(params = "method=getMessage", method = RequestMethod.GET)
    public void getMessage(HttpServletRequest request,
                           HttpServletResponse response, String uuid, ModelMap model)
            throws IOException {
        String user = getUserName(request);
    	Map<String, RowInfo> userMap = FlowRunningHelper.RESULT_MAP.get(user);
        try {
            String str = "";
            if (userMap != null) {
                RowInfo row = userMap.get(uuid);

                if (row != null && row.getRowList() != null
                        && row.getRowList().size() > 0) {
                    // avoid the listener update the row at the same time
                    synchronized (row.getRowList()) {
                        DataStore ds = new DataStore();
                        ds.setRow(row);
                        
                        //flowRunningPropetyMap
                        LinkedHashMap<String, String> runningProperties = FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.get(uuid) ;
                        if(runningProperties!=null){
                        	 List<String[]> rows = ds.getRow().getRowList();
                 
                            for (int i = 0; i < rows.size(); i++) {
                        		 String[] oneRow = rows.get(i);
                        		if(oneRow[1].equals("process_finished") ){
                                       runningProperties = FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.get(uuid) ;
                                    Set<String> keys = runningProperties.keySet() ;
                                    for (Iterator<String> iterator = keys.iterator(); iterator
                                           .hasNext();) {
                                        String key = iterator.next();
                                           //0 means no bad data
                                        if(key.indexOf( AlpinePigConstants.COUNT_BADDATA_PIG)==0
                                                 &&"0".equals( runningProperties.get(key))==false){
                                             String opName =key.replace(AlpinePigConstants.COUNT_BADDATA_PIG, "");
                                             String [] arguments = new String[]{opName};
                                             oneRow[6] =oneRow[6] + ALP_EXTRA_MSG_SEP+" "  +   ErrorNLS.getMessage(ErrorNLS.BADDATA_COUNT_PIG, request.getLocale(),arguments);

                                        }else if(key.indexOf( HadoopConstants.COUNT_BADDATA_MR)==0
                                                 &&"0".equals( runningProperties.get(key))==false){
                                             String opName =key.replace(HadoopConstants.COUNT_BADDATA_MR, "");
                                             String [] arguments = new String[]{opName,runningProperties.get(key)};

                                             oneRow[6] =oneRow[6] +ALP_EXTRA_MSG_SEP+ " " +
                                                       ErrorNLS.getMessage(ErrorNLS.BADDATA_COUNT_MR, request.getLocale(),arguments);

                                        }else if(key.indexOf( AlpinePigConstants.BAD_COLUMN_INDEX)==0
                                                 &&runningProperties.get(key)!=null
                                                 &&"null".equals( runningProperties.get(key))==false){
                                             String opName =key.replace(AlpinePigConstants.BAD_COLUMN_INDEX, "");
                                             String badColumnIndex =runningProperties.get(key);
                                             if(badColumnIndex.endsWith(",")){
                                                 badColumnIndex=badColumnIndex.substring(0,badColumnIndex.length()-1);
                                             }
                                             badColumnIndex = badColumnIndex.replace(",",", ");
                                             String [] arguments = new String[]{badColumnIndex,opName};

                                             oneRow[6] =oneRow[6] + ALP_EXTRA_MSG_SEP+" "
                                             + ErrorNLS.getMessage(ErrorNLS.BADCOLUMN_IDX_PIG, request.getLocale(),arguments);
                                        }
                                    }
                        		}
						    }
                        }
                        
                        str = RequestUtil.toJson(ds);
                        if(isFlowFinished(row)){
                        	FlowRunningHelper.getInstance().removeResult(user,uuid);
                        	FlowRunningHelper.getInstance().removeRunner(user,uuid);
                    		if(FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.containsKey(uuid)){
                    			FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.get(uuid) ;
                            }
                    		FlowRunningHelper.STEP_RUNNER_MAP_FOR_SYSTEM_UPDATE.remove(user);
                        }
                        row.getRowList().clear();
                    }
                } else {
                    // System.out.println("No data at this time " + new Date());
                }
                ProtocolUtil.sendResponse(response, str);
            }
        } catch (IOException e) {
            itsLogger.error(e.getMessage(),e);
            FlowRunningHelper.getInstance().removeResult(user,uuid);
            FlowRunningHelper.getInstance().removeRunner(user,uuid);
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=clearStepRunResult", method = RequestMethod.GET)
    public void clearStepRunResult(String runUUid, String operatorName, HttpServletRequest request, HttpServletResponse response) throws IOException{
//      disposeStepRunner(getUserName(request));
    	WebWorkFlowStepRunner stepRunner = FlowRunningHelper.STEP_RUNNER_MAP.get(getUserName(request));
        if(stepRunner != null){
        	stepRunner.clearStepRunResult(operatorName);
        }
    	
    }

    @RequestMapping(params = "method=putFlowRunningProperty", method = RequestMethod.GET)
    public void putFlowRunningProperty(String uuid,String pKey,String pValue,  HttpServletRequest request,
            HttpServletResponse response, ModelMap model) throws IOException {
    	if(FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.containsKey(uuid)==false){
    		FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.put(uuid, new LinkedHashMap<String, String> ()) ;
    	}
    	LinkedHashMap<String, String> properties = FlowRunningHelper.FLOW_RUNNING_PROPERTY_MAP.get(uuid) ;
		pKey =URLDecoder.decode(pKey);
		//utf ?

    	if(properties.containsKey( pKey)==false){
    		properties.put(pKey, pValue) ;
    	}else{
    		if((pKey.startsWith(HadoopConstants.COUNT_BADDATA_MR)
    				||pKey.startsWith(AlpinePigConstants.COUNT_BADDATA_PIG))
    				&&properties.get(pKey)!=null){
    			Long count =Long.valueOf(properties.get(pKey)) ;
        		properties.put(pKey, String.valueOf(count+Long.parseLong(pValue))) ;

    		}else if(pKey.startsWith(AlpinePigConstants.BAD_COLUMN_INDEX)
    				&&properties.get(pKey)!=null){
    			 String[] columnList = properties.get(pKey).split(",");//1,2,3
    			 String[] pValueArray = pValue.split(",");//2,3,4
    			 List<String> resultList = new ArrayList<String> ();   
    			 for (int i = 0; i < columnList.length; i++) {
					if(StringUtil.isEmpty(columnList[i])==false &&resultList.contains(columnList[i])==false){
						resultList.add(columnList[i]) ;
					}
				}
    			 for (int i = 0; i < pValueArray.length; i++) {
    				 if(StringUtil.isEmpty(pValueArray[i])==false &&resultList.contains(pValueArray[i])==false){
 						resultList.add(pValueArray[i]) ;
 					}
				}
    		}else{
        		properties.put(pKey, pValue) ;
    		}

		}
    }    
    
    // just make runFlowFile in FlowController able to call this function.
    static RowInfo initResultInfo(String user, String uuid,Locale locale) {
        Map<String, RowInfo> userMap = FlowRunningHelper.RESULT_MAP.get(user);
        if(userMap==null){
        	FlowRunningHelper.RESULT_MAP.put(user, new HashMap<String, RowInfo>()) ;
        }
        userMap = FlowRunningHelper.RESULT_MAP.get(user);
        RowInfo row = userMap.get(uuid);
        if (row != null) {
            row.getRowList().clear();
        } else {
            row = new RowInfo();

            row.addColumn(COLUMN_OPERATORNAME);
            row.addColumn(COLUMN_MESSAGE);
            row.addColumn(COLUMN_DATE);

            // this is the flow id
            row.addColumn(COLUMN_UUID);
            row.addColumn(COLUMN_ERRMESSAGE);

            // this is possible output of this flow
            row.addColumn(COLUMN_OUTPUT);
            row.addColumn(COLUMN_LOGMESSAGE);
        }
        String[] item = new String[RESULT_COLUMN_NUMBER];
        Date date = new Date();

        item[0] = STR_NULL;
        item[1] = Resources.PROCESS_START;
        item[2] = dateFormat.format(date);
        item[3] = uuid;
        item[4] = STR_NULL;
        item[5] = STR_NULL;
        item[6] = VisualNLS.getMessage(VisualNLS.PROCESS_START,locale);
        row.addRow(item);

        userMap.put(uuid, row);

        return row;
    }

	// only for run and step run
	private String getTmpFlowPath4FlowRun(FlowInfo info, Locale locale, String userName)
			throws  Exception {
		OperatorWorkFlow workflow = ResourceManager.getInstance().getFlowData(
				info, locale);
		String tempDir = System.getProperty("java.io.tmpdir");
		if(tempDir.endsWith(File.separator)==false){
			tempDir = tempDir + File.separator;
		}
		String tmpPath = tempDir 
				+ UUID.randomUUID().toString() + info.getId() 	+ FilePersistence.AFM;
		
		XMLWorkFlowSaver workflowSaver=new XMLWorkFlowSaver() ;
		workflowSaver.doSave(tmpPath, workflow, userName, false) ;
		//pivotal 42015171: new subflow need be copied to temp folder
		copySubFlowToTempDir(tempDir,workflow,workflowSaver);
 
		return tmpPath;
	}
	
    private boolean isFlowFinished(RowInfo row) {
        List<String[]> rowList = row.getRowList();
        if(rowList!=null){
            for(int i=0;i<rowList.size();i++){
                String[] aRow = rowList.get(i);
                if(aRow[1].equals(Resources.PROCESS_STOP)
                		||aRow[1].equals(Resources.PROCESS_FINISHED)){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void copySubFlowToTempDir(String tempDir, OperatorWorkFlow workflow,XMLWorkFlowSaver workflowSaver) throws Exception {
    	if(workflow==null||workflow.getChildList()==null){
    		return;
    	}
    	for ( UIOperatorModel uiModel : workflow.getChildList()) { 
			if(uiModel.getOperator() instanceof SubFlowOperator){
				SubFlowOperator subOperator = (SubFlowOperator)uiModel.getOperator();
				OperatorWorkFlow subFlow = subOperator.getSubWorkflow();
				if(subFlow!=null){
					workflowSaver.doSave(tempDir+subFlow.getName()+FilePersistence.AFM, subFlow, false);
					//subflow's subflow
					copySubFlowToTempDir(tempDir, subFlow, workflowSaver);
				}
			} 
		}
		
	}
}
