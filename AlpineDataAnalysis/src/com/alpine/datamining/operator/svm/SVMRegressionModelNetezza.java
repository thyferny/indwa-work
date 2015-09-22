/**
 * ClassName SVMRegressionModelNetezza
 *
 * Version information: 1.00
 *
 * Data: 2011-4-20
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.TableTransferParameter;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 */
public class SVMRegressionModelNetezza extends SVMRegressionModel {
    private static final Logger itsLogger = Logger.getLogger(SVMRegressionModelNetezza.class);

    private static final long serialVersionUID = 4486928292072567374L;
	private NetezzaProcUtil netezzaProcUtil = new NetezzaProcUtil();

	public SVMRegressionModelNetezza(DataSet dataSet, DataSet newDataSet) {
		super(dataSet, newDataSet);
	}

	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		String newTableName = ((DBTable) dataSet.getDBTable()).getTableName();
		String newTempTableName = "NT"+System.currentTimeMillis();

		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();

		Statement st = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		String tempTableName = "T" + System.currentTimeMillis();

		try {
			st = databaseConnection.createStatement(false);
			netezzaProcUtil.initTable(st, true);
			TableTransferParameter.insertTable(netezzaProcUtil.getWhereCondTableName(), st, netezzaProcUtil.generatePredictColumnWhere(getTrainingHeader()));
			TableTransferParameter.insertTable(netezzaProcUtil.getColumnTableName(), st, netezzaProcUtil.generatePredictColumnsSqlArray(getTrainingHeader(), getAllTransformMap_valueKey()));
			TableTransferParameter.insertTable(netezzaProcUtil.getWeightsTableName(), st, getWeights());
			TableTransferParameter.insertTable(netezzaProcUtil.getIndividualsTableName(), st, getIndividuals());
			sql.append("create table ").append(newTempTableName).append(
					" as select *, row_number() over(order by 1) as  "+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" from ").append(newTableName);
			itsLogger.debug(
					"SVMClassificationModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			sql = new StringBuffer();
			sql.append("create  table ").append(tempTableName).append(
					" ("+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" bigint, prediction double)");
			itsLogger.debug(
					"SVMClassificationModel.performPrediction():sql=" + sql);
			st.execute(sql.toString());

		} catch (SQLException e1) {
			e1.printStackTrace();
			itsLogger.warn(e1.getLocalizedMessage());
			throw new OperatorException(e1.getLocalizedMessage());
		}

		sql = new StringBuffer();

		sql.append("call alpine_miner_svs_predict( '")
		.append(newTempTableName).append("','")
		.append(netezzaProcUtil.getColumnTableName()).append("','")
		.append(netezzaProcUtil.getWhereCondTableName()).append("',")
		.append(getKernelType()).append(",")
		.append(getDegree()).append(",")
		.append(getGamma()).append(",'")
		.append(netezzaProcUtil.getIndividualsTableName()).append("','")
		.append(netezzaProcUtil.getWeightsTableName()).append("','")
		.append(tempTableName).append("')");

		try {
			itsLogger.debug(
					"SVMRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());

			StringBuffer sqltemp = new StringBuffer();
			sqltemp.append("update ").append(newTempTableName).append(" set ")
					.append(StringHandler.doubleQ(predictedLabel.getName()))
					.append(" =  ").append(tempTableName).append(
							".prediction from ").append(tempTableName).append(
							" where  ").append(tempTableName).append(
							"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+" = ").append(newTempTableName).append(
							"."+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"");

			itsLogger.debug(
							"SVMRegressionModelNetezza.performPrediction():sql="
									+ sqltemp);
			st.execute(sqltemp.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(tempTableName);
			itsLogger.debug(
					"SVMRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTableName);
			itsLogger.debug(
					"SVMRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			StringBuffer columnSql = new StringBuffer();
			Iterator<Column> allColumns = dataSet.getColumns().allColumns();
			boolean first = true;
			while(allColumns.hasNext()){
				Column column = allColumns.next();
				if(!column.getName().equals(""+AlpineDataAnalysisConfig.ALPINE_MINER_ID+"")){
					if(first){
						first = false;
					}else{
						columnSql.append(",");
					}
					columnSql.append(StringHandler.doubleQ(column.getName()));
				}
			}
			sql.append("create table ").append(newTableName).append(
					" as select ").append(columnSql).append(" from ").append(newTempTableName);
			itsLogger.debug(
					"SVMRegressionModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			sql = new StringBuffer();
			sql.append("drop table ").append(newTempTableName);
			itsLogger.debug(
					"SVMClassificationModelNetezza.performPrediction():sql=" + sql);
			st.execute(sql.toString());
			netezzaProcUtil.dropProcTable(st,true);
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
