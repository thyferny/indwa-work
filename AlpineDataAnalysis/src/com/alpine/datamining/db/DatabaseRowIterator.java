/**
 * ClassName DatabaseRowIterator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.sql.ResultSet;
import java.sql.SQLException;



/**
 * This class read data from a ResultSet, a data structure that
 * is returned from a database query.
 * 
 * @author Eason
 *          
 */
public class DatabaseRowIterator implements RowIterator {

	private ResultSet resultSet;
	
	private static final int DONT_KNOW_YET = 0;

	private static final int YES = 1;

	private static final int NO = 2;

	private int hasNext = DONT_KNOW_YET;

	/**
	 * Creates a datarow reader from a ResultSet. The column meta data must be
	 * tranformed to an {@link Column} array.
	 */
	public DatabaseRowIterator(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;
	}

	public boolean hasNext() {
		switch (hasNext) {
			case YES:
				return true;
			case NO:
				return false;
			case DONT_KNOW_YET:
				try {
					if (resultSet.next()) {
						hasNext = YES;
						return true;
					} else {
						hasNext = NO;
						return false;
					}
				} catch (SQLException e) {
					return false;
				}
			default:
				// impossible
				return false;
		}
	}

	public Row next() {
		if (hasNext()) {
			hasNext = DONT_KNOW_YET;
			try {
				return new DataBaseRow(resultSet);
			} catch (SQLException sqle) {
				throw new RuntimeException("Error accessing the result of a query:" + sqle.toString(), sqle);
			}
		} else {
			return null;
		}
	}
	
	public void remove() {
		throw new UnsupportedOperationException("The method 'remove' is not supported by DataRowReaders on databases!");
	}
}
