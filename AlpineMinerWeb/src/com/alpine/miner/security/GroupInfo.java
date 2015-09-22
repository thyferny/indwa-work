/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * GroupInfo.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 22, 2011
 */
 
package com.alpine.miner.security;

/**
 * @author sam_zang
 *
 */
public class GroupInfo {
	public static final String ID = "id";
	public static final String OWNER = "owner";
	public static final String DESC = "description";
	
	public GroupInfo() { description = ""; }
	public GroupInfo(String id) {
		this.id = id;
		description = "";
	}
	
	public GroupInfo(String id, String desc) {
		this.id = id;
		this.description = desc;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	private String id;
	private String owner;
	private String description;
}
