/**
 * ClassName SVMRegressionModel
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

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


/**
 * @author Eason
 */
public class SVMRegressionModel extends SVMModel {
    private static final Logger itsLogger = Logger.getLogger(SVMRegressionModel.class);
	private static final long serialVersionUID = 4486928292072567374L;
	public SVMRegressionModel(DataSet dataSet,DataSet newDataSet) {
		super(dataSet,newDataSet);
	}

	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		String newTableName=((DBTable) dataSet.getDBTable())
		.getTableName();
		
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();

		Statement st = null;
		ResultSet rs = null;
		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new OperatorException(e1.getLocalizedMessage());
		}

		StringBuffer sql = new StringBuffer();
		sql.append("update ").append(newTableName).append(" set ").append(StringHandler.doubleQ(predictedLabel.getName()))
		.append(" = alpine_miner_svs_predict(").append(generateModelString()).append(",").append(generateColumnsString(getTrainingHeader())).append(" , "+getKernelType()+","+getDegree()+","+getGamma()+") where ").append(getColumnWhere(getTrainingHeader()));
		try {
			itsLogger.debug("SVMRegressionModel.performPrediction():sql="+sql);
			st.execute(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage ());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null){
					st.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage ());
			}
		}

		return dataSet;
	}
}
