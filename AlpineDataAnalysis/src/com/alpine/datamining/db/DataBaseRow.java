/**
 * ClassName DataBaseRow.java
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
 * Reads datarows from a data base.
 * 
 * @author Eason
 */
public class DataBaseRow extends Row {

	private static final long serialVersionUID = -868261000593050878L;

	/** 
	 * The result set which backs this data row. 
	 */
	private transient ResultSet resultSet;
	
	/** 
	 * The current row of the result set.
	 */
	private int row;
    
	/** 
	 * The last column for which a query should be / was performed. 
	 */
	private Column lastColumn = null;
	
	/**
	 * Creates a data row from the given result set. The current row of the
	 * result set if used as data source.
	 */
	public DataBaseRow(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;
		this.row = resultSet.getRow();
	}

	/** 
	 * Ensures that the current row is the current row of the result set. 
	 */
	private void ensureRowCorrect() throws SQLException {
		if (row != resultSet.getRow()) {
			throw new RuntimeException("ResultSet was modified!");
		}
	}

	/**
	 * Returns the desired data for the given column
	 */
	public double get(Column column) {
		this.lastColumn = column;
		double value = column.getValue(this);
		this.lastColumn = null;
		return value;
	}

	public void set(Column column, double value) {
		try {
			ensureRowCorrect();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		this.lastColumn = column;
		column.setValue(this, value);
		this.lastColumn = null;
	}
	
	protected double get(int index, double defaultValue) {
		if (lastColumn == null) {
			throw new RuntimeException("NULL");
		} else {
			try {
				return readColumn(this.resultSet, lastColumn);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}


	protected void set(int index, double value, double defaultValue) {
		try {
			String name = this.lastColumn.getName();
			if (Double.isNaN(value)) {
				resultSet.updateNull(name);
			} else {
				if (this.lastColumn.isNominal()) {
					resultSet.updateString(name, this.lastColumn.getMapping().mapIndex((int) value));
				} else {
					resultSet.updateDouble(name, value);
				}
			}
			resultSet.updateRow();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Does nothing. */
	protected void ensureNumberOfColumns(int numberOfColumns) {}

	public String toString() {
		return "Database Data Row";
	}
	
	/** Reads the data for the given column from the result set. */
	public static double readColumn(ResultSet resultSet, Column column) throws SQLException {
		String name = column.getName();		
		if (column.isNominal()) {
			String dbString = resultSet.getString(name);
			if (dbString == null)
				return Double.NaN;
			return column.getMapping().mapString(dbString);
		} else {
			double value = resultSet.getDouble(name);

			if (resultSet.wasNull()) {
				return Double.NaN;
			} else {
				if (column.isCategory()){
					String ret = resultSet.getString(name);
		        	if (column.isNumerical() && ret != null){
						column.getMapping().mapString(ret.split("\\.")[0]);
		        	}else{
						column.getMapping().mapString(ret);
		        	}
				}
                return value;
			}
		}
	}
}
