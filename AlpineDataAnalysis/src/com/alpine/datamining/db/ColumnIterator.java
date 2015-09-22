/**
 * ClassName ColumnIterator.java
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
 * Iterates columns.
 * 
 * @author  Eason
 */
public class ColumnIterator implements Iterator<Column> {
	
	/**
	 * the columns iterator
	 */
	private Iterator<Column> parent;
	
	/**
	 * column type, regular or specical
	 */
	private int type = Column.REGULAR;
	
	/**
	 * current column
	 */
	private Column current = null;
	
	/**
	 * indicate has next invoked
	 */
	private boolean hasNextInvoked = false;

	/**
	 * current column
	 */
	private Column currentColumn = null;
	
	public ColumnIterator(Iterator<Column> parent, int type) {
		this.parent = parent;
		this.type = type;
	}
	
	/**
	 * see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		this.hasNextInvoked = true;
		if (!parent.hasNext() && currentColumn == null) {
			current = null;
			return false;
		} else {
			Column column;
			if (currentColumn  == null) {
				column = parent.next();
			} else {
				column = currentColumn;
			}
			switch (type) {
			case Column.REGULAR:
				if (!column.isSpecial()) {
					current = column;
					currentColumn = column;
					return true;
				} else {
					return hasNext();
				}
			case Column.SPECIAL:
				if (column.isSpecial()) {
					current = column;
					currentColumn = column;					
					return true;
				} else {
					return hasNext();
				}
			case Column.ALL:
				current = column;
				currentColumn = column;				
				return true;
			default:
				current = null;
				return false;
			}
		}
	}

	/**
	 * see java.util.Iterator#next()
	 */
	public Column next() {
		if (!this.hasNextInvoked)
			hasNext();
		this.hasNextInvoked = false;
		this.currentColumn = null;
		return current;
	}

	/**
	 * see java.util.Iterator#remove()
	 */
	public void remove() {
		parent.remove();
		this.currentColumn = null;
		this.hasNextInvoked = false;
	}
}
