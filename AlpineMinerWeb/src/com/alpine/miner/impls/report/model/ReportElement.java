/**
 * ClassName :ReportElement.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-3
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

/**
 * @author zhaoyong
 *
 */
public interface ReportElement {

	public abstract String getId();

	public abstract void setId(String id);

	public abstract String getTitle();

	public abstract void setTitle(String title);
	
	//mostly will be css class
	public abstract String getStyleId();

	public abstract void setStyleId(String styleID);
	

}