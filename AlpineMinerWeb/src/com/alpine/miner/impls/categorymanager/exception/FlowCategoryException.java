/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowCategoryException.java
 * 2012-2-20
 */
package com.alpine.miner.impls.categorymanager.exception;

/**
 * @author Gary
 */
public class FlowCategoryException extends Exception {
	private static final String EXCEPTION_PREFIX = "flow_category_error_";
	private ExceptionType type;
	/**
	 * @param message
	 */
	public FlowCategoryException(ExceptionType message) {
		super(EXCEPTION_PREFIX + message.name());
	}
	
	public FlowCategoryException(ExceptionType message, Throwable e) {
		super(EXCEPTION_PREFIX + message.name(), e);
	}

	/**
	 * @return the type
	 */
	public ExceptionType getType() {
		return type;
	}
	

	public static enum ExceptionType{
		CANNOT_READ_STORE_FILE,
		SAVE_STORE_FAILED,//create failed.
		NAME_ALREADY_EXISTS,//create failed.
		PARENT_ISNOT_FOLDER,//create failed. TODO
		SOME_FLOW_IN_CATEGORY,//delete failed.
		CANNOT_FIND_CATEGORY,//rename failed.
		RENAME_FAILED;
	}
}
