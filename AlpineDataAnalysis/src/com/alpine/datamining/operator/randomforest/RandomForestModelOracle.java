

package com.alpine.datamining.operator.randomforest;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;



public class RandomForestModelOracle extends RandomForestModel{

	
	private static final long serialVersionUID = 9112298641927527533L;

	public RandomForestModelOracle(DataSet trainingDataSet) {
		super(trainingDataSet);
		// TODO Auto-generated constructor stub
	}

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

	@Override
	protected void randomForestPredictResult(String outTable, long timeStamp,
			String tempOutTable, String dependentColumn, Statement st,
			StringBuffer inforArray, double sumc, DataSet dataSet)
			throws OperatorException {
		try {

			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_rf_prere('");
			sql.append(outTable);
			sql.append("','");
			sql.append(dependentColumn.replace("\"", "\"\""));
			sql.append("',");
			sql.append(inforArray);
			sql.append(",");
			sql.append((getLabel().isNumerical() ? 1 : 0));
			sql.append(") from dual");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("RandomForestModelOracle.RandomForestPredictionResult():sql="+sql.toString());
			
			}
			st.executeQuery(sql.toString());
			String sql1 = "drop table " + tempOutTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("RandomForestModelOracle.RandomForestPredictionResult():sql="+sql1);
			}
			st.execute(sql1);

			String sql2 = "alter table " + outTable
					+ " drop column \"alpine_randomforest_id\"";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("RandomForestModelOracle.RandomForestPredictionResult():sql="+sql2);
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
				itsLogger.debug("RandomForestModelOracle.RandomForestPredictionResult():sql="+normaString.toString());
			}
			st.execute(normaString.toString());
		} catch (SQLException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
	}
 

	@Override
	protected void randomForestPredictStep(String outTable,
			String tempOutTable, String dependentColumn, Statement st,
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
		sql.append("select alpine_miner_rf_prestep('");
		sql.append(outTable);
		sql.append("','");
		sql.append(StringHandler.doubleQ(tempOutTable));
		sql.append("',");
		sql.append(sampleArray);
		sql.append(") from dual");
		if(itsLogger.isDebugEnabled()){
			itsLogger.debug("randomForestModelOracle.randomForestPredictionPredictStep():sql="+sql.toString());
		}
		st.executeQuery(sql.toString());
		
	}

	@Override
	protected String randomForestPredictionInit(String outTable,
			String tempOutTable, long timeStamp, String schemaName,
			String dependentColumn, Statement st, StringBuffer inforArray)
			throws OperatorException {
		try {

			StringBuffer sql = new StringBuffer();
			sql.append("select alpine_miner_rf_initpre(");
			sql.append(StringUtil.isEmpty(schemaName) ? "null" : ("'"
					+ StringHandler.doubleQ(schemaName) + "'"));
			sql.append(",'");
			sql.append(outTable);
			sql.append("','");
			sql.append(timeStamp);
			sql.append("',");
			
			sql.append(inforArray);
			sql.append(") from dual");
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("randomforestModelOracle.randomforestPredictionInit():sql="+sql.toString());
			}
			st.executeQuery(sql.toString());
			String sql1 = "alter session force parallel ddl";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("randomforestModelOracle.randomforestPredictionInit():sql="+sql1);
			}
			st.execute(sql1);
			sql1 = "CREATE TABLE "
					+ (StringUtil.isEmpty(schemaName) ? "" : (StringHandler
							.doubleQ(schemaName) + "."))
					+ StringHandler.doubleQ(tempOutTable)
					+ " parallel as select * from  " + outTable;
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("randomforestModelOracle.randomforestPredictionInit():sql="+sql1);
			}
			st.execute(sql1);
			sql1 = "alter session disable parallel ddl";
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("randomforestModelOracle.randomforestPredictionInit():sql="+sql1);
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

}
