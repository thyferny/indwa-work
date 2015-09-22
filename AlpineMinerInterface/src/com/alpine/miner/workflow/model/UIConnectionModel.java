/**
 * ClassName UIOperatorModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.model;

public interface UIConnectionModel{
 

	public UIOperatorModel getSource() ;

	public void setSource(UIOperatorModel source);

	public UIOperatorModel getTarget() ;

	public void setTarget(UIOperatorModel target) ;
	
	public void attachSource();
	public void attachTarget();
	public void detachSource();
	public void detachTarget();
	
}
