
package com.alpine.datamining.operator.regressions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.resources.AlpineDataAnalysisLanguagePack;
import com.alpine.datamining.tools.matrix.Matrix;
import com.alpine.datamining.utility.ColumnTypeInteractionTransformer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.resources.AlpineThreadLocal;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.log.LogUtils;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;


public abstract class LinearRegressionImp {

    private static final Logger itsLogger = Logger.getLogger(LinearRegressionImp.class);
    protected ArrayList<String> null_list = null;
	protected Statement st = null;
	protected ResultSet rs = null;
	protected Double[] coefficients = null;
	HashMap<String, Double> coefficientmap = new HashMap<String, Double>();
	protected LinearRegressionModelDB model = null;
	DataSet dataSet;
	DataSet newDataSet = null;
	ColumnTypeInteractionTransformer transformer = new ColumnTypeInteractionTransformer();
	protected Matrix hessian = null;

	public LinearRegressionImp() {
	}

	public Model learn(DataSet dataSet, LinearRegressionParameter para,
			String columnNames) throws OperatorException {
		this.dataSet = dataSet;
		StringBuilder orignotnull = new StringBuilder();
		String depend = dataSet.getColumns().getLabel().getName();
		orignotnull.append(" where ").append(StringHandler.doubleQ(depend))
				.append(" is not null ");
//		int k = 0;
		ArrayList<String> columnNamesList = new ArrayList<String>();
		if (columnNames != null && !StringUtil.isEmpty(columnNames.trim())) {
			String[] columnNamesArray = columnNames.split(",");
			for (String s : columnNamesArray) {
				columnNamesList.add(s);
//				if (k == 0) {
//					orignotnull.append(StringHandler.doubleQ(s)).append(
//							" is not null");
//					k = 1;
//				} else
					orignotnull.append(" and ")
							.append(StringHandler.doubleQ(s))
							.append(" is not null ");
			}
		}
		// if(k==0) orignotnull=new StringBuilder("");
		transformer.setColumnNames(columnNamesList);
		transformer.setAnalysisInterActionModel(para
				.getAnalysisInterActionModel());
		newDataSet = transformer.TransformCategoryToNumeric_new(dataSet, null);
		DatabaseConnection databaseConnection = ((DBTable) newDataSet
				.getDBTable()).getDatabaseConnection();

		Column label = newDataSet.getColumns().getLabel();
		// k = 0;

		// tempnotnull.append(" where ");
		// for(Column c:newDataSet.getColumns()){
		// if (k == 0) {
		// tempnotnull.append(StringHandler.doubleQ(c.getName())).append(" is not null");
		// k = 1;
		// } else
		// tempnotnull.append(" and ").append(StringHandler.doubleQ(c.getName())).append(" is not null ");
		//
		// }
		String labelName = StringHandler.doubleQ(label.getName());
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();

		String newTableName = ((DBTable) newDataSet.getDBTable())
				.getTableName();

		try {
			st = databaseConnection.createStatement(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		try {
			newDataSet.computeAllColumnStatistics();
			Columns atts = newDataSet.getColumns();

			Iterator<Column> atts_i = atts.iterator();

			int count = 0;
			String[] columnNamesArray = new String[atts.size()];
			while (atts_i.hasNext()) {
				Column att = atts_i.next();
				columnNamesArray[count] = att.getName();
				count++;
			}
			null_list = calculateNull(newDataSet);
			StringBuilder sb_notNull = getWhere(atts);

			getCoefficientAndR2(columnNames, dataSet, labelName, tableName,
					newTableName, atts, columnNamesArray, orignotnull,
					sb_notNull);

			StringBuffer sSQL = new StringBuffer();
			sSQL.append("select count(*) from ").append(tableName)
					.append(orignotnull.toString());
			int datasize = 0;
			try {
                itsLogger.debug("LinearRegressionImp.learn():sql=" + sSQL);
				rs = st.executeQuery(sSQL.toString());
				while (rs.next()) {
					datasize = rs.getInt(1);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(), e);
				throw new OperatorException(e.getLocalizedMessage());
			}
			long dof = datasize - columnNamesArray.length - 1;

			if (dof <= 0) {
				model.setS(Double.NaN);
				return model;
			}

			sSQL = createSSQLL(datasize, newTableName, label, columnNamesArray,
					coefficients).append(sb_notNull.toString());
			double s = 0.0;

			try {
				itsLogger.debug("LinearRegressionImp.learn():sql=" + sSQL);
				rs = st.executeQuery(sSQL.toString());
				while (rs.next()) {
					s = rs.getDouble(1);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				itsLogger.error(e.getMessage(), e);
				throw new OperatorException(e.getLocalizedMessage());
			}
			Matrix varianceCovarianceMatrix = getVarianceCovarianceMatrix(
					newTableName, columnNamesArray, st);
			if (varianceCovarianceMatrix == null) {
				model.setErrorString(AlpineDataAnalysisLanguagePack.getMessage(
						AlpineDataAnalysisLanguagePack.MATRIX_IS_SIGULAR,
						AlpineThreadLocal.getLocale())
						+ Tools.getLineSeparator());
			}
			caculateStatistics(columnNamesArray, coefficients, model, s,
					varianceCovarianceMatrix, dof);

			if (null_list.size() != 0) {
				StringBuilder sb_null = new StringBuilder();
				for (int i = 0; i < null_list.size(); i++) {
					sb_null.append(StringHandler.doubleQ(null_list.get(i)))
							.append(",");
				}
				sb_null = sb_null.deleteCharAt(sb_null.length() - 1);
				String table_exist_null = AlpineDataAnalysisLanguagePack
						.getMessage(
								AlpineDataAnalysisLanguagePack.TABLE_EXIST_NULL,
								AlpineThreadLocal.getLocale());
				String[] temp = table_exist_null.split(";");
				model.setErrorString(temp[0] + sb_null.toString() + temp[1]
						+ Tools.getLineSeparator());
			}
			if (transformer.isTransform()) {
				dropTable(newTableName);
			}
			st.close();
			itsLogger.debug(LogUtils.exit("LinearRegressionImp", "learn", model.toString()));
			return model;
		} catch (Exception e) {
			itsLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	protected void dropTable(String tableName) throws OperatorException {
		StringBuffer truncate = new StringBuffer();
		truncate.append("truncate table ").append(tableName);
		try {
			itsLogger.debug(
                    "LinearRegressionImp.dropTable():sql="
                            + truncate.toString());
			st.execute(truncate.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		StringBuffer dropSql = new StringBuffer();
		dropSql.append("drop table ");
		dropSql.append(tableName);
		try {
			itsLogger.debug(
                    "LinearRegressionImp.dropTable():sql="
                            + dropSql.toString());
			st.execute(dropSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

	abstract protected void getCoefficientAndR2(String columNames,
			DataSet newDataSet, String labelName, String tableName,
			String newTableName, Columns atts, String[] columnNames,
			StringBuilder sb_notNull, StringBuilder sbNotNull)
			throws OperatorException;

	protected StringBuilder getWhere(Columns atts) {
		if (null_list.size() == 0) {
			return new StringBuilder("");
		}
		Iterator<Column> atts_i;
		StringBuilder sb_notNull = new StringBuilder(" where ");
		atts_i = atts.iterator();
		while (atts_i.hasNext()) {
			Column att = atts_i.next();
			if (null_list.contains(att.getName())) {
				sb_notNull.append(StringHandler.doubleQ(att.getName())).append(
						" is not null and ");
			}
		}
		sb_notNull.delete(sb_notNull.length() - 4, sb_notNull.length());
		return sb_notNull;
	}

	
	protected StringBuffer createSSQLL(int datasize, String tableName,
			Column label, String[] columnNames, Double[] coefficients) {
		StringBuffer predictedY = new StringBuffer("("
				+ coefficients[coefficients.length - 1]);
		for (int i = 0; i < columnNames.length; i++) {
			predictedY.append("+" + coefficients[i] + "*\"" + columnNames[i]
					+ "\"");
		}
		String labelName = StringHandler.doubleQ(label.getName());
		predictedY.append(")");
		StringBuffer sSQL = new StringBuffer("select sqrt(");

		sSQL.append("sum((" + labelName + " - " + predictedY + ")*1.0*("
				+ labelName + " - " + predictedY + "))/"
				+ (datasize - columnNames.length - 1));
		sSQL.append(") from ").append(tableName);
		return sSQL;
	}

	
	protected void convertCoefficients(Double[] coefficients) {
		if (coefficients.length >= 2) {
			double bias = coefficients[0];
			for (int i = 0; i < coefficients.length - 1; i++) {
				coefficients[i] = coefficients[i + 1];
			}
			coefficients[coefficients.length - 1] = bias;
		}
	}

	
	protected void caculateStatistics(String[] columnNames,
			Double[] coefficients, LinearRegressionModelDB model, double s,
			Matrix varianceCovarianceMatrix, long dof) {
		double[] se = new double[columnNames.length + 1];
		double[] t = new double[columnNames.length + 1];
		double[] p = new double[columnNames.length + 1];

		// int newI = 0;
		for (int i = 0; i < columnNames.length; i++) {
			if (varianceCovarianceMatrix != null) {
				if (Double.isNaN(varianceCovarianceMatrix.get(i + 1, i + 1))) {
					se[i] = Double.NaN;
				} else {
					se[i] = s
							* Math.sqrt(Math.abs(varianceCovarianceMatrix.get(
									i + 1, i + 1)));
				}
			} else {
				se[i] = Double.NaN;
			}
			t[i] = coefficients[i] / se[i];
			p[i] = studT(t[i], dof);
		}
		if (varianceCovarianceMatrix != null) {
			if (Double.isNaN(varianceCovarianceMatrix.get(0, 0))) {
				se[columnNames.length] = Double.NaN;
			} else {
				se[columnNames.length] = s
						* Math.sqrt(Math.abs(varianceCovarianceMatrix.get(0, 0)));
			}
		} else {
			se[columnNames.length] = Double.NaN;
		}
		t[columnNames.length] = coefficients[columnNames.length]
				/ se[columnNames.length];
		p[columnNames.length] = studT(t[columnNames.length], dof);
		;

		model.setS(s);
		model.setSe(se);
		model.setT(t);
		model.setP(p);
	}

	protected Matrix getVarianceCovarianceMatrix(String tableName,
			String[] columnNames, Statement st) throws OperatorException {

		Matrix varianceCovarianceMatrix = null;
		try {
			varianceCovarianceMatrix = hessian.SVDInverse();
		} catch (Exception e) {
			return null;
		}
		return varianceCovarianceMatrix;
	}

	double studT(double t, long dof) {
		t = Math.abs(t);
		double PiD2 = Math.PI / 2;
		double w = t / Math.sqrt(dof);
		double th = Math.atan(w);
		if (dof == 1) {
			return 1 - th / PiD2;
		}
		double sth = Math.sin(th);
		double cth = Math.cos(th);
		if ((dof % 2) == 1) {
			return 1 - (th + sth * cth * statCom(cth * cth, 2, dof - 3, -1))
					/ PiD2;
		} else {
			return 1 - sth * statCom(cth * cth, 1, dof - 3, -1);
		}
	}

	double statCom(double q, int i, long j, int b) {
		double zz = 1;
		double z = zz;
		int k = i;
		while (k <= j) {
			zz = zz * q * k / (k - b);
			z = z + zz;
			k = k + 2;
		}
		return z;
	}

	protected void getCoefficientMap(String[] columnNames) {
		for (int i = 0; i < coefficients.length; i++) {
			if (i == 0) {
				coefficientmap.put("intercept",
						coefficients[coefficients.length - 1]);
			} else {
				coefficientmap.put(columnNames[i - 1], coefficients[i - 1]);
			}

		}
	}

	protected ArrayList<String> calculateNull(DataSet dataSet)
			throws OperatorException {
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable())
				.getDatabaseConnection();
		String tableName = ((DBTable) dataSet.getDBTable()).getTableName();
		float count = dataSet.size();
		ArrayList<String> null_list = new ArrayList<String>();
		Iterator<Column> i = dataSet.getColumns().iterator();
		StringBuilder sb_count = new StringBuilder("select ");
		while (i.hasNext()) {
			Column att = i.next();
			sb_count.append("count(")
					.append(StringHandler.doubleQ(att.getName())).append(")");
			sb_count.append(StringHandler.doubleQ(att.getName())).append(",");
		}
		sb_count = sb_count.deleteCharAt(sb_count.length() - 1);
		sb_count.append(" from ").append(tableName);
		try {
			Statement st = databaseConnection.createStatement(false);
			itsLogger.debug(
                    "LinearRegressionImp.calculateNull():sql="
                            + sb_count.toString());

			ResultSet rs = st.executeQuery(sb_count.toString());
			rs.next();
			for (int j = 0; j < rs.getMetaData().getColumnCount(); j++) {
				if (rs.getFloat(j + 1) != count) {
					null_list.add(dataSet.getColumns()
							.get(rs.getMetaData().getColumnName(j + 1))
							.getName());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(), e);
			throw new OperatorException(e.getLocalizedMessage());
		}
		return null_list;
	}
}
