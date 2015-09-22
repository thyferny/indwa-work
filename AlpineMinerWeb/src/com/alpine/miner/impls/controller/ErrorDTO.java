/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ErrorDTO.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 30, 2011
 */
package com.alpine.miner.impls.controller;

/**
 * @author sam_zang
 *
 */
public class ErrorDTO {
	public static final int UNKNOW_ERROR=9999;
	public static final int OPERATION_SUCCESS = 0;
	private String message;
	private int error_code; 
	
	public ErrorDTO(int code) {
		this.error_code = code;
	}

	public ErrorDTO() {
		this.error_code = -1; // default.-- not login
	}
	
	public ErrorDTO(String message) { 
		this.message=message;
		this.error_code = -1; // default.-- not login
	}
	
	public ErrorDTO(int code, String message) { 
		this.message=message;
		this.error_code = code;
	}

	public String getMessage() {
		
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return error_code;
	}

	public void setCode(int code) {
		this.error_code = code;
	}
}
