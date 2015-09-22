/**
 * 
 */
package com.alpine.miner.impls.taskmanager.rule;

import java.io.Serializable;

/**
 * @author Gary
 *
 */
public class TimeExpression implements Serializable {
	
	private static final long serialVersionUID = 7211997122363623218L;

	public static final String NONE = "";
	
	private String frequence;

	private String  second = NONE,
					hour = NONE,
					dayOfMonth = NONE,
					dayOfWeek = NONE,
					
					month = NONE;

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getFrequence() {
		return frequence;
	}

	public void setFrequence(String frequence) {
		this.frequence = frequence;
	}
}
