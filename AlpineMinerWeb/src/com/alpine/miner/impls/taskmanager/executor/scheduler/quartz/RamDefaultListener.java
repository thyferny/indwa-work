/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.listeners.SchedulerListenerSupport;

import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.taskmanager.impl.TaskConfManager;
import com.alpine.miner.impls.taskmanager.impl.TaskConfManager.TriggerReader;

/**
 * Listener for RAM scheduler
 * @author Gary
 *
 */
public class RamDefaultListener extends SchedulerListenerSupport {

	@Override
	public void triggerFinalized(Trigger trigger) {
		final TriggerKey key = trigger.getKey();
		TaskConfManager.readConfByGroup(key.getGroup(), new TriggerReader(){
			@Override
			public void read(TaskTrigger trigger) {
				if(trigger.getName().equals(key.getName())){
					trigger.toInvalid();
					TaskConfManager.writeConf(trigger);
				}
			}
		});
	}
}
