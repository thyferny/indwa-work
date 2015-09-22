/**
 * ClassName Row.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.io.Serializable;



/**
 * This interface defines methods for all entries of Table
 * implementations. 
 * 
 * @author Eason
 */
public abstract class Row implements Serializable {

	private static final long serialVersionUID = 2953321020546996288L;

	/**
	 * @param index
	 * @param defaultValue
	 * @return the value for the given index.
	 */
	protected abstract double get(int index, double defaultValue);

	/**
	 * Sets the given data for the given index. 
	 * @param index
	 * @param value
	 * @param defaultValue
	 */
	protected abstract void set(int index, double value, double defaultValue);

	/**
	 * Ensures that throw a runtime exception for all 0 <= i <= numberOfColumns.
	 * @param numberOfColumns
	 */
	protected abstract void ensureNumberOfColumns(int numberOfColumns);

	
	public abstract String toString();
	
	/**
	 * @param column
	 * @return Returns the value stored at the given {@link Column}'s index. 
	 *  Returns Double.NaN if the given column is null.
	 */
	public double get(Column column) {
		if (column == null) {
			return Double.NaN;
		} else {
			try {
				return column.getValue(this);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ArrayIndexOutOfBoundsException("table index " + column.getTableIndex() + " of Column " + column.getName() + " is out of bounds.");
			}
		}
	}
	
	/**
	 * set column value;
	 * @param column
	 * @param value
	 */
	public void set(Column column, double value) {
		column.setValue(this, value);
	}
}
