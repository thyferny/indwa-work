package com.alpine.datamining.api.utility;

import java.sql.Statement;

import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;

public abstract class AbstractN2TUtil {

	abstract public String getDistribution(DatabaseConnection databaseConnection,
			String tableName) throws OperatorException;
	
	abstract public void alterColumnType(Statement st,String columnName,String tableName) throws OperatorException, AnalysisError;

}
