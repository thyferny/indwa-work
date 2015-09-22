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
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.utility.DatabaseConnection;
import org.apache.log4j.Logger;

/**
 * Helper class containing some methods for K-S plots, and ks calculation.
 * 
 * @author Eason
 */
public class KSDataGeneratorDBGeneral extends Operator implements Serializable {
	/**
	 * 
	 */
    private static final Logger itsLogger = Logger.getLogger(KSDataGeneratorDBGeneral.class);
    private static final long serialVersionUID = 6284474543865201106L;
	/** Defines the maximum amount of points which is plotted in the ks curve. */
	public static final int MAX_KS_POINTS = 200;
	private List<double[]> data = null;
//	public static final String PARAMETER_USE_MODEL = "use_model";
//	public static final String PARAMETER_TARGET_CLASS = "target_class";
	EvaluatorParameter para;


	/** Creates a new Lift data generator. */
	public KSDataGeneratorDBGeneral() {
		super();
	}
	
	public ConsumerProducer[] apply() throws OperatorException {
		para = (EvaluatorParameter)getParameter();
		DataSet dataSet;
		dataSet = getInput(DataSet.class);
		if (dataSet.getColumns().getLabel() == null) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.MISS_DEP);
		}
		if (!dataSet.getColumns().getLabel().isNominal()) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.NON_NOMINAL_DEP, "", dataSet.getColumns().getLabel());
		}
		Model model = null;
		if (para.isUseModel()) {
			try {
				model = getInput(Model.class);
			} catch (RuntimeException e) {
				throw new OperatorException(e.getLocalizedMessage());
			}		
			dataSet = model.apply(dataSet);
		}
		DoubleListAndDoubleData data = CreateAllKSData(dataSet);

		if (para.isUseModel()) {
			return new ConsumerProducer[] { dataSet, model, data};
		} else
			return new ConsumerProducer[] {dataSet, data };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { DataSet.class, Model.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { DataSet.class, Model.class, ROCData.class };
	}
	
//	public List<ParameterType> getParameterTypes() {
//		List<ParameterType> types = super.getParameterTypes();
//		types.add(new ParameterTypeBoolean(PARAMETER_USE_MODEL, "If checked a given model will be applied for generating ROCChart. If not the data set must have a predicted label.", true));
//		types.add(new ParameterTypeString(PARAMETER_TARGET_CLASS, "Indicates the target class for which the ROC chart should be produced."));
//		return types;
//	}

	public List<double[]> createKSData(DataSet dataSet) throws OperatorException {
		data = new ArrayList<double[]>();
//		Column weightAttr = null;
		String weightString = "1.0";
//		weightAttr = dataSet.getColumns().getWeight();
//		if (weightAttr!=null)
//		{
//			weightString = weightAttr.getName();
//		}
//		else
//		{
//			weightString = "1.0";
//		}

		double tp = 0.0d;
		double sum = 0.0d;

		String targetClass = para.getColumnValue();
		String targetClassConfidence = "\""+dataSet.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + targetClass).getName()+"\"";
		Column label = dataSet.getColumns().getLabel();
		String labelName = label.getName();
		DatabaseConnection databaseConnection = null;
		String tableName = null;
		Statement st = null;
		ResultSet rs = null;

		databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		tableName = ((DBTable) dataSet.getDBTable())
				.getTableName();

//		String schemaName;
//		if (tableName.contains(".")) {
//			schemaName = tableName.split("\\.")[0];
//			tableName = tableName.split("\\.")[1];
//		} else {
//			schemaName = "public";
//		}
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
				+ targetClass + "'";

		try {
			itsLogger.debug("KSDataGeneratorDBGeneral.createKSData():sql="+sumTotalSql);
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
			itsLogger.debug("KSDataGeneratorDBGeneral.createKSData():sql="+pTotalSql);
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

		double nTotal = sumTotal - pTotal;
		if (pTotal == 0)
		{
			pTotal += 0.001;
		}
		if (nTotal == 0)
		{
			nTotal += 0.001;
		}
		double min = 0.0;
		double max = 0.0;

		sql = "select min(" + targetClassConfidence + ") from " //+schemaName+"."
		+ tableName;
		try {
			itsLogger.debug("KSDataGeneratorDBGeneral.createKSData():sql="+sql);
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

		sql = "select max(" + targetClassConfidence + ") from "// +schemaName+"."
		+tableName;
		try {
			itsLogger.debug("KSDataGeneratorDBGeneral.createKSData():sql="+sql);
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
			+ tableName + " where " + targetClassConfidence + " <= "
					+ (min + (i + 1) * diff);
			String tpSql = "select sum(" + weightString + ") from " //+schemaName+"."
			+ tableName
					+ " where " + targetClassConfidence + " <= "
					+ (min + (i + 1) * diff) + " and " + labelName + " ='"
					+ targetClass + "'";
			try {
				itsLogger.debug("KSDataGeneratorDBGeneral.createKSData():sql="+sumSql);
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
				itsLogger.debug("KSDataGeneratorDBGeneral.createKSData():sql="+tpSql);
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
					tp / (pTotal),
					(sum - tp) / (nTotal) });
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

	public DoubleListAndDoubleData CreateAllKSData(DataSet dataSet) throws OperatorException {
		DoubleListAndDoubleData AllData = new DoubleListAndDoubleData();
		AllData
				.setDoubleList(createKSData(dataSet));
		AllData.setDouble(calculateKS());
		return AllData;
	}
}
