/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * InvalidDateException.java
 */
package com.alpine.importdata;

/**
 * @author Gary
 * Aug 17, 2012
 */
public class InvalidTimeException extends RuntimeException {

	private String dataVal;
	
	/**
	 * @param message
	 */
	public InvalidTimeException(String dataVal) {
		super("Invalid date type:" + dataVal);
		this.dataVal = dataVal;
	}

	public InvalidTimeException() {
		super();
	}

	public String getInvalidDateValue(){
		return dataVal;
	}
}
