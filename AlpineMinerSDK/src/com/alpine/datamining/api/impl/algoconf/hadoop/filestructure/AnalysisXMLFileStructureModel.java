/**
 * ClassName AnalysisXMLFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-26
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
public class AnalysisXMLFileStructureModel extends AbstractFileStructureModel {
	
	public static final String NO_ATTRIBUTE = "no" ;
	public static final String HALF_ATTRIBUTE = "half" ;
	public static final String PURE_ATTRIBUTE = "pure" ;
	
	public static final String TAG_NAME="XMLFileStructureModel";
	//no means  no attribute (start tag and end tag is pure: <container> and </container>),  
	//half means half atttribute (start is "<container ")
	//pure means pure attribute  (start is "<container " end is "/>")
	public static final String ATTR_ATTR_MODE="attrMode";
	//public static final String ATTR_ROOT="root";
	
	public static final String ATTR_CONTAINER="container";
	public static final String ATTR_XPATH = "xPath";
	public static final String XPATH_TAG_NAME = "xPaths";
	public static final String ATTR_XMLDataStructureType = "xmlDataStructureType";
	public static final String ATTR_CONTAINER_XPATH="containerXPath";
	
	public static final String STRUCTURE_TYPE_STANDARD = "sts";
	public static final String STRUCTURE_TYPE_LINE = "stl";
	
//	String root;
	String container;
	List<String> xPathList;
	String attrMode = NO_ATTRIBUTE;//default value
	private String xmlDataStructureType=STRUCTURE_TYPE_STANDARD;
	private String containerXPath="";
	
	public String getAttrMode() {
		return attrMode;
	}

	public void setAttrMode(String attrMode) {
		this.attrMode = attrMode;
	}

	public String getXmlDataStructureType() {
		return xmlDataStructureType;
	}

	public void setXmlDataStructureType(String xmlDataStructureType) {
		this.xmlDataStructureType = xmlDataStructureType;
	}
	
	public String getContainerXPath() {
		return containerXPath;
	}

	public void setContainerXPath(String containerXPath) {
		this.containerXPath = containerXPath;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public List<String> getxPathList() {
		return xPathList;
	}

	public void setxPathList(List<String> xPathList) {
		this.xPathList = xPathList;
	}


	
	@Override
	public AnalysisXMLFileStructureModel clone()  throws CloneNotSupportedException {
		AnalysisXMLFileStructureModel model= new AnalysisXMLFileStructureModel();
		
		cloneCommonField(model);
		
		return model;
	}

	protected void cloneCommonField(AnalysisXMLFileStructureModel model)
			throws CloneNotSupportedException {
		super.cloneCommonField(model);
		model.setAttrMode(attrMode);
		model.setContainer(container);
		model.setxPathList(xPathList);
		model.setContainerXPath(containerXPath);
		model.setXmlDataStructureType(xmlDataStructureType);
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisXMLFileStructureModel){
			return  super.equals((AnalysisFileStructureModel)obj)&&
					StringUtil.safeEquals(attrMode,((AnalysisXMLFileStructureModel)obj).getAttrMode())
					&&StringUtil.safeEquals(containerXPath,((AnalysisXMLFileStructureModel)obj).getContainerXPath())
					&&StringUtil.safeEquals(container,((AnalysisXMLFileStructureModel)obj).getContainer())
					&&StringUtil.safeEquals(xmlDataStructureType,((AnalysisXMLFileStructureModel)obj).getXmlDataStructureType())
					&&ListUtility.equalsFocusOrder(xPathList, ((AnalysisXMLFileStructureModel)obj).getxPathList());
	}else{
		return false;
		}
	}
	
	protected static void fillCommonField4XML(Element element,
			AnalysisXMLFileStructureModel model) {
		NodeList xPathItemList = element.getElementsByTagName(XPATH_TAG_NAME);
		List<String> xPathList=new ArrayList<String>();
		for (int i = 0; i < xPathItemList.getLength(); i++) {
			if (xPathItemList.item(i) instanceof Element ) {
				String xPath=((Element)xPathItemList.item(i)).getAttribute(ATTR_XPATH);
				xPathList.add(xPath);
			}
		}
		String container=element.getAttribute(ATTR_CONTAINER);
		model.setContainer(container);
		String containerXPath=element.getAttribute(ATTR_CONTAINER_XPATH);
		model.setContainerXPath(containerXPath);
		String xmlDataStructureType=element.getAttribute(ATTR_XMLDataStructureType);
		if(StringUtil.isEmpty(xmlDataStructureType)==false){
			model.setXmlDataStructureType(xmlDataStructureType);
		}
		String attrMode=element.getAttribute(ATTR_ATTR_MODE);
		if(StringUtil.isEmpty(attrMode)==false){
			model.setAttrMode( attrMode);	
		}
		
		model.setxPathList(xPathList);
	}
	public static AnalysisXMLFileStructureModel fromXMLElement(Element element) {
		AnalysisXMLFileStructureModel model=new AnalysisXMLFileStructureModel();

		fillColumnElements(element, model);
		
		fillCommonField4XML(element, model);
		return model;
	}
	
 
}
