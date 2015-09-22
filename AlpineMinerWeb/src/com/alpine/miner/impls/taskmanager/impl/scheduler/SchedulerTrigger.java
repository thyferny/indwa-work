/**
 * 
 */
package com.alpine.miner.impls.taskmanager.impl.scheduler;

import com.alpine.miner.impls.taskmanager.Task;
import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public class SchedulerTrigger implements TaskTrigger {
	private static final long serialVersionUID = -5665372488715639933L;

	private boolean isValid = true;
	
	private String group,
					name;
	private Task handler;
	private TriggerRule rule;
	
	public SchedulerTrigger(String group,String name,Task handler,TriggerRule rule, boolean isValid){
		this.group = group;
		this.name = name;
		this.handler = handler;
		this.rule = rule;
		this.isValid = isValid;
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskTrigger#getGroup()
	 */
	@Override
	public String getGroup() {
		return group;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskTrigger#getHandler()
	 */
	@Override
	public Task getTask() {
		return handler;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskTrigger#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.TaskTrigger#getRule()
	 */
	@Override
	public TriggerRule getRule() {
		return rule;
	}

	public boolean isValid() {
		return isValid;
	}

	public void toInvalid() {
		this.isValid = false;
	}

}
