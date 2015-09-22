/**
 * ClassName AdaboostGreenplum.java
 *
 * Version information: 1.00
 *
 * Data: 9 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.trainer.adaboost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DataType;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * @author Shawn
 * 
 */
public class AdaboostGreenplum extends AdaboostIMP {
    private static Logger itsLogger = Logger.getLogger(AdaboostGreenplum.class);

    /*
	 * (non-Javadoc)
	 * 
	 * @seecom.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#
	 * adaboostTrainInit(java.lang.String, java.lang.String, long,
	 * java.lang.String, java.sql.Statement, java.lang.StringBuffer,
	 * java.util.Iterator)
	 */
	@Override
	public void adaboostTrainInit(String inputSchema, String tableName,
			long timeStamp, String dependentColumn, Statement st,
			Iterator<String> dependvalueIterator, DataSet dataSet)
			throws SQLException {
		this.dataSet = dataSet;
		pnewTable = "pnew" + timeStamp;
		deleteNullTable = "dn" + timeStamp;
		outputTable = "tp" + timeStamp;
		sampleTable = "s" + timeStamp;
		predictTable = "p" + timeStamp;
		randomTable = "r" + timeStamp;
		sumPeosoTable="sp"+ timeStamp;
		StringBuffer inforArray = new StringBuffer();
		inforArray.append("array[");
		while (dependvalueIterator.hasNext()) {
			String columnValues = (String) dependvalueIterator.next();
			columnValues = StringHandler.doubleQ(columnValues);
			columnValues = columnValues.substring(1, columnValues.length() - 1);
			inforArray.append("'");
			inforArray.append(columnValues);
			inforArray.append("',");
		}
		inforArray.deleteCharAt(inforArray.length() - 1);
		inforArray.append("]");
		StringBuffer sql = new StringBuffer();
		sql.append("select alpine_miner_adaboost_inittra('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(tableName));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("','");
		sql.append(StringHandler.doubleQ(dependentColumn));
		sql.append("',");
		sql.append(inforArray);
		sql.append(")");
		itsLogger.debug("AdaboostGreenplum.adaboostTrainInit():sql="+sql.toString());

		st.executeQuery(sql.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#
	 * adaboostTrainSampleOnce(java.lang.String, long, java.sql.Statement)
	 */
	@Override
	public void adaboostTrainSampleOnce(String inputSchema, long timeStamp,
			Statement st, String pnewTable) throws SQLException {
		
		StringBuffer sql = new StringBuffer();
		sql.append("select alpine_miner_adaboost_sample('");
		sql.append(StringHandler.doubleQ(inputSchema));
		sql.append("','");
		sql.append(StringHandler.doubleQ(pnewTable));
		sql.append("','");
		sql.append(timeStamp);
		sql.append("',");
		sql.append(AlpineMinerConfig.ADABOOST_SAMPLE_NUMBER);
		sql.append(")");
		itsLogger.debug("AdaboostGreenplum.adaboostTrainSampleOnce():sql="+sql.toString());
		st.executeQuery(sql.toString());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#
	 * adaboostChangePeoso(java.lang.String, java.lang.String, long,
	 * java.lang.String, java.lang.String, java.sql.Statement,
	 * java.sql.ResultSet, java.util.Iterator)
	 */

	@Override
	public void clearTrainResult(String inputSchema, long timeStamp,
			Statement st, String pnewTable) throws AnalysisException {
		try {
			String sql = new String();
			sql = "drop table IF EXISTS  " + StringHandler.doubleQ(inputSchema)
					+ "." + StringHandler.doubleQ(pnewTable) + " ";
			itsLogger.debug("AdaboostGreenplum.clearTrainResult():sql="+sql);
			st.execute(sql);
			
			sql = "drop table IF EXISTS  " + StringHandler.doubleQ(inputSchema)
			+ "." + StringHandler.doubleQ(sumPeosoTable) + " ";
			itsLogger.debug("AdaboostGreenplum.clearTrainResult():sql="+sql);
			st.execute(sql);

			sql = "drop table IF EXISTS " + StringHandler.doubleQ(inputSchema)
					+ "." + StringHandler.doubleQ(deleteNullTable);
			itsLogger.debug("AdaboostGreenplum.clearTrainResult():sql="+sql);
			st.execute(sql);
			sql = "drop table IF EXISTS  " + StringHandler.doubleQ(inputSchema)
			+ "." +  StringHandler.doubleQ(outputTable);
			itsLogger.debug("AdaboostGreenplum.clearTrainResult():sql="+sql);
			st.execute(sql);
			sql = "drop table IF EXISTS  "+ StringHandler.doubleQ(inputSchema)
			+ "." + StringHandler.doubleQ(predictTable);
			itsLogger.debug("AdaboostGreenplum.clearTrainResult():sql="+sql);
			st.execute(sql);
			sql = "drop table IF EXISTS " + StringHandler.doubleQ(inputSchema)
					+ "." + StringHandler.doubleQ(sampleTable);
			itsLogger.debug("AdaboostGreenplum.clearTrainResult():sql="+sql);
			st.execute(sql);
		} catch (SQLException e) {
			itsLogger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}

	}

	public double adaboostChangePeoso(String inputSchema, String tableName,
			long timeStamp, String dependentColumn,
			String dependentColumnReplaceQ, Statement st, ResultSet rs,
			Iterator<String> sampleDvalueIterator) throws AnalysisException {

		StringBuffer sampleArray = new StringBuffer();
		double result=0;
		try {
			sampleArray.append("array[");
			while (sampleDvalueIterator.hasNext()) {
				String str = (String) sampleDvalueIterator.next();
				str = StringHandler.doubleQ(str);
				str = str.substring(1, str.length() - 1);
				sampleArray.append("'");
				sampleArray.append(str);
				sampleArray.append("',");
			}
			sampleArray.deleteCharAt(sampleArray.length() - 1);
			sampleArray.append("]");
			String sql1 = "drop table IF EXISTS  "+ StringHandler.doubleQ(inputSchema)
			+ "." + StringHandler.doubleQ(outputTable) + "  ";
			itsLogger.debug("AdaboostGreenplum.adaboostChangePeoso():sql="+sql1);
			st.execute(sql1);

			sql1 = "Create table "+ StringHandler.doubleQ(inputSchema)
			+ "." + StringHandler.doubleQ(outputTable)
					+ " as select * from  "+ StringHandler.doubleQ(inputSchema)
					+ ".\"t" + outputTable
					+ "\" DISTRIBUTED BY ("
					+ StringHandler.doubleQ(alpine_adaboost_id) + ")";
			itsLogger.debug("AdaboostGreenplum.adaboostChangePeoso():sql="+sql1);
			st.execute(sql1);
			sql1 = "drop table "+ StringHandler.doubleQ(inputSchema)
					+ ".\"t" + outputTable + "\"";
			itsLogger.debug("AdaboostGreenplum.adaboostChangePeoso():sql="+sql1);
			st.execute(sql1);
			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_changep('");
			sql.append(StringHandler.doubleQ(inputSchema));
			sql.append("','");
			sql.append(StringHandler.doubleQ(tableName));
			sql.append("','");
			sql.append(timeStamp);
			sql.append("','");
			sql.append(StringHandler.doubleQ(dependentColumn));
			sql.append("','");
			sql.append(dependentColumnReplaceQ);
			sql.append("','");
			sql.append(getDependentColumnStringGPPG(dataSet.getColumns()
					.getLabel()));
			sql.append("',");
			sql.append(sampleArray);
			sql.append(")");
			itsLogger.debug("AdaboostGreenplum.adaboostChangePeoso():sql="+sql.toString());

			rs = st.executeQuery(sql.toString());
			
				if (rs.next()) {
					result=rs.getDouble(1);
					}

		} catch (SQLException e) {
            itsLogger.error(e.getMessage(),e);
			throw new AnalysisException(e);
		}

		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#setOutputTable
	 * (com.alpine.datamining.api.impl.algoconf.PredictorConfig)
	 */
	@Override
	public void setOutputTable(PredictorConfig tempconfig) {
		tempconfig.setOutputTable("t" + outputTable);
	}

	protected StringBuffer getDependentColumnStringGPPG(Column column) {
		StringBuffer sb = new StringBuffer();
		if (column.getValueType() == DataType.BOOLEAN) {
			sb.append(" (case when ").append(
					StringHandler.doubleQ(column.getName())).append(
					" is true then ''t'' else ''f'' end)");
		} else if (column.getValueType() == DataType.DATE
				|| column.getValueType() == DataType.DATE_TIME
				|| column.getValueType() == DataType.TIME) {
			sb.append(StringHandler.doubleQ(column.getName())).append(
					"::text");
		} else {
			sb.append(StringHandler.doubleQ(column.getName())).append(
					"::text");
		}
		return sb;
	}
}
