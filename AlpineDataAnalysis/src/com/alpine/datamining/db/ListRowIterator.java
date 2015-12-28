
package com.alpine.datamining.db;

import java.util.Iterator;



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
