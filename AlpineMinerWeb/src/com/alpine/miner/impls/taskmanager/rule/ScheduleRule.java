/**
 * 
 */
package com.alpine.miner.impls.taskmanager.rule;

import java.util.Date;

/**
 * @author Gary
 *
 */
public class ScheduleRule extends TriggerRule{
	private static final long serialVersionUID = -648599696125332944L;

	private TimeExpression executeTime;
	
	/**
	 * 
	 * @param beginning execute immediate, if beginning is null.
	 * @param ending	execute until terminate by manual if null
	 * @param executeTime	execute time. the year column always be ignore
	 */
	public ScheduleRule(Date beginning,TimeExpression executeTime){
		super(beginning);
		this.executeTime = executeTime;
	}

	public TimeExpression getExecuteTime() {
		return executeTime;
	}
}
