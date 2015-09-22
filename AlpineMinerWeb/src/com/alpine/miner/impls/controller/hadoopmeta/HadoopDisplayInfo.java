/**
 * 
 */
package com.alpine.miner.impls.controller.hadoopmeta;

import com.alpine.utility.hadoop.HadoopFile;

/**
 * ClassName: HadoopDisplayInfo.java
 * <p/>
 * Data: 2012-6-7
 * <p/>
 * Author: Gary
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class HadoopDisplayInfo {

	private String 	key,
					name;
	private boolean isDir;
	
	public HadoopDisplayInfo(){}
	
	public HadoopDisplayInfo(HadoopFile proxy){
		this.key = proxy.getFullPath();
		this.name = proxy.getName();
		this.isDir = proxy.isDir();
	}
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the isDir
	 */
	public boolean isDir() {
		return isDir;
	}
	/**
	 * @param isDir the isDir to set
	 */
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}
}
