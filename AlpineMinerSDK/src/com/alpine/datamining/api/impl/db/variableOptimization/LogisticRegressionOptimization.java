/**
 * ClassName LogisticRegressionOptimization.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.variableOptimization;
/** 
 * Jeff
 */

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionOptimizationConfig;
import com.alpine.datamining.api.impl.db.AbstractDBAnalyzer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.regressions.LogisticRegressionDBNewton;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.operator.regressions.LogisticRegressionParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.utility.file.StringUtil;

public class LogisticRegressionOptimization extends AbstractDBAnalyzer {
	private static Logger logger= Logger.getLogger(LogisticRegressionOptimization.class);
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		try {
			LogisticRegressionOptimizationConfig config = (LogisticRegressionOptimizationConfig) source
					.getAnalyticConfig();
			DataSet dataSet = getDataSet(
					((DataBaseAnalyticSource) source), config);
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			double confidence = Double.parseDouble(config.getConfidence());
			LogisticRegressionModelDB model = null;
			while (dataSet.getColumns().size() != 0) {
				logger.debug("VariableSelector:Enter loop");
				Columns atts = dataSet.getColumns();
				Operator learner = initOperator(config, dataSet);
				model = (LogisticRegressionModelDB) ((Training) learner)
						.train(dataSet);
				String maxAttName = findMaxPValue(model, confidence);
				if (maxAttName == null)
					break;
				else {
					if (atts.get(maxAttName) != null) {
						logger.debug(
								"VariableSelector:remove column: "
										+ maxAttName);
						Column att = atts.get(maxAttName);
						atts.remove(att);
					} else {
						maxAttName = maxAttName.substring(0, maxAttName
								.lastIndexOf("_"));
						logger.debug(
								"VariableSelector:remove column: "
										+ maxAttName);
						Column att = atts.get(maxAttName);
						atts.remove(att);
					}
				}
			}
			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(
					model);
			return analyzerOutPutModel;
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof WrongUsedException) {
				throw new AnalysisError(this, (WrongUsedException) e);
			} else if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		}

	}

	private String findMaxPValue(LogisticRegressionModelDB model,
			double confidence) {
		double[] pValueArray = model.getpValue();
		String[] columnNameArray = model.getColumnNames();
		double max = Double.NEGATIVE_INFINITY;
		String maxColumn = null;
		for (int i = 0; i < pValueArray.length - 1; i++) {
			if (pValueArray[i] > max) {
				max = pValueArray[i];
				maxColumn = columnNameArray[i];
			}
		}
		if (max > confidence) {
			return maxColumn;
		} else {
			return null;
		}
	}

	private Operator initOperator(LogisticRegressionOptimizationConfig config,
			DataSet dataSet) throws OperatorException{
		Operator learner = OperatorUtil
				.createOperator(LogisticRegressionDBNewton.class);
		logger.debug(
				"VariableSelector  addAnalyzerID: ="
						+ String.valueOf(this.hashCode()));
		LogisticRegressionParameter parameter = new LogisticRegressionParameter();
		logger.info("LogisticRegressionOptimization  addAnalyzerID: ="+String.valueOf(this.hashCode()));

		if(!StringUtil.isEmpty(config.getAdd_intercept()))
		{
			parameter.setAddInercept(Boolean.parseBoolean(config.getAdd_intercept()));
		}
		if(!StringUtil.isEmpty(config.getMax_generations()))
		{
			parameter.setMaxGenerations(Integer.parseInt(config.getMax_generations()));
		}

		if ( !StringUtil.isEmpty(config.getGoodValue()))
		{
			parameter.setGoodValue(config.getGoodValue());
		}
		if ( !StringUtil.isEmpty(config.getEpsilon()))
		{
			parameter.setEpsilon(Double.parseDouble(config.getEpsilon()));
		}
		learner.setParameter(parameter);
		warnTooManyValue(dataSet, Integer
				.parseInt(AlpineMinerConfig.C2N_WARNING),config.getLocale());
		return learner;
	}

	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) throws OperatorException {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((LogisticRegressionOptimizationConfig)config).getDependentColumn());

	}

}
