/**
 * ClassName  WindowFieldsModel.java
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

//groupByFieldList
//aggregateExpressionList(expression, alias)
//aggregateFieldAliasList

public class WindowFieldsModel extends AbstractParameterObject{
	public static final String TAG_NAME="WindowFieldsModel";
 
	List<WindowField> windowFieldList=null;  
 
 
	public List<WindowField> getWindowFieldList() {
		return windowFieldList;
	}

	public void setWindowFieldList(List<WindowField> windowFieldList) {
		this.windowFieldList = windowFieldList;
	}

	public WindowFieldsModel(List<WindowField> windowFieldList){ 
		this.windowFieldList=windowFieldList;
	}
	
	public WindowFieldsModel( ){
		this.windowFieldList=new ArrayList<WindowField>();
	}
	
	 
	
	public boolean equals(Object obj) {
		if(obj instanceof WindowFieldsModel){
				return  ListUtility.equalsIgnoreOrder(windowFieldList,
						((WindowFieldsModel)obj).getWindowFieldList()) ;
		}else{
			return false;
		}
	
	}
  
	public void addDerivedFieldItem(WindowField item) {
		if(windowFieldList==null){
			windowFieldList= new ArrayList<WindowField>();
		}
		windowFieldList.add(item) ;
		
	}
	public void removeDerivedFieldItem(WindowField item) {
		if(windowFieldList!=null&&windowFieldList.contains(windowFieldList)){
			windowFieldList.remove(item) ;
		}
	}

	/** not ready...
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getWindowFieldList()!=null){
			for (Iterator<WindowField> iterator = getWindowFieldList().iterator(); iterator.hasNext();) {
				WindowField item =iterator.next();
				if(item!=null){
					Element itemElement=item.toXMLElement(xmlDoc);
					element.appendChild(itemElement); 
				}
				
			}
		}
		return element;
	}

	 
	/**
	 * @return
	 */
	@Override
	public WindowFieldsModel clone()  throws CloneNotSupportedException {
		WindowFieldsModel model= new WindowFieldsModel();
		if(this.getWindowFieldList()!=null){
			List clone = ParameterUtility.cloneObjectList( this.getWindowFieldList());
			model.setWindowFieldList(clone);
		}
		 
		return model;
	}
 

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public static WindowFieldsModel fromXMLElement(Element element) {
		List<WindowField> winFieldItems = new ArrayList<WindowField>();
 		NodeList windowItemList = element.getElementsByTagName(WindowField.TAG_NAME);
		for (int i = 0; i < windowItemList.getLength(); i++) {
			if (windowItemList.item(i) instanceof Element ) {
				WindowField winFieldItem=WindowField.fromXMLElement((Element)windowItemList.item(i));
				winFieldItems.add(winFieldItem);
 
			}
		}
		WindowFieldsModel windowFieldsModel=new WindowFieldsModel();
		windowFieldsModel.setWindowFieldList(winFieldItems);
		return windowFieldsModel;
	}
}