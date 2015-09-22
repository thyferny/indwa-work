/**
 * ClassName JSONFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-1
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisJSONFileStructureModel;

/**
 * @author Jeff Dong
 *
 */
public class JSONFileStructureModel extends AnalysisJSONFileStructureModel
		implements FileStructureModel {

	public static final String TAG_NAME="JSONFileStructureModel";
	public static final String ATTR_ROOT="root";
	public static final String ATTR_CONTAINER="container";
	public static final String ATTR_JSONPATH = "jsonPath";
	public static final String ATTR_CONTAINER_JSONPATH="containerJsonPath";
	public static final String JSONPATH_TAG_NAME = "jsonPaths";
	
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element element = xmlDoc.createElement(TAG_NAME);
		
		element.setAttribute(ATTR_ROOT, getRoot());
		element.setAttribute(ATTR_CONTAINER, getContainer());
		element.setAttribute(ATTR_CONTAINER_JSONPATH, getContainerJsonPath());
		element.setAttribute(ATTR_JSONDataStructureType, getJsonDataStructureType());
		
		if(getJsonPathList()!=null){
			for(String jsonPath:getJsonPathList()){
				Element jsonPathEle=xmlDoc.createElement(JSONPATH_TAG_NAME);
				jsonPathEle.setAttribute(ATTR_JSONPATH, jsonPath);
				element.appendChild(jsonPathEle);
			}	
		}
		
		FileStructureModelUtility.fillCommonXMLElement(this,xmlDoc, element);
		return element;
	}

	
	public static JSONFileStructureModel fromXMLElement(Element element) {
		JSONFileStructureModel model=new JSONFileStructureModel();

		fillColumnElements(element, model);
		
		fillCommonField4JSON(element, model);
		return model;
	}


	@Override
	public void initFromXmlElement(Element element) {
		//no use
		
	}

	public JSONFileStructureModel clone() throws CloneNotSupportedException {
		JSONFileStructureModel model= new JSONFileStructureModel();
		super.cloneCommonField(model);
		return model;
	}

}
