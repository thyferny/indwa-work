/**
 * ClassName PCADB2.java
 *
 * Version information: 1.00
 *
 * Data: 10 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.attribute.pca;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 * 
 */
public class PCADB2 extends PCAImpl {
    private static final Logger itsLogger = Logger.getLogger(PCADB2.class);

    /**
	 * 
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.attribute.pca.PCAIMP#getPCAresult(java
	 * .lang.String, java.lang.String[], java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * int, java.sql.Statement, java.lang.StringBuffer, java.lang.StringBuffer,
	 * java.lang.String, int)
	 */
	@Override
	public void generatePCAResult(String remainColumns, String[] remainColumnsArray,
			String tableName, String DBType, String outSchema, String outTable,
			String valueOutTable, String valueOutSchema, int remainNumber,
			Statement st, StringBuffer columnArray, StringBuffer remainArray,
			String dropIfExists, int PCANumber,
			DatabaseConnection databaseConnection, String appendOnlyString, String endingString) throws SQLException {
		String[] columnsArray = columnArray.substring(6,
				columnArray.length() - 1).split(",");
		Array sqlArray = databaseConnection.getConnection().createArrayOf(
				"VARCHAR", columnsArray);
		String sql = "call alpine_miner_pcaresult('" + tableName + "','"
				+ StringHandler.doubleQ(outSchema) + "','"
				+ StringHandler.doubleQ(outTable) + "','"
				+ StringHandler.doubleQ(valueOutSchema) + "."
				+ StringHandler.doubleQ(valueOutTable) + "',";
		if (StringUtil.isEmpty(remainColumns)) {
			remainNumber = 0;
			sql = sql + PCANumber + ",'" + dropIfExists + "',null,?)";
			itsLogger.debug(
					"PCADB2.getPCAResult():sql=" + sql.toString());
			CallableStatement stpCall = databaseConnection.getConnection()
					.prepareCall(sql);
			remainNumber = 0;
			String nullString = "";
			remainColumnsArray = nullString.split(",");
			stpCall.setArray(1, sqlArray);
			stpCall.execute();
			stpCall.close();
		} else {
			for(int i=0;i<remainColumnsArray.length;i++)
			{
				remainColumnsArray[i]=StringHandler.doubleQ(remainColumnsArray[i]);
			}
			sql = sql + PCANumber + ",'" + dropIfExists + "',?,?)";
			itsLogger.debug("PCADB2.pcaresult():sql=" + sql);
			CallableStatement stpCall = databaseConnection.getConnection()
					.prepareCall(sql);
			Array remainSqlArray = databaseConnection.getConnection()
					.createArrayOf("VARCHAR", remainColumnsArray);
			stpCall.setArray(1, remainSqlArray);
			stpCall.setArray(2, sqlArray);
			stpCall.execute();
			stpCall.close();
		}
		
	}
	/**
	 *
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.attribute.pca.PCAIMP#initPCA(java.lang
	 * .String[], java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, int, java.sql.Statement, java.lang.StringBuffer,
	 * java.sql.ResultSet, java.lang.String)
	 */
	@Override
	public Object[] initPCA(String[] columnsArray, String tableName,
			String anaType, String valueOutTable, String valueOutSchema,
			int columnsNumber, Statement st, StringBuffer columnArray,
			ResultSet rs, String dropIfExists,
			DatabaseConnection databaseConnection, String appendOnlyString, String endingString) throws SQLException {
		for(int i=0;i<columnsArray.length;i++)
		{
			columnsArray[i]=StringHandler.doubleQ(columnsArray[i]);
		}
		columnArray.append("Array[");
		for (int i = 0; i < columnsNumber; i++) {
			columnArray.append(columnsArray[i] + ",");
		}
		columnArray.deleteCharAt(columnArray.length() - 1);
		columnArray.append("]");
		Array sqlArray = databaseConnection.getConnection().createArrayOf(
				"VARCHAR", columnsArray);
		StringBuffer sql = new StringBuffer();
		sql.append("call alpine_miner_initpca( '").append(tableName).append(
				"','").append(StringHandler.doubleQ(valueOutSchema)).append(
				"','").append(StringHandler.doubleQ(valueOutTable))
				.append("',").append("?,?,?,?)");
		Object[] outputResult = new Object[columnsArray.length
				* columnsArray.length + 1];
		itsLogger.debug(
				"PCADB2.initPCA():sql=" + sql.toString());
		CallableStatement stpCall = databaseConnection.getConnection()
				.prepareCall(sql.toString());
		stpCall.setArray(1, sqlArray);
		stpCall.setString(2, anaType.toLowerCase());
		stpCall.setString(3, dropIfExists);
		stpCall.registerOutParameter(4, java.sql.Types.ARRAY);
		stpCall.execute();
		outputResult = (Object[]) stpCall.getArray(4).getArray();
		stpCall.close();
		return outputResult;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.pca.PCAImpl#checkErr(java.lang.String, double)
	 */
	@Override
	public boolean ValidateConstant(String anaType, double tempNumber) {
		if (anaType.equalsIgnoreCase(PCACovPop)
				|| anaType.equalsIgnoreCase(PCACovSam)) {
			if (tempNumber == 0)
				return true;
		} else if (anaType.equalsIgnoreCase(PCACorr)) {

			return false;
		}
		return false;
	}
}
