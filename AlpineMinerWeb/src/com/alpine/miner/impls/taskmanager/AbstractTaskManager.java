/**
 * 
 */
package com.alpine.miner.impls.taskmanager;

import java.util.List;

import com.alpine.miner.impls.taskmanager.executor.Executor;
import com.alpine.miner.impls.taskmanager.executor.ExecutorFactory;

/**
 * @author Gary
 *
 */
public abstract class AbstractTaskManager implements TaskManager {
	
	private Executor handler = ExecutorFactory.getExecutor();

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#appendTrigger(com.alpine.miner.impls.taskmanager.TaskTrigger)
	 */
	@Override
	public void appendTrigger(TaskTrigger trigger) {
		if(trigger.isValid()){
			handler.run(trigger);
		}else{
			handler.terminate(trigger);
		}
		saveTrigger(trigger);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#removeTrigger(com.alpine.miner.impls.taskmanager.TaskTrigger)
	 */
	@Override
	public void removeTrigger(TaskTrigger trigger) {
		handler.terminate(trigger);
		deleteTrigger(trigger);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#startup()
	 */
	@Override
	public void startup() {
		List<TaskTrigger> triggerArray = this.loadAllOfTrigger();
		for(TaskTrigger t: triggerArray){
			if(t.isValid())
				handler.run(t);
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#termination()
	 */
	@Override
	public void termination() {
		List<TaskTrigger> triggerArray = this.loadAllOfTrigger();
		for(TaskTrigger t: triggerArray){
			handler.terminate(t);
		}
		handler.shutdown(true);
	}

	@Override
	public List<TaskKeyInfo> getRunningTaskKeys() {
		return handler.getRunningTaskList();
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#loadAllOfTrigger()
	 */
	@Override
	public abstract List<TaskTrigger> loadAllOfTrigger();

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#loadTriggerByGroup(java.lang.String)
	 */
	@Override
	public abstract List<TaskTrigger> loadTriggerByGroup(String group);

	/**
	 * persistence trigger
	 * @param trigger
	 */
	protected abstract void saveTrigger(TaskTrigger trigger);
	
	/**
	 * remove trigger
	 * @param trigger
	 */
	protected abstract void deleteTrigger(TaskTrigger trigger);

	protected final Executor getHandler() {
		return handler;
	}
	
}
