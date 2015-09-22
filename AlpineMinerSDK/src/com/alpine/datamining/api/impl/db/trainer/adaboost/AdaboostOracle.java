/**
 * ClassName AdaboostOracle.java
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
public class AdaboostOracle extends AdaboostIMP {
    private static Logger itsLogger = Logger.getLogger(AdaboostOracle.class);

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
		inforArray.append("varchar2array(");

		while (dependvalueIterator.hasNext()) {
			String str = dependvalueIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			inforArray.append("'");
			inforArray.append(str);
			inforArray.append("',");
		}
		inforArray.deleteCharAt(inforArray.length() - 1);
		inforArray.append(")");
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
		sql.append(") from dual");
		itsLogger.debug("AdaboostOracle.adaboostTrainInit():sql="+sql.toString());
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
		sql.append(") from dual");
		itsLogger.debug("AdaboostOracle.adaboostTrainSampleOnce():sql="+sql.toString());
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
	public double adaboostChangePeoso(String inputSchema, String tableName,
			long timeStamp, String dependentColumn,
			String dependentColumnReplaceQ, Statement st, ResultSet rs,
			Iterator<String> sampleDvalueIterator) throws AnalysisException {

		StringBuffer sampleArray = new StringBuffer();
		double result=0;
		try {
			sampleArray.append("varchar2array(");

			while (sampleDvalueIterator.hasNext()) {
				String str = sampleDvalueIterator.next();
				str = StringHandler.doubleQ(str);
				str = str.substring(1, str.length() - 1);
				sampleArray.append("'");
				sampleArray.append(str);
				sampleArray.append("',");
			}
			sampleArray.deleteCharAt(sampleArray.length() - 1);
			sampleArray.append(")");

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
			sql.append(getDependentColumnStringOracle(dataSet.getColumns()
					.getLabel()));
			sql.append("',");
			sql.append(sampleArray);
			sql.append(") from dual");
			itsLogger.debug("AdaboostOracle.adaboostChangePeoso():sql="+sql.toString());

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
	 * @seecom.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP#
	 * clearTrainResult(java.lang.String, long, java.sql.Statement,
	 * java.lang.String)
	 */
	@Override
	public void clearTrainResult(String inputSchema, long timeStamp,
			Statement st, String pnewTable) throws AnalysisException {

		{

			try {

				String sql = new String();

				sql = "select alpine_miner_adaboost_cleanre('"
						+ StringHandler.doubleQ(inputSchema) + "','"
						+ timeStamp + "') from dual";
				itsLogger.debug("AdaboostOracle.clearTrainResult():sql="+sql);
				st.executeQuery(sql);

			} catch (SQLException e) {
				itsLogger.error(e.getMessage(),e);
				throw new AnalysisException(e);
			}

		}

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
		tempconfig.setOutputTable(outputTable);
	}

	protected StringBuffer getDependentColumnStringOracle(Column column) {
		StringBuffer sb = new StringBuffer();
		if (column.getValueType() == DataType.BOOLEAN) {
			sb.append(" (case when ").append(
					StringHandler.doubleQ(column.getName())).append(
					" is true then ''t'' else ''f'' end)");
		} else if (column.getValueType() == DataType.DATE
				|| column.getValueType() == DataType.DATE_TIME
				|| column.getValueType() == DataType.TIME) {
			sb.append("to_char(").append(
					StringHandler.doubleQ(column.getName())).append(")");
		} else if (column.isNumerical() && column.isCategory()) {
			sb.append("to_char(").append(
					StringHandler.doubleQ(column.getName())).append(")");
		} else {
			sb.append(StringHandler.doubleQ(column.getName()));
		}
		return sb;
	}
}
