/**
 * ClassName AnnalysiticSource.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api;


/**
 * @author John Zhao
 *
 */
public interface AnalyticSource extends AnalyticSourceFromXml {
	public AnalyticConfiguration getAnalyticConfig();
	public void  setAnalyticConfiguration(AnalyticConfiguration config);
	public String getDataSourceType();
	public void setNameAlias(String name);
	public String getNameAlias();
}
