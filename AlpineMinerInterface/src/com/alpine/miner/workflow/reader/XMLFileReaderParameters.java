/**
 * ClassName XMLFileReaderParameters.java
 *
 * Version information: 1.00
 *
 * Data: 2011/04/02
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;


public class XMLFileReaderParameters extends AbstractReaderParameters {
	
	String filePath;
	String userName;
	ResourceType resourceType;
	
//	public XMLFileReaderParameters(String filePath) {
//		super();
//		this.filePath = filePath;
//	}

	public XMLFileReaderParameters(String filePath,String userName,ResourceType resourceType) {
		super();
		this.filePath = filePath;
		this.userName=userName;
		this.resourceType=resourceType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

}
