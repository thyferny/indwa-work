/**
 * ClassName XMLOperatorParametersIOPara.java
 *
 * Version information: 1.00
 *
 * Data: 2011/04/02
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import org.w3c.dom.Node;

import com.alpine.utility.xml.XmlDocManager;

public class XMLOperatorParametersIOPara  {
	
	private XmlDocManager opTypeXmlManager;
	private Node node;
	public XMLOperatorParametersIOPara(XmlDocManager opTypeXmlManager, Node node) {
		super();
		this.opTypeXmlManager = opTypeXmlManager;
		this.node = node;
	}
	public XmlDocManager getOpTypeXmlManager() {
		return opTypeXmlManager;
	}
	public void setOpTypeXmlManager(XmlDocManager opTypeXmlManager) {
		this.opTypeXmlManager = opTypeXmlManager;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}

}
