/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.aggregate;

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

 
public class AggregateFieldsModel extends AbstractParameterObject{
	public static final String TAG_NAME="AggregateFieldsModel";
	
	public static final String GROUPBY_TAG_NAME="groupBy";

	public static final String ATTR_COLUMNNAME = "columnName";
	
	public static final String PARENT_TAG_NAME="parentFieldList";
 
	List<AggregateField> aggregateFieldList=null;
	List<String> groupByFieldList=null;
	List<String> parentFieldList=null;
	
	public List<AggregateField> getAggregateFieldList() {
		return aggregateFieldList;
	}
	
	public void setAggregateFieldList(List<AggregateField> aggregateFieldList) {
		this.aggregateFieldList = aggregateFieldList;
	}
	
	public List<String> getGroupByFieldList() {
		return groupByFieldList;
	}
	
	public void setGroupByFieldList(List<String> groupByFieldList) {
		this.groupByFieldList = groupByFieldList;
	}
 	
	public AggregateFieldsModel(List<AggregateField> aggregateFieldList,List<String>groupByFieldList,List<String> parentFieldList){ 
		this.aggregateFieldList=aggregateFieldList;
		this.groupByFieldList=groupByFieldList;
		this.parentFieldList=parentFieldList;
	}
	
	public AggregateFieldsModel( ){
		this.aggregateFieldList=new ArrayList<AggregateField>();
		this.groupByFieldList=new ArrayList<String>();
	}
	
	 
	
	public List<String> getParentFieldList() {
		return parentFieldList;
	}

	public void setParentFieldList(List<String> parentFieldList) {
		this.parentFieldList = parentFieldList;
	}

	public boolean equals(Object obj) {
		if(obj instanceof AggregateFieldsModel){
		 
				return  ListUtility.equalsIgnoreOrder(aggregateFieldList,
						((AggregateFieldsModel)obj).getAggregateFieldList()) 
					&&ListUtility.equalsFocusOrder(groupByFieldList,
						((AggregateFieldsModel)obj).getGroupByFieldList())
					&&ListUtility.equalsIgnoreOrder(parentFieldList,
						((AggregateFieldsModel)obj).getParentFieldList()) ;
			 
		}else{
			return false;
		}
	
	}
  
	public void addAggregateField(AggregateField item) {
		if(aggregateFieldList==null){
			aggregateFieldList= new ArrayList<AggregateField>();
		}
		aggregateFieldList.add(item) ;
		
	}
	public void removeAggregateField(AggregateField item) {
		if(aggregateFieldList!=null&&aggregateFieldList.contains(aggregateFieldList)){
			aggregateFieldList.remove(item) ;
		}
	}

	/** not ready...
	 * @param xmlDoc
	 * @return
	 */
	@Override
	public Element toXMLElement(Document xmlDoc) {
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getGroupByFieldList()!=null){
			for(String s:getGroupByFieldList()){
				Element groupByEle=xmlDoc.createElement(GROUPBY_TAG_NAME);
				groupByEle.setAttribute(ATTR_COLUMNNAME, s);
				element.appendChild(groupByEle);
			}
			
		}
		if(getParentFieldList()!=null){
			for(String s:getParentFieldList()){
				Element parentEle=xmlDoc.createElement(PARENT_TAG_NAME);
				parentEle.setAttribute(ATTR_COLUMNNAME, s);
				element.appendChild(parentEle);
			}
			
		}
		if(getAggregateFieldList()!=null){
			for (Iterator<AggregateField> iterator = getAggregateFieldList().iterator(); iterator.hasNext();) {
				AggregateField item = iterator.next();
				if(item!=null){
					Element itemElement=item.toXMLElement(xmlDoc);
					element.appendChild(itemElement); 
				}			
			}
		}
		return element;
	}

	public static AggregateFieldsModel fromXMLElement(Element element) {
		List<AggregateField> aggFieldItems = new ArrayList<AggregateField>();
 		NodeList quantileItemList = element.getElementsByTagName(AggregateField.TAG_NAME);
		for (int i = 0; i < quantileItemList.getLength(); i++) {
			if (quantileItemList.item(i) instanceof Element ) {
				AggregateField aggFieldItem=AggregateField.fromXMLElement((Element)quantileItemList.item(i));
				aggFieldItems.add(aggFieldItem);
 
			}
		}
		
		NodeList groupByItemList = element.getElementsByTagName(GROUPBY_TAG_NAME);
		List<String> groupByList=new ArrayList<String>();
		for (int i = 0; i < groupByItemList.getLength(); i++) {
			if (groupByItemList.item(i) instanceof Element ) {
				String groupByColumn=((Element)groupByItemList.item(i)).getAttribute(ATTR_COLUMNNAME);
				groupByList.add(groupByColumn);
			}
		}
		
		NodeList parnetItemList = element.getElementsByTagName(PARENT_TAG_NAME);
		List<String> parnetList=new ArrayList<String>();
		for (int i = 0; i < parnetItemList.getLength(); i++) {
			if (parnetItemList.item(i) instanceof Element ) {
				String parnetColumn=((Element)parnetItemList.item(i)).getAttribute(ATTR_COLUMNNAME);
				parnetList.add(parnetColumn);
			}
		}
		
		return new AggregateFieldsModel(aggFieldItems,groupByList,parnetList);
	}
	/**
	 * @return
	 */
	@Override
	public AggregateFieldsModel clone()  throws CloneNotSupportedException {
		AggregateFieldsModel model= new AggregateFieldsModel();
		List clone = ParameterUtility.cloneObjectList( this.getAggregateFieldList());
		model.setAggregateFieldList(clone);
		clone  = ListUtility.cloneStringList( this.getGroupByFieldList());
		model.setGroupByFieldList(clone);
		clone  = ListUtility.cloneStringList( this.getParentFieldList());
		model.setParentFieldList(clone);
		
		return model;
	}
 
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
  
	
}
