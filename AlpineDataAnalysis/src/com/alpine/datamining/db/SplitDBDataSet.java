/**
 * ClassName SplitDBDataSet.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;

/**
 * This class is used to split DataSet used for database.
 * This class is used for data in database.
 * 
 * @author  Eason
 */
public class SplitDBDataSet {

	/**
	 * @param dataSet
	 * @param column
	 * @return list of dataset split by nominal values.
	 * @throws OperatorException
	 */
	public static List<DataSet> splitByColumn(DataSet dataSet, Column column) throws OperatorException {
    	List<DataSet> list = new ArrayList<DataSet>();
    	String columnName = StringHandler.doubleQ(column.getName());
    	DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
    	String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
    	String whereCondition = ((DBTable)dataSet.getDBTable()).getWhereCondition();
		String url = ((DBTable) dataSet
				.getDBTable()).getUrl();
		String userName = ((DBTable) dataSet
				.getDBTable()).getUserName();
		String password = ((DBTable) dataSet
				.getDBTable()).getPassword();
		ArrayList<Column> regularColumns = new ArrayList<Column>();
		for (Column regularColumn: dataSet.getColumns())
		{
			regularColumns.add((Column) regularColumn.clone());
		}
    	String newWhereCondition = null;
    	Iterator<String> i = column.getMapping().getValues().iterator();
    	Table table = null;
    	while (i.hasNext())
    	{
			if (whereCondition !=null && whereCondition.length() != 0)
	    	{
	    		newWhereCondition = whereCondition + " and "+columnName+"="
	    		+CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, i.next());

	    	}
	    	else 
	    	{
	    		newWhereCondition = columnName+"="
	    		+CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, i.next());

	    	}
			try {
				table = DBTable.createDatabaseDataTableDB(databaseConnection, url, userName, password, tableName, newWhereCondition);
				Column labelColumn =  (Column)dataSet.getColumns().getLabel().clone();
				DataSet dataSetNew = table.createDataSet(labelColumn, regularColumns);
//				dataSetNew.recalculateAllcolumnStatistics();
				list.add(dataSetNew);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}

    	}
		return list;
	}
	/**
	 * @param dataSet
	 * @param column
	 * @param values
	 * @return 2 dataSet by containing nominal columns values or not.
	 * @throws OperatorException
	 */
	public static List<DataSet> splitByColumn(DataSet dataSet, Column column, List<String> values) throws OperatorException {
    	List<DataSet> list = new ArrayList<DataSet>();
    	String columnName = StringHandler.doubleQ(column.getName());
    	DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
    	String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
    	String whereCondition = ((DBTable)dataSet.getDBTable()).getWhereCondition();
		String url = ((DBTable) dataSet
				.getDBTable()).getUrl();
		String userName = ((DBTable) dataSet
				.getDBTable()).getUserName();
		String password = ((DBTable) dataSet
				.getDBTable()).getPassword();
		ArrayList<Column> regularColumns = new ArrayList<Column>();
		for (Column regularColumn: dataSet.getColumns())
		{
			regularColumns.add((Column) regularColumn.clone());
		}
		
    	StringBuffer newWhereCondition = new StringBuffer();
    	Table table = null;
    	StringBuffer valuesCondition = new StringBuffer("(");
    	for (int i = 0; i < values.size(); i++)
    	{
    		if (i != 0)
    		{
    			valuesCondition.append(" or ");
    		}
    		valuesCondition.append(columnName).append("=")
			  .append(CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, values.get(i)));

    	}
    	valuesCondition.append(")");
		if (whereCondition !=null && whereCondition.length() != 0)
    	{
    		newWhereCondition.append(whereCondition).append(" and ");
    	}

   		newWhereCondition.append(valuesCondition.toString());

		try {
			table = DBTable.createDatabaseDataTableDB(databaseConnection, url, userName, password, tableName, newWhereCondition.toString());
			Column labelColumn =  (Column)dataSet.getColumns().getLabel().clone();
			DataSet dataSetNew = table.createDataSet(labelColumn, regularColumns);
			list.add(dataSetNew);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		newWhereCondition = new StringBuffer();
		valuesCondition = new StringBuffer("(");
    	for (int i = 0; i < values.size(); i++)
    	{
    		if (i != 0)
    		{
    			valuesCondition.append(" and ");
    		}
    		valuesCondition.append(columnName).append("!=")
			.append(CommonUtility.quoteValue(databaseConnection.getProperties().getName(), column, values.get(i)));

    	}
    	valuesCondition.append(")");
		if (whereCondition !=null && whereCondition.length() != 0)
    	{
    		newWhereCondition.append(whereCondition).append(" and ");
    	}
   		newWhereCondition.append(valuesCondition.toString());
		try {
			table = DBTable.createDatabaseDataTableDB(databaseConnection, url, userName, password, tableName, newWhereCondition.toString());
			Column labelColumn =  (Column)dataSet.getColumns().getLabel().clone();
			DataSet dataSetNew = table.createDataSet(labelColumn, regularColumns);
			list.add(dataSetNew);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
	
//    	}
		return list;
	}
 
    /**
     * @param dataSet
     * @param column
     * @param value
     * @return an data set split into two parts containing all data providing a greater (smaller) value
     * for the given column than the given value. The greater dataSet is the first.
     * @throws OperatorException
     */
    public static List<DataSet> splitByColumn(DataSet dataSet, Column column, double value) throws OperatorException {

    	List<DataSet> list = new ArrayList<DataSet>();
    	DatabaseConnection databaseConnection = ((DBTable)dataSet.getDBTable()).getDatabaseConnection();
    	String tableName = ((DBTable)dataSet.getDBTable()).getTableName();
    	String whereCondition = ((DBTable)dataSet.getDBTable()).getWhereCondition();
		String url = ((DBTable) dataSet
				.getDBTable()).getUrl();
		String userName = ((DBTable) dataSet
				.getDBTable()).getUserName();
		String password = ((DBTable) dataSet
				.getDBTable()).getPassword();

    	String columnName = StringHandler.doubleQ(column.getName());
    	String newWhereCondition = null;
    	if (whereCondition !=null && whereCondition.length() != 0)
    	{
    		newWhereCondition = whereCondition + " and "+columnName+">"+value;
    	}
    	else 
    	{
    		newWhereCondition = columnName+">"+value;
    	}
    	Table table = null;
		try {
			table = DBTable.createDatabaseDataTableDB(databaseConnection, url, userName, password, tableName, newWhereCondition);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		Column labelColumn =  (Column)dataSet.getColumns().getLabel().clone();
		ArrayList<Column> regularColumns = new ArrayList<Column>();
		for (Column regularColumn: dataSet.getColumns())
		{
			regularColumns.add((Column) regularColumn.clone());
		}
		DataSet dataSetNew = table.createDataSet(labelColumn, regularColumns);
		dataSetNew.computeAllColumnStatistics();
		list.add(dataSetNew);
		
		if (whereCondition !=null && whereCondition.length() != 0)
    	{
    		newWhereCondition = whereCondition + " and "+columnName+"<="+value;
    	}
    	else 
    	{
    		newWhereCondition = columnName+"<="+value;
    	}

		try {
			table = DBTable.createDatabaseDataTableDB(databaseConnection, url, userName, password, tableName, newWhereCondition);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		labelColumn =  (Column)dataSet.getColumns().getLabel().clone();
		dataSetNew = table.createDataSet(labelColumn, regularColumns);
		list.add(dataSetNew);

		return list;
    	
    }
}
