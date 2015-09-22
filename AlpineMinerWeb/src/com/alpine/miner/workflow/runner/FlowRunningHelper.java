/**
 * ClassName FlowRunningHelper.java
 *
 * Version information: 1.00
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.runner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import com.alpine.miner.impls.web.resource.WebWorkFlowStepRunner;

public class FlowRunningHelper {
	
	private static FlowRunningHelper INSTANCE = new FlowRunningHelper();
    // HashMap<uuid, Property>
    public static final  Map<String, LinkedHashMap<String, String>> FLOW_RUNNING_PROPERTY_MAP = new  HashMap<String, LinkedHashMap<String, String>> ();

    //HashMap<userName,HashMap<uuid, RowInfo>>
    public static final Map<String,Map<String, RowInfo>> RESULT_MAP = new HashMap<String,Map<String, RowInfo>>();
    //this is for runner : username->uuid->runner
    public static final Map<String,Map<String, WebWorkFlowRunner>> RUNNER_MAP = new HashMap<String,Map<String, WebWorkFlowRunner>>();
    //user- > WebWorkFlowRunner  :  for release  step run contex
    public static final Map<String,WebWorkFlowStepRunner> STEP_RUNNER_MAP = new HashMap<String,WebWorkFlowStepRunner>();
    //for system upate get status
    public static final Map<String,String> STEP_RUNNER_MAP_FOR_SYSTEM_UPDATE = new HashMap<String,String>();
	
	private FlowRunningHelper(){
		
	}
	
	public static FlowRunningHelper getInstance(){
		return INSTANCE;
	}
	
	public Map<String, WebWorkFlowRunner> getUserRunnerMap(String userName, boolean createIfNotExist){
		Map<String, WebWorkFlowRunner> userRunnerMap = RUNNER_MAP.get(userName);
		if(userRunnerMap == null && createIfNotExist){
			userRunnerMap = new HashMap<String, WebWorkFlowRunner>();
			RUNNER_MAP.put(userName, userRunnerMap);
		}
		return userRunnerMap;
	}

    public void disposeStepRunner(String user) {
		 WebWorkFlowStepRunner stepRunner = STEP_RUNNER_MAP.remove(user) ;
		 if(stepRunner != null){
			 stepRunner.dispose();
		 }
	}

	/**
	 * @param user
	 * @param uuid
	 */
	public void removeResult(String user, String uuid) {
		  Map<String, RowInfo> userResultMap = RESULT_MAP.get(user);
		  if(userResultMap != null&&userResultMap.containsKey(uuid)){
			  userResultMap.remove(uuid);
	      }
	}

	public void removeRunner(String user, String uuid) {
        Map<String, WebWorkFlowRunner> userRunnerMap = RUNNER_MAP.get(user);
        if(userRunnerMap != null&&userRunnerMap.containsKey(uuid)){
    	 	userRunnerMap.remove(uuid);
        }
    }

    public void setOperatorFinished( String operatorName,boolean finish,String user){
    	WebWorkFlowStepRunner stepRunner = STEP_RUNNER_MAP.get(user);
        if(stepRunner == null){
            return ;
        }
        stepRunner.setNodeFinished(operatorName,finish);
    }
	/**
	 *  @param user
	 */
	public void releaseUserMemory(String user) {
		RESULT_MAP.remove(user);
		RUNNER_MAP.remove(user);
		disposeStepRunner(user);
		
	}
}
