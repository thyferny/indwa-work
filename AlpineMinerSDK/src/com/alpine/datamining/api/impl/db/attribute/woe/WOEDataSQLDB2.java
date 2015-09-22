/**
 * ClassName WOEDataSQLDB2.java
 *
 * Version information: 1.00
 *
 * Data: 16 Nov 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.attribute.woe;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutWOE;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.woe.AnalysisWOEColumnInfo;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 * 
 */
public class WOEDataSQLDB2 extends WOEDataSQL {
    private static Logger itsLogger= Logger.getLogger(WOEDataSQLDB2.class);

    static {
		DBType=DataSourceInfoDB2.dBType;
		}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#getComputeWOESQL
	 * (java.lang.String, java.lang.String, java.lang.StringBuffer)
	 */
	@Override
	public void generalPreComputeWOESQL(String dependColumn, String goodValue,
			StringBuffer sqlBuffer) {
		sqlBuffer
				.append(" select ")
				.append("  1.0*sum( case when  ")
				.append(StringHandler.doubleQ(dependColumn))
				.append("  = '")
				.append(goodValue)
				.append("' then 1 else 0 end ) \"ALPINEWOEGOOD\" ,")
				.append(" 1.0*sum( case when  ")
				.append(StringHandler.doubleQ(dependColumn))
				.append("  = '")
				.append(goodValue)
				.append(
						"' then 0  else 1  end) \"ALPINEWOEBAD\" ,\"AlpineWOEGroup\" ")
				.append("from (select ").append(
						StringHandler.doubleQ(dependColumn));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#getComputeWOESQL2
	 * (java.lang.String, java.lang.String, java.lang.StringBuffer)
	 */
	@Override
	public void generalAfterComputeWOESQL(String inputSchema, String tableName,
			StringBuffer sqlBuffer) {
		sqlBuffer.append("   from ").append(StringHandler.doubleQ(inputSchema))
				.append(".").append(StringHandler.doubleQ(tableName)).append(
						") group by \"AlpineWOEGroup\"");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#genarateCreateSQL
	 * (java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.StringBuffer)
	 */
	@Override
	public AnalysisWOETable createTable(AnalyticSource analyticSource,
			  String inputSchema,
			String tableName, String outputSchema, String outputTableName,
			 String dropIfExist,
			AnalysisWOETable tableWOEInfor, Connection conncetion,
			DataSet dataSet, String appendOnlyString, String endingString) throws AnalysisException, OperatorException,
			SQLException {
		if (tableWOEInfor.getDataTableWOE().isEmpty()) {
			AnalyzerOutPutWOE result = (AnalyzerOutPutWOE) WOEAutoGroup
					.autoGroup(analyticSource);
			tableWOEInfor = result.getResultList();

		}

		Statement st=null;

		StringBuffer createBuffer = new StringBuffer();
		StringBuffer selectSQL = new StringBuffer();
		StringBuffer insertSQL = new StringBuffer();
		createBuffer.append(" create table  ").append(
				StringHandler.doubleQ(outputSchema)).append(".").append(
				StringHandler.doubleQ(outputTableName)).append(" as   ");
		insertSQL.append("insert into").append(
				StringHandler.doubleQ(outputSchema)).append(".").append(
				StringHandler.doubleQ(outputTableName));
		selectSQL.append("( select ").append(StringHandler.doubleQ(inputSchema))
		.append(".").append(StringHandler.doubleQ(tableName)).append(".* , ");


		Iterator<Column> iter = dataSet.getColumns().iterator();
		while (iter.hasNext()) {
			Column tempColumn = iter.next();

			AnalysisWOEColumnInfo woeInfor = tableWOEInfor
					.getOneColumnWOE(tempColumn.getName());
			if(woeInfor==null){
				continue;
			}

			if (tempColumn.isNumerical()) {
				createNumbernicSQL(selectSQL, woeInfor, tempColumn);
			} else {
				creatNominalSQL(selectSQL, woeInfor, tempColumn);
			}
			selectSQL.append(" ,");
		}

		selectSQL.deleteCharAt(selectSQL.length() - 1);
		selectSQL.append(" from ").append(StringHandler.doubleQ(inputSchema))
				.append(".").append(StringHandler.doubleQ(tableName)).append(
						" )");
		st = conncetion.createStatement();
		if (dropIfExist.equalsIgnoreCase(Resources.YesOpt)) {

			String sql = "call PROC_DROPSCHTABLEIFEXISTS('"
					+ StringHandler.doubleQ(outputSchema) + "','"
					+ StringHandler.doubleQ(outputTableName) + "')";
			itsLogger.debug("WOEDataSQLDB2.createTable():sql=" + sql);
			st.execute(sql);

		}
		createBuffer.append(selectSQL).append("  definition only");
		itsLogger.debug("WOEDataSQLDB2.createTable():sql=" + createBuffer.toString());
		st.execute(createBuffer.toString());
		insertSQL.append(selectSQL);
		itsLogger.debug("WOEDataSQLDB2.createTable():sql=" + insertSQL.toString());
		st.execute(insertSQL.toString());
		st.close();
		return tableWOEInfor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#generalTotalSQL
	 * (java.lang.StringBuffer, java.lang.String, java.lang.String)
	 */
	@Override
	public void generalTotalSQL(StringBuffer sumFrequencySQL,
			String dependColumn, String goodValue) {
		sumFrequencySQL.append(" select 1.0*sum( case when   ").append(
				StringHandler.doubleQ(dependColumn)).append(" =").append("'")
				.append(goodValue).append("'");
		sumFrequencySQL.append(
				" then 1 else 0 end ), 1.0*sum ( case when   ").append(
				StringHandler.doubleQ(dependColumn)).append(" =").append("'")
				.append(goodValue).append("'");
		sumFrequencySQL.append(" then 0 else 1 end ) ");

	}
	
}
