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
public abstract class AbstractReportElement implements ReportElement {

	String id = null; 

	String title = null;

	private String styleId =null; 

	protected AbstractReportElement(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
	//mostly will be css class
	public   String getStyleId(){
		return styleId;
	}

	public   void setStyleId(String styleID){
		this.styleId=styleID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.miner.impls.report.model.ReportElement#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.impls.report.model.ReportElement#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.miner.impls.report.model.ReportElement#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.impls.report.model.ReportElement#setTitle(java.lang.
	 * String)
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

}
