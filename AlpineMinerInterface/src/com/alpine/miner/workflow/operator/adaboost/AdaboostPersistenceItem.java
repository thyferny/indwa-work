/**
 * ClassName AdaboostPersistenceItem.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-15
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.adaboost;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

public class AdaboostPersistenceItem {
	public static final String TAG_NAME="AdaboostPersistenceItem";
	
	public static final String ATTR_TYPE = "adaType";
	public static final String ATTR_NAME = "adaName";
 	
	private Map<String,String> parameterMap;
	
	private String adaType;
	private String adaName;
	
	public AdaboostPersistenceItem(String adaType,String adaName){
		this.adaType=adaType;
		this.adaName=adaName;
	}

	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public Element toXMLElement(Document xmlDoc){
		Element element=xmlDoc.createElement(TAG_NAME);
		element.setAttribute(ATTR_TYPE, adaType);
		element.setAttribute(ATTR_NAME, adaName);
		Iterator<Entry<String, String>> iter=parameterMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry=iter.next();
			element.setAttribute(entry.getKey(), entry.getValue());
		}
		return element;
	}
	
	public static AdaboostPersistenceItem fromXMLElement(Element element){
		AdaboostPersistenceItem item=null;
		HashMap<String,String> parameterMap=new HashMap<String,String>();
		String adaType=null;
		String adaName=null;
		NamedNodeMap nodeMap=element.getAttributes();
		adaType=nodeMap.getNamedItem(ATTR_TYPE).getNodeValue();
		adaName=nodeMap.getNamedItem(ATTR_NAME).getNodeValue();
		for(int i=0;i<nodeMap.getLength();i++){
			Node node=nodeMap.item(i);
			if(node.getNodeName().equals(ATTR_TYPE)
					||node.getNodeName().equals(ATTR_NAME)){
				continue;
			}
			parameterMap.put(node.getNodeName(),node.getNodeValue());
		}
		item=new AdaboostPersistenceItem(adaType,adaName);
		item.setParameterMap(parameterMap);
		return item;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null||!(obj instanceof AdaboostPersistenceItem)){
			return false;
		}
		AdaboostPersistenceItem target=(AdaboostPersistenceItem)obj;
		if(!target.getAdaName().equals(getAdaName())){
			return false;
		}else if(!target.getAdaType().equals(getAdaType())){
			return false;
		}else{
			Map<String, String> targetParas=target.getParameterMap();
			Map<String, String> paras=getParameterMap();
			if(targetParas.size()!=paras.size()){
				return false;
			}
			Iterator<String> iter=targetParas.keySet().iterator();
			while(iter.hasNext()){
				String key=iter.next();
				String targetValue=targetParas.get(key);
				String value=paras.get(key);
			
				if(StringUtil.isEmpty(targetValue)^StringUtil.isEmpty(value)){//keep same status
					return false;
				}else 	if(value!=null&&targetValue!=null&&"hidden_layers".endsWith(key)){
					String[] valueArray = value.split(";");
					String[] targetValueArray = targetValue.split(";");
					if(valueArray!=null){ 
						List sourceValueList =Arrays.asList(valueArray);
						List targetValueList  =Arrays.asList(targetValueArray); 
						if(ListUtility.equalsIgnoreOrder(sourceValueList, targetValueList)==false){
							return false;
						}
					}
				}
			 
				else if(!StringUtil.isEmpty(targetValue)
						&&!StringUtil.isEmpty(value)
						&&!targetValue.equals(value)){
					return false;
				}
			}
		}
		return true;
	}

	public String getAdaType() {
		return adaType;
	}

	public void setAdaType(String adaType) {
		this.adaType = adaType;
	}

	public String getAdaName() {
		return adaName;
	}

	public void setAdaName(String adaName) {
		this.adaName = adaName;
	}
	
}
