/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ICategory
 * Feb 17, 2012
 */
package com.alpine.miner.impls.categorymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Personal flow category information
 * @author Gary
 *
 */
public class FlowCategory implements FlowDisplayModel {
	// For File persistence implement: 
	//		the key of field means file path start at root of flow. and generate from name by persistence.
	private String 	key,
					name;
	private boolean isCategory = true;
	
	private List subItems = new ArrayList();
	
	private String parentKey;

	//for invoked by JSON framework 
	public FlowCategory(){
		
	}
	
	public FlowCategory(String name){
		this.name = name;
	}
	
	public FlowCategory(String key, String name){
		this.key = key;
		this.name = name;
	}

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

	public String getParentKey() {
		return this.parentKey;
	}

	public void setParentKey(String parent) {
		this.parentKey = parent;
	}
	
	public List getSubItems(){
		return this.subItems;
	}

	public void setSubItems(List subItems) {
		this.subItems = subItems;
	}

	public boolean isCategory() {
		return isCategory;
	}

	public void setCategory(boolean isCategory) {
		this.isCategory = isCategory;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.categorymanager.model.FlowDisplayModel#getPath()
	 */
	@Override
	public String getPath() {
		return key;
	}
}
