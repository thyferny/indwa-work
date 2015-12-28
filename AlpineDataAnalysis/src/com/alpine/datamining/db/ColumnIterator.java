
package com.alpine.datamining.db;

import java.util.Iterator;



public class ColumnIterator implements Iterator<Column> {
	
	
	private Iterator<Column> parent;
	
	
	private int type = Column.REGULAR;
	
	
	private Column current = null;
	
	
	private boolean hasNextInvoked = false;

	
	private Column currentColumn = null;
	
	public ColumnIterator(Iterator<Column> parent, int type) {
		this.parent = parent;
		this.type = type;
	}
	
	
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

	
	public Column next() {
		if (!this.hasNextInvoked)
			hasNext();
		this.hasNextInvoked = false;
		this.currentColumn = null;
		return current;
	}

	
	public void remove() {
		parent.remove();
		this.currentColumn = null;
		this.hasNextInvoked = false;
	}
}
