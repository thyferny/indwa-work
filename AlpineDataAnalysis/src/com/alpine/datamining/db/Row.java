
package com.alpine.datamining.db;

import java.io.Serializable;




public abstract class Row implements Serializable {

	private static final long serialVersionUID = 2953321020546996288L;

	
	protected abstract double get(int index, double defaultValue);

	
	protected abstract void set(int index, double value, double defaultValue);

	
	protected abstract void ensureNumberOfColumns(int numberOfColumns);

	
	public abstract String toString();
	
	
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
	
	
	public void set(Column column, double value) {
		column.setValue(this, value);
	}
}
