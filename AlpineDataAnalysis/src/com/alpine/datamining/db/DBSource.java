/**
 * ClassName DBSource.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;

/**
 * @author  Eason
 */
public class DBSource extends DataSource {
	
	private DatabaseSourceParameter para;
 	
	
	public DBSource() {
		super();
	}

	/**
	 * @param databaseConnection
	 * @param tableName
	 * @param recalculateStatistics
	 * @return DataSet
	 * @throws OperatorException
	 */
	public DataSet createDataSetUsingExitingDBConnection(DatabaseConnection databaseConnection,
			String tableName, boolean recalculateStatistics) throws OperatorException {
		para = (DatabaseSourceParameter)getParameter();

		try {

			String url = para.getUrl();
			String userName = para.getUsername();
			String password = para.getPassword();
			Table table = DBTable.createDatabaseDataTableDB(
					databaseConnection, url, userName, password, tableName, null);
			
			DataSet dataSet = createDataSet(table, this);// table.createdataSet();

			// statistics are only necessary for value mapping
			if (recalculateStatistics == true) {
				dataSet.computeAllColumnStatistics();
			}
			return dataSet;
		} catch (SQLException e) {
			throw new WrongUsedException(this, e, AlpineAnalysisErrorName.DB_ERROR, e.getMessage());
		}
	}

	/**
	 * @param table
	 * @param operator
	 * @return DataSet
	 * @throws OperatorException
	 */
	public static DataSet createDataSet(Table table, Operator operator) throws OperatorException {
		String labelName = ((DatabaseSourceParameter)operator.getParameter()).getLabel();
		String idName = ((DatabaseSourceParameter)operator.getParameter()).getId();
		Column label = table.findColumn(labelName);
		Column id = table.findColumn(idName);
		
		Map<Column, String> specialMap = new HashMap<Column, String>();
		if(label!=null){
			specialMap.put(label, Column.DEPENDENT_NAME);
		}
		if(id!=null){
			specialMap.put(id, Column.ID_NAME);
		}
		return table.createDataSet(specialMap);
	}
	 
}
