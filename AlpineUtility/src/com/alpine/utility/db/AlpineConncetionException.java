/**
 * ClassName  AlpineConncetionException.java
 *
 * Version information: 1.00
 *
 * Data: 2011-11-26
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.db;



public class AlpineConncetionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2090836104235932980L;
	/**
	 * 
	 */
	private String errorMsg;
	
	public AlpineConncetionException(Exception e){
		super(e.getMessage(),e);
	}

	public AlpineConncetionException(String errorMsg) {
		super(errorMsg);
		this.errorMsg=errorMsg;
	}
	public AlpineConncetionException(String errorMsg, Exception e) {
		super(errorMsg,e);
		this.errorMsg=errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
	

}
