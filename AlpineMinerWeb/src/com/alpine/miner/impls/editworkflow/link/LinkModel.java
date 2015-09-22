/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LinkModel.java
 */
package com.alpine.miner.impls.editworkflow.link;

/**
 * @author Gary
 * Aug 9, 2012
 */
public class LinkModel {

	private String sourceId, targetId;
	
	public LinkModel(String sourceId, String targetId) {
		this.sourceId = sourceId;
		this.targetId = targetId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTargetId() {
		return targetId;
	}
}
