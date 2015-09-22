/**
 * ClassName Columns.java
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
 * Columns is the interface of the columns. It has the information of columns of a table.
 * 
 * @author  Eason
 *
 */
public interface Columns extends Iterable<Column>, Cloneable, Serializable {

	/**
	 * @return a clone of this column set.
	 */
	public Object clone();

	/**
	 * @param o
	 * @return true if the given object is equal to this column set.
	 */
	public boolean equals(Object o);

	/**
	 * @return the hash code .
	 */
	public int hashCode();

	/**
	 * Iterates for regular column
	 */
	public Iterator<Column> iterator();

	/**
	 * @return an iterator over all column.
	 */
	public Iterator<Column> allColumns();

	/**
	 * @return an iterator over the column roles of the special column.
	 */
	public Iterator<Column> specialColumns();

	/**
	 * @return the number of regular column.
	 */
	public int size();

	/**
	 * @return the number of all column.
	 */
	public int allSize();

	/**
	 * Adds a new column .
	 * @param column
	 */
	public void add(Column column);

	/**
	 * Adds the given column as regular column.
	 * @param column
	 */
	public void addRegular(Column column);


	/**
	 * Removes the given column.
	 * @param column
	 * @return
	 */
	public boolean remove(Column column);

	/**
	 * Returns the column for the given name.
	 * @param name
	 * @return
	 */
	public Column get(String name);

	/**
	 * Returns the special column for the given special name.
	 * @param name
	 * @return
	 */
	public Column getSpecial(String name);

	/**
	 * @return the dependent column. 
	 */
	public Column getLabel();

	/**
	 * Sets the dependent column.
	 * @param label
	 */
	public void setLabel(Column label);

	/**
	 * @return the predicted label column.
	 */
	public Column getPredictedLabel();

	/**
	 * Sets the predicted label column.
	 * @param predictedLabel
	 */
	public void setPredictedLabel(Column predictedLabel);

	/**
	 * @return the id column 
	 */
	public Column getId();

	/**
	 * Sets the id column.
	 * @param id
	 */
	public void setId(Column id);

	/** 
	 * Sets the special column for the given name.
	 * @param column
	 * @param specialName
	 */
	public void setSpecialColumn(Column column, String specialName);
}
