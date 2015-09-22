/**
 * ClassName PCAOracle.java
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
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 * 
 */
public class PCAOracle extends PCAImpl {
    private static final Logger itsLogger = Logger.getLogger(PCAOracle.class);

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

		columnArray.append("varchar2array(");
		for (int i = 0; i < columnsNumber; i++) {
			columnArray.append("'" + StringHandler.doubleQ(columnsArray[i])
					+ "',");
		}
		columnArray.deleteCharAt(columnArray.length() - 1);
		columnArray.append(")");
		String sql = "select alpine_miner_initpca('" + tableName + "','"
				+ StringHandler.doubleQ(valueOutSchema) + "."
				+ StringHandler.doubleQ(valueOutTable) + "'," + columnArray
				+ ",'" + anaType.toLowerCase() + "','" + dropIfExists
				+ "') from dual";
		itsLogger.debug("PCAOracle.initPCA(): sql="+sql);
		rs = st.executeQuery(sql);
		if(rs.next())
		;
		Object[] outputResult = new Object[columnsArray.length
				* columnsArray.length + 1];
		
		ResultSet  resultSet =rs.getArray(1).getResultSet();
		while(resultSet.next()){
			if(resultSet.getObject(2)==null)
			{
				outputResult[resultSet.getInt(1)-1 ]=null;
			}
			else 
			{
				outputResult[resultSet.getInt(1)-1 ]=resultSet.getDouble(2);
			}
		}
		
		
		return outputResult;

	}

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

		if (!StringUtil.isEmpty(remainColumns)) {
			remainArray.append("varchar2array(");
			for (int i = 0; i < remainNumber; i++) {
				remainArray.append("'"
						+ StringHandler.doubleQ(remainColumnsArray[i]) + "',");
			}
			remainArray.deleteCharAt(remainArray.length() - 1);
			remainArray.append(")");
		} else {
			remainArray.append("null");
		}
		String sql = "select alpine_miner_pcaresult('" + tableName + "',"

		+ columnArray + ",'" + StringHandler.doubleQ(outSchema) + "."
				+ StringHandler.doubleQ(outTable) + "'," + remainArray + ",'"
				+ StringHandler.doubleQ(valueOutSchema) + "."
				+ StringHandler.doubleQ(valueOutTable) + "'," + PCANumber
				+ ",'" + dropIfExists + "') from dual";

		itsLogger.debug("PCAOracle.getPCAResult()"+sql);
		st.executeQuery(sql);

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
		if (Double.compare(tempNumber,Double.NaN) == 0)
		return true;
 	}
	return false;

}
	public void dropTable(String tableName, Statement st) throws OperatorException {
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		try {
			itsLogger.debug("PCAAnalyzer.dropTable():sql="
							+ truncate.toString());
			st.execute(truncate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
			itsLogger.debug("PCAAnalyzer.dropTable():sql="
							+ dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

}
