/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor;

import java.util.List;

import com.alpine.miner.impls.taskmanager.ExecuteException;
import com.alpine.miner.impls.taskmanager.TaskKeyInfo;
import com.alpine.miner.impls.taskmanager.TaskTrigger;


/**
 * @author Gary
 *
 */
public interface Executor {
	public static final String KEY_TASK = "task";
	/**
	 * register and run trigger
	 * @param trigger
	 */
	void run(TaskTrigger trigger);
	
	/**
	 * terminate and stop trigger
	 * @param trigger
	 */
	void terminate(TaskTrigger trigger);
	
	/**
	 * shutdown scheduler 
	 * @param waitForExecuting	wait for executing complete if true.
	 */
	void shutdown(boolean waitForExecuting) throws ExecuteException;
	
	/**
	 * return all of running task key info.
	 * @return
	 */
	List<TaskKeyInfo> getRunningTaskList();
}
