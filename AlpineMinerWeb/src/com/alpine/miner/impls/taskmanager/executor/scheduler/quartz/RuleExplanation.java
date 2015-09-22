/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public interface RuleExplanation {

	/**
	 * translate TriggerRule to Scheduler of Quartz
	 * @param rule
	 * @return
	 */
	ScheduleBuilder<? extends Trigger> buildScheduler(TriggerRule rule);
}
