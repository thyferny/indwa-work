/**
 * ClassName FileStructureModelFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-29
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.utility.xml.XmlDocManager;

/**
 * @author Jeff Dong
 *
 */
public class FileStructureModelFactory {
	
	public static FileStructureModel createFileStructureModelByXML(XmlDocManager opTypeXmlManager,
			Node operatorNode){
		
		List<Node> nodeList = opTypeXmlManager.getNodeList(
				operatorNode, CSVFileStructureModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			CSVFileStructureModel csvFileStructureModel = CSVFileStructureModel.fromXMLElement(element);
					return csvFileStructureModel;
		}
		
		nodeList = opTypeXmlManager.getNodeList(
				operatorNode, XMLFileStructureModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			XMLFileStructureModel xmlFileStructureModel = XMLFileStructureModel.fromXMLElement(element);
			return xmlFileStructureModel;
		}
		
		nodeList = opTypeXmlManager.getNodeList(
				operatorNode, AlpineLogFileStructureModel.TAG_NAME);
		if (nodeList != null && nodeList.size() > 0) {
			Element element = (Element) nodeList.get(0);
			AlpineLogFileStructureModel logFileStructureModel = AlpineLogFileStructureModel.fromXMLElement(element);
			return logFileStructureModel;
		}

        nodeList = opTypeXmlManager.getNodeList(
                operatorNode, JSONFileStructureModel.TAG_NAME);
        if (nodeList != null && nodeList.size() > 0) {
            Element element = (Element) nodeList.get(0);
            JSONFileStructureModel jsonFileStructureModel = JSONFileStructureModel.fromXMLElement(element);
            return jsonFileStructureModel;
        }
		return null;
	}

}
