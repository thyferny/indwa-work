/**
 * ClassName AbstractDataTable.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.utility.Tools;

/**
 * This class is the core data supplier for data sets. Several data sets
 * can use the same data and access the column values by reference.
 * 
 * @author  Eason
 */
public abstract class AbstractDataTable implements Table {

	private static final long serialVersionUID = -4819129758493509078L;

	/**
	 * List of instances of {@link Column}. The <i>i</i>-th entry in the
	 * list belongs to the <i>i</i>-th data column.
	 */
	private List<Column> columns = new ArrayList<Column>();

	/**
	 * List of Integers referencing indices of columns that were removed
	 */
	private List<Integer> unusedColumnList = new LinkedList<Integer>();

	/**
	 * Creates a new Table.
	 * 
	 * @param columns
	 *            List of {@link Column}. The indices of the attibutes are
	 *            set to values reflecting their position in the list.
	 */
	public AbstractDataTable(List<Column> columns) {
		addColumns(columns);
	}

	// ------------------------------------------------------------

	/** Returns a new array containing all {@link Column}s. */
	public Column[] getColumns() {
		Column[] column = new Column[columns.size()];
		columns.toArray(column);
		return column;
	}

	/**
	 * Returns the column of the column number 
	 */
	public Column getColumn(int i) {
		return columns.get(i);
	}

	/** Returns the column with the given name. */
	public Column findColumn(String name){
		if (name == null)
			return null;
		Iterator<Column> i = columns.iterator();
		while (i.hasNext()) {
			Column att = i.next();
			if (att != null) {
				if (att.getName().equals(name))
					return att;
			}
		}
		return null;
	}

	/**
	 * Adds all {@link Column}s in <code>newcolumns</code> to the end
	 * of the list of columns
	 */
	public void addColumns(Collection<Column> newColumns) {
		Iterator<Column> i = newColumns.iterator();
		while (i.hasNext())
			addColumn(i.next());
	}

	/**
	 * Adds the column to the list of columns assigning it a free column
	 * index.
	 */
	public int addColumn(Column a) {
		int index = -1;
		if (unusedColumnList.size() > 0) {
			index = unusedColumnList.remove(0);
			columns.set(index, a);
		} else {
			index = columns.size();
			columns.add(a);	
		}
        if (a != null)
            a.setTableIndex(index);
		return index;
	}

	/**
	 * Equivalent to calling
	 * <code>removecolumn(column.getTableIndex())</code>.
	 */
	public void removeColumn(Column column) {
		removeColumn(column.getTableIndex());
	}

	public void removeColumn(int index) {
		Column a = columns.get(index);
		if (a == null)
			return;
		columns.set(index, null);
		unusedColumnList.add(index);
	}

	/**
	 * Returns the number of columns.
	 */
	public int getNumberOfColumns() {
		return columns.size();
	}

	public DataSet createDataSet(Column labelColumn, List<Column> regularColumns) {
		Map<Column, String> specialColumns = new HashMap<Column, String>();
		if (labelColumn != null)
			specialColumns.put(labelColumn, Column.DEPENDENT_NAME);
		return new DBDataSet(this, regularColumns, specialColumns);
	}	
	/**
	 * Returns a new data set with all columns switched on.
	 */
	public DataSet createDataSet(Column labelColumn, Column idColumn) {
		Map<Column, String> specialColumns = new HashMap<Column, String>();
		if (labelColumn != null)
			specialColumns.put(labelColumn, Column.DEPENDENT_NAME);
		if (idColumn != null)
			specialColumns.put(idColumn, Column.ID_NAME);
		return new DBDataSet (this, specialColumns);

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
		return new DBDataSet(this, specialColumns);
	}

	// ------------------------------------------------------------

	public String toString() {
		return "Table, " + columns.size() + " columns, " + size() + " data rows,"+Tools.getLineSeparator()+"columns: " + columns;
	}
 }
