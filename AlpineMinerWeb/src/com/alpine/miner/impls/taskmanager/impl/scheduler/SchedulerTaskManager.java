/**
 * 
 */
package com.alpine.miner.impls.taskmanager.impl.scheduler;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.impls.taskmanager.AbstractTaskManager;
import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.taskmanager.impl.TaskConfManager;
import com.alpine.miner.impls.taskmanager.impl.TaskConfManager.TriggerReader;

/**
 * @author Gary
 *
 */
public class SchedulerTaskManager extends AbstractTaskManager {

	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#loadAllOfTrigger()
	 */
	@Override
	public List<TaskTrigger> loadAllOfTrigger() {
		final List<TaskTrigger> triggers = new ArrayList<TaskTrigger>();
		TaskConfManager.readConf(new TriggerReader(){
			@Override
			public void read(TaskTrigger trigger) {
				triggers.add(trigger);
			}
		});
		return triggers;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskManager#loadTriggerByGroup(java.lang.String)
	 */
	@Override
	public List<TaskTrigger> loadTriggerByGroup(String group) {
		final List<TaskTrigger> triggers = new ArrayList<TaskTrigger>();
		TaskConfManager.readConfByGroup(group, new TriggerReader(){
			@Override
			public void read(TaskTrigger trigger) {
				triggers.add(trigger);
			}
		});
		return triggers;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.AbstractTaskManager#deleteTrigger(com.alpine.miner.impls.taskmanager.TaskTrigger)
	 */
	@Override
	protected void deleteTrigger(TaskTrigger trigger) {
		TaskConfManager.removeConf(trigger);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.AbstractTaskManager#saveTrigger(com.alpine.miner.impls.taskmanager.TaskTrigger)
	 */
	@Override
	protected void saveTrigger(TaskTrigger trigger) {
		TaskConfManager.writeConf(trigger);
	}
}
