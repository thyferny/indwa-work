/**
 * ClassName Column.java
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
 * Column contains the column information of data set 
 * @author Eason
 * 
 */
public interface Column extends Cloneable, Serializable {
	/**
	 * Indicates regular columns.
	 */
	public static final int REGULAR = 0;
	
	/** 
	 * Indicates special columns. 
	 */
	public static final int SPECIAL = 1;
	
	/**
	 *  Indicates all columns. 
	 */
	public static final int ALL = 2;
	
	/** 
	 * The name of the confidence special columns. 
	 */
	public static final String CONFIDENCE_NAME = "C";
	
	/** 
	 * The name of the special column id. 
	 */
	public static final String ID_NAME = "ALPINE_ID_NAME";

	/** 
	 * The name of the special column label. 
	 */
	public static final String DEPENDENT_NAME = "ALPINE_DEPENDENT_NAME";

	/** 
	 * The name of the special column prediction. 
	 */
	public static final String PREDICTION_NAME = "P";
	

	/**
	 * Show that this column is not part of any table.
	 */
	public static final int UNDEFINED_COLUMN_INDEX = -1;

	/**
	 * for nominal missing value
	 */
	public static final String MISSING_NOMINAL_VALUE = "?";

	/**
	 * @param o
	 * @return true if the given object is an column with the same name and
	 * table index.
	 */
	public boolean equals(Object o);

	/**
	 * Returns the hash code.
	 */
	public int hashCode();

	/**
	 * @return Clone
	 */
	public Object clone();

	/**
	 * @return the name of the column.
	 */
	public String getName();

	/**
	 * Sets the name of the column.
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @return the index in the table.
	 */
	public int getTableIndex();

	/** Sets the index in the table. */
	/**
	 * @param index
	 */
	public void setTableIndex(int index);

	/**
	 * @return the categary mapping between nominal values and internal double
	 * representations.
	 */
	public Mapping getMapping();

	/**
	 * Returns the category mapping between category values and internal double
	 * representations.c
	 * @param mapping
	 */
	public void setMapping(Mapping mapping);

	/**
	 * @param row
	 * @return the value for the column in the data row.
	 */
	public double getValue(Row row);

	/**
	 * Sets the value for the column in the data row. 
	 * @param row
	 * @param value
	 */
	public void setValue(Row row, double value);

	/**
	 * @return the value type.
	 */
	public int getValueType();

	/**
	 * toString
	 * @return
	 */
	public String toString();

	/**
	 * Sets the default value.
	 * @param value
	 */
	public void setDefault(double value);

	/**
	 * @return the default value.
	 */
	public double getDefault();

	/**
	 * @return true if the column is category.
	 */
	public boolean isNominal();

	/**
	 * @return true if the column is numerical.
	 */
	public boolean isNumerical();

	/**
	 * @return true if the column is category used for integer dependent column
	 */
	public boolean isCategory();

	/**
	 * @param value
	 * @param digits
	 * @param quoteNominal
	 * @return formatted string of the given value.
	 */
	public String getAsString(double value, int digits, boolean quoteNominal);

	/**
	 * @return Stats iterator
	 */
	public Iterator<ColumnStats> getAllStats();

	/**
	 * @return whether the column is id or dependent column
	 */
	public boolean isSpecial();

	/**
	 * get special name
	 * @return special name
	 */
	public String getSpecialName();

	/**
	 * set special name
	 * @param specialName
	 */
	public void setSpecialName(String specialName);

	/**
	 * Registers the statistics.
	 * @param columnStats
	 */
	public void registerStats(ColumnStats columnStats);

}
