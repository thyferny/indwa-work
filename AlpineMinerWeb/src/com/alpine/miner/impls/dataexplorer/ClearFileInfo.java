/**
* ClassName ClearFileInfo.java
*
* Version information: 1.00
*
* Data: Jan 18, 2013
*
* COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
**/
package com.alpine.miner.impls.dataexplorer;
/**
 * @author Jeff Dong
 *
 */
public class ClearFileInfo {

	private String id, uuid, operatorName, connectionName, filePath, resourceType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
}
