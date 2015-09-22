/**
 * ClassName ResultSetRowIterator.java
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
import java.util.List;

import org.apache.log4j.Logger;



/**
 * Objects of this class read data from a ResultSet
 *@author eason 
 */
public class ResultSetRowIterator implements RowIterator {
    private static Logger itsLogger= Logger.getLogger(ResultSetRowIterator.class);

    private Column[] columns;

	private ResultSet resultSet;
	
	private static final int DONT_KNOW_YET = 0;

	private static final int YES = 1;

	private static final int NO = 2;

	private int hasNext = DONT_KNOW_YET;

	private RowFactory factory;

	/**
	 * Constructor.
	 * 
	 * @param columnList
	 *            List of columns
	 * @param resultSet
	 *            A ResultSet as returned from a database query
	 */
	public ResultSetRowIterator(RowFactory rowFactory, List<Column> columnList, ResultSet resultSet) {
		this.factory = rowFactory;
		this.resultSet = resultSet;
		this.columns = new Column[columnList.size()];
		columnList.toArray(this.columns);
	}


	public RowFactory getFactory() {
		return factory;
	}

	public void remove() {
		throw new UnsupportedOperationException("Not supported!");
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
						resultSet.close();
						return false;
					}
				} catch (SQLException e) {
					itsLogger.error(e.getMessage());
					return false;
				}
			default:
				return false;
		}
	}

	public Row next() {
		if (hasNext()) {
			hasNext = DONT_KNOW_YET;
			try {
				Row row = getFactory().create(columns.length);
				for (int i = 0; i < columns.length; i++) {
					double value = DataBaseRow.readColumn(resultSet, columns[i]);
					row.set(columns[i], value);
				}
				return row;
			} catch (SQLException sqle) {
				throw new RuntimeException("Error accessing the result of a query:" + sqle.toString());
			}
		} else {
			return null;
		}
	}
}
