/**
 * ClassName  AnalyticFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;

import com.alpine.datamining.api.impl.AnalyticFlowImpl;
import com.alpine.datamining.api.impl.AnalyticNodeImpl;
import com.alpine.datamining.api.impl.AnalyticProcessImpl;

/**
 * @author John Zha0
 *
 */
public class AnalyticFactory {
	
	public static final AnalyticFactory instance=new AnalyticFactory();
	
	private AnalyticFactory(){
		
	}
	public AnalyticProcess createAnalyticProcess(String flowFilePath){
		return new AnalyticProcessImpl(flowFilePath);
	}
	
	public AnalyticFlow createAnalyticFlow(){
		return new AnalyticFlowImpl();
	}
	
	public AnalyticNode  createAnalyticNode(){
		return new AnalyticNodeImpl();
	}
}
