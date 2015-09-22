/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * LinkBaseException.java
 */
package com.alpine.miner.impls.editworkflow.link.exception;

/**
 * @author Gary
 * Jul 16, 2012
 */
public class LinkBaseException extends Exception {

	/**
	 * @param message
	 * @param cause
	 */
	public LinkBaseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public LinkBaseException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public LinkBaseException(Throwable cause) {
		super(cause);
	}

	
}
