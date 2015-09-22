/**
 * 
 */
package com.alpine.miner.impls.taskmanager.rule;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Gary
 *
 */
public abstract class TriggerRule implements Serializable {
	private static final long serialVersionUID = -3524759411393487106L;
	
	private Date beginningTime;
	private Date endingTime;
	
	public TriggerRule(Date beginningTime){
		this.beginningTime = beginningTime;
	}

	public final Date getBeginningTime() {
		return beginningTime;
	}

	public Date getEndingTime() {
		return endingTime;
	}

	public void setEndingTime(Date endingTime) {
		this.endingTime = endingTime;
	}

}
