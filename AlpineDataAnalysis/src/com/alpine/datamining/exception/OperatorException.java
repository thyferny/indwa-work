/**
 * ClassName OperatorException.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.exception;

import com.alpine.datamining.operator.Operator;

/**
 * Exception class whose instances are thrown by instances of the class
 * {@link Operator} or one of its subclasses.
 * 
 * 
 *          
 */
public class OperatorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9065401316329372281L;

	public OperatorException(String message) {
		super(message);
	}
	public OperatorException(Throwable cause) {
		super(cause);
	}
	public OperatorException(String message, Throwable cause) {
		super(message, cause);
	}
}
