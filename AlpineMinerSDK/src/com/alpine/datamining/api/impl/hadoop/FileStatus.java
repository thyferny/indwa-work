/**
 * ClassName AnalyzerOutPutHadoopFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;
/**
 * @author Eason
 * 
 */

public class FileStatus {
	String pathSuffix;
	String permission;
	String type;
	String owner;
	String group;
	int blockSize;
	public String getPathSuffix() {
		return pathSuffix;
	}
	public void setPathSuffix(String pathSuffix) {
		this.pathSuffix = pathSuffix;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public int getBlockSize() {
		return blockSize;
	}
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	
	@Override
	public String toString() {
		return "FileStatus [blockSize=" + blockSize + ", group=" + group
				+ ", owner=" + owner + ", pathSuffix=" + pathSuffix
				+ ", permission=" + permission + ", type=" + type + "]";
	}
}
