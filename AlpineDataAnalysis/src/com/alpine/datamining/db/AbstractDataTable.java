
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.utility.Tools;


public abstract class AbstractDataTable implements Table {

	private static final long serialVersionUID = -4819129758493509078L;

	
	private List<Column> columns = new ArrayList<Column>();

	
	private List<Integer> unusedColumnList = new LinkedList<Integer>();

	
	public AbstractDataTable(List<Column> columns) {
		addColumns(columns);
	}

	// ------------------------------------------------------------

	
	public Column[] getColumns() {
		Column[] column = new Column[columns.size()];
		columns.toArray(column);
		return column;
	}

	
	public Column getColumn(int i) {
		return columns.get(i);
	}

	
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

	
	public void addColumns(Collection<Column> newColumns) {
		Iterator<Column> i = newColumns.iterator();
		while (i.hasNext())
			addColumn(i.next());
	}

	
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

	
	public int getNumberOfColumns() {
		return columns.size();
	}

	public DataSet createDataSet(Column labelColumn, List<Column> regularColumns) {
		Map<Column, String> specialColumns = new HashMap<Column, String>();
		if (labelColumn != null)
			specialColumns.put(labelColumn, Column.DEPENDENT_NAME);
		return new DBDataSet(this, regularColumns, specialColumns);
	}	
	
	public DataSet createDataSet(Column labelColumn, Column idColumn) {
		Map<Column, String> specialColumns = new HashMap<Column, String>();
		if (labelColumn != null)
			specialColumns.put(labelColumn, Column.DEPENDENT_NAME);
		if (idColumn != null)
			specialColumns.put(idColumn, Column.ID_NAME);
		return new DBDataSet (this, specialColumns);

	}
	

	
	public DataSet createDataSet() {
		return createDataSet(new HashMap<Column, String>());
	}

	
	public DataSet createDataSet(Map<Column, String> specialColumns) {
		return new DBDataSet(this, specialColumns);
	}

	// ------------------------------------------------------------

	public String toString() {
		return "Table, " + columns.size() + " columns, " + size() + " data rows,"+Tools.getLineSeparator()+"columns: " + columns;
	}
 }
