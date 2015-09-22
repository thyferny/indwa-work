/**
 * ClassName VisuliazationManager.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow;

import com.alpine.datamining.api.AnalyticOutPut;

/**
 * @author John Zhao
 *
 */
public interface VisualizationManager {
	 
	//get out put from oprator as input
	//generate  Visuliazation output
	public static final VisualizationManager instance=new VisualizationManagerImpl();
	//will set VisualizationOutPut into AnalyticOutPut
	public void visual(AnalyticOutPut outPut, boolean drawChart) throws Exception;
}
