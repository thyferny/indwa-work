/**
 * 
 */
package com.alpine.miner.impls.taskmanager;

import java.io.Serializable;

import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public interface TaskTrigger extends Serializable{
	
	public static final int HOUR = 3600000,
							MINS = 60000;

	/**
	 * return the Trigger's name
	 * @return
	 */
	String getName();
	
	/**
	 * return the group name of trigger's belong
	 * @return
	 */
	String getGroup();
	
	/**
	 * return task handler instance
	 * @return
	 */
	Task getTask();
	
	/**
	 * return the rule of trigger
	 * @return
	 */
	TriggerRule getRule();
	
	/**
	 * true if the trigger valid
	 * @return
	 */
	boolean isValid();
	
	/**
	 * make this trigger to invalid
	 */
	void toInvalid();
}
