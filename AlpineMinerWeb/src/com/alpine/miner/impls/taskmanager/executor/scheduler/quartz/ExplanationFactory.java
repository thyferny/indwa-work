/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import com.alpine.miner.impls.taskmanager.rule.IntervalRule;
import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public class ExplanationFactory {

	private static final RuleExplanation INTERVAL = new IntervalExplanation(),
										 SCHEDULE = new ScheduleExplanation();
	
	private ExplanationFactory(){}
	
	public static RuleExplanation getExplanation(TriggerRule rule){
		if(rule instanceof IntervalRule){
			return INTERVAL;
		}else{
			return SCHEDULE;
		}
	}
}
