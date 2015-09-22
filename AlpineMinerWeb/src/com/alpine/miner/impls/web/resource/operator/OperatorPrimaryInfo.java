/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OperatorPrimaryInfo.java
 */
package com.alpine.miner.impls.web.resource.operator;


/**
 * @author Gary
 * Jul 9, 2012
 */
public abstract class OperatorPrimaryInfo {
	/**
	 * operatorType
	 */
	public static final String 	DATABASE_TYPE = "DB",
								HADOOP_TYPE = "HADOOP";
	
	private String uid;
	private String name;
	private String classname;
	private int x;
	private int y;
	private boolean isValid;
	private String connectionName;
	private String operatorType;
	private String description;
	
	public static String getDatabaseType() {
		return DATABASE_TYPE;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}
	public static String getHadoopType() {
		return HADOOP_TYPE;
	}
	public String getUid() {
		return uid;
	}
	public String getName() {
		return name;
	}
	public String getClassname() {
		return classname;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public boolean isValid() {
		return isValid;
	}
	public String getConnectionName() {
		return connectionName;
	}
	public String getOperatorType() {
		return operatorType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
