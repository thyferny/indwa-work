/**
* ClassName WOEDataSQLPGGP.java
*
* Version information: 1.00
*
* Data: 7 Nov 2011
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
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.Resources;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 *
 */
public class WOEDataSQLPGGP extends WOEDataSQL{
    private static final Logger itsLogger = Logger.getLogger(WOEDataSQLPGGP.class);

    static {
		DBType=DataSourceInfoGreenplum.dBType;
		}
	
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#getComputeWOESQL()
	 */
	@Override
	public void generalPreComputeWOESQL(String dependColumn, String goodValue,
			StringBuffer sqlBuffer) {
		
			sqlBuffer.append(" select ").append("  1.0*sum( case when ").append(
					StringHandler.doubleQ(dependColumn)).append(" = '").append(
					goodValue).append(
					"' then 1 else 0 end ) \"ALPINEWOEGOOD\" ,").append(
					" 1.0*sum( case when ").append(
					StringHandler.doubleQ(dependColumn)).append(" = '").append(
					goodValue).append("' then 0  else 1  end) \"ALPINEWOEBAD\" ");
		
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#getComputeWOESQL2(java.lang.String, java.lang.String, java.lang.StringBuffer)
	 */
	@Override
	public void generalAfterComputeWOESQL(String inputSchema, String tableName,
			StringBuffer sqlBuffer) {
			sqlBuffer.append(" from ").append(
					StringHandler.doubleQ(inputSchema)).append(".").append(
							StringHandler.doubleQ(tableName)).append(
							" group by \"AlpineWOEGroup\"");
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#genarateCreateSQL(java.lang.String, java.lang.String, java.lang.String, java.lang.StringBuffer)
	 */
	@Override
	public  AnalysisWOETable createTable(AnalyticSource analyticSource,
			  String inputSchema,
			String tableName, String outputSchema, String outputTableName,
			 String dropIfExist, AnalysisWOETable tableWOEInfor,
			Connection conncetion, DataSet dataSet, String appendOnlyString, String endingString) throws AnalysisException,
			OperatorException, SQLException {
		if (tableWOEInfor.getDataTableWOE().isEmpty()) {
			AnalyzerOutPutWOE result = (AnalyzerOutPutWOE) WOEAutoGroup
					.autoGroup(analyticSource);
			tableWOEInfor = result.getResultList();

		}
		Statement st=null;

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append(" create table  ").append(
				StringHandler.doubleQ(outputSchema)).append(".").append(
				StringHandler.doubleQ(outputTableName))
				.append(appendOnlyString).append(
				" as select *,");

		Iterator<Column> iter = dataSet.getColumns().iterator();
		while (iter.hasNext()) {
			Column tempColumn = iter.next();

			AnalysisWOEColumnInfo woeInfor = tableWOEInfor.getOneColumnWOE(tempColumn.getName());
			if(woeInfor==null){
				continue;
			}
			if (tempColumn.isNumerical()) {
				createNumbernicSQL(sqlBuffer, woeInfor, tempColumn);
			} else {
				creatNominalSQL(sqlBuffer, woeInfor, tempColumn);
			}
			sqlBuffer.append(" ,");
		}

		sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
		sqlBuffer.append(" from ").append(
				StringHandler.doubleQ(inputSchema)).append(".").append(
				StringHandler.doubleQ(tableName)).append(" ");
		sqlBuffer.append(endingString);	
		st = conncetion.createStatement();
		if (dropIfExist.equalsIgnoreCase(Resources.YesOpt)) {
			
				String sql = "DROP TABLE IF EXISTS "
						+ StringHandler.doubleQ(outputSchema) + "."
						+ StringHandler.doubleQ(outputTableName);
				itsLogger.debug("WOEDataSQLPGGP.createTable():sql=" + sql);
				st.execute(sql);
			
		}
		itsLogger.debug("WOEDataSQLPGGP.createTable():sql=" + sqlBuffer.toString());
		st.execute(sqlBuffer.toString());
		st.close();
		return tableWOEInfor;
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.attribute.WOE.WOEDataSQL#generalTotalSQL(java.lang.StringBuffer, java.lang.String, java.lang.String)
	 */
	@Override
	public void generalTotalSQL(StringBuffer sumFrequencySQL,
			String dependColumn, String goodValue) {
			sumFrequencySQL.append(" select 1.0*sum( case when ").append(
					StringHandler.doubleQ(dependColumn)).append("=")
					.append("'").append(goodValue).append("'");
			sumFrequencySQL
					.append(" then 1 else 0 end ), 1.0*sum ( case when ")
					.append(StringHandler.doubleQ(dependColumn)).append("=")
					.append("'").append(goodValue).append("'");
			sumFrequencySQL.append(" then 0 else 1 end ) ");
	}

	

	

}
