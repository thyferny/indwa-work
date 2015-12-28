
package com.alpine.datamining.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;


public class DBSource extends DataSource {
	
	private DatabaseSourceParameter para;
 	
	
	public DBSource() {
		super();
	}

	
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
