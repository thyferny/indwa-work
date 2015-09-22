/**
 * ClassName  QuantileModel.java
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
public class DerivedFieldsModel extends AbstractParameterObject{
	public static final String TAG_NAME="DerivedFieldsModel";
 
	public static final String SELECTED_COLUMN_TAG_NAME="selectedColumnName";

	private static final String ATTR_COLUMNNAME = "columnName";
	
	List<DerivedFieldItem> derivedFieldsList=null;
	
	List<String> selectedFieldList=null;
	
	public DerivedFieldsModel(List<DerivedFieldItem> derivedFieldsList){ 
		this.derivedFieldsList=derivedFieldsList;
	}
	public DerivedFieldsModel( ){
		this.derivedFieldsList=new ArrayList<DerivedFieldItem>();
	}
	
	 public List<DerivedFieldItem> getDerivedFieldsList() {
		return derivedFieldsList;
	}
	public void setDerivedFieldsList(List<DerivedFieldItem> derivedFieldsList) {
		this.derivedFieldsList = derivedFieldsList;
	}
	
	public List<String> getSelectedFieldList() {
		return selectedFieldList;
	}
	public void setSelectedFieldList(List<String> selectedFieldList) {
		this.selectedFieldList = selectedFieldList;
	}
	public boolean equals(Object obj) {
		if(obj instanceof DerivedFieldsModel){
				return ListUtility.equalsIgnoreOrder(derivedFieldsList,
						((DerivedFieldsModel)obj).getDerivedFieldsList())
						&&ListUtility.equalsIgnoreOrder(selectedFieldList,
								((DerivedFieldsModel)obj).getSelectedFieldList());
				
		}else{
			return false;
		}
	
	}
  
	public void addDerivedFieldItem(DerivedFieldItem item) {
		if(derivedFieldsList==null){
			derivedFieldsList= new ArrayList<DerivedFieldItem>();
		}
		derivedFieldsList.add(item) ;
		
	}
	public void removeDerivedFieldItem(DerivedFieldItem item) {
		if(derivedFieldsList!=null&&derivedFieldsList.contains(derivedFieldsList)){
			derivedFieldsList.remove(item) ;
		}
	}

	/** not ready...
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getSelectedFieldList()!=null){
			for(String s:getSelectedFieldList()){
				Element groupByEle=xmlDoc.createElement(SELECTED_COLUMN_TAG_NAME);
				groupByEle.setAttribute(ATTR_COLUMNNAME, s);
				element.appendChild(groupByEle);
			}
		}
		if(getDerivedFieldsList()!=null){
			for (Iterator iterator = getDerivedFieldsList().iterator(); iterator.hasNext();) {
				DerivedFieldItem quantileItem = (DerivedFieldItem) iterator.next();
				if(quantileItem!=null){
					Element itemElement=quantileItem.toXMLElement(xmlDoc);
					element.appendChild(itemElement); 
				}
				
			}
		}
		return element;
	}

	 
	/**
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	public DerivedFieldsModel clone() throws CloneNotSupportedException {
		DerivedFieldsModel model= new DerivedFieldsModel();
 
		model.setDerivedFieldsList(ParameterUtility.cloneObjectList(this.getDerivedFieldsList()));
		model.setSelectedFieldList(ParameterUtility.cloneObjectList(this.getSelectedFieldList()));
		
		return model;
	}
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	public static DerivedFieldsModel fromXMLElement(Element element) {
		List<DerivedFieldItem> derivedFieldItems = new ArrayList<DerivedFieldItem>();
 		NodeList derivedItemList = element.getElementsByTagName(DerivedFieldItem.TAG_NAME);
		for (int i = 0; i < derivedItemList.getLength(); i++) {
			if (derivedItemList.item(i) instanceof Element ) {
				DerivedFieldItem derivedFieldItem=DerivedFieldItem.fromXMLElement((Element)derivedItemList.item(i));
				derivedFieldItems.add(derivedFieldItem);
			}
		}
		
		NodeList selectedItemList = element.getElementsByTagName(SELECTED_COLUMN_TAG_NAME);
		List<String> selectedList=new ArrayList<String>();
		for (int i = 0; i < selectedItemList.getLength(); i++) {
			if (selectedItemList.item(i) instanceof Element ) {
				String selectedColumn=((Element)selectedItemList.item(i)).getAttribute(ATTR_COLUMNNAME);
				selectedList.add(selectedColumn);
			}
		}
		DerivedFieldsModel derivedFieldsModel=new DerivedFieldsModel(derivedFieldItems);
		derivedFieldsModel.setSelectedFieldList(selectedList);
		return derivedFieldsModel;
	}
	 
}
