/**
 * ClassName AnalysisLogFileStructureModel.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-26
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop.filestructure;

import org.w3c.dom.Element;

import com.alpine.utility.file.StringUtil;

public class AnalysisLogFileStructureModel extends
		AbstractFileStructureModel {
	public static final String ATTR_LOG_FORMAT="LogFormat";
	public static final String ATTR_LOG_TYPE="LogType";
	public static final String TAG_NAME="AlpineLogFileStructureModel";
		
	//"%d %{}i %r"
	private String logFormat;
	private String logType;
	public String getLogFormat() {
		return logFormat;
	}
	
	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}
	
	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	@Override
	public AnalysisLogFileStructureModel clone()  throws CloneNotSupportedException {
		AnalysisLogFileStructureModel model= new AnalysisLogFileStructureModel();
		
		cloneCommonField(model);
		return model;
	}

	protected void cloneCommonField(AnalysisLogFileStructureModel model)
			throws CloneNotSupportedException {
		super.cloneCommonField(model);
		model.setLogFormat(logFormat);
		model.setLogType(logType);
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnalysisLogFileStructureModel){
			return  super.equals((AnalysisFileStructureModel)obj)&&StringUtil.safeEquals(logFormat,
					((AnalysisLogFileStructureModel)obj).getLogFormat())&&StringUtil.safeEquals(logType,
							((AnalysisLogFileStructureModel)obj).getLogType());
	}else{
		return false;
	}
	}
	
	public static AnalysisLogFileStructureModel fromXMLElement(Element element) {
		AnalysisLogFileStructureModel model=new AnalysisLogFileStructureModel();

		fillColumnElements(element, model);
		
		String logFormat=element.getAttribute(ATTR_LOG_FORMAT);
		model.setLogFormat(logFormat);
		String logType=element.getAttribute(ATTR_LOG_TYPE);
		model.setLogType(logType);
		return model;
	}
}
