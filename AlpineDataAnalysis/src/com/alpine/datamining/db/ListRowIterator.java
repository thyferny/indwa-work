/**
 * ClassName ListRowIterator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.Iterator;


/**
 * Iterates over a list of DataRows. This class does not use a list but an iterator over an arbitrary collection.
 * @author Eason
 */
public class ListRowIterator implements RowIterator {

	private Iterator<Row> iterator;

	public ListRowIterator(Iterator<Row> i) {
		this.iterator = i;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public Row next() {
		return iterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException("Not supported!");
	}
}
