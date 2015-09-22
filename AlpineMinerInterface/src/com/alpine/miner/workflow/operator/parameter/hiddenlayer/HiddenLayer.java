/**
 * ClassName :AggregateField.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.hiddenlayer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.XMLFragment;

/**
 * @author zhaoyong
 * 
 */
public class HiddenLayer implements XMLFragment {

	public static final String TAG_NAME="HiddenLayer";

	private static final String ATTR_LAYERNAME = "layerName";

	private static final String ATTR_LAYERSIZE = "layerSize";
	
	String layerName = null;	
	String layerSize = null;
	
	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getLayerSize() {
		return layerSize;
	}

	public void setLayerSize(String layerSize) {
		this.layerSize = layerSize;
	}


 

	/**
	 * @param alias2
	 * @param aggregateExpression2
	 */
	public HiddenLayer(String layerName, String layerSize) {
		this.layerName = layerName;
		this.layerSize = layerSize;
	}

	/**
	 * @param xmlDoc
	 * @return
	 */
	public Element toXMLElement(Document xmlDoc) {
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_LAYERNAME, getLayerName());
		element.setAttribute(ATTR_LAYERSIZE, String.valueOf(getLayerSize()));
		return element;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new HiddenLayer(layerName,layerSize);
	}
	
	 public boolean equals(Object obj) {
		 if(this==obj){
			 return true;
		 }else if(obj!=null && obj instanceof HiddenLayer){
			 HiddenLayer hiddenLayer = (HiddenLayer) obj;
			 return ParameterUtility.nullableEquales(layerName ,hiddenLayer.getLayerName())
			 && ParameterUtility.nullableEquales(layerSize ,hiddenLayer.getLayerSize())			 ;
 
		 }else{
			 return false;
		 }
	 }
 
	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#initFromXmlElement(org.w3c.dom.Element)
	 */
	@Override
	public void initFromXmlElement(Element element) {
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.workflow.operator.parameter.XMLElement#getXMLTagName()
	 */
	@Override
	public String getXMLTagName() {
 
		return TAG_NAME;
	}

	public static HiddenLayer fromXMLElement(Element item) {
		String layerName=item.getAttribute(ATTR_LAYERNAME);
		String layerSize=item.getAttribute(ATTR_LAYERSIZE);
		HiddenLayer hiddenLayer=new HiddenLayer(layerName,layerSize);
		return hiddenLayer;
	}
}