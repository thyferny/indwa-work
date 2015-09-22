/**
 * ClassName DoubleRow.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;


/**
 * Implementation of Row that is backed by a double array.
 * 
 * @author  Eason
 */
public class DoubleRow extends Row {

	private static final long serialVersionUID = -6917570408548407625L;
	/** Holds the data for all columns. */
	private double[] data;

	/** Creates a new data row backed by an primitive array. */
	public DoubleRow(double[] data) {
		this.data = data;
	}
	
	protected double get(int index, double defaultValue) {
		return data[index];
	}

	/** Sets the given data for the given index. */
	protected void set(int index, double value, double defaultValue) {
		data[index] = value;
	}

	/**
	 * Creates a new array of the given size if necessary and copies the data
	 * into the new array.
	 */
	protected void ensureNumberOfColumns(int numberOfColumns) {
		if (data.length >= numberOfColumns)
			return;
		double[] newData = new double[numberOfColumns];
		System.arraycopy(data, 0, newData, 0, data.length);
		data = newData;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < data.length; i++)
			result.append((i == 0 ? "" : ",") + data[i]);
		return result.toString();
	}
}
