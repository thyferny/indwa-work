/**
* ClassName EmptyInputException.java
*
* Version information: 1.00
*
* Data: Jan 4, 2013
*
* COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
**/
package com.alpine.utility.exception;
/**
 * @author Jeff Dong
 *
 */
public class EmptyInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 147360405758334258L;

	public EmptyInputException(Exception e){
		super(e.getMessage(),e);
	}

	public EmptyInputException(String cause) {
		super(cause);
	}
}
