/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import java.text.MessageFormat;
import java.text.ParseException;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import com.alpine.miner.impls.taskmanager.ExecuteException;
import com.alpine.miner.impls.taskmanager.rule.ScheduleRule;
import com.alpine.miner.impls.taskmanager.rule.TimeExpression;
import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public class ScheduleExplanation implements RuleExplanation {
	
	private static final MessageFormat EXPRESSION_FORMATTER = new MessageFormat("{0} {1} {2} {3} {4} {5}");
	
	public static final String IGNORE = "?";

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.executor.quartz.explanation.RuleExplanation#buildScheduler(com.alpine.miner.impls.taskmanager.rule.TriggerRule)
	 */
	@Override
	public ScheduleBuilder<? extends Trigger> buildScheduler(TriggerRule arg) {
		ScheduleRule rule = (ScheduleRule) arg;
		try {
			return CronScheduleBuilder.cronSchedule(translateCronExpression(rule));
		} catch (ParseException e) {
			throw new ExecuteException(e);
		}
	}
	
	private String translateCronExpression(ScheduleRule rule){
		TimeExpression expression = rule.getExecuteTime();
		String[] args = {
			"0",
			"0",
			buildNormalVal(expression.getHour()),
			buildDay(expression),
			buildNormalVal(expression.getMonth()),
			buildWeek(expression)
		};
		return EXPRESSION_FORMATTER.format(args);
	}
	
	private String buildDay(TimeExpression ex){
		if(!TimeExpression.NONE.equals(ex.getDayOfWeek())){
			return IGNORE;
		}else{
			return buildNormalVal(ex.getDayOfMonth());
		}
	}
	
	private String buildWeek(TimeExpression ex){
		if(TimeExpression.NONE.equals(ex.getDayOfWeek())){
			return IGNORE;
		}else{
			return ex.getDayOfWeek();
		}
	}
	
	private String buildNormalVal(String val){
		return (val == null || "".equals(val)) ? "*" : val;
	}
}
