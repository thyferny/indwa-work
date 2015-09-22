/**
 * 
 */
package com.alpine.miner.impls.taskmanager.impl;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.workflow.AnalyticEngine;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.taskmanager.SchedulerAnalyticProcessListener;
import com.alpine.miner.impls.taskmanager.TaskRuner;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;

/**
 * @author Gary
 * 
 */
public class FlowTaskRunerImpl implements TaskRuner{
    private static Logger itsLogger = Logger.getLogger(FlowTaskRunerImpl.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = 5499869394857264013L;
	// can oly run private flow now
	public static final String FLOW_NAME = "FLOW_NAME";
	public static final String USER_NAME = "USER_NAME";
	public static final String TRIGGER_NAME = "TRIGGER_NAME";;
	public static final String FLOW_VERSION = "FLOW_VERSION";
	public static final String FLOW_FILE_NAME = "FLOW_FILE_NAME";
	public static final String FLOW_FULL_NAME = "FLOW_FULL_NAME";
	
	public static final String SEPARTOR = "#";
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.impls.taskmanager.job.TaskJob#execute(java.util.Map)
	 */
	@Override
	public boolean execute(Map<String, String> params) {
		String userName =  params.get(USER_NAME);
		String triggerName = params.get(TRIGGER_NAME);
		Queue<String> flowNameQ =  asQueue(params.get(FLOW_NAME).split(SEPARTOR));
		Queue<String> flowFileVersionQ = asQueue(params.get(FLOW_VERSION).split(SEPARTOR));
		Queue<String> flowFileNameQ = asQueue(params.get(FLOW_FILE_NAME).split(SEPARTOR));
		Queue<String> flowFullNameQ = asQueue(params.get(FLOW_FULL_NAME).split(SEPARTOR));
		
		String flowName = flowNameQ.poll();
		SchedulerAnalyticProcessListener listener = new SchedulerAnalyticProcessListener(userName, flowName,triggerName);
		listener.setFlowFileNameQ(flowFileNameQ);
		listener.setFlowFileVersionQ(flowFileVersionQ);
		listener.setFlowNameQ(flowNameQ);
		listener.setFlowFullNameQ(flowFullNameQ);

		listener.setFlowFullName(flowFullNameQ.poll());
		listener.setFlowFileVersion(flowFileVersionQ.poll());
		AnalyticProcessListener[] listenerArray = {listener};
//		String filePath = FlowFileStore.getFlowPath(flowFileName);
		String tempDir=WebWorkFlowRunner.RUNTIME_DIR + File.separator + userName + File.separator + UUID.randomUUID();
		String tmpFilePath=tempDir+File.separator+UUID.randomUUID().toString()+Resources.AFM;
		listener.setFilePath(tmpFilePath);
		try {
			boolean addSuffixToOutput=Boolean.parseBoolean(ProfileReader.getInstance().getParameter(ProfileUtility.UI_ADD_PREFIX));
			WebWorkFlowRunner.copyFile(flowFileNameQ.poll(), ResourceType.Personal, userName, tmpFilePath,Locale.getDefault(),addSuffixToOutput);
			AnalyticEngine.instance.runAnalysisProcessFile(tmpFilePath, Arrays.asList(listenerArray),false,Locale.getDefault(),userName);
		} catch (Exception e) {
			itsLogger.error("The " + flowName + " of " + userName + " has a error when startup.Detail is: " + e.getMessage());
			return false;
		}
		return true;

	}
	
	private Queue<String> asQueue(String[] array){
		LinkedList<String> q = new LinkedList<String>();
		q.addAll(Arrays.asList(array));
		return q;
	}
}
