/**
 * ClassName MemoryTable.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;



/**
 * This class is the core data supplier for data sets. Several data sets
 * can use the same data and access the column values by reference. In this
 * case the data is hold in the main memory during the process.
 * @author Eason
 */
public class MemoryTable extends AbstractDataTable {

	private static final long serialVersionUID = -8285739722766740536L;

	/** List of Rows. */
	private List<Row> dataList = new ArrayList<Row>();

	/** 
	 * Number of columns. 
	 */
	private int columns;

	/**
	 *  Number of columns to add when new columns are allocated. 
	 */
	private static final int INCREMENT = 10;

	/**
	 * Creates a new instance of MemoryTable.
	 * 
	 * @param columns
	 *            List of {@link Column} containing the columns of the
	 *            columns. None of these must be null.
	 */
	public MemoryTable(List<Column> columns) {
		super(columns);
		this.columns = columns.size();
	}

	/**
	 * Creates an empty memory data table and fills it with the data rows
	 * read from i.
	 */
	public MemoryTable(List<Column> columns, RowIterator i) {
		this(columns, i, false);
	}

	/**
	 * Creates an empty memory data table and fills it with the data rows
	 * read from i.
	 */
	public MemoryTable(List<Column> columns, RowIterator i, boolean permutate) {
		this(columns);
		readData(i, permutate);
	}

	/**
	 * Reads the data into memory in the order they are delivered by the
	 * given reader. Removes all old data rows first.
	 */
	public void readData(RowIterator i) {
		readData(i, false);
	}

    /**
     * Reads the data into memory and permutates the order. Removes all old
     * data rows first.
     */
    public void readData(RowIterator i, boolean permutate) {
        readData(i, false, null);
    }
    
	/**
	 * Reads the data into memory and permutates the order. Removes all old
	 * data rows first.
	 */
	public void readData(RowIterator i, boolean permutate, Random random) {
		dataList.clear();
		while (i.hasNext()) {
			if (permutate) {
				int index = random.nextInt(dataList.size() + 1);
				dataList.add(index, i.next());
			} else {
				dataList.add(i.next());
			}
		}
	}

	/** 
	 * Returns a new data row reader. 
	 */
	public RowIterator getDataRowReader() {
		return new ListRowIterator(dataList.iterator());
	}

	/** 
	 * Returns the data row with the given index. 
	 */
	public Row getDataRow(int index) {
		return dataList.get(index);
	}

	/** 
	 * Returns the size of this data table, i.e. the number of data rows. 
	 */
	public long size() {
		return dataList.size();
	}

	/**
	 * Convenience method allowing the adding of data rows without a data row
	 * reader.
	 */
	public void addRow(Row row) {
		dataList.add(row);
	}

	/** Convenience method for removing data rows. */
	public boolean removeRow(Row row) {
		return dataList.remove(row);
	}
	
	/** Convenience method for removing data rows. */
	public Row removeRow(int index) {
		return dataList.remove(index);
	}
	
	/** Clears the table. */
	public void clear() {
		dataList.clear();
	}
	
	/**
	 * Adds a new column to this data table by invoking the super method.
	 */
	public synchronized int addColumn(Column column) {
		int index = super.addColumn(column);
		if (dataList == null)
			return index;
		int n = getNumberOfColumns();
		if (n <= columns)
			return index;
		int newSize = n + INCREMENT;
		columns = newSize;

		if (dataList != null) {
			Iterator<Row> i = dataList.iterator();
			while (i.hasNext())
				i.next().ensureNumberOfColumns(columns);
		}
		return index;
	}
    

	/**
	 * Returns a new data set with all columns switched on.
	 */
	public DataSet createDataSet() {
		return createDataSet(new HashMap<Column, String>());
	}

	/**
	 * Returns a new data set with all columns switched on.
	 */
	public DataSet createDataSet(Map<Column, String> specialColumns) {
		return new MemoryDataSet(this, specialColumns);
	}
}
