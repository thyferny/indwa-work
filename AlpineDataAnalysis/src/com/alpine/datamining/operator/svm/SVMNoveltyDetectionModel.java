
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



public class SVMNoveltyDetectionModel extends SVMModel {
    private static final Logger itsLogger = Logger.getLogger(SVMNoveltyDetectionModel.class);
	private static final long serialVersionUID = 4486928292072567374L;
	public SVMNoveltyDetectionModel(DataSet dataSet,DataSet newDataSet) {
		super(dataSet,newDataSet);
	}

	@Override
	public DataSet performPrediction(DataSet dataSet, Column predictedLabel)
			throws OperatorException {
		String tableName=((DBTable) dataSet.getDBTable())
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
		StringBuffer add = new StringBuffer();
		add.append("ALTER TABLE ").append(tableName).append(" ADD \"alpine_prediction\" INT");
		StringBuffer sql = new StringBuffer();
		sql.append("update ").append(tableName).append(" set ").append( "\"alpine_prediction\"")
		.append(" = (case when alpine_miner_svs_predict(").append(generateModelString()).append(", ").append(generateColumnsString(getTrainingHeader())).append(", "+getKernelType()+","+getDegree()+","+getGamma()+")> 0 then 1 else 0 end) where ").append(getColumnWhere(getTrainingHeader()));
		try {
			itsLogger.debug("SVMNoveltyDetectionModel.performPrediction():sql="+add);
			st.execute(add.toString());
			itsLogger.debug("SVMNoveltyDetectionModel.performPrediction():sql="+sql);
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

	public DataSet apply(DataSet dataSet)
			throws OperatorException {
		return performPrediction(dataSet, null);
	}
	public String toString(){
		return  getName();		
	}
}
