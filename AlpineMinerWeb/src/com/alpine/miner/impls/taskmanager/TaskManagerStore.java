/**
 * 
 */
package com.alpine.miner.impls.taskmanager;

import com.alpine.miner.impls.taskmanager.impl.scheduler.SchedulerTaskManager;

/**
 * @author Gary
 *
 */
public enum TaskManagerStore {
	
	SCHEDULER(new SchedulerTaskManager());

	private TaskManager instance;
	
	private TaskManagerStore(TaskManager instance){
		this.instance = instance;
	}
	
	public TaskManager getInstance(){
		return instance;
	}
}
