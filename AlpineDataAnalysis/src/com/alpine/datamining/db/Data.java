/**
 * ClassName Data.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.io.Serializable;
import java.util.Iterator;



/**
 * A Data consists of a Row and some methods to access the data. 
 * Hence, all values are actually doubles, symbolic values are
 * mapped to integers stored in doubles.<br>
 * @author Eason
 */
public class Data implements Serializable{



	private static final long serialVersionUID = 8039209604093669030L;

	/** 
	 * Separator used in the getString() method . 
	 */
	public static final String SEPARATOR = " ";
	
 
	/**
	 *  The row for this data. 
	 */
	private Row data;

	/** 
	 * The parent DataSet holding all column information for this data row. 
	 */
	private Columns columns;
	
	/**
	 * Creates a new Data that uses the data stored in a Row
	 */
	public Data(Row data, Columns columns) {
		this.data = data;
		this.columns = columns;
	}

	/**
	 * @return the data row which backs up the data in the data table.
	 */
	public Row getDataRow() {
		return this.data;
	}
	
	/**
	 * @return the columns.
	 */
	public Columns getColumns() {
		return columns;
	}

    /**
     * @param a
     * @return the value of column a. In the case of nominal columns return the internal index.
     */
    public double getValue(Column a) {
		return data.get(a);
	}

    /**
     * @param a
     * @return the nominal value for the given column. 
     */
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
    /**
     * @param a
     * @return the numerical value for the given column. 
     */
    public double getNumericalValue(Column a) {
        if (!a.isNumerical()) {
            throw new RuntimeException("Get of numerical value for nominal column '" + a.getName() + "' not possible.");
        }
        return getValue(a);
    }
	/**
	 *  Sets the value of column . The column need be numeric.
	 * @param a
	 * @param value
	 */
	public void setValue(Column a, double value) {
		data.set(a, value);
	}

	/**
	 * Sets the value of column a which must be a nominal column.
	 * @param a
	 * @param str
	 */
	public void setValue(Column a, String str) {
		if (!a.isNominal())
			throw new RuntimeException("only supported for nominal values!");
		if (str != null)
			setValue(a, a.getMapping().mapString(str));
		else
			setValue(a, Double.NaN);
	}
	
    /**
     * @param first
     * @param second
     * @return true if both nominal values are the same 
     */
    public boolean equalValue(Column first, Column second) {
        if (first.isNominal() && second.isNominal()) {
            return getValueAsString(first).equals(getValueAsString(second));
        } else if ((!first.isNominal()) && (!second.isNominal())) {
            return com.alpine.datamining.utility.Tools.isEqual(getValue(first), getValue(second));
        } else {
            return false;
        }
    }
    
    
	/**
	 * @return dependent column value
	 */
	public double getLabel() {
		return getValue(getColumns().getLabel());
	}


	/**
	 * @param column
	 * @return the value of this column as string representation
	 */
	public String getValueAsString(Column column) {
		return getValueAsString(column, NumericColumn.UNLIMITED_NUMBER_OF_DIGITS, false);
	}

	/**
	 * @param column
	 * @param fractionDigits
	 * @param quoteNominal
	 * @return the value of this column as string representation
	 */
	public String getValueAsString(Column column, int fractionDigits, boolean quoteNominal) {
		double value = getValue(column);
		return column.getAsString(value, fractionDigits, quoteNominal);
	}
	
    public String toString() {
    	return toDenseString(NumericColumn.UNLIMITED_NUMBER_OF_DIGITS, true);
    }

	/**
	 * @param fractionDigits
	 * @param quoteNominal
	 * @return a dense string representation of the data 
	 */
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
