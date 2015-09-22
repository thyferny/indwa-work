/**
 * ClassName FrequencyStack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

/**
 * A stack for frequencies.
 * 
 * @author Eason
 */
public interface FrequencyStack {

	/**
	 * Increases the frequency stored on stackHeight level of stack by value
	 * 
	 * @param stackHeight
	 *            describes the level of stack,
	 * @param weight
	 *            is the amount added
	 */
	public void increaseFrequency(int stackHeight, long weight);

	/**
	 * This method deletes the heightTH element of stack.
	 */
	public void popFrequency(int height);

	/**
	 * Returns the frequency stored on height of stack.
	 */
	public long getFrequency(int height);
}
