/**
 * ClassName DatabaseSource.java
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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;


/**
 * <p>This operator reads a DataSet from an SQL
 * database. 
 * @author Eason
 */
public class DatabaseSource extends DataSource {
    private static Logger itsLogger= Logger.getLogger(DatabaseSource.class);

    private DatabaseSourceParameter para;
	
	/** The database connection handler. */
	private DatabaseConnection databaseConnection;

	/**
	 * @return DatabaseConnection
	 */
	public DatabaseConnection getDatabaseConnection() {
		return databaseConnection;
	}

	/**
	 * set database connection
	 * @param databaseConnection
	 */
	public void setDatabaseConnection(DatabaseConnection databaseConnection) {
		this.databaseConnection = databaseConnection;
	}

	/** 
	 * This is only used for the case that the data is read into memory. 
	 */
	private Statement statement;
	
	
	public DatabaseSource() {
		super();
	}
	/**
	 * create DataSet Using Exiting DBHandler
	 * @param databaseConnection
	 * @param isCategoryLabel
	 * @param label
	 * @return
	 * @throws OperatorException
	 */
	public DataSet createDataSetUsingExitingDBHandler(DatabaseConnection databaseConnection, boolean isCategoryLabel, String label)throws OperatorException {
		para = (DatabaseSourceParameter)getParameter();
		ResultSet resultSet = getResultSet(databaseConnection);
		List<Column> columnList = null;
		try {
			columnList = DatabaseConnection.createColumns(resultSet);
		} catch (SQLException e) {
			throw new WrongUsedException(this, e,
					AlpineAnalysisErrorName.DB_ERROR, e.getMessage());
		}
		if (isCategoryLabel) {
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).getName().equals(label)
						&& columnList.get(i).isNumerical()) {
					((NumericColumn) columnList.get(i)).setCategory(true);
				}
			}
		}
		RowIterator reader = new ResultSetRowIterator(new RowFactory(), columnList, resultSet);
		Table table = new MemoryTable(columnList, reader);

		tearDown();

		return createDataSet(table, this);
	}
	/**
	 * teardown, close statment
	 */
	public void tearDown() {
		if (this.statement != null) {
			try {
				this.statement.close();
			} catch (SQLException e) {
			}
			this.statement = null;
		}
	}

	/**
	 * @return select query by the table name
	 * @throws OperatorException
	 */
	private String getQuery() throws OperatorException {
		para = (DatabaseSourceParameter)getParameter();
		String query = para.getQuery();
		if (query != null)
			query = query.trim();

		if ((query == null) || (query.length() == 0)) {

		} else {
		}

		if ((query == null) || (query.trim().length() == 0)) {
			if (!StringUtil.isEmpty(para.getTableName())) {
				query = "SELECT * FROM " + para.getTableName();
			}
		}

		if (query == null) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.PARA_DEP_THREE, new Object[] { "query", "query_file", "table_name" });
		}

		return query;
	}


	/**
	 * @param databaseConnection
	 * @return resultset 
	 * @throws OperatorException
	 */
	public ResultSet getResultSet(DatabaseConnection databaseConnection) throws OperatorException {
		ResultSet rs = null;
		try {
			this.databaseConnection = databaseConnection;
			String query = getQuery();
			this.statement = databaseConnection.createStatement(false);
			itsLogger.debug("DatabaseSource.getResultSet():sql=" + query);
			rs = this.statement.executeQuery(query);
		} catch (SQLException sqle) {
			throw new WrongUsedException(this, sqle, AlpineAnalysisErrorName.DB_ERROR, sqle.getMessage());
		}
		return rs;
	}

	/**
	 * @param columnList
	 * @param name
	 * @return column by name from a column list
	 * @throws OperatorException
	 */
	protected static Column find(List columnList, String name) throws OperatorException {
		if (name == null)
			return null;
		Iterator i = columnList.iterator();
		while (i.hasNext()) {
			Column column = (Column) i.next();
			if (column.getName().equals(name))
				return column;
		}
		throw new WrongUsedException(null, AlpineAnalysisErrorName.NO_SUCH_COL, name);
	}

	/**
	 * @param table
	 * @param operator
	 * @return dataSet from table and the id, dependent column
	 * @throws OperatorException
	 */
	public static DataSet createDataSet(Table table, Operator operator) throws OperatorException {
		String labelName =((DatabaseSourceParameter)operator.getParameter()).getLabel();
		String idName = ((DatabaseSourceParameter)operator.getParameter()).getId();
		Column label = table.findColumn(labelName);
		Column id = table.findColumn(idName);
		
		Map<Column, String> specialMap = new HashMap<Column, String>();
		specialMap.put(label, Column.DEPENDENT_NAME);
		specialMap.put(id, Column.ID_NAME);
		return table.createDataSet(specialMap);
	}

}
