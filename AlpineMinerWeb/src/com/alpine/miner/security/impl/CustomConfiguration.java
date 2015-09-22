/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * CustomConfiguration.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 11, 2012
 */
package com.alpine.miner.security.impl;

/**
 * @author sam_zang
 *
 */
public class CustomConfiguration {
	private String jarFile;
	private String className;
	
	CustomConfiguration() {
		this.jarFile = "";
		this.className = "";
	}
	
	/**
	 * @return the jar File
	 */
	public String getJarFile() {
		return jarFile;
	}
	/**
	 * @param jarFile the jar File to set
	 */
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}
	/**
	 * @return the class Name
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the class Name to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

}
