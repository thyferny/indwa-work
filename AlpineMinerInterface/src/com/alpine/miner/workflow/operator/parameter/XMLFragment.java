/**
 * ClassName :XMLElement.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XMLElement represent a dom node for ParametObject and some small parameter fileds of ParameterObject
 * It can be cloned for UI use reason.
 * It also must implement the equals method 
 * @author zhaoyong
 *
 */
public interface XMLFragment extends Cloneable {
	//only 2 model implements the xml model... now
	public Element toXMLElement(Document xmlDoc);
	
	public void initFromXmlElement(Element element);
	
	public String getXMLTagName();
	
	public  Object clone() throws CloneNotSupportedException;
}
