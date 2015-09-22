/**
 * ClassName  AggregateFieldsModel.java
 *
 * Version information: 1.00
 *
 * Data: 2010-8-9
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hiddenlayer;

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

public class HiddenLayersModel extends AbstractParameterObject{
	public static final String TAG_NAME="HiddenLayersModel";
	List<HiddenLayer> hiddenLayers=null;
	
  	
	public List<HiddenLayer> getHiddenLayers() {
		return hiddenLayers;
	}

	public void setHiddenLayers(List<HiddenLayer> hiddenLayers) {
		this.hiddenLayers = hiddenLayers;
	}

	public HiddenLayersModel(List<HiddenLayer> hiddenLayers ){ 
		this.hiddenLayers=hiddenLayers;
 
	}
	
	public HiddenLayersModel( ){
	 
		this.hiddenLayers=new ArrayList<HiddenLayer>();
	}
	  
	
	public boolean equals(Object obj) {
		if(obj instanceof HiddenLayersModel){
		 
				return  ListUtility.equalsIgnoreOrder(hiddenLayers,
						((HiddenLayersModel)obj).getHiddenLayers()) 
					 ;
			 
				
		}else{
			return false;
		}
	
	}
  
	public void addHiddenLayer (HiddenLayer layer) {
		if(hiddenLayers==null){
			hiddenLayers= new ArrayList<HiddenLayer>();
		}
		hiddenLayers.add(layer) ;
		 
		
	}
	public String toString(){
		String temp="";
		if (hiddenLayers != null){
			int count = hiddenLayers.size();
			int i =0;
			for(HiddenLayer hiddenLayer: hiddenLayers){
				temp+=hiddenLayer.getLayerName()+","+hiddenLayer.getLayerSize();
				i++;
				if(i<count){
					temp+=";";
				}
			}
		}
		return temp;

	}

	/** not ready...
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		 
		Element element = xmlDoc.createElement(TAG_NAME);
		if(getHiddenLayers()!=null){
			for (Iterator<HiddenLayer> iterator = getHiddenLayers().iterator(); iterator.hasNext();) {
				HiddenLayer item = (HiddenLayer) iterator.next();
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
	public HiddenLayersModel clone()  throws CloneNotSupportedException {
		HiddenLayersModel model= new HiddenLayersModel();
		List clone = ParameterUtility.cloneObjectList( this.getHiddenLayers());
		model.setHiddenLayers(clone); 
		return model;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}

	public static HiddenLayersModel fromXMLElement(Element element) {
		List<HiddenLayer> hiddenLayers = new ArrayList<HiddenLayer>();
 		NodeList hiddenItemList = element.getElementsByTagName(HiddenLayer.TAG_NAME);
		for (int i = 0; i < hiddenItemList.getLength(); i++) {
			if (hiddenItemList.item(i) instanceof Element ) {
				HiddenLayer aggFieldItem=HiddenLayer.fromXMLElement((Element)hiddenItemList.item(i));
				hiddenLayers.add(aggFieldItem);
			}
		}
		HiddenLayersModel hiddenLayersModel=new HiddenLayersModel();
		hiddenLayersModel.setHiddenLayers(hiddenLayers);
		return hiddenLayersModel;
	}
	  
 
	
}
