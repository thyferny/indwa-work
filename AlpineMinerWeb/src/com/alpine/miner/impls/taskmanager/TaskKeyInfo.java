/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * TaskKeyInfo.java
 */
package com.alpine.miner.impls.taskmanager;

/**
 * @author Gary
 * Dec 17, 2012
 */
public class TaskKeyInfo {

	private String 	userName,
					taskName;
	
	public TaskKeyInfo(String userName, String taskName){
		this.userName = userName;
		this.taskName = taskName;
	}

	public String getUserName() {
		return userName;
	}

	public String getTaskName() {
		return taskName;
	}
}
