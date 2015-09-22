/**
 * ClassName AbstractDataIterator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;


/** Abstract implementation which implements the method remove by throwing an Exception 
 * @author Eason  
 */
public abstract class AbstractDataIterator implements DataIterator {

	public void remove() {
		throw new UnsupportedOperationException("Not supported!");
	}
}
