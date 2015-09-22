/**
 * ClassName KSDataGeneratorDB.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.evaluator;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.exception.OperatorException;

import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;

/**
 * @author Eason
 */
public class KSDataGeneratorDB implements Serializable {

	/**
	 * 
	 */
    private static final Logger itsLogger = Logger.getLogger(KSDataGeneratorDB.class);
    private static final long serialVersionUID = -2609914691878066423L;
	/** Defines the maximum amount of points which is plotted in the gini curve. */
	public static final int MAX_KS_POINTS = 200;
	private List<double[]> data = null;

	public List<double[]> createKSData(DataSet dataSet, Model model) throws OperatorException {
		data = new ArrayList<double[]>();
//		Column weightAttr = null;
		String weightString = "1.0";
//		if (useDataWeights) {
//			weightAttr = dataSet.getColumns().getWeight();
//			weightString = weightAttr.getName();
//		}

		double tp = 0.0d;
		double sum = 0.0d;

		String goodColumn = ((LogisticRegressionModelDB) model).getGood();
		Column label = ((LogisticRegressionModelDB) model).getLabel();
		String labelName = label.getName();
		DatabaseConnection databaseConnection = null;
		String tableName = null;
		Statement st = null;
		ResultSet rs = null;

		databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		tableName = ((DBTable) dataSet.getDBTable())
				.getTableName();


		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		rs = null;
		String sql = "";

		double sumTotal = 0.0d;
		double pTotal = 0.0d;
		String sumTotalSql = "select sum(" + weightString + ") from "
				//+ schemaName + "."
				+ tableName;
		String pTotalSql = "select sum(" + weightString + ") from "
				//+ schemaName + "." 
				+ tableName + " where " + labelName + " ='"
				+ goodColumn + "'";

		try {
			itsLogger.debug("KSDataGeneratorDB.createKSData():sql="+sumTotalSql);
			rs = st.executeQuery(sumTotalSql);
			while (rs.next()) {
				sumTotal = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
		}

		try {
			itsLogger.debug("KSDataGeneratorDB.createKSData():sql="+pTotalSql);
			rs = st.executeQuery(pTotalSql);
			while (rs.next()) {
				pTotal = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
		}

		double min = 0.0;
		double max = 0.0;

		sql = "select min(" + "confidence_good" + ") from "// +schemaName+"."
		+ tableName;
		try {
			itsLogger.debug("KSDataGeneratorDB.createKSData():sql="+sql);
			rs = st.executeQuery(sql);
			while (rs.next()) {
				min = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
		}

		sql = "select max(" + "confidence_good" + ") from "// +schemaName+"."
				+ tableName;
		try {
			itsLogger.debug("KSDataGeneratorDB.createKSData():sql="+sql);
			rs = st.executeQuery(sql);
			while (rs.next()) {
				max = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
		}

		double diff = (max - min) / MAX_KS_POINTS;
		for (int i = 0; i < MAX_KS_POINTS; i++) {
			String sumSql = "select sum(" + weightString + ") from "
			//+schemaName+"."
			+ tableName + " where " + "confidence_good" + " <= "
					+ (min + (i + 1) * diff);
			String tpSql = "select sum(" + weightString + ") from " 
			//+schemaName+"."
			+ tableName
					+ " where " + "confidence_good" + " <= "
					+ (min + (i + 1) * diff) + " and " + labelName + " ='"
					+ goodColumn + "'";
			try {
				itsLogger.debug("KSDataGeneratorDB.createKSData():sql="+sumSql);
				rs = st.executeQuery(sumSql);
				while (rs.next()) {
					sum = rs.getDouble(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			} finally {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
			try {
				itsLogger.debug("KSDataGeneratorDB.createKSData():sql="+tpSql);
				rs = st.executeQuery(tpSql);
				while (rs.next()) {
					tp = rs.getDouble(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			} finally {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new OperatorException(e.getLocalizedMessage());
				}
			}
			data.add(new double[] { (min + (i + 1) * diff),
					tp / (pTotal + 0.001),
					(sum - tp) / (sumTotal - pTotal + 0.001) });
		}
		try {
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return data;
	}

	public double calculateKS() {

		double KS = 0.0d;
		double maxKS = 0.0d;
		Iterator<double[]> i = data.iterator();
		while (i.hasNext()) {
			double[] point = i.next();
			double tpDivP = point[0]; // false positives divided by sum of all
			// negatives
			double fpDivN = point[1]; // true positives divided by sum of all
			// positives
			KS = Math.abs(tpDivP - fpDivN);
			if (KS > maxKS) {
				maxKS = KS;
			}
		}
		return maxKS;
	}

	public DoubleListAndDoubleData CreateAllKSData(DataSet dataSet,
			Model model, boolean useDataWeights) throws OperatorException {
		DoubleListAndDoubleData AllData = new DoubleListAndDoubleData();
		AllData
				.setDoubleList(createKSData(dataSet, model));
		AllData.setDouble(calculateKS());
		return AllData;
	}
}
