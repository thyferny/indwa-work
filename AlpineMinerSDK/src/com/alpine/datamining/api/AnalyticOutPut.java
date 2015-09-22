/**
 * ClassName AnalyzerOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import java.io.Serializable;


/**
 * @author John Zhao
 *
 */
public interface AnalyticOutPut extends Serializable{
	public DataAnalyzer getDataAnalyzer();
	public void setDataAnalyzer(DataAnalyzer analyzer);
	//source or this output
	public AnalyticNode getAnalyticNode();
	public void setAnalyticNode(AnalyticNode node);
	//operator out put
//	public Container getOperatorOutPut();
	
	public String getVisualizationTypeClass();
	public void setVisualizationTypeClass(String visualTypeClass);
	
	public VisualizationOutPut getVisualizationOutPut();
	public void setVisualizationOutPut(VisualizationOutPut outPut);
	//time cost
	public AnalyticNodeMetaInfo getAnalyticNodeMetaInfo();
	public void setAnalyticNodeMetaInfo(AnalyticNodeMetaInfo nodeMetaInfo);

	
	public void setExtraLogMessage(String message)  ;
	public String getExtraLogMessage();
}
