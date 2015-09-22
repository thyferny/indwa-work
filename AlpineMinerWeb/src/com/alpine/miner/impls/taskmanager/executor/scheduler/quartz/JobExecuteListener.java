/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * JobExecuteListener.java
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

import com.alpine.miner.impls.taskmanager.TaskKeyInfo;

/**
 * @author Gary
 * Dec 17, 2012
 */
public class JobExecuteListener implements JobListener {

	private static Map<String, JobKey> runningTaskSet = new HashMap<String, JobKey>();


	public static List<TaskKeyInfo> getRunningTaskList(){
		List<TaskKeyInfo> result = new ArrayList<TaskKeyInfo>();
		for(JobKey jobKey : runningTaskSet.values()){
			if(!jobKey.getGroup().equals("sys_temp_group")){//exclude clean temp file task.
				result.add(new TaskKeyInfo(jobKey.getGroup(), jobKey.getName()));
			}
		}
		return result;
	}
	
	public static void completeTask(String userName, String taskName){
		runningTaskSet.remove(userName + "." + taskName);
	}

	/* (non-Javadoc)
	 * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
	 */
	@Override
	public void jobExecutionVetoed(JobExecutionContext arg0) {
		
	}

	/* (non-Javadoc)
	 * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
	 */
	@Override
	public void jobToBeExecuted(JobExecutionContext arg0) {
		
	}

	/* (non-Javadoc)
	 * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
	 */
	@Override
	public void jobWasExecuted(JobExecutionContext context,
			JobExecutionException arg1) {
		JobKey key = context.getJobDetail().getKey();
		runningTaskSet.put(key.toString(), key);
	}

	/* (non-Javadoc)
	 * @see org.quartz.JobListener#getName()
	 */
	@Override
	public String getName() {
		return "JobExecuteListener";
	}
}
