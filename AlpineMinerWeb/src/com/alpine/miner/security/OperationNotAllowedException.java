/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * OperationNotAllowedException.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security;

/**
 * @author sam_zang
 *
 */
public class OperationNotAllowedException extends Exception {

	private static final long serialVersionUID = 709144442980147592L;

	/**
	 * 
	 */
	public OperationNotAllowedException() {
	}

	/**
	 * @param message
	 */
	public OperationNotAllowedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public OperationNotAllowedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OperationNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

}
