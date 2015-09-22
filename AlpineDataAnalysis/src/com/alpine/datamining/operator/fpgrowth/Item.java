/**
 * ClassName Item.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.io.Serializable;

/**
 * Item the base class for itemsets and provide all frequency information.
 * 
 * @author Eason
 */
public interface Item extends Comparable<Item>, Serializable {


	public long getFrequency();


	public void increaseFrequency(long frequency);

	public String toString();
	
}
