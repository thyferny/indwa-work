/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * DatasetTransformationException
 * Mar 7, 2012
 */
package com.alpine.miner.impls.web.resource.operator.dataset;

/**
 * @author Gary
 *
 */
public class DatasetTransformationException extends Exception {

	public static enum ExceptionType{
		TRANSFORMATION_FAIL;
	}
	
	/**
	 * 
	 */
	public DatasetTransformationException(ExceptionType exceptionType) {
		super(exceptionType.name());
	}
}
