/**
 * ClassName PCAIMP.java
 *
 * Version information: 1.00
 *
 * Data: 9 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.attribute.pca;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
/** 
 * @author Shawn
 *
 */
public abstract class PCAImpl {
    private static final Logger logger = Logger.getLogger(PCAImpl.class);
    protected String PCACovSam="cov-sam";
	protected String PCACovPop="cov-pop";
	protected String PCACorr="corr";

	public abstract Object[] initPCA(String[] columnsArray, String tableName,
			String anaType, String valueOutTable, String valueOutSchema,
			int columnsNumber, Statement st, StringBuffer columnArray,
			ResultSet rs, String dropIfExists,
			DatabaseConnection databaseConnection, String appendOnlyString, String endingString) throws SQLException;

	public abstract void generatePCAResult(String remainColumns,
			String[] remainColumnsArray, String tableName, String DBType,
			String outSchema, String outTable, String valueOutTable,
			String valueOutSchema, int remainNumber, Statement st,
			StringBuffer columnArray, StringBuffer remainArray,
			String dropIfExists, int PCANumber,
			DatabaseConnection databaseConnection, String appendOnlyString, String endingString) throws SQLException;

	/**
	 * @param anaType
	 * @param tempNumber
	 * @return
	 */
	public abstract boolean ValidateConstant(String anaType, double tempNumber) ;

	/**
	 * @param tableName
	 * @param st 
	 * @throws OperatorException 
	 */
	public void dropTable(String tableName, Statement st) throws OperatorException {
		// TODO Auto-generated method stub
		
	}
}
