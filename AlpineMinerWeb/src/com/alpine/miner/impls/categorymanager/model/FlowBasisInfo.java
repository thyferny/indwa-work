/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ICategory
 * Feb 17, 2012
 */
package com.alpine.miner.impls.categorymanager.model;

import com.alpine.miner.impls.web.resource.FlowInfo;

/**
 * personal flow information
 * @author Gary
 *
 */
public class FlowBasisInfo implements FlowDisplayModel {
	
	private String 	key,
					name;
	
	private String path;
	private String parentKey;
	
	private FlowInfo info;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FlowInfo getInfo() {
		return info;
	}

	public void setInfo(FlowInfo info) {
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.model.FlowDisplayModel#getParent()
	 */
	@Override
	public String getParentKey() {
		return this.parentKey;
	}

	public void setParentKey(String parent) {
		this.parentKey = parent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
