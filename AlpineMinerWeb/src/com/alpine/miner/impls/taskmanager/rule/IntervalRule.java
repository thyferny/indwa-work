/**
 * 
 */
package com.alpine.miner.impls.taskmanager.rule;

import java.util.Date;

/**
 * @author Gary
 *
 */
public class IntervalRule extends TriggerRule{
	private static final long serialVersionUID = -4700077646050235685L;

	private int repeatCount;
	
	private long interval;
	
	private RepeatType repeatType;
	
	/**
	 * 
	 * @param beginning
	 * @param repeatCount
	 * @param interval the calendar must be clear first.
	 */
	public IntervalRule(Date beginning,int repeatCount,long interval){
		this(beginning, repeatCount, interval, RepeatType.MILLISECOND);
	}
	
	public IntervalRule(Date beginning,int repeatCount,long interval, RepeatType repeatType){
		super(beginning);
		this.repeatCount = repeatCount;
		this.interval = interval;
		this.repeatType = repeatType;
	}

	public long getInterval() {
		return interval;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public RepeatType getRepeatType() {
		return repeatType == null ? RepeatType.MILLISECOND : repeatType;
	}
	
}
