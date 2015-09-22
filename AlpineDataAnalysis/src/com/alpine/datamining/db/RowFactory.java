/**
 * ClassName RowFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;



/**
 * Factory class for Row objects. One factory should be used for one
 * Table only.
 * @author Eason
 */
public class RowFactory {
	
	public RowFactory() {
	}

	/** Creates a new Row with the given initial capacity. */
	public Row create(int size) {
		Row row = null;
		row = new DoubleRow(new double[size]);
		return row;
	}

}
