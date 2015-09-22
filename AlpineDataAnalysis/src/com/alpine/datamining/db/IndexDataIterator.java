/**
 * ClassName IndexDataIterator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;



/**
 * Returns only a subset of an data set specified by an instance of
 * Subsets.
 * @author Eason
 */
public class IndexDataIterator extends AbstractDataIterator {

	/**
	 * Index of the current data.
	 */
	private int current;

	/**
	 * dataset
	 */
	private DataSet parent;
	
	/**
	 * The next data that will be returned.
	 */
	private Data next;

	/**
	 * size of dataset
	 */
	private long size;
	
	public IndexDataIterator(DataSet parent) {
		this.parent = parent;
		this.size = parent.size();
		current = -1;
		hasNext();
	}

	public boolean hasNext() {
		while (next == null) {
			current++;
			
			if (current >= size)
				return false;
			
			next = parent.getRow(current);
		}
		return true;
	}

	public Data next() {
		if (!hasNext()) {
			return null;
		} else {
			Data dummy = next;
			next = null;
			return dummy;
		}
	}

}
