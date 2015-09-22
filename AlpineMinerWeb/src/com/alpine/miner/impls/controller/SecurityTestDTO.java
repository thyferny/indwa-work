/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ErrorDTO.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 30, 2011
 */
package com.alpine.miner.impls.controller;

/**
 * @author sam_zang
 *
 */
public class SecurityTestDTO {
	private String message;
	private boolean connection; 
	private int userCount; 
	private int groupCount;
	
	public SecurityTestDTO() {
		connection = true;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the connection
	 */
	public boolean isConnection() {
		return connection;
	}
	/**
	 * @param connection the connection to set
	 */
	public void setConnection(boolean connection) {
		this.connection = connection;
	}
	/**
	 * @return the userCount
	 */
	public int getUserCount() {
		return userCount;
	}
	/**
	 * @param userCount the userCount to set
	 */
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
	/**
	 * @return the groupCount
	 */
	public int getGroupCount() {
		return groupCount;
	}
	/**
	 * @param groupCount the groupCount to set
	 */
	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	} 
	
	
}
