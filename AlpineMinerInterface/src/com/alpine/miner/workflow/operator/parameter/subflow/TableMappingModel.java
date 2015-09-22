/**
 * ClassName TableMappingModel
 *
 * Version information: 1.00
 *
 * Data: 2012-4-8
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.subflow;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;

public class TableMappingModel {
	
	public static final String TAG_NAME = "TableMappingModel"; 
 
	List<TableMappingItem> mappingItems ;

	@Override
	public String toString() {
		return "TableMappingModel [mappingItems=" + mappingItems + "]";
	}

	public List<TableMappingItem> getMappingItems() {
		return mappingItems;
	}

	public void setMappingItems(List<TableMappingItem> mappingItems) {
		this.mappingItems = mappingItems;
	}

	public TableMappingModel(List<TableMappingItem> mappingItems) {
		super();
		this.mappingItems = mappingItems;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mappingItems == null) ? 0 : mappingItems.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableMappingModel other = (TableMappingModel) obj;
		if (mappingItems == null) {
			if (other.mappingItems != null)
				return false;
		} else if (ListUtility.equalsIgnoreOrder(mappingItems,other.mappingItems)==false)
			return false;
		return true;
	}

	
	public Element toXMLElement(Document xmlDoc,boolean addSuffixToOutput, String userName){
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(mappingItems!=null){
			for(int i =0;i<mappingItems.size();i++){
				element.appendChild(mappingItems.get(i).toXMLElement(xmlDoc,  addSuffixToOutput,userName)) ; 
			}
		} 
		return element;
		
	}
	
	public static TableMappingModel fromXMLElement(Element element) { 
 
		List<TableMappingItem> items = new ArrayList<TableMappingItem> ();
		NodeList nodes = element.getElementsByTagName(TableMappingItem.TAG_NAME);
		for(int i =0;i<nodes.getLength();i++){
			Node node = nodes.item(i);
			items.add(TableMappingItem.fromXMLElement((Element)node));
		}
		
		TableMappingModel tableMappingModel = new TableMappingModel(items);
		
		return tableMappingModel;

	} 
}
