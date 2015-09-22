/**
 * ClassName InterActionModel.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-23
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.interaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.db.Resources;

/**
 * @author zhaoyong
 *
 */
public class InterActionColumnsModel  extends AbstractParameterObject{
	public static final String TAG_NAME="InterActionModel";
	
	private List<InterActionItem> interActionItems;

	public InterActionColumnsModel(List<InterActionItem> interActionItems) {
		this.interActionItems = interActionItems;
	}
	
	public InterActionColumnsModel() {
		this.interActionItems = new ArrayList<InterActionItem>();
	}

	public List<InterActionItem> getInterActionItems() {
		return interActionItems;
	}

	public void setInterActionItems(List<InterActionItem> interActionItems) {
		this.interActionItems = interActionItems;
	}
	public void addInterActionItem(InterActionItem item){
		interActionItems.add(item);
	}
	public Element toXMLElement(Document xmlDoc){
		Element element = xmlDoc.createElement(TAG_NAME);
		List<InterActionItem>  interactionItems=getInterActionItems();
		Iterator<InterActionItem>  iter=interactionItems.iterator();
		while(iter.hasNext()){
			InterActionItem item=iter.next();
			Element itemElement=item.toXMLElement(  xmlDoc);
			element.appendChild(itemElement); 
		}
		return element;
	}
	public static InterActionColumnsModel fromXMLElement(Element element) {
		List<InterActionItem> interActionItems = new ArrayList<InterActionItem>();
 		NodeList interActionItemList = element.getElementsByTagName(InterActionItem.TAG_NAME);
		for (int i = 0; i < interActionItemList.getLength(); i++) {
			if (interActionItemList.item(i) instanceof Element ) {
				InterActionItem interActionItem=InterActionItem.fromXMLElement((Element)interActionItemList.item(i));
				interActionItems.add(interActionItem);
			}
		}
		return new InterActionColumnsModel(interActionItems);
	}

	@Override
	public boolean equals(Object obj) {
			if(obj!= null &&obj instanceof InterActionColumnsModel){
				if(interActionItems.size()!=
					((InterActionColumnsModel)obj).getInterActionItems().size()){
					return false;
				}else if ((interActionItems==null&&((InterActionColumnsModel)obj).getInterActionItems()!=null)
						||(interActionItems!=null&&((InterActionColumnsModel)obj).getInterActionItems()==null)){
					return false;
				}
				else{
					return interActionItems.containsAll(((InterActionColumnsModel)obj).getInterActionItems())
					&&((InterActionColumnsModel)obj).getInterActionItems().containsAll(interActionItems);

				}				
		}else{
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		int i=0;
		 for (Iterator<InterActionItem> iterator = getInterActionItems().iterator(); iterator.hasNext();) {
			 InterActionItem item = iterator.next();
			 sb.append(item.toString());
			 if(i!=getInterActionItems().size()-1){
				 sb.append(Resources.FieldSeparator);
			 }
			 i++;
		}
		return sb.toString();
	}

	@Override
	public String getXMLTagName() {
 
		return TAG_NAME;
	}

	
	/**
	 * @return
	 */
	@Override
	public InterActionColumnsModel clone()  throws CloneNotSupportedException {
		InterActionColumnsModel model= new InterActionColumnsModel();
		List clone = ParameterUtility.cloneObjectList( this.getInterActionItems());
		model.setInterActionItems(clone); 
		return model;
	}
}
