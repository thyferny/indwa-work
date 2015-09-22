/**
 * ClassName NoBugError.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.exception;

/**
 * Interface of all exceptions that are no bugs (but caused by an error of the user).
 * 
 * @author Eason
 */
public interface WrongUsed {

	/** Returns the error details/description. */
	public String getMessage();
}
