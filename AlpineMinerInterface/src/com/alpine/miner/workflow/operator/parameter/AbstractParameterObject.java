/**
 * ClassName :AbstractParameterObject.java
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
 * @author zhaoyong
 *
 */
public abstract class AbstractParameterObject implements ParameterObject {
	
	@Override
	public void initFromXmlElement(Element element){
		// TODO Auto-generated method stub
	}
  
	@Override
	public Element toXMLElement(Document xmlDoc) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public  Object clone() throws CloneNotSupportedException{
		return new CloneNotSupportedException();
	}
 
}
