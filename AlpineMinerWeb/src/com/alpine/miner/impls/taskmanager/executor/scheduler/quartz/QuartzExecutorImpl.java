/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.core.jmx.JobDetailSupport;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.EverythingMatcher;

import com.alpine.miner.impls.taskmanager.ExecuteException;
import com.alpine.miner.impls.taskmanager.Task;
import com.alpine.miner.impls.taskmanager.TaskKeyInfo;
import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.taskmanager.executor.Executor;
import com.alpine.miner.impls.taskmanager.executor.InitializeException;

/**
 * @author Gary
 *
 */
public class QuartzExecutorImpl implements Executor {
	
	private final Scheduler scheduler;
	
	public QuartzExecutorImpl() throws InitializeException{
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
			this.scheduler.getListenerManager().addSchedulerListener(new RamDefaultListener());
			this.scheduler.getListenerManager().addJobListener(new JobExecuteListener(), EverythingMatcher.allJobs());
			this.scheduler.start();
		} catch (SchedulerException e) {
			LogFactory.getLog(QuartzExecutorImpl.class).error(e);
			throw new InitializeException("initial Quartz engine failed.",e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.executor.Executor#run(com.alpine.miner.impls.taskmanager.TaskTrigger)
	 */
	@Override
	public void run(TaskTrigger taskTrigger) {
		try {
			Trigger trigger = buildTrigger(taskTrigger);
			boolean isExist = scheduler.checkExists(trigger.getKey());

			if(isExist){
				JobDetail job = buildJob(taskTrigger.getTask());
				scheduler.addJob(job, true);
				scheduler.rescheduleJob(trigger.getKey(), trigger);
			}else{
				boolean jobExists;
				jobExists = scheduler.checkExists(JobKey.jobKey(taskTrigger.getTask().getName(), taskTrigger.getTask().getGroup()));
				if(!jobExists){
					JobDetail job = buildJob(taskTrigger.getTask());
					scheduler.scheduleJob(job, trigger);
				}else{
					scheduler.scheduleJob(trigger);
				}
			}
		} catch (SchedulerException e) {
			LogFactory.getLog(QuartzExecutorImpl.class).error(e);
			throw new ExecuteException(e);
		}
		
	}
	
	private Trigger buildTrigger(TaskTrigger arg){
		TriggerBuilder<?> triggerBuilder = TriggerBuilder.newTrigger()
							.forJob(JobKey.jobKey(arg.getTask().getName(), arg.getTask().getGroup()))
							.withIdentity(TriggerKey.triggerKey(arg.getName(), arg.getGroup()))
							.withSchedule(ExplanationFactory.getExplanation(arg.getRule()).buildScheduler(arg.getRule()));
		if(arg.getRule().getBeginningTime() != null){
			triggerBuilder.startAt(arg.getRule().getBeginningTime());
		}
		if(arg.getRule().getEndingTime() != null){
			triggerBuilder.endAt(arg.getRule().getEndingTime());
		}
		return triggerBuilder.build();
	}
	
	private JobDetail buildJob(Task task){
	//	task.getParams().put(JobHandler.PARAM_HANDLER, task.getTaskJob());
		
		Map<String,Object> attrMap = new HashMap<String,Object>();
		attrMap.put("name", task.getName());
		attrMap.put("group", task.getGroup());
		attrMap.put("jobClass", "com.alpine.miner.impls.taskmanager.executor.scheduler.quartz.QuartzExecutorHandler");
		Map<String,Object> jobDataMap = new HashMap<String,Object>();
		jobDataMap.put(KEY_TASK, task) ;
 
		attrMap.put("jobDataMap", jobDataMap);
		
		JobDetail job;
		try {
			job = JobDetailSupport.newJobDetail(attrMap);
		} catch (ClassNotFoundException e) {
			LogFactory.getLog(QuartzExecutorImpl.class).error(e);
			throw new ExecuteException(e);
		}
		return job;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.executor.Executor#terminate(com.alpine.miner.impls.taskmanager.TaskTrigger)
	 */
	@Override
	public void terminate(TaskTrigger trigger) {
		try {
			scheduler.unscheduleJob(TriggerKey.triggerKey(trigger.getName(), trigger.getGroup()));
		} catch (SchedulerException e) {
			LogFactory.getLog(QuartzExecutorImpl.class).error(e);
			throw new ExecuteException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.executor.Executor#shutdown(boolean)
	 */
	@Override
	public void shutdown(boolean waitForExecuting) throws ExecuteException {
		try {
			scheduler.shutdown(waitForExecuting);
		} catch (SchedulerException e) {
			throw new ExecuteException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.executor.Executor#getRunningTaskList()
	 */
	@Override
	public List<TaskKeyInfo> getRunningTaskList() {
		return JobExecuteListener.getRunningTaskList();
	}
}
