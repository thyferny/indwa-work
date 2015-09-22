/**
 * ClassName AdaboostModelOracle.java
 *
 * Version information: 1.00
 *
 * Data: 9 Oct 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.operator.adboost;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 * 
 */
public class AdaboostModelOracle extends AdaboostModel {

	/**
	 * 
	 */
	private static final Logger itsLogger = Logger.getLogger(AdaboostModelOracle.class);
	private static final long serialVersionUID = 217156591719684515L;

	/**
	 * @param paramDataSet
	 */
	public AdaboostModelOracle(DataSet paramDataSet) {
		super(paramDataSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.operator.adboost.AdaboostModel#spellArray(java.
	 * lang.StringBuffer, java.util.Iterator)
	 */
	@Override
	protected StringBuffer spellArray(StringBuffer inforArray,
			Iterator<String> localIterator) {

		inforArray.append("varchar2array(");

		while (localIterator.hasNext()) {
			String str = (String) localIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			inforArray.append("'" + str + "',");
		}
		inforArray.deleteCharAt(inforArray.length() - 1);
		inforArray.append(")");

		return inforArray;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.operator.adboost.AdaboostModel#adaboostPredictionInit
	 * (java.lang.String, java.lang.String, long, java.lang.String,
	 * java.lang.String, java.sql.Statement, java.lang.StringBuffer)
	 */
	@Override
	protected String adaboostPredictionInit(String outTable,
			String tempOutTable, long timeStamp, String schemaName,
			String dependentColumn, Statement st, StringBuffer inforArray)
			throws OperatorException {
		try {

			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_initpre(");
			sql.append(StringUtil.isEmpty(schemaName) ? "null" : ("'"
					+ StringHandler.doubleQ(schemaName) + "'"));
			sql.append(",'");
			sql.append(outTable);
			sql.append("','");
			sql.append(timeStamp);
			sql.append("','");
			sql.append(StringHandler.doubleQ(dependentColumn));
			sql.append("',");
			sql.append(inforArray);
			sql.append(") from dual");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionInit():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());
			String sql1 = "alter session force parallel ddl";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionInit():sql="+sql1);
			}
			st.execute(sql1);
			sql1 = "CREATE TABLE "
					+ (StringUtil.isEmpty(schemaName) ? "" : (StringHandler
							.doubleQ(schemaName) + "."))
					+ StringHandler.doubleQ(tempOutTable)
					+ " parallel as select * from  " + outTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionInit():sql="+sql1);
			}
			st.execute(sql1);
			sql1 = "alter session disable parallel ddl";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionInit():sql="+sql1);
			}
			st.execute(sql1);

			tempOutTable = (StringUtil.isEmpty(schemaName) ? ""
					: (StringHandler.doubleQ(schemaName) + "."))
					+ StringHandler.doubleQ(tempOutTable);

		} catch (SQLException e1) {
			throw new OperatorException(e1.getLocalizedMessage());
		}
		return tempOutTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.operator.adboost.AdaboostModel#adaboostPredictStep
	 * (java.lang.String, java.lang.String, java.lang.String,
	 * java.sql.Statement, double, java.util.Iterator, java.lang.StringBuffer)
	 */
	@Override
	protected void adaboostPredictStep(String outTable, String tempOutTable,
			String dependentColumn, Statement st, double algWeight,
			Iterator<String> sampleDvalueIterator, StringBuffer sampleArray)
			throws SQLException {

		sampleArray.append("varchar2array(");

		while (sampleDvalueIterator.hasNext()) {
			String str = (String) sampleDvalueIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			sampleArray.append("'");
			sampleArray.append(str);
			sampleArray.append("',");
		}
		sampleArray.deleteCharAt(sampleArray.length() - 1);
		sampleArray.append(")");

		StringBuffer sql = new StringBuffer();
		sql.append("select alpine_miner_adaboost_prestep('");
		sql.append(outTable);
		sql.append("','");
		sql.append(StringHandler.doubleQ(tempOutTable));
		sql.append("','");
		sql.append(dependentColumn);
		sql.append("',");
		sql.append(algWeight);
		sql.append(",");
		sql.append(sampleArray);
		sql.append(") from dual");
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("AdaboostModelOracle.adaboostPredictionPredictStep():sql="+sql.toString());
		}
		st.executeQuery(sql.toString());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.operator.adboost.AdaboostModel#adaboostPredictResult
	 * (java.lang.String, long, java.lang.String, java.lang.String,
	 * java.sql.Statement, java.lang.StringBuffer, double)
	 */
	@Override
	protected void adaboostPredictResult(String outTable, long timeStamp,
			String tempOutTable, String dependentColumn, Statement st,
			StringBuffer inforArray, double sumc,DataSet dataSet) throws OperatorException {

		try {

			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_prere('");
			sql.append(outTable);
			sql.append("','");
			sql.append(dependentColumn.replace("\"", "\"\""));
			sql.append("',");
			sql.append(inforArray);
			sql.append(",");
			sql.append((getLabel().isNumerical() ? 1 : 0));
			sql.append(") from dual");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionResult():sql="+sql.toString());
				}
			st.executeQuery(sql.toString());
			String sql1 = "drop table " + tempOutTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionResult():sql="+sql1);
			}
			st.execute(sql1);

			String sql2 = "alter table " + outTable
					+ " drop column \"alpine_adaboost_id\"";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionResult():sql="+sql2);
			}
			st.execute(sql2);
			StringBuffer normaString = new StringBuffer();
			normaString.append("update " + outTable + " set ");
			Iterator<String> localIterator = getTrainingHeader().getColumns()
					.getLabel().getMapping().getValues().iterator();
			while (localIterator.hasNext()) {
				String str = (String) localIterator.next();
				str = StringHandler.doubleQ(str);
				str = str.substring(1, str.length() - 1);
				normaString.append("\"").append(Column.CONFIDENCE_NAME).append(
						"(").append(str).append(")\"=").append("\"").append(
						Column.CONFIDENCE_NAME).append("(").append(str).append(
						")\"/").append(sumc).append(" ,");
			}
			normaString.deleteCharAt(normaString.length() - 1);
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelOracle.adaboostPredictionResult():sql="+normaString.toString());
			}
			st.execute(normaString.toString());
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
	}

}