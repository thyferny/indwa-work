/**
 * ClassName GiniDataGeneratorDB.java
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
 * Helper class containing some methods for Gini plots, and gini calculation.
 * 
 * @author Eason
 */
public class GiniDataGeneratorDB implements Serializable {
    private static Logger itsLogger= Logger.getLogger(GiniDataGeneratorDB.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = 2084122578529757089L;
	/** Defines the maximum amount of points which is plotted in the gini curve. */
	public static final int MAX_GINI_POINTS = 200;
	private static GiniDataGeneratorDB instance;
	private List<double[]> data = new ArrayList<double[]>();

	public List<double[]> createGiniData(DataSet dataSet, Model model) throws OperatorException {

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
//			itsLogger.debug(sumTotalSql);
			itsLogger.debug("GiniDataGeneratorDB.createGiniData():sql="+sumTotalSql);
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
			itsLogger.debug(pTotalSql);
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

		sql = "select min(" + "confidence_good" + ") from "
		//+ schemaName + "."
				+ tableName;
		try {
//			itsLogger.debug(sql);
			itsLogger.debug("GiniDataGeneratorDB.createGiniData():sql="+sql);
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

		sql = "select max(" + "confidence_good" + ") from "
		// + schemaName + "."
				+ tableName;
		try {
			itsLogger.debug("GiniDataGeneratorDB.createGiniData():sql="+sql);
//			itsLogger.debug(sql);
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

		double diff = (max - min) / MAX_GINI_POINTS;
		for (int i = 0; i < MAX_GINI_POINTS; i++) {
			String sumSql = "select sum(" + weightString + ") from "
					//+ schemaName + "." 
					+ tableName + " where "
					+ "confidence_good" + " <= " + (min + (i + 1) * diff);
			String tpSql = "select sum(" + weightString + ") from "
					//+ schemaName + "."
					+ tableName + " where "
					+ "confidence_good" + " <= " + (min + (i + 1) * diff)
					+ " and " + labelName + " ='" + goodColumn + "'";
			try {
//				itsLogger.debug(sumSql);
				itsLogger.debug("GiniDataGeneratorDB.createGiniData():sql="+sumSql);
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
//				itsLogger.debug(tpSql);
				itsLogger.debug("GiniDataGeneratorDB.createGiniData():sql="+tpSql);
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
			data.add(new double[] { tp / (pTotal + 0.001),
					(sum - tp) / (sumTotal - pTotal + 0.001) });
		}
		return data;
	}

	public double calculateGini(List<double[]> data) {
		if (data.size() == 2) {
			return 0.0;
		}
		// calculate AUC (area under curve)
		double gini = 0.0d;
		double[] last = null;
		Iterator<double[]> i = data.iterator();
		while (i.hasNext()) {
			double[] point = i.next();
			double tpDivP = point[0]; // false positives divided by sum of all
			// negatives
			double fpDivN = point[1]; // true positives divided by sum of all
			// positives
			if (last != null) {
				gini += ((fpDivN - last[1]) * (tpDivP - last[0]) / 2.0d)
						+ (last[1] * (tpDivP - last[0]));
			}

			last = new double[] { tpDivP, fpDivN };
		}
		gini = 2 * gini - 1;
		return gini;
	}

	public DoubleListAndDoubleData CreateAllGiniData(DataSet dataSet,
			Model model) throws OperatorException {
		DoubleListAndDoubleData AllData = new DoubleListAndDoubleData();
		AllData.setDoubleList(createGiniData(dataSet, model));
		AllData.setDouble(calculateGini(data));
		return AllData;
	}

	private GiniDataGeneratorDB() {

	}

	/**
	 * @return
	 */
	public static GiniDataGeneratorDB getInstance() {
		if (instance == null) {
			instance = new GiniDataGeneratorDB();
		}
		return instance;
	}
}
