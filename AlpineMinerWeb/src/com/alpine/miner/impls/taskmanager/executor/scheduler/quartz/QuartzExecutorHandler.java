/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alpine.miner.impls.taskmanager.Task;
import com.alpine.miner.impls.taskmanager.executor.Executor;

/**
 * @author Gary
 *
 */
public class QuartzExecutorHandler implements Job {
	
 

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		  Task task = (Task) context.getMergedJobDataMap().get(Executor.KEY_TASK);
	//	context.getMergedJobDataMap().remove(PARAM_HANDLER);
//		TaskJob target;
//		try {
//			target = (TaskJob) Class.forName(clazzName).newInstance();
//		} catch (Exception e) {
//			LogFactory.getLog(JobHandler.class).error(e);
//			throw new ExecuteException(e);
//		}
		  task.getParams().put("TRIGGER_NAME", context.getTrigger().getKey().getName());
		  task.run();
		
	}

}
