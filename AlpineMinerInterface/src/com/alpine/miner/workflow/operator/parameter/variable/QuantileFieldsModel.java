/**
 * ClassName  QuantileFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.miner.workflow.operator.parameter.AbstractParameterObject;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;

/**
 * @author zhaoyong
 *
 */
public class QuantileFieldsModel extends AbstractParameterObject{
	public static final String TAG_NAME="QuantileModel";
 
	List<QuantileItem> quantileItems=null;
	
	public QuantileFieldsModel(List<QuantileItem> quantileItems){
		this.quantileItems=quantileItems;
	}
	public QuantileFieldsModel( ){
		this.quantileItems=new ArrayList<QuantileItem>();
	}
	
	 public List<QuantileItem> getQuantileItems() {
		return quantileItems;
	}
	public void setQuantileItems(List<QuantileItem> quantileItems) {
		this.quantileItems = quantileItems;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof QuantileFieldsModel){
		 
				return ListUtility.equalsIgnoreOrder(quantileItems,
						((QuantileFieldsModel)obj).getQuantileItems()) ;
			 
				
		}else{
			return false;
		}
	
	}
	
	public static String getQuantileTypeLabel(int type){
		if(type==QuantileItem.TYPE_CUSTOMIZE){
			return QuantileItem.TYPE_CUSTIMZE_LABEL;
		}else if(type==QuantileItem.TYPE_AVG_ASC){
			return QuantileItem.TYPE_AVG_ASC_LABEL;
		}
//		else if (type==QuantileItemBin.TYPE_AVG_DESC){
//			return QuantileItemBin.TYPE_AVG_DESC_LABEL;
//		}
		else{
			return null;
		}
	}
	
	public static int getQuantileTypeValue(String label){
		if(label.equals(QuantileItem.TYPE_CUSTIMZE_LABEL)){
			return QuantileItem.TYPE_CUSTOMIZE;
		}else if(label.equals(QuantileItem.TYPE_AVG_ASC_LABEL)){
			return QuantileItem.TYPE_AVG_ASC;
		}
//		else if (label.equals(QuantileItemBin.TYPE_AVG_DESC_LABEL)){
//			return QuantileItemBin.TYPE_AVG_DESC ;
//		}
		else{
			return -1;
		}
	}
	/**
	 * @param item
	 */
	public void addQuantileItem(QuantileItem item) {
		if(quantileItems==null){
			quantileItems= new ArrayList<QuantileItem>();
		}
		quantileItems.add(item) ;
		
	}
	public void removeQuantileItem(QuantileItem item) {
		if(quantileItems!=null&&quantileItems.contains(quantileItems)){
			quantileItems.remove(item) ;
		}
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getQuantileItems()!=null){
			for (Iterator iterator = getQuantileItems().iterator(); iterator.hasNext();) {
				QuantileItem quantileItem = (QuantileItem) iterator.next();
				if(quantileItem!=null){
					Element itemElement=quantileItem.toXMLElement(  xmlDoc);
					element.appendChild(itemElement); 
				}
				
			}
		}
		return element;
	}

	/**
	 * @param item
	 * @return
	 */
	public static QuantileFieldsModel fromXMLElement(Element element) {
		List<QuantileItem> quantileItems = new ArrayList<QuantileItem>();
 		NodeList quantileItemList = element.getElementsByTagName(QuantileItem.TAG_NAME);
		for (int i = 0; i < quantileItemList.getLength(); i++) {
			if (quantileItemList.item(i) instanceof Element ) {
				QuantileItem quantileItem=QuantileItem.fromXMLElement((Element)quantileItemList.item(i));
				quantileItems.add(quantileItem);
 
			}
		}
		return new QuantileFieldsModel(quantileItems);
	}
	/**
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	public QuantileFieldsModel clone() throws CloneNotSupportedException {
		QuantileFieldsModel model= new QuantileFieldsModel();
	 
		 model.setQuantileItems(ParameterUtility.cloneObjectList(getQuantileItems()));
		 
	 	return model;
	}
	
	@Override
	public String toString() {
		StringBuilder out=new StringBuilder();
		 for (Iterator iterator = getQuantileItems().iterator(); iterator.hasNext();) {
			 QuantileItem item = (QuantileItem) iterator.next();
			out.append(item.toString());		
		}
		return out.toString();
	}
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	} 
	
	
}
