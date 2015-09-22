/**
 * ClassName HadoopFile.java
 *
 * Version information: 1.00
 *
 * Data: 2012-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.hadoop;

import java.util.ArrayList;
import java.util.List;


public class HadoopFile {
	
 
	public static final String SEPARATOR = "/"; 
	private String connName;
 
	private String name ;
	private long blockSize ;
	private String group;
	private String parentDir;
	private long length;
	private String owner;
	private String permission;
	private long modificationTime;
	private long accessTime;
	private	List<HadoopFile> children;
	private boolean dir;
	private short replication;
	 
	public String getParentDir() {
		return parentDir;
	}

	public void setParentDir(String parentDir) {
		this.parentDir = parentDir;
	}

	//getfulklpath ...
	public long getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}

	public long getAccessTime() {
		return accessTime;
	}

	public void getAccessTime(long lastAccessTime) {
		this.accessTime = lastAccessTime;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public long getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(long modificationTime) {
		this.modificationTime = modificationTime;
	}
 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HadoopFile> getChildren() {
		return children;
	}

	public void setChildren(List<HadoopFile> children) {
		this.children = children;
	}
	
	public void addChildren(List<HadoopFile> childs) {
		if(this.children==null){
			this.children=new ArrayList<HadoopFile>();
		}
		this.children.addAll(childs);
	}
	
	public void addChild(HadoopFile child) {
		if(this.children==null){
			this.children=new ArrayList<HadoopFile>();
		}
		this.children.add(child);
	}
	
	public String getFullPath() {
		if(parentDir!=null&&parentDir.endsWith(SEPARATOR)==false){
			parentDir=parentDir+SEPARATOR;
		}
		return getParentDir() +getName();
	}

	public String getConnName() {
		return connName;
	}

	public void setConnName(String connName) {
		this.connName = connName;
	}

	public void setIsDir(boolean dir) {
		this.dir=dir;
		
	}
	
	public boolean isDir(  ) {
		return dir;
		
	}


	public short getReplication() {
		return replication;
	}

	public void setReplication(short replication) {
		this.replication=replication;
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HadoopFile [connName=");
		builder.append(connName);
		builder.append(", name=");
		builder.append(name);
		builder.append(", blockSize=");
		builder.append(blockSize);
		builder.append(", group=");
		builder.append(group);
		builder.append(", parentDir=");
		builder.append(parentDir);
		builder.append(", length=");
		builder.append(length);
		builder.append(", owner=");
		builder.append(owner);
		builder.append(", permission=");
		builder.append(permission);
		builder.append(", modificationTime=");
		builder.append(modificationTime);
		builder.append(", lastAccessTime=");
		builder.append(accessTime);
		builder.append(", children=");
		builder.append(children);
		builder.append(", dir=");
		builder.append(dir);
		builder.append(", replication=");
		builder.append(replication);
		builder.append("]");
		return builder.toString();
	}
	
	
	
	
}
