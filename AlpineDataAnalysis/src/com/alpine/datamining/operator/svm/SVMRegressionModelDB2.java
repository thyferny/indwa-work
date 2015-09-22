/**
 * ClassName SVMRegressionModelDB2
 *
 * Version information: 1.00
 *
 * Data: 2011-4-20
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 */
public class SVMRegressionModelDB2 extends SVMRegressionModel {
    private static final Logger itsLogger = Logger.getLogger(SVMRegressionModelDB2.class);
	private static final long serialVersionUID = 4486928292072567374L;

	public SVMRegressionModelDB2(DataSet dataSet, DataSet newDataSet) {
		super(dataSet, newDataSet);
	}

	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		String newTableName = ((DBTable) dataSet.getDBTable()).getTableName();

		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();

		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		String tempTableName = "T" + System.currentTimeMillis();

		try {
			st = databaseConnection.createStatement(false);
			sql.append("alter table ").append(newTableName).append(
					" add column "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint");
			itsLogger.debug(
					"SVMRegressionModelDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			sql.append("update ").append(newTableName).append(
					"  set "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = row_number() over()");
			itsLogger.debug(
					"SVMRegressionModelDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("create  table ").append(tempTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint, prediction double)");
			itsLogger.debug(
					"SVMClassificationModel.performPrediction():sql=" + sql);
			st.execute(sql.toString());

		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}

		sql = new StringBuffer();

		sql.append("call  alpine_miner_svs_predict_proc ('").append(
				newTableName).append("','").append(tempTableName).append(
				"',' where ").append(
				StringHandler.escQ(getColumnWhere(getTrainingHeader()).toString())).append(
				"',").append(getKernelType()).append(",").append(getDegree())
				.append(",").append(getGamma()).append(",").append(getNsvs())
				.append(",").append(getIndDim()).append(",").append("?,?,?)");
		try {
			CallableStatement stpCall = databaseConnection.getConnection()
					.prepareCall(sql.toString());
			stpCall.setArray(1, generateWeightsSqlArray(databaseConnection));
			stpCall
					.setArray(2,
							generateIndividualsSqlArray(databaseConnection));
			stpCall.setArray(3, generateColumnsSqlArray(getTrainingHeader(), ((DBTable) dataSet
					.getDBTable()).getDatabaseConnection()));

			itsLogger.debug(
					"SVMRegressionModelDB2.performPrediction():sql=" + sql);

			stpCall.execute();
			stpCall.close();
			StringBuffer sqltemp = new StringBuffer();
			sqltemp.append("update ").append(newTableName).append(" set ")
					.append(StringHandler.doubleQ(predictedLabel.getName()))
					.append(" = (select ").append(tempTableName).append(
							".prediction from ").append(tempTableName).append(
							" where  ").append(tempTableName).append(
							"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTableName).append(
							"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+")");

			itsLogger.debug(
							"SVMRegressionModelDB2.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(tempTableName);
			itsLogger.debug(
					"SVMRegressionModelDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("alter table ").append(newTableName).append(
					" drop column "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");
			itsLogger.debug(
					"SVMRegressionModelDB2.performPrediction():sql=" + sql);
			st.execute(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
		}

		return dataSet;
	}
}
