/**
 * ClassName AnalysisInterActionItem.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-23
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.regressions;


/**
 * @author Eason
 *
 */
public class AnalysisInterActionItem {
	
	private String id="";
	private String firstColumn="";
	private String secondColumn="";
	private String interactionType="";
	
	
	public AnalysisInterActionItem() {
	}
	public AnalysisInterActionItem(String id, String firstColumn, String secondColumn,
			String interactionType) {
		this.id = id;
		this.firstColumn = firstColumn;
		this.secondColumn = secondColumn;
		this.interactionType = interactionType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFirstColumn() {
		return firstColumn;
	}
	public void setFirstColumn(String firstColumn) {
		this.firstColumn = firstColumn;
	}
	public String getSecondColumn() {
		return secondColumn;
	}
	public void setSecondColumn(String secondColumn) {
		this.secondColumn = secondColumn;
	}
	public String getInteractionType() {
		return interactionType;
	}
	public void setInteractionType(String interactionType) {
		this.interactionType = interactionType;
	}
	

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisInterActionItem){
			return ((AnalysisInterActionItem)obj).getFirstColumn().equals(firstColumn)&&
			((AnalysisInterActionItem)obj).getId().equals(id)&&
			((AnalysisInterActionItem)obj).getInteractionType().equals(interactionType)&&
			((AnalysisInterActionItem)obj).getSecondColumn().equals(secondColumn);
		}else {
			return false;
		}
	}
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(firstColumn).append(interactionType).append(secondColumn);
		return sb.toString();
	}
	
}
