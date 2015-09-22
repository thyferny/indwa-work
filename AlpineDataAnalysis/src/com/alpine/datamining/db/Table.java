/**
 * ClassName Table.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/** 
 * Table is the real Data table.
 * @author Eason
 *
 */
public interface Table extends Serializable {

	/**
	 * @return the number of data.
	 */
	public long size();

	/**
	 * @return a new array containing all Columns. 
	 */
	public Column[] getColumns();

	/**
	 * @param i
	 * @return the column of the column number i.
	 */
	public Column getColumn(int i);

	/**
	 * @param name
	 * @return the column with the given name.
	 */
	public Column findColumn(String name);

	/**
	 * Adds all Columns 
	 * @param newColumns
	 */
	public void addColumns(Collection<Column> newColumns);

	/**
	 * Adds the column to the list of columns
	 * @param a
	 * @return column index
	 */
	public int addColumn(Column a);

	/**
 	 * remove column by column

	 * @param column
	 */
	public void removeColumn(Column column);

	/**
	 * remove column by index
	 * @param index
	 */
	public void removeColumn(int index);

	/**
	 * @return the number of columns.
	 */
	public int getNumberOfColumns();

	/**
	 * create data set from table
	 * @param labelcolumn
	 * @param regularcolumns
	 * @return data set from table by dependent column and regular columns;
	 */
	public DataSet createDataSet(Column labelcolumn, List<Column> regularcolumns);

	/**
	 * @param specialcolumns
	 * @return data set from table by special columns
	 */
	public DataSet createDataSet(Map<Column, String> specialcolumns);
	
	/**
	 * @return data set from table
	 */
	public DataSet createDataSet();

	/**
	 * @return a string representation of this data table.
	 */
	public String toString();

	/**
	 * @param index
	 * @return row by index
	 */
	public Row getDataRow(int index);

	/**
	 * @return row iterator
	 */
	public RowIterator getDataRowReader();

	
}
