/**
* ClassName WOENode.java
*
* Version information: 1.00
*
* Data: 28 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.miner.workflow.operator.parameter.woe;


/**
 * @author Shawn
 *
 */
public abstract class WOENode  {
	protected static final String ATTR_GROUPINFO = "groupInfo";
	protected static final String ATTR_WOEVALUE = "WOEValue";
	protected String groupInfo;
	public double getWOEValue() {
		return WOEValue;
	}

	public void setWOEValue(double wOEValue) {
		WOEValue = wOEValue;
	}

	protected double WOEValue;
	public String getGroupInfo() {
		return groupInfo;
	}

	public void setGroupInfo(String groupInfo) {
		this.groupInfo = groupInfo;
	}

}
