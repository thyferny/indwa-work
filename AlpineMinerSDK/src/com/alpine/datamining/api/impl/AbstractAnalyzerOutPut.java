/**
 * ClassName AbstractAnalyzerOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

import com.alpine.datamining.api.AnalyticNode;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.VisualizationOutPut;


/**
 * @author John Zhao
 * 
 */
public class AbstractAnalyzerOutPut    implements AnalyticOutPut {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2267725655868827212L;
 
	private VisualizationOutPut visualOutPut;
	
	AnalyticNode analyticNode;
 
	
	private String visualizationTypeClass;
	
	private AnalyticNodeMetaInfo nodeMetaInfo;

	private DataAnalyzer analyzer;

	private String extraMessage =null;
	
	public String getVisualizationTypeClass() {
		return visualizationTypeClass;
	}

	public void setVisualizationTypeClass(String visualizationTypeClass) {
		this.visualizationTypeClass = visualizationTypeClass;
	}

	public VisualizationOutPut getVisualizationOutPut() {
		return visualOutPut;
	}

	public void setVisualizationOutPut(VisualizationOutPut visualOutPut) {
		this.visualOutPut = visualOutPut;
	}
 
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyzerOutPut#getAnalyticNode()
	 */
	@Override
	public AnalyticNode getAnalyticNode() {
		// TODO Auto-generated method stub
		return analyticNode;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyzerOutPut#setAnalyticNode(com.alpine.datamining.api.AnalyticNode)
	 */
	@Override
	public void setAnalyticNode(AnalyticNode node) {
		this.analyticNode=node;
		
	}

	public AnalyticNodeMetaInfo getAnalyticNodeMetaInfo(){
		return nodeMetaInfo;
	}
	public void setAnalyticNodeMetaInfo(AnalyticNodeMetaInfo nodeMetaInfo){
		this.nodeMetaInfo=nodeMetaInfo;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticOutPut#getDataAnalyzer()
	 */
	@Override
	public DataAnalyzer getDataAnalyzer() {
		return analyzer;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.AnalyticOutPut#setDataAnalyzer(com.alpine.datamining.api.DataAnalyzer)
	 */
	@Override
	public void setDataAnalyzer(DataAnalyzer analyzer) {
		this.analyzer=analyzer;
		
	}

	public void setExtraLogMessage(String message)  {
		this.extraMessage  = message;
	}
	public String getExtraLogMessage(){
		return this.extraMessage;
	}
}

