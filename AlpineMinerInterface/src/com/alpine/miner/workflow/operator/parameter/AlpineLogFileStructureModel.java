/**
 * ClassName AlpineLogFileStructureModel.java
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

import com.alpine.datamining.api.impl.algoconf.hadoop.filestructure.AnalysisLogFileStructureModel;

public class AlpineLogFileStructureModel extends AnalysisLogFileStructureModel implements FileStructureModel{
	
	public static final String ATTR_COLUMNNAME = "columnName";
	public static final String COLUMNNAMES_TAG_NAME = "columnNames";
	public static final String ATTR_COLUMNTYPE = "columnType";
	public static final String COLUMNTYPES_TAG_NAME = "columnTypes";
	

	
	@Override
	public String getXMLTagName() {
		return TAG_NAME;
	}
	

	@Override
	public Element toXMLElement(Document xmlDoc) {
		Element element = xmlDoc.createElement(TAG_NAME);
		
		element.setAttribute(ATTR_LOG_FORMAT, getLogFormat());
		element.setAttribute(ATTR_LOG_TYPE, getLogType());
		
		FileStructureModelUtility.fillCommonXMLElement(this,xmlDoc, element);
		return element;
	}

	
	public static AlpineLogFileStructureModel fromXMLElement(Element element) {
		AlpineLogFileStructureModel model=new AlpineLogFileStructureModel();

		fillColumnElements(element, model);
		
		String logFormat=element.getAttribute(ATTR_LOG_FORMAT);
		model.setLogFormat(logFormat);
		String logType=element.getAttribute(ATTR_LOG_TYPE);
		model.setLogType(logType);
		return model;
	}


	@Override
	public void initFromXmlElement(Element element) {
		//no use
	}

	public AlpineLogFileStructureModel clone() throws CloneNotSupportedException {
		AlpineLogFileStructureModel model= new AlpineLogFileStructureModel();
		super.cloneCommonField(model);
		return model;
	}

}
