package com.alpine.miner.workflow.operator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.alpine.utility.common.VariableModelUtility;

/**
 * ClassName VariableModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-10
 *
 * COPYRIGHT   2011  Alpine Solutions. All Rights Reserved.
 **/
public class VariableModel {
	
	public static String VARIABLE_PREFIX=VariableModelUtility.VARIABLE_PREFIX;
	public static String VARIABLE_ESCAPRE=VariableModelUtility.VARIABLE_ESCAPRE;
	public static String VARIABLE_ESCAPRE_SUFFIX=VariableModelUtility.VARIABLE_ESCAPRE_SUFFIX;
	public static String DEFAULT_SCHEMA=VariableModelUtility.DEFAULT_SCHEMA;
	public static String DEFAULT_PREFIX=VariableModelUtility.DEFAULT_PREFIX;
	public static String DEFAULT_TMPDIR=VariableModelUtility.DEFAULT_TMPDIR;
	public static String DEFAULT_SCHEMA_DEFAULTVALUE=VariableModelUtility.DEFAULT_SCHEMA_DEFAULTVALUE;
	public static String DEFAULT_PREFIX_DEFAULTVALUE=VariableModelUtility.DEFAULT_PREFIX_DEFAULTVALUE;
	public static String DEFAULT_TMPDIR_DEFAULTVALUE=VariableModelUtility.DEFAULT_TMPDIR_DEFAULTVALUE;
	
	public static final String MODEL_TAG_NAME="VariableModel";
	public static final String VARIABLE_TAG_NAME="Variable";
	public static final String VARIABLE_NAME_TAG_NAME="Name";
	public static final String VARIABLE_VALUE_TAG_NAME="Value";
	
	private LinkedHashMap<String,String> variableMap;

	public Iterator<Map.Entry<String,String>> getIterator(){
		if(variableMap!=null){
			return variableMap.entrySet().iterator();
		}
		return null;
	}
	
	public LinkedHashMap<String,String> getVariableMap(){
		return variableMap;
	}
	private void init(){
		if(variableMap==null){
			variableMap=new LinkedHashMap<String,String>();
			variableMap.put(DEFAULT_SCHEMA, DEFAULT_SCHEMA_DEFAULTVALUE);
			variableMap.put(DEFAULT_PREFIX, DEFAULT_PREFIX_DEFAULTVALUE);
			variableMap.put(DEFAULT_TMPDIR, DEFAULT_TMPDIR_DEFAULTVALUE);
		}
	}
	public void addVariable(String name,String value){
		init();
		variableMap.put(name, value);
	}
	
	public VariableModel() {
		init();
	}

	public String getVariable(String name){
		if(variableMap!=null&&variableMap.containsKey(name)){
			return variableMap.get(name);
		}
		return null;
	}
	public boolean containsKey(String name){
		if(variableMap!=null){
			return variableMap.containsKey(name);
		}
		return false;
	}
	
	public static VariableModel fromXMLElement(Element element) {
		VariableModel variableModel=new VariableModel();
		NodeList variableItemList = element.getElementsByTagName(VARIABLE_TAG_NAME);
		if(variableItemList!=null){
			for (int i = 0; i < variableItemList.getLength(); i++) {
				if (variableItemList.item(i) instanceof Element ) {
					Element itemElement=(Element)variableItemList.item(i);
					String variableName=itemElement.getElementsByTagName(VARIABLE_NAME_TAG_NAME).item(0).getTextContent();		
					String variableValue = itemElement.getElementsByTagName(VARIABLE_VALUE_TAG_NAME).item(0).getTextContent();
					variableModel.addVariable(variableName, variableValue);
				}
			}
		}
		return variableModel;
	}
	
	public Element toXMLElement(Document xmlDoc){
		Element element = xmlDoc.createElement(MODEL_TAG_NAME);
		Iterator<Entry<String, String>> iter = getIterator();
		if(iter!=null){
			while(iter.hasNext()){
				Entry<String, String> entry = iter.next();
				Element itemElement=xmlDoc.createElement(VARIABLE_TAG_NAME);
				
				Element nameElement=xmlDoc.createElement(VARIABLE_NAME_TAG_NAME);
				Text nameElementText = xmlDoc.createTextNode(entry.getKey());
				nameElement.appendChild(nameElementText);
				itemElement.appendChild(nameElement);
				
				Element valueElement=xmlDoc.createElement(VARIABLE_VALUE_TAG_NAME);
				Text valueElementText=xmlDoc.createTextNode(entry.getValue());
				valueElement.appendChild(valueElementText);
				itemElement.appendChild(valueElement);
				
				element.appendChild(itemElement); 
			}
		}
		return element;
	}
	
	public VariableModel clone(){
		VariableModel clone = new VariableModel();
		
		Iterator<Entry<String, String>> iter = getIterator();
		if(iter!=null){
			while(iter.hasNext()){
				Entry<String, String> entry = iter.next();
				clone.addVariable(entry.getKey(), entry.getValue());
 			}
		}
		return clone;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VariableModel [variableMap=");
		builder.append((null==variableMap)?"NULL":variableMap.toString());
		builder.append("]");
		return builder.toString();
	}
}
