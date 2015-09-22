/**
 * 
 */
package com.alpine.miner.impls.taskmanager.executor.scheduler.quartz;

import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import com.alpine.miner.impls.taskmanager.rule.IntervalRule;
import com.alpine.miner.impls.taskmanager.rule.RepeatType;
import com.alpine.miner.impls.taskmanager.rule.TriggerRule;

/**
 * @author Gary
 *
 */
public class IntervalExplanation implements RuleExplanation {

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.taskmanager.executor.quartz.explanation.RuleExplanation#buildScheduler(com.alpine.miner.impls.taskmanager.rule.TriggerRule)
	 */
	@Override
	public ScheduleBuilder<? extends Trigger> buildScheduler(TriggerRule arg) {
		IntervalRule rule = (IntervalRule) arg;
		long interval = rule.getInterval();
		int repeatCount = rule.getRepeatCount() == 0 ? SimpleTrigger.REPEAT_INDEFINITELY : rule.getRepeatCount() - 1;
		SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule().withRepeatCount(repeatCount);
		setRepeat(builder, rule.getRepeatType(), interval);
//		CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
		return builder;
	}

	private void setRepeat(SimpleScheduleBuilder builder, RepeatType repeatType, long interval){
		switch(repeatType){
		case MILLISECOND:
			builder.withIntervalInMilliseconds(interval);
			break;
		case MINUTE: 
			builder.withIntervalInMinutes((int) interval);
			break;
		case HOUR:
			builder.withIntervalInHours((int) interval);
			break;
		case DAY:
			builder.withIntervalInHours((int) (interval * 24));
			break;
		case WEEK:
			builder.withIntervalInHours((int) (interval * 24 * 7));
			break;
		}
	}
}
