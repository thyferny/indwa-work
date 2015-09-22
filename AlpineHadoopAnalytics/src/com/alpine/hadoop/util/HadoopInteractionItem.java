/**
 * 
 * ClassName HadoopInteractionItem.java
 *
 * Version information: 1.00
 *
 * Date: Dec 7, 2012
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.

 */
package com.alpine.hadoop.util;

/**
 * @author Peter
 * 
 */

public class HadoopInteractionItem {
	private int leftId=-1;
	private int rightId=-1;
	private String interactionType="";
	
	
	public HadoopInteractionItem() {
	}
	public HadoopInteractionItem(int leftId, int rightId,
			String interactionType) {
		this.leftId = leftId;
		this.rightId = rightId;
		this.interactionType = interactionType;
	}
	public HadoopInteractionItem(String interactionItem) {
		if (interactionItem.indexOf("*") != -1) {
			interactionType="*";
			leftId = Integer.valueOf(interactionItem
					.split("\\*")[0]);
			rightId = Integer.valueOf(interactionItem
					.split("\\*")[1]);
		} else if (interactionItem.indexOf(":") != -1) {
			interactionType=":";
			leftId = Integer.valueOf(interactionItem
					.split(":")[0]);
			rightId = Integer.valueOf(interactionItem
					.split(":")[1]);
		}
	}

	public int getLeftId() {
		return leftId;
	}
	public void setLeftId(int leftId) {
		this.leftId = leftId;
	}
	public int getRightId() {
		return rightId;
	}
	public void setRightId(int rightId) {
		this.rightId = rightId;
	}
	
	public String getInteractionType() {
		return interactionType;
	}
	public void setInteractionType(String interactionType) {
		this.interactionType = interactionType;
	}
	public double getInteractionResult(String[] columnValues) throws NumberFormatException{
		double result=Double.NaN;
		try {
			result = Double.parseDouble(columnValues[leftId])
					* Double.parseDouble(columnValues[rightId]);
		} catch (NumberFormatException e) {
			throw e;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof HadoopInteractionItem){
			return ((HadoopInteractionItem)obj).getLeftId()==leftId&&
			((HadoopInteractionItem)obj).getInteractionType().equals(interactionType)&&
			((HadoopInteractionItem)obj).getRightId()==rightId;
		}else {
			return false;
		}
	}
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(leftId).append(interactionType).append(rightId);
		return sb.toString();
	}
}
