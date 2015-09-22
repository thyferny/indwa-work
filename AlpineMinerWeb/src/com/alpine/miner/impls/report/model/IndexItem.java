/**
 * ClassName :IndexItem.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-7
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoyong
 *
 */
public class IndexItem extends AbstractReportElement{

	private String refID;

	private List<IndexItem> children;
	public List<IndexItem> getChildren() {
		return children;
	}

	public void setChildren(List<IndexItem> children) {
		this.children = children;
	}
	
	public void addChild(IndexItem child) {
		if(children==null){
			children= new ArrayList<IndexItem>();
		}
		children.add(child) ;
	}

	public String getRefID() {
		return refID;
	}

	public void setRefID(String refID) {
		this.refID = refID;
	}

	/**
	 * @param id
	 * @param title
	 */
	protected IndexItem(String id, String title,String refID) { 
		super(id, title);
		//this is the index referenced ID or name of html or others...
		this. refID = refID; 

		
		
	}

}
