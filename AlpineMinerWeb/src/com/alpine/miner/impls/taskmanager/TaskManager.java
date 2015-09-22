/**
 * 
 */
package com.alpine.miner.impls.taskmanager;

import java.util.List;

/**
 * @author Gary
 *
 */
public interface TaskManager {

	/**
	 * initialize and run the task manager
	 */
	void startup();
	
	/**
	 * destroy all of task instance and terminate all of trigger
	 */
	void termination();
	
	/**
	 * add trigger to store, update if the name of trigger is exist.
	 * @param trigger
	 */
	void appendTrigger(TaskTrigger trigger);
	
	/**
	 * terminate and remove the trigger from store
	 * @param trigger
	 */
	void removeTrigger(TaskTrigger trigger);
	
	List<TaskTrigger> loadTriggerByGroup(String group);
	
	List<TaskTrigger> loadAllOfTrigger();
	
	List<TaskKeyInfo> getRunningTaskKeys();
}
