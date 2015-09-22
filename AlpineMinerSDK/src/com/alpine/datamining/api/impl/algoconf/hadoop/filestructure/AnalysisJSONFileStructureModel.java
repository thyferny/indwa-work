/**
 * ClassName AbstractJSONFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-1
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop.filestructure;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public class AnalysisJSONFileStructureModel extends AbstractFileStructureModel {
	
	public static final String TAG_NAME="JSONFileStructureModel";
	public static final String ATTR_ROOT="root";
	public static final String ATTR_CONTAINER="container";
	public static final String ATTR_JSONPATH = "jsonPath";
	public static final String ATTR_JSONDataStructureType = "jsonDataStructureType";
	public static final String JSONPATH_TAG_NAME = "jsonPaths";
	public static final String ATTR_CONTAINER_JSONPATH="containerJsonPath";
	
	public static final String STRUCTURE_TYPE_STANDARD = "sts";
	public static final String STRUCTURE_TYPE_LINE = "stl";
	public static final String STRUCTURE_TYPE_PURE_DATA_ARRAY = "stp";
	public static final String STRUCTURE_TYPE_OBJECT_ARRAY = "sto";
	
	private String root;
	private String container;
	private String containerJsonPath="";
	private List<String> jsonPathList;
	private String jsonDataStructureType=STRUCTURE_TYPE_STANDARD;
	
	public String getRoot() {
		return root;
	}
	public void setRoot(String root) {
		this.root = root;
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	public List<String> getJsonPathList() {
		return jsonPathList;
	}
	public void setJsonPathList(List<String> jsonPathList) {
		this.jsonPathList = jsonPathList;
	}
	public String getJsonDataStructureType() {
		return jsonDataStructureType;
	}
	public void setJsonDataStructureType(String jsonDataStructureType) {
		this.jsonDataStructureType = jsonDataStructureType;
	}
	public String getContainerJsonPath() {
		return containerJsonPath;
	}
	public void setContainerJsonPath(String containerJsonPath) {
		this.containerJsonPath = containerJsonPath;
	}
	@Override
	public AnalysisJSONFileStructureModel clone()  throws CloneNotSupportedException {
		AnalysisJSONFileStructureModel model= new AnalysisJSONFileStructureModel();
		
		cloneCommonField(model);
		
		return model;
	}

	protected void cloneCommonField(AnalysisJSONFileStructureModel model)
			throws CloneNotSupportedException {
		super.cloneCommonField(model);
		model.setRoot(root);
		model.setContainer(container);
		model.setJsonPathList(jsonPathList);
		model.setJsonDataStructureType(jsonDataStructureType);
		model.setContainerJsonPath(containerJsonPath);
	}

	public static AnalysisJSONFileStructureModel fromXMLElement(Element element) {
		AnalysisJSONFileStructureModel model=new AnalysisJSONFileStructureModel();

		fillColumnElements(element, model);
		
		fillCommonField4JSON(element, model);
		
		return model;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisJSONFileStructureModel){
			return  super.equals((AnalysisFileStructureModel)obj)&&
					StringUtil.safeEquals(root,((AnalysisJSONFileStructureModel)obj).getRoot())
					&&StringUtil.safeEquals(container,((AnalysisJSONFileStructureModel)obj).getContainer())
					&&StringUtil.safeEquals(containerJsonPath,((AnalysisJSONFileStructureModel)obj).getContainerJsonPath())
					&&ListUtility.equalsFocusOrder(jsonPathList, ((AnalysisJSONFileStructureModel)obj).getJsonPathList());
	}else{
		return false;
		}
	}
	
	protected static void fillCommonField4JSON(Element element,
			AnalysisJSONFileStructureModel model) {
		NodeList jsonPathItemList = element.getElementsByTagName(JSONPATH_TAG_NAME);
		List<String> jsonPathList=new ArrayList<String>();
		model.setJsonPathList(jsonPathList);
		for (int i = 0; i < jsonPathItemList.getLength(); i++) {
			if (jsonPathItemList.item(i) instanceof Element ) {
				String jsonPath=((Element)jsonPathItemList.item(i)).getAttribute(ATTR_JSONPATH);
				jsonPathList.add(jsonPath);
			}
		}
		String container=element.getAttribute(ATTR_CONTAINER);
		model.setContainer(container);
		String root=element.getAttribute(ATTR_ROOT);
		model.setRoot(root);
		String containerJsonPath=element.getAttribute(ATTR_CONTAINER_JSONPATH);
		model.setContainerJsonPath(containerJsonPath);
		String structureType=element.getAttribute(ATTR_JSONDataStructureType);
		if(StringUtil.isEmpty(structureType)){
			model.setJsonDataStructureType(STRUCTURE_TYPE_STANDARD);
		}else{
			model.setJsonDataStructureType(structureType);
		}
	}
}
