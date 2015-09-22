/**
 * 

* ClassName CategoriCondition.java
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

public class CategoriCondition extends TreeCondition{
	String conditionValue;

	public CategoriCondition(String conditionValue,int conditionIndex){
		this.conditionValue=conditionValue;
		this.coditionColumnIndex=conditionIndex;
	}
	
	public String getConditionValue() {
		return conditionValue;
	}

	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}
	
}
