/**
 * ClassName InterActionItem.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-23
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.interaction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author zhaoyong
 *
 */
public class InterActionItem implements XMLFragment{   
	public static final String TAG_NAME="InterActionItem";
	
	
	public static final String ATTR_ID="id";
	public static final String ATTR_FIRST_COLUMN="firstColumn";
	public static final String ATTR_SECOND_COLUMN="secondColumn";
	public static final String ATTR_INTERACTION_TYPE="interactionType";
	
	private String id="";
	private String firstColumn="";
	private String secondColumn="";
	private String interactionType="";
	
	
	public InterActionItem() {
	}
	public InterActionItem(String id, String firstColumn, String secondColumn,
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
	
	public Element toXMLElement(Document xmlDoc){
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_ID, getId());
		element.setAttribute(ATTR_FIRST_COLUMN , getFirstColumn());
		element.setAttribute(ATTR_INTERACTION_TYPE, getInteractionType());
		element.setAttribute(ATTR_SECOND_COLUMN, getSecondColumn());
		return element;
	}
	
	public static InterActionItem fromXMLElement(Element element){
		InterActionItem item=new InterActionItem();
		item.setId(element.getAttribute(ATTR_ID));
		item.setFirstColumn(element.getAttribute(ATTR_FIRST_COLUMN));
		item.setInteractionType(element.getAttribute(ATTR_INTERACTION_TYPE));
		item.setSecondColumn(element.getAttribute(ATTR_SECOND_COLUMN));
		return item;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof InterActionItem){
			return ((InterActionItem)obj).getFirstColumn().equals(firstColumn)&&
			((InterActionItem)obj).getId().equals(id)&&
			((InterActionItem)obj).getInteractionType().equals(interactionType)&&
			((InterActionItem)obj).getSecondColumn().equals(secondColumn);
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
	@Override
	public String getXMLTagName() {
 
		return TAG_NAME;
	}
	@Override
	public void initFromXmlElement(Element element) {
		this.setId(element.getAttribute(ATTR_ID));
		this.setFirstColumn(element.getAttribute(ATTR_FIRST_COLUMN));
		this.setInteractionType(element.getAttribute(ATTR_INTERACTION_TYPE));
		this.setSecondColumn(element.getAttribute(ATTR_SECOND_COLUMN));

		
	}

	@Override
	public  Object clone() throws CloneNotSupportedException{
		InterActionItem item = new InterActionItem();
		item.setId(this.getId());
		item.setFirstColumn(this.getFirstColumn());
		item.setInteractionType(this.getInteractionType());
		item.setSecondColumn(this.getSecondColumn());
		return item;
		
	}
}
