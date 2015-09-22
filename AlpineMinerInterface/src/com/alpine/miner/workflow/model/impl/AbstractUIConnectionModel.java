/**
 * ClassName AbstractConnectionModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model.impl;

import org.apache.log4j.Logger;

import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.model.UIOperatorModel;

/**
 * 
 * @author jimmy
 *
 */
public abstract class AbstractUIConnectionModel implements UIConnectionModel{

	/**
	 * variable source and target point of operator model
	 */
	private UIOperatorModel source,target;
	private static Logger itsLogger = Logger.getLogger(AbstractUIConnectionModel.class);
	public UIOperatorModel getSource() {
		return source;
	}

	public void setSource(UIOperatorModel source) {
		this.source = source;
	}

	public UIOperatorModel getTarget() {
		return target;
	}

	public void setTarget(UIOperatorModel target) {
		this.target = target;
	}
	
	public void attachSource(){
		if(null==source){
			itsLogger.error("Source is null and not expected to be null");
			return;
		}
		if(!source.getTargetConnection().contains(this))
			source.addTargetConnection(this);
	}
	public void attachTarget(){
		if(null==target){
			itsLogger.error("Target is null and not expected to be null");
			return;
		}
		if(!target.getSourceConnection().contains(this))
			target.addSourceConnection(this);
	}
	public void detachSource(){
		if(null==source){
			itsLogger.error("Source is null and not expected to be null");
			return;
		}
		source.removeTargetConnection(this);
		 
	}
	public void detachTarget(){
		if(null==target){
			itsLogger.error("Target is null and not expected to be null");
			return;
		}
		target.removeSourceConnection(this);		 
	}
}
