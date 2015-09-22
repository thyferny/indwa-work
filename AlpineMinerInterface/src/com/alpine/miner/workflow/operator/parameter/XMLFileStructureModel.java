/**
 * ClassName XMLFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-25
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisXMLFileStructureModel;

/**
 * @author Jeff Dong
 *
 */
public class XMLFileStructureModel extends AnalysisXMLFileStructureModel implements FileStructureModel{

	
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element element = xmlDoc.createElement(TAG_NAME);
		
		element.setAttribute(ATTR_ATTR_MODE, getAttrMode());
		element.setAttribute(ATTR_CONTAINER, getContainer());
		element.setAttribute(ATTR_XMLDataStructureType, getXmlDataStructureType());
		element.setAttribute(ATTR_CONTAINER_XPATH, getContainerXPath());
		
		if(getxPathList()!=null){
			for(String xPath:getxPathList()){
				Element xPathEle=xmlDoc.createElement(XPATH_TAG_NAME);
				xPathEle.setAttribute(ATTR_XPATH, xPath);
				element.appendChild(xPathEle);
			}	
		}
		
		FileStructureModelUtility.fillCommonXMLElement(this,xmlDoc, element);
		return element;
	}

	
	public static XMLFileStructureModel fromXMLElement(Element element) {
		XMLFileStructureModel model=new XMLFileStructureModel();

		fillCommonField4XML(element, model);
		
		fillColumnElements(element, model)	;
		return model;
	}


	@Override
	public void initFromXmlElement(Element element) {
		//no use
		
	}

	public XMLFileStructureModel clone() throws CloneNotSupportedException {
		XMLFileStructureModel model= new XMLFileStructureModel();
		 super.cloneCommonField(model);
		return model;
	}

}
