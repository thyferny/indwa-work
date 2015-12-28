
package com.alpine.datamining.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;




public class MemoryTable extends AbstractDataTable {

	private static final long serialVersionUID = -8285739722766740536L;

	
	private List<Row> dataList = new ArrayList<Row>();

	
	private int columns;

	
	private static final int INCREMENT = 10;

	
	public MemoryTable(List<Column> columns) {
		super(columns);
		this.columns = columns.size();
	}

	
	public MemoryTable(List<Column> columns, RowIterator i) {
		this(columns, i, false);
	}

	
	public MemoryTable(List<Column> columns, RowIterator i, boolean permutate) {
		this(columns);
		readData(i, permutate);
	}

	
	public void readData(RowIterator i) {
		readData(i, false);
	}

    
    public void readData(RowIterator i, boolean permutate) {
        readData(i, false, null);
    }
    
	
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

	
	public RowIterator getDataRowReader() {
		return new ListRowIterator(dataList.iterator());
	}

	
	public Row getDataRow(int index) {
		return dataList.get(index);
	}

	
	public long size() {
		return dataList.size();
	}

	
	public void addRow(Row row) {
		dataList.add(row);
	}

	
	public boolean removeRow(Row row) {
		return dataList.remove(row);
	}
	
	
	public Row removeRow(int index) {
		return dataList.remove(index);
	}
	
	
	public void clear() {
		dataList.clear();
	}
	
	
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
    

	
	public DataSet createDataSet() {
		return createDataSet(new HashMap<Column, String>());
	}

	
	public DataSet createDataSet(Map<Column, String> specialColumns) {
		return new MemoryDataSet(this, specialColumns);
	}
}
