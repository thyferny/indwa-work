/**
 * ClassName SVMClassificationModel
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
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoPostgres;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


/**
 * @author Eason
 */
public class SVMClassificationModel extends SVMModel {
    private static final Logger logger = Logger.getLogger(SVMClassificationModel.class);

    private static final long serialVersionUID = 4486928292072567374L;
	public SVMClassificationModel(DataSet dataSet,DataSet newDataSet) {
		super(dataSet, newDataSet);
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
		StringBuffer confidence0 = new StringBuffer();
		confidence0.append("\"").append(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(0)).getName()).append("\"");
		StringBuffer confidence1 = new StringBuffer();
		confidence1.append("\"").append(dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + getLabel().getMapping().mapIndex(1)).getName()).append("\"");

		StringBuffer sqltemp = new StringBuffer();
		sqltemp.append("update ").append(newTableName).append(" set ").append(confidence0)
		.append(" = (alpine_miner_svs_predict(").append(generateModelString()).append(", ").append(generateColumnsString(getTrainingHeader())).append(", "+getKernelType()+","+getDegree()+","+getGamma()+") + 1) where ").append(getColumnWhere(getTrainingHeader()));

		sql.append("update ").append(newTableName).append(" set ")
		.append(confidence0).append(" = ").append("(case when "+confidence0+" < 0 then 0 when "+confidence0+" > 2 then 1 else ("+confidence0+")/2.0 end),")
		.append(confidence1).append(" = ").append("(case when "+confidence0+" < 0 then 1 when "+confidence0+" > 2 then 0 else 1- ("+confidence0+")/2.0 end),")
		.append(StringHandler.doubleQ(predictedLabel.getName()))
		.append(" = (case when "+confidence0+" >= 1 then '"+getLabel().getMapping().mapIndex(0)+"' else '"+getLabel().getMapping().mapIndex(1)+"' end)");
		if ((getDataSourceInfo().getDBType().equals(DataSourceInfoPostgres.dBType)||getDataSourceInfo().getDBType().equals(DataSourceInfoGreenplum.dBType)) && getLabel().isNumerical() && getLabel().isCategory())
		{
			sql.append("::float");
		}
		try {
			logger.debug("SVMClassificationModel.performPrediction():sql=" + sqltemp);
			st.execute(sqltemp.toString());
			logger.debug("SVMClassificationModel.performPrediction():sql=" + sql);
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
