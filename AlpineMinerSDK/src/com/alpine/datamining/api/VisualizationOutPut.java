/**
 * ClassName VisuliazationOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;


/**
 * @author John Zhao
 *
 */
public interface VisualizationOutPut   {

	public VisualizationType getVisualizationType();
	
	public DataAnalyzer getAnalyzer();
	
	public Object getVisualizationObject();
	public void setAnalyzer(DataAnalyzer analyzer);
	public void setVisualizationType(VisualizationType vType);
	
	public String getName();
	public String getDescription();
	public void setName(String name);
	public void setDescription(String description);
}
