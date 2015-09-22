/**
 * ClassName GoodnessOfFitGenerator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.evaluator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.resources.AlpineAnalysisErrorName;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.utility.tools.StringHandler;
import org.apache.log4j.Logger;

/**
 * GoodnessOfFitGenerator generate.
 */
public class GoodnessOfFitGenerator extends Operator{

    private static Logger itsLogger= Logger.getLogger(GoodnessOfFitGenerator.class);

    private EvaluatorParameter para ;
	private int columnCountLimit = 1000;
	public GoodnessOfFitGenerator() {
		super();
	}
	
	public ConsumerProducer[] apply() throws OperatorException {
		para = (EvaluatorParameter)getParameter();
        DataSet dataSet;
		try {
			dataSet = getInput(DataSet.class);
		} catch (RuntimeException e) {
			throw new OperatorException(e.getLocalizedMessage());
		}
		if (dataSet.getColumns().getLabel() == null) {
			throw new WrongUsedException(this, AlpineAnalysisErrorName.MISS_DEP);
		}
		Model model = null;
		if (para.isUseModel()) {
			model = getInput(Model.class);
			if(model instanceof com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel){
				throw new WrongUsedException(this, AlpineAnalysisErrorName.REGRESSION_EVALUATE,"Goodness Of Fit");
			}
			dataSet = model.apply(dataSet);
		}
		GoodnessOfFit goodnessOfFit = new GoodnessOfFit();
		createGoodnessOfFit(dataSet, goodnessOfFit);
	
		if (para.isUseModel()) {
			return new ConsumerProducer[] { dataSet, model, goodnessOfFit};
		} else
			return new ConsumerProducer[] {dataSet, goodnessOfFit };
	}

	private void createGoodnessOfFit(DataSet dataSet,
			GoodnessOfFit goodnessOfFit)
			throws OperatorException {
		Column label = dataSet.getColumns().getLabel();
		if(dataSet.getColumns().getPredictedLabel()==null)
		{
			throw new WrongUsedException(this, AlpineAnalysisErrorName.NULL_PREDICT_COL);
		}
		Column predictedLabel = dataSet.getColumns().getPredictedLabel();
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
		String labelName=StringHandler.doubleQ(label.getName());
		String predictLabelName=StringHandler.doubleQ(predictedLabel.getName());
		dataSet.computeColumnStatistics(label);
		ArrayList<String> array = new ArrayList<String>();
		for ( int i = 0; i < label.getMapping().size(); i++)
		{
			String targetClass = label.getMapping().mapIndex(i);
			targetClass=StringHandler.escQ(targetClass);
			array.add("sum(case when "+labelName+" = '"+targetClass+"' then 1 else 0 end) ");
			array.add("sum(case when "+predictLabelName+" = '"+targetClass+"' then 1 else 0 end)");
			array.add("sum(case when "+predictLabelName+" = '"+targetClass+"' and "+labelName+" = '"+targetClass+"' then 1 else 0 end)");
		}
		columnCountLimit = AlpineDataAnalysisConfig.ROC_MAX_COLUMN_COUNT;
		int cycle = array.size()/columnCountLimit;
		long[] result = new long[array.size()];
		for(int i = 0; i < cycle; i++){
			StringBuffer sql = new StringBuffer("select ");
			for(int j = 0; j < columnCountLimit; j++){
				if(j != 0){
					sql.append(",");
				}
				sql.append(array.get(i * columnCountLimit + j));
			}
			sql.append(" from ").append(tableName);
			try {
				itsLogger.debug("GoodnessOfFitGenerator.createGoodnessOfFit():sql=" + sql);
				rs = st.executeQuery(sql.toString());
				if(rs.next()){
					long[] currentResult = getResult(rs, columnCountLimit);
					for(int j = 0; j < currentResult.length; j++){
						result[i * columnCountLimit + j] = currentResult[j];
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}
		}
		if (array.size() > cycle * columnCountLimit){
			StringBuffer sql = new StringBuffer("select ");
			for(int j = 0; j < array.size() - cycle * columnCountLimit; j++){
				if(j != 0){
					sql.append(",");
				}
				sql.append(array.get(cycle * columnCountLimit + j));
			}
			sql.append(" from ").append(tableName);
			try {
				itsLogger.debug("GoodnessOfFitGenerator.createGoodnessOfFit():sql=" + sql);
				rs = st.executeQuery(sql.toString());
				if(rs.next()){
					long[] currentResult = getResult(rs, array.size() - cycle * columnCountLimit);
					for(int j = 0; j < currentResult.length; j++){
						result[cycle * columnCountLimit + j] = currentResult[j];
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new OperatorException(e.getLocalizedMessage());
			}

		}
		int allTrue = 0;
		for (int i = 0; i < label.getMapping().size(); i++) {
			String targetClass = label.getMapping().mapIndex(i);
			targetClass = StringHandler.escQ(targetClass);
			long actual = 0;
			long predicted = 0;
			long predictedTrue = 0;
			actual = result[i * 3];
			predicted = result[i * 3 + 1];
			predictedTrue = result[i * 3 + 2];
			allTrue += predictedTrue;
			double recall = Double.NaN;
			if (actual != 0) {
				recall = predictedTrue * 1.0 / actual;
			}
			// Precision(Positive Predicted Value,PV+)=true positive/ total
			// predicted positive=d/b+d
			double precision = Double.NaN;
			if (predicted != 0) {
				precision = predictedTrue * 1.0 / predicted;
			}
			double f1 = Double.NaN;
			if (!Double.isNaN(recall) && !Double.isNaN(precision)
					&& recall + precision != 0) {
				f1 = 2 * recall * precision / (recall + precision);
			}
			double specificity = Double.NaN;
			double sensitivity = Double.NaN;
			if (label.getMapping().size() == 2) {
				if (dataSet.size() - actual != 0) {
					specificity = (dataSet.size() - actual - predicted + predictedTrue)
							* 1.0 / (dataSet.size() - actual);
				}
				if (actual != 0) {
					sensitivity = predictedTrue * 1.0 / actual;
				}
			}
			ValueGoodnessOfFit data = new ValueGoodnessOfFit(targetClass,
					recall, precision, f1, specificity, sensitivity);
			// Specificity (TNR) = (TN) / actually are negative (TN+FP).
			// Sensitivity (TPR) = (TP) / actually are positive (TP+FN).
			goodnessOfFit.getGoodness().add(data);
		}

		double accuracy = Double.NaN;
		if (dataSet.size() != 0) {
			accuracy = allTrue * 1.0 / dataSet.size();
		}
		goodnessOfFit.setAccuracy(accuracy);
		goodnessOfFit.setError(1 - accuracy);
	}
	private long[] getResult(ResultSet rs, int length) throws SQLException{
		long[] result = new long[length];
		for(int i = 0; i < length; i++){
			result[i] = rs.getLong(i+1);
		}
		return result;
	}
	public Class<?>[] getInputClasses() {
		return new Class[] { DataSet.class, Model.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { DataSet.class, Model.class, GoodnessOfFit.class };
	}
}
