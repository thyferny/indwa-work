
package com.alpine.datamining.db;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.alpine.datamining.utility.DataType;


public abstract class AbstractColumn implements Column {

	private static final long serialVersionUID = -2388353035299425174L;

	private boolean special = false;
	
	private String specialName = null;
	
	
	private String name;

	
	private int valueType = DataType.COLUMN_VALUE;

	
	
	private double defaultValue = 0.0;

	
	private int index = Column.UNDEFINED_COLUMN_INDEX;

	
	private List<ColumnStats> columnStats = new LinkedList<ColumnStats>();
	
	
	AbstractColumn(AbstractColumn column) {
	    this.special = column.special;
	    this.specialName = column.specialName;

		this.columnStats = new LinkedList<ColumnStats>();
		for (ColumnStats columnStats : column.columnStats) {
			this.columnStats.add((ColumnStats)columnStats.clone());
		}
		this.name = column.name;
		this.valueType = column.valueType;
		this.defaultValue = column.defaultValue;
		this.index = column.index;
	}
	
	
	AbstractColumn(String name, int valueType) {
		this.name = name;
		this.valueType = valueType;
		this.defaultValue = 0.0d;
		this.index = UNDEFINED_COLUMN_INDEX;

	}
	
	
	public abstract Object clone();
	
	public double getValue(Row row) {
		double result = row.get(getTableIndex(), getDefault()); 
		return result;
	}

	public void setValue(Row row, double value) {
		row.set(getTableIndex(), value, getDefault());
	}
	public boolean equals(Object o) {
		if (!(o instanceof AbstractColumn))
			return false;
		AbstractColumn a = (AbstractColumn) o;
		if (!(o instanceof AbstractColumn))
			return false;
		if (!this.name.equals(a.getName()))
			return false;
		return true;
	}

	
	public Iterator<ColumnStats> getAllStats() {
		return this.columnStats.iterator();
	}
	
    public void registerStats(ColumnStats columnStats) {
        this.columnStats.add(columnStats);
    }
  
	public boolean isSpecial() {
		return special;
	}
	
	public String getSpecialName() {
		return specialName;
	}
	
	public void setSpecialName(String specialName) {	
		this.specialName = specialName;
		if (specialName != null)
			this.special = true;
		else
			this.special = false;		
	}

	
	public AbstractColumn(String name, int valueType, double defaultValue, int tableIndex) {
		this.name = name;
		this.valueType = valueType;
		this.defaultValue = defaultValue;
		this.index = tableIndex;

	}
	
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public int getValueType() {
		return this.valueType;
	}
	
	public double getDefault() {
		return this.defaultValue;
	}
	
	public void setDefault(double defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public int getTableIndex() {
		return this.index;
	}
	
	public void setTableIndex(int i) {
		this.index = i;
	}
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("#");
		result.append(this.getTableIndex());
		result.append(": ");
		result.append(this.getName());
		result.append(" (");
		result.append(DataType.COLUMN_VALUE_TYPE.mapIndex(this.getValueType()));
		result.append(")");
		return result.toString();
	}
}
