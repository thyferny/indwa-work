/**
 * 

* ClassName NumericCondition.java
*
* Version information: 1.00
*
* Date: 2013-1-16
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.hadoop.tree.model;

/**
 * @author Shawn
 *
 *  
 */

public class NumericCondition extends TreeCondition{
	boolean lessthan;
	double maxConditionValue;
	double minConditionValue;
	
	public NumericCondition(double conditionValue,int conditionIndex,boolean lessthan){
		
		this.coditionColumnIndex=conditionIndex;
		this.lessthan=lessthan;
		if(this.lessthan==true)
		{
			this.maxConditionValue=conditionValue;
			this.minConditionValue=Double.NEGATIVE_INFINITY;
		}else
		{
			this.maxConditionValue=Double.POSITIVE_INFINITY;
			this.minConditionValue=conditionValue;
		}
		
		
//		this.conditionValue=conditionValue;
	}
	
	
	public boolean isMorethan() {
		return lessthan;
	}
	public void setMorethan(boolean morethan) {
		this.lessthan = morethan;
	}


	public double getMaxConditionValue() {
		return maxConditionValue;
	}


	public void setMaxConditionValue(double maxConditionValue) {
		this.maxConditionValue = maxConditionValue;
	}


	public double getMinConditionValue() {
		return minConditionValue;
	}


	public void setMinConditionValue(double minConditionValue) {
		this.minConditionValue = minConditionValue;
	}
	 
	
}
