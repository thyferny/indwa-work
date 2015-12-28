
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.Iterator;




public class Data implements Serializable{



	private static final long serialVersionUID = 8039209604093669030L;

	
	public static final String SEPARATOR = " ";
	
 
	
	private Row data;

	
	private Columns columns;
	
	
	public Data(Row data, Columns columns) {
		this.data = data;
		this.columns = columns;
	}

	
	public Row getDataRow() {
		return this.data;
	}
	
	
	public Columns getColumns() {
		return columns;
	}

    
    public double getValue(Column a) {
		return data.get(a);
	}

    
    public String getNominalValue(Column a) {
        if (!a.isNominal()) {
            throw new RuntimeException("Get nominal value for numerical column '" + a.getName() + "' not possible.");
        }
        double value = getValue(a);
        if (Double.isNaN(value))
        	return Column.MISSING_NOMINAL_VALUE;
        else
        	return a.getMapping().mapIndex((int)value);
    }
    
    public double getNumericalValue(Column a) {
        if (!a.isNumerical()) {
            throw new RuntimeException("Get of numerical value for nominal column '" + a.getName() + "' not possible.");
        }
        return getValue(a);
    }
	
	public void setValue(Column a, double value) {
		data.set(a, value);
	}

	
	public void setValue(Column a, String str) {
		if (!a.isNominal())
			throw new RuntimeException("only supported for nominal values!");
		if (str != null)
			setValue(a, a.getMapping().mapString(str));
		else
			setValue(a, Double.NaN);
	}
	
    
    public boolean equalValue(Column first, Column second) {
        if (first.isNominal() && second.isNominal()) {
            return getValueAsString(first).equals(getValueAsString(second));
        } else if ((!first.isNominal()) && (!second.isNominal())) {
            return com.alpine.datamining.utility.Tools.isEqual(getValue(first), getValue(second));
        } else {
            return false;
        }
    }
    
    
	
	public double getLabel() {
		return getValue(getColumns().getLabel());
	}


	
	public String getValueAsString(Column column) {
		return getValueAsString(column, NumericColumn.UNLIMITED_NUMBER_OF_DIGITS, false);
	}

	
	public String getValueAsString(Column column, int fractionDigits, boolean quoteNominal) {
		double value = getValue(column);
		return column.getAsString(value, fractionDigits, quoteNominal);
	}
	
    public String toString() {
    	return toDenseString(NumericColumn.UNLIMITED_NUMBER_OF_DIGITS, true);
    }

	
	public String toDenseString(int fractionDigits, boolean quoteNominal) {
		StringBuffer result = new StringBuffer();
		Iterator<Column> a = getColumns().allColumns();
		boolean first = true;
		while (a.hasNext()) {
			if (first) {
				first = false;
			} else {
				result.append(SEPARATOR);
			}
			result.append(getValueAsString(a.next(), fractionDigits, quoteNominal));
		}
		return result.toString();
	}
}
