
package com.alpine.datamining.db;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;




public class MemoryDataSet extends AbstractDataSet {


	
	private static final long serialVersionUID = -5776634011859528779L;

	
	private Table table;

	
	private Columns columns = new ColumnsImp();
	
	
	
	public MemoryDataSet(Table table) {
		this(table, null, null);
	}

	
	public MemoryDataSet(Table table, List<Column> regularColumns) {
		this(table, regularColumns, null);
	}
	
	
	public MemoryDataSet(Table table, Map<Column, String> specialColumns) {
		this(table, null, specialColumns);
	}
	
	
	public MemoryDataSet(Table table, List<Column> regularColumns, Map<Column, String> specialColumns) {
		this.table = table;
		List<Column> regularList = regularColumns;
		if (regularList == null) {
			regularList = new LinkedList<Column>();
			for (int a = 0; a < table.getNumberOfColumns(); a++) {
				Column column = table.getColumn(a);
				if (column != null)
					regularList.add(column);	
			}
		}
		
		for (Column column : regularList) {
			if ((specialColumns == null) || (specialColumns.get(column) == null))
				getColumns().add((column));
		}
		
		if (specialColumns != null) {
			Iterator<Map.Entry<Column, String>> s = specialColumns.entrySet().iterator();
			while (s.hasNext()) {
				Map.Entry<Column, String> entry = s.next();
				getColumns().setSpecialColumn(entry.getKey(), entry.getValue());
			}
		}
	}
	
	
	public MemoryDataSet(MemoryDataSet dataSet) {
		this.table = dataSet.table;
		this.columns = (Columns)dataSet.getColumns().clone();
	}

	
	public Columns getColumns() {
		return columns;
	}
	

	public Table getDBTable() {
		return table;
	}
	
	public long size() {
		return table.size();
	}

	public Data getRow(int index) {
		Row row = getDBTable().getDataRow(index);
		if (row == null)
			return null;
		else
			return new Data(row, this.getColumns());
	}
	
	public Iterator<Data> iterator() {
		return new MemoryDataIterator(getDBTable().getDataRowReader(), this);
	}
}
