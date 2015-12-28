
package com.alpine.datamining.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.utility.Tools;




public class ColumnsImp  implements Columns {

	private static final long serialVersionUID = 2355888539299120605L;

	private List<Column> columns = new LinkedList<Column>();
	
	
	private transient Map<String,Column> nameToColumnMap = new HashMap<String,Column>();
	
	private transient Map<String,Column> specialNameToColumnap = new HashMap<String,Column>();
	
	public ColumnsImp() {
	}
	
	private ColumnsImp(ColumnsImp columns) {
        for (Column column : columns.columns) {            
            register((Column)column.clone(), false);
        }
	}
	
	private Column getColumnByName(String name) {
		return nameToColumnMap.get(name);
	}

	
	private Column getColumnBySpecialName(String specialName) {
		return specialNameToColumnap.get(specialName);
	}
		public Object readResolve() {
		if (nameToColumnMap == null) {
			nameToColumnMap = new HashMap<String,Column>();			
			specialNameToColumnap = new HashMap<String,Column>();
			for (Column column : columns) {
				register(column, true);
			}
		}	
		return this;
	}
	
	public Object clone() {
		return new ColumnsImp(this);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ColumnsImp)) {
			return false;
		}
		ColumnsImp other = (ColumnsImp)o;
		if(columns.size() != columns.size()){
			return false;
		}
		for(int i = 0; i < columns.size(); i++){
			if(columns.get(i) != other.columns.get(i)){
				return false;
			}
		}
		return true;
	}
	
	public int hashCode() {
		return columns.hashCode();
	}
	public Iterator<Column> iterator() {
		return new ColumnIterator(allColumnsInner(), Column.REGULAR);
	}
	
	public Iterator<Column> allColumns() {
		return new ColumnIterator(allColumnsInner(), Column.ALL);
	}

	public Iterator<Column> specialColumns() {
		return new ColumnIterator(allColumnsInner(), Column.SPECIAL);
	}

	public int allSize() {
		return calculateSize(allColumns());
	}
	
	public int size() {
		return calculateSize(iterator());
	}

	private int calculateSize(Iterator<?> i) {
		int counter = 0;
		while (i.hasNext()) {
			i.next();
			counter++;
		}
		return counter;
	}
	
	public void addRegular(Column column) {
		add((column));
	}

	public Column get(String name) {
		Column result = getColumnByName(name);
		if (result == null) {
			result = getColumnBySpecialName(name);
		}
		if (result != null) {
			return result;
		} else {
			return null;
		}
	}

	public Column getSpecial(String name) {
		Column column = getColumnBySpecialName(name);
		return column;
	}
	public Column getLabel() {
		return getSpecial(Column.DEPENDENT_NAME);
	}

	public void setLabel(Column label) {
		setSpecialColumn(label, Column.DEPENDENT_NAME);
	}
	
	public Column getPredictedLabel() {
		return getSpecial(Column.PREDICTION_NAME);
	}

	public void setPredictedLabel(Column predictedLabel) {
		setSpecialColumn(predictedLabel, Column.PREDICTION_NAME);
	}
	
	public Column getId() {
		return getSpecial(Column.ID_NAME);
	}
	
	public void setId(Column id) {
		setSpecialColumn(id, Column.ID_NAME);
	}
	
	public void setSpecialColumn(Column column, String specialName) {				
		Column oldColumn = get(specialName);		
		if (oldColumn != null) {
			remove(oldColumn);			
		}		
		if (column != null) {			
			remove(column);						
			column.setSpecialName(specialName);			
			add(column);			
		}				
	}
	
	public Column[] createRegularColumnArray() {
		int index = 0;
		Column[] result = new Column[size()];
		for (Column column : this)
			result[index++] = column;
		return result;
	}
    
	public String toString() {
		StringBuffer result = new StringBuffer(Tools.classNameWOPackage(getClass()) + ": ");
		Iterator<Column> r = allColumns();
		boolean first = true;
		while (r.hasNext()) {
			if (!first)
				result.append(", ");
			result.append(r.next());
			first = false;
		}
		return result.toString();
	}

	
	private Iterator<Column> allColumnsInner() {
		final Iterator<Column> i = columns.iterator();
		return new Iterator<Column>() {
			private Column current;
			public boolean hasNext() {
				return i.hasNext();
			}
			public Column next() {
				current = i.next();
				return current;
			}

			public void remove() {
				i.remove();
				unregister(current, true);
			}			
		};
	}

	
	private void register(Column column, boolean onlyMaps) {
		String name = column.getName();		
		if (nameToColumnMap.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate column: "+name);
		}
		String specialName = column.getSpecialName();
		if (specialName != null) {
			if (specialNameToColumnap.containsKey(specialName)) {
				throw new IllegalArgumentException("Duplicate column: "+specialName);
			}
		}
		this.nameToColumnMap.put(name, column);
		if (specialName != null) {
			this.specialNameToColumnap.put(specialName, column);
		}
		if (!onlyMaps) {
			this.columns.add(column);
		}
	}
	
	
	private boolean unregister(Column column, boolean onlyMap) {
		if (!nameToColumnMap.containsKey(column.getName())) {
			return false;
		}		
		this.nameToColumnMap.remove(column.getName());		
		if (column.getSpecialName() != null) {
			this.specialNameToColumnap.remove(column.getSpecialName());
		}
		if (!onlyMap) {
			this.columns.remove(column);
		}
		return true;
	}

	
	public void add(Column column) {
		register(column, false);		
	}
	
	
	public boolean remove(Column column) {
		return unregister(column, false);		
	}


}
