/**
 * ClassName SVD
 *
 * Version information: 1.00
 *
 * Data: 2011-6-16
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.svd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Trainer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.db.DataSourceInfoOracle;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 *  <p>This operator calculates SVD . </p>
 *
 */
public class SVD extends Trainer {
    private static final Logger itsLogger = Logger.getLogger(SVD.class);

    SVDParameter para;
	public SVD() {
		super();
	}
	public Model train(DataSet dataSet) throws OperatorException {
		para = (SVDParameter)getParameter();	
		String tableName = ((DBTable) dataSet.getDBTable())
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
		String value = dataSet.getColumns().getLabel().getName();
		String sql = " select alpine_miner_svd('"
			+tableName+"','"
			+StringHandler.doubleQ(para.getColName())+"','"
			+StringHandler.doubleQ(para.getRowName())+"','"
			+StringHandler.doubleQ(value)+"',"
			+para.getNumFeatures()+","
			+para.getOriginalStep()+","
			+para.getSpeedupConst()+","
			+para.getFastSpeedupConst()+","
			+para.getSlowdownConst()+","
			+para.getNumIterations()+","
			+para.getMinNumIterations()+","
			+para.getMinImprovement()+","
			+para.getImprovementReached()+","
			+para.getInitValue()+","
			+para.getEarlyTeminate()+",'"
			+para.getUmatrix()+"','"
			+para.getVmatrix()+"',"
			+para.getUdrop()+","
			+para.getVdrop()
			+")";
		if (databaseConnection.getProperties().getName().equals(DataSourceInfoOracle.dBType)){
			sql += " from dual ";
		}

		try {
			itsLogger.debug("SVD.train():sql="+sql);
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
		return new SVDModel(dataSet,  para.getUmatrix(), para.getVmatrix() ,StringHandler.doubleQ(para.getColName()), StringHandler.doubleQ(para.getRowName()));
	}
	
}
