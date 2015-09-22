/**
 * ClassName AnalyticSourceToXml.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.util.Map;

import org.w3c.dom.Node;

import com.alpine.utility.xml.XmlDocManager;

public interface AnalyticSourceFromXml {
	public static final String OP_TYPE_ATTR = "type";
	public static final String OP_NAME_ATTR = "name";
	public static final String PARAMETER = "Parameter";
	public static final String VALUE = "value";
	public static final String KEY = "key";
	public void setSourceInfoByNodeIndex(
			XmlDocManager opTypeXmlManager, 
			Node opNode,int index,
			Map<String,String> variableMap);
}
