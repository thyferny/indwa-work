

package com.alpine.datamining.operator.adboost;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.tools.StringHandler;


public class AdaboostModelGreenplum extends AdaboostModel {

	
	private static final Logger itsLogger = Logger.getLogger(AdaboostModelGreenplum.class);
	private static final long serialVersionUID = -3493418828591059884L;

	
	public AdaboostModelGreenplum(DataSet paramDataSet) {
		super(paramDataSet);
	}

	protected StringBuffer spellArray(StringBuffer inforArray,
			Iterator<String> localIterator) {

		inforArray.append("array[");

		while (localIterator.hasNext()) {
			String str = (String) localIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			inforArray.append("'" + str + "',");

		}
		inforArray.deleteCharAt(inforArray.length() - 1);
		inforArray.append("]");
		return inforArray;
	}

	
	@Override
	protected String adaboostPredictionInit(String outTable,
			String tempOutTable, long timeStamp, String schemaName,
			String dependentColumn, Statement st, StringBuffer inforArray)
			throws OperatorException {
		try {

			boolean isTemp = false;
			if (!outTable.contains(".")) 
			{
				isTemp = true;
			};

			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_adaboost_initpre('");
			sql.append(outTable);
			sql.append("','");
			sql.append(timeStamp);
			sql.append("','");
			sql.append(StringHandler.doubleQ(dependentColumn));
			sql.append("',");
			sql.append(inforArray);
			sql.append(",");
			sql.append(isTemp).append(")");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelGrennplum.adaboostPredictionInit():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());
			String sql1 = "CREATE TEMP TABLE \"" + tempOutTable
					+ "\" as select * from  " + outTable
					+ " DISTRIBUTED BY (\"alpine_adaboost_id\")";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelGrennplum.adaboostPredictionInit():sql="+sql1);
			}
			st.execute(sql1);

		} catch (SQLException e1) {
			throw new OperatorException(e1.getLocalizedMessage());
		}
		return tempOutTable;
	}

	
	@Override
	protected void adaboostPredictStep(String outTable, String tempOutTable,
			String dependentColumn, Statement st, double algWeight,
			Iterator<String> sampleDvalueIterator, StringBuffer sampleArray)
			throws SQLException {

		sampleArray.append("array[");

		while (sampleDvalueIterator.hasNext()) {
			String str = (String) sampleDvalueIterator.next();
			str = StringHandler.doubleQ(str);
			str = str.substring(1, str.length() - 1);
			sampleArray.append("'");
			sampleArray.append(str);
			sampleArray.append("',");

		}
		sampleArray.deleteCharAt(sampleArray.length() - 1);
		sampleArray.append("]");

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
		sql.append(")");
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("AdaboostModelGrennplum.adaboostPredictionPredictStep():sql="+sql.toString());
		}
		st.executeQuery(sql.toString());



	}

	
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
			sql.append(")");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelGreenplum.adaboostPredictionResult():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());

			String sql1 = "alter table " + outTable
					+ " drop column \"alpine_adaboost_id\"";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("AdaboostModelGreenplum.adaboostPredictionResult():sql="+sql1);
			}
			st.execute(sql1);
			StringBuffer normaString = new StringBuffer();
			normaString.append("update " + outTable + " set ");
			Iterator<String> localIterator = getTrainingHeader().
				getColumns().getLabel().getMapping().getValues()
				.iterator();
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
				itsLogger.debug("AdaboostModelGreenplum.adaboostPredictionResult():sql="+normaString.toString());
			}
			st.execute(normaString.toString());
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
}
