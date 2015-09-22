/**
 * 
 */
package com.alpine.miner.impls.taskmanager;

/**
 * @author Gary
 *
 */
public class ExecuteException extends RuntimeException {

	public ExecuteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecuteException(String message) {
		super(message);
	}

	public ExecuteException(Throwable cause) {
		super(cause);
	}

}
