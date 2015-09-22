/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * DataOutOfSync.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jul 2, 2011
 */
 
package com.alpine.miner.impls.resource;

/**
 * @author sam_zang
 *
 */
public class DataOutOfSyncException extends Exception {

	/**
	 * @param arg0
	 */
	public DataOutOfSyncException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public DataOutOfSyncException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DataOutOfSyncException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}