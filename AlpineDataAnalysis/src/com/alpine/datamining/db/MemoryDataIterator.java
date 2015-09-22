/**
 * ClassName MemoryDataIterator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;


/**
 * This reader simply uses all data from a table.
 * @author Eason
 * 
 */
public class MemoryDataIterator extends AbstractDataIterator {

	/** The parent data reader. */
	private RowIterator rowIterator;
	
	/** The current data set. */
	private DataSet dataSet;
	
	/** Creates a simple data reader. */
	public MemoryDataIterator(RowIterator drr, DataSet dataSet) {
		this.rowIterator = drr;
		this.dataSet = dataSet;
	}

	/** Returns true if there are more data rows. */
	public boolean hasNext() {
		if (rowIterator == null)
		{
			return false;
		}
		return rowIterator.hasNext();
	}

	/** Returns a new data based on the current data row. */
	public Data next() {
		if (!hasNext())
			return null;
		Row data = rowIterator.next();
		if (data == null)
			return null;
		return new Data(data, dataSet.getColumns());
	}
}
