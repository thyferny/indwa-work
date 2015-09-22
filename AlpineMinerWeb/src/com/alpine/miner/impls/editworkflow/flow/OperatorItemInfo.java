/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OperatorItemInfo.java
 */
package com.alpine.miner.impls.editworkflow.flow;

/**
 * @author Gary
 * Aug 2, 2012
 */
public class OperatorItemInfo {

	private String uuid;
	private String newUUID;
	private String newName;

	public String getNewUUID() {
		return newUUID;
	}

	public void setNewUUID(String newUUID) {
		this.newUUID = newUUID;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
