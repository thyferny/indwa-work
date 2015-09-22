/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * FlowInfo.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */
 
package com.alpine.miner.impls.web.resource;

import com.alpine.miner.impls.resource.ResourceInfo;


/**
 * @author sam_zang
 *
 */
public class FlowInfo extends ResourceInfo {
	enum TagType { TOP, CHILD }

	public static final String INIT_VERSION= "1";
	
	public FlowInfo(String user, String id, ResourceType type) {
		super(user, id, type);
		xmlString = "";
		tag = TagType.TOP;
		if (type == ResourceType.Group) {
			tag = TagType.CHILD;
//			setGroupName(user);
		}
	}

	public FlowInfo() {
		xmlString = "";
		tag = TagType.TOP;
	}
	
	private String xmlString;
	private TagType tag;
	
	 
	
	/**
	 * @return the xmlString
	 */
	public String getXmlString() {
		return xmlString;
	}

	/**
	 * @param xmlString the xmlString to set
	 */
	public void setXmlString(String xmlString) {
		this.xmlString = xmlString;
	}

	/**
	 * 
	 */
	public void setChild() {
		tag = TagType.CHILD;	
	}

 
}
