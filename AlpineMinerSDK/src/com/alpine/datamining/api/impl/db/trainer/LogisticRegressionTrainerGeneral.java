/**
 * ClassName LogisticRegressionTrainerGeneral.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionAIC;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionAICLogistic;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionIMP;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionSBC;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionSBCLogistic;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.datamining.operator.regressions.LogisticRegressionDBNewton;
import com.alpine.datamining.operator.regressions.LogisticRegressionModelDB;
import com.alpine.datamining.operator.regressions.LogisticRegressionParameter;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.datamining.utility.StatisticsChiSquareTest;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.StringHandler;

/**
 * 
 * @author Jeff
 * 
 */

public class LogisticRegressionTrainerGeneral extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(LogisticRegressionTrainerGeneral.class);
	
	private double MAXDOUBLE = Double.MAX_VALUE;
	private double checkValue = 0.95;
	private String trueString = Resources.TrueOpt;

	private String no = Resources.NoOpt;
	private String forward = "FORWARD";
	private String backward = "BACKWARD";
	private String stepwise = "STEPWISE";

	private String changedString = null;
	boolean noCriterion = false;
	protected AbstractAnalyzer stepwiseAnalyzer = null;

	protected Model train(AnalyticSource source) throws AnalysisException {

		try {
			LogisticRegressionConfigGeneral config = (LogisticRegressionConfigGeneral) source
					.getAnalyticConfig();
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource) source),
					config);
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			Operator learner = OperatorUtil
					.createOperator(LogisticRegressionDBNewton.class);
			LogisticRegressionParameter parameter = new LogisticRegressionParameter();
			logger.info(
					"LogisticRegressionTrainer  addAnalyzerID: ="
							+ String.valueOf(this.hashCode()));
			if (!StringUtil.isEmpty(config.getColumnNames())) {
				parameter.setColumnNames(config.getColumnNames());
			}

			if (!StringUtil.isEmpty(config.getAdd_intercept())) {
				parameter.setAddInercept(Boolean.parseBoolean(config
						.getAdd_intercept()));
			}
			if (!StringUtil.isEmpty(config.getMax_generations())) {
				parameter.setMaxGenerations(Integer.parseInt(config
						.getMax_generations()));
			}

			if (!StringUtil.isEmpty(config.getGoodValue())) {
				parameter.setGoodValue(config.getGoodValue());
			}
			if (!StringUtil.isEmpty(config.getEpsilon())) {
				parameter.setEpsilon(Double.parseDouble(config.getEpsilon()));
			}
			if (config.getInterActionModel() != null) {
				parameter.setAnalysisInteractionModel(config
						.getInterActionModel());
			}
			if (config.getSplitModelGroupByColumn() !=null
					&&config.getIsStepWise().equals(Resources.FalseOpt))
			{
				parameter.setGroupBy(true);
				parameter.setGroupByColumn(config.getSplitModelGroupByColumn());
				config.setVisualizationTypeClass(LogisticRegressionConfigGeneral.SPLIT_MODEL_VISUALIZATION_TYPE);
			}
			
			
			learner.setParameter(parameter);
			warnTooManyValue(dataSet, Integer
					.parseInt(AlpineMinerConfig.C2N_WARNING),config.getLocale());
			Model model = null;
			if (!StringUtil.isEmpty(config.getIsStepWise())
					&& config.getIsStepWise().equalsIgnoreCase(trueString)) {
				model = stepWise(source);
			} else {
				model = ((Training) learner).train(dataSet);
			}
			return model;
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

	protected Model stepWise(AnalyticSource source) throws AnalysisException,
			OperatorException {
		Statement st=null;
		ResultSet rs = null;
		try {

			LogisticRegressionConfigGeneral config = (LogisticRegressionConfigGeneral) source
					.getAnalyticConfig();

			String dbSystem = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo().getSystem();

			String url = ((DataBaseAnalyticSource) source).getDataBaseInfo()
					.getUrl();
			String userName = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo().getUserName();
			String password = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo().getPassword();
			String inputSchema = ((DataBaseAnalyticSource) source)
					.getTableInfo().getSchema();
			String tableName = ((DataBaseAnalyticSource) source).getTableInfo()
					.getTableName();
			String stepType = config.getStepWiseType();
			Connection conncetion = ((DataBaseAnalyticSource) source)
					.getConnection();

			 st = conncetion.createStatement();

			String sql = "select count(*) from "
					+ StringHandler.doubleQ(inputSchema) + "."
					+ StringHandler.doubleQ(tableName);
			
			rs = st.executeQuery(sql);
			long rowNumber = 0;
			while (rs.next()) {
				rowNumber = rs.getLong(1);
			}

			if (!StringUtil.isEmpty(config.getCheckValue())) {
				checkValue = 1 - Double.parseDouble(config.getCheckValue());
			}
			String useSSL = ((DataBaseAnalyticSource) source)
					.getDataBaseInfo().getUseSSL ();
		
			DataBaseAnalyticSource tempsource = new DataBaseAnalyticSource(
					dbSystem, url, userName, password, inputSchema, tableName,useSSL);
			String criterionType = config.getCriterionType();
			CriterionIMP criterion = null;

			if (StringUtil.isEmpty(criterionType)) {

				criterion = new CriterionAICLogistic();
				noCriterion = true;

			} else if (criterionType
					.equalsIgnoreCase(CriterionAIC.criterionType)) {
				criterion = new CriterionAICLogistic();

			} else if (criterionType
					.equalsIgnoreCase(CriterionSBC.criterionType)) {
				criterion = new CriterionSBCLogistic();
			}
			String columnNames = config.getColumnNames();
			LogisticRegressionConfigGeneral tempConfig = new LogisticRegressionConfigGeneral();
			tempConfig.setMax_generations(config.getMax_generations());
			tempConfig.setGoodValue(config.getGoodValue());
			tempConfig.setDependentColumn(config.getDependentColumn());
			tempConfig.setForceRetrain(config.getForceRetrain());
			tempConfig.setIsStepWise(no);

			tempsource.setAnalyticConfiguration(tempConfig);
			tempsource.setConenction(conncetion);
			String[] columnNamesArray = columnNames.split(",");
			List<String> chooseCoeList = new ArrayList<String>();
			List<String> unChooseCoeList = new ArrayList<String>();
			boolean firstTime = true;
			LogisticRegressionModelDB bestModel = null;
			if (stepType.equalsIgnoreCase(forward)) {
				bestModel = forward(rowNumber, tempsource, criterion,
						tempConfig, columnNamesArray, chooseCoeList,
						unChooseCoeList, firstTime);
				return bestModel;

			} else if (stepType.equalsIgnoreCase(backward)) {
				bestModel = backward(rowNumber, tempsource, criterion,
						columnNames, tempConfig, columnNamesArray,
						chooseCoeList, unChooseCoeList);
				return bestModel;
			} else if (stepType.equalsIgnoreCase(stepwise)) {
				bestModel = stepwise(rowNumber, tempsource, criterion,
						tempConfig, columnNamesArray, chooseCoeList,
						unChooseCoeList, firstTime);
				return bestModel;
			}
		} catch (SQLException e1) {
			logger.debug(e1.toString());
			throw new OperatorException(e1.getLocalizedMessage());
		}finally{
			try {
				if(st != null)
				{
					st.close();
				}
				if(rs!=null)
				{
					rs.close();
				}
			} catch (SQLException e) {
				logger.debug(e.toString());
			throw new OperatorException(e.getLocalizedMessage());
			}
		}
		return null;

	}

	/**
	 * @param rowNumber
	 * @param tempsource
	 * @param criterion
	 * @param tempConfig
	 * @param columnNamesArray
	 * @param chooseCoeList
	 * @param unChooseCoeList
	 * @param firstTime
	 * @return
	 * @throws AnalysisException
	 */
	private LogisticRegressionModelDB stepwise(long rowNumber,
			DataBaseAnalyticSource tempsource, CriterionIMP criterion,
			LogisticRegressionConfigGeneral tempConfig,
			String[] columnNamesArray, List<String> chooseCoeList,
			List<String> unChooseCoeList, boolean firstTime)
			throws AnalysisException {
		LogisticRegressionModelDB bestModel;
		LogisticRegressionModelDB preModel = null;

		double bestCreiterion;

		for (String tempColumnName : columnNamesArray) {
			unChooseCoeList.add(tempColumnName);
		}
 
 		preModel = (LogisticRegressionModelDB) addOneColumn(tempConfig,
				chooseCoeList, unChooseCoeList, firstTime, tempsource,
				rowNumber);
		bestModel = preModel;
		bestCreiterion = criterion.getCriterion(preModel, rowNumber);
		firstTime = false;
		while (!unChooseCoeList.isEmpty()) {

			LogisticRegressionModelDB afterModel = (LogisticRegressionModelDB) addOneColumn(
					tempConfig, chooseCoeList, unChooseCoeList, firstTime,
					tempsource, rowNumber);

			double afterQ = afterModel.getModelDeviance();
			double preQ = preModel.getModelDeviance();
			double p = StatisticsChiSquareTest.chiSquareTest(1.0 / 2,
					(preQ - afterQ) / 2);
			if (p < checkValue) {
				if (chooseCoeList.contains(changedString)) {
					chooseCoeList.remove(changedString);
				}

				unChooseCoeList.add(changedString);
				if (noCriterion) {
					return preModel;
				} else {
					return bestModel;
				}
			} else {
				LogisticRegressionModelDB dropPreModel = afterModel;
				LogisticRegressionModelDB dropBestModel = afterModel;
				double dropBestCreiterion = criterion.getCriterion(afterModel,
						rowNumber);
				while (chooseCoeList.size() > 1) {
					LogisticRegressionModelDB dropAfterModel = (LogisticRegressionModelDB) dropOneColumn(
							tempConfig, chooseCoeList, unChooseCoeList,
							tempsource, rowNumber);
					double dropAfterQ = dropAfterModel.getModelDeviance();
					double dropPreQ = dropPreModel.getModelDeviance();
					double dropP = StatisticsChiSquareTest.chiSquareTest(
							1.0 / 2, (dropAfterQ - dropPreQ) / 2);
					if (dropP > checkValue) {
						if (unChooseCoeList.contains(changedString)) {
							unChooseCoeList.remove(changedString);
						}

						chooseCoeList.add(changedString);
						break;
					} else {
						double dropTempCriterion = criterion.getCriterion(
								dropAfterModel, rowNumber);
						if (dropTempCriterion < dropBestCreiterion) {
							dropBestModel = dropAfterModel;
							dropBestCreiterion = dropTempCriterion;
						}
						dropPreModel = dropAfterModel;
					}
				}
				double tempCriterion = criterion.getCriterion(dropBestModel,
						rowNumber);
				if (tempCriterion < bestCreiterion) {
					bestModel = dropBestModel;
					bestCreiterion = tempCriterion;
				}
				preModel = dropBestModel;
			}
		}
		return bestModel;
	}

	/**
	 * @param rowNumber
	 * @param tempsource
	 * @param criterion
	 * @param columnNames
	 * @param tempConfig
	 * @param columnNamesArray
	 * @param chooseCoeList
	 * @param unChooseCoeList
	 * @return
	 * @throws AnalysisException
	 */
	private LogisticRegressionModelDB backward(long rowNumber,
			DataBaseAnalyticSource tempsource, CriterionIMP criterion,
			String columnNames, LogisticRegressionConfigGeneral tempConfig,
			String[] columnNamesArray, List<String> chooseCoeList,
			List<String> unChooseCoeList) throws AnalysisException {
		LogisticRegressionModelDB bestModel;
		LogisticRegressionModelDB preModel = null;

		double bestCreiterion;

		for (String tempColumnName : columnNamesArray) {
			chooseCoeList.add(tempColumnName);
		}
		tempConfig.setColumnNames(columnNames);
		tempsource.setAnalyticConfiguration(tempConfig);
		stepwiseAnalyzer = new LogisticRegressionTrainerGeneral();
		stepwiseAnalyzer.setListeners(getListeners());
		preModel = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
				.doAnalysis(tempsource)).getEngineModel().getModel();
		bestModel = preModel;
		bestCreiterion = criterion.getCriterion(preModel, rowNumber);
		while (chooseCoeList.size() > 1) {
			LogisticRegressionModelDB afterModel = (LogisticRegressionModelDB) dropOneColumn(
					tempConfig, chooseCoeList, unChooseCoeList, tempsource,
					rowNumber);
			double afterQ = afterModel.getModelDeviance();
			double preQ = preModel.getModelDeviance();
			double p = StatisticsChiSquareTest.chiSquareTest(1.0 / 2,
					(afterQ - preQ) / 2);
			if (p > checkValue) {
				if (unChooseCoeList.contains(changedString)) {
					unChooseCoeList.remove(changedString);
				}

				chooseCoeList.add(changedString);
				if (noCriterion) {
					return preModel;
				} else {
					return bestModel;
				}
			} else {
				double tempCriterion = criterion.getCriterion(afterModel,
						rowNumber);
				if (tempCriterion < bestCreiterion) {
					bestModel = afterModel;
					bestCreiterion = tempCriterion;
				}
				preModel = afterModel;
			}

		}
		return bestModel;
	}

	/**
	 * @param rowNumber
	 * @param tempsource
	 * @param criterion
	 * @param tempConfig
	 * @param columnNamesArray
	 * @param chooseCoeList
	 * @param unChooseCoeList
	 * @param firstTime
	 * @return
	 * @throws AnalysisException
	 */
	private LogisticRegressionModelDB forward(long rowNumber,
			DataBaseAnalyticSource tempsource, CriterionIMP criterion,
			LogisticRegressionConfigGeneral tempConfig,
			String[] columnNamesArray, List<String> chooseCoeList,
			List<String> unChooseCoeList, boolean firstTime)
			throws AnalysisException {
		LogisticRegressionModelDB bestModel;
		LogisticRegressionModelDB preModel = null;
		tempConfig.setColumnNames("");
		double bestCreiterion;

		for (String tempColumnName : columnNamesArray) {
			unChooseCoeList.add(tempColumnName);
		}

		preModel = (LogisticRegressionModelDB) addOneColumn(tempConfig,
				chooseCoeList, unChooseCoeList, firstTime, tempsource,
				rowNumber);
		bestModel = preModel;
		bestCreiterion = criterion.getCriterion(preModel, rowNumber);
		firstTime = false;
		while (!unChooseCoeList.isEmpty()) {
			LogisticRegressionModelDB afterModel = (LogisticRegressionModelDB) addOneColumn(
					tempConfig, chooseCoeList, unChooseCoeList, firstTime,
					tempsource, rowNumber);

			double afterQ = afterModel.getModelDeviance();
			double preQ = preModel.getModelDeviance();

			double p = StatisticsChiSquareTest.chiSquareTest(1.0 / 2,
					(preQ - afterQ) / 2);
			if (p < checkValue) {
				if (chooseCoeList.contains(changedString)) {
					chooseCoeList.remove(changedString);
				}

				unChooseCoeList.add(changedString);
				if (noCriterion) {
					return preModel;
				} else {
					return bestModel;
				}
			} else {
				double tempCriterion = criterion.getCriterion(afterModel,
						rowNumber);
				if (tempCriterion < bestCreiterion) {
					bestModel = afterModel;
					bestCreiterion = tempCriterion;
				}
				preModel = afterModel;
			}
		}
		return bestModel;
	}

	protected Model addOneColumn(AbstractModelTrainerConfig tempConfig,
			List<String> chooseCoeList, List<String> unChooseCoeList,
			boolean firstTime, DataBaseAnalyticSource tempsource, long rowNumber)
			throws AnalysisException {

		String dynamicColumnName;

		double miniMD = MAXDOUBLE;
		LogisticRegressionModelDB result = null;
		boolean firstLoop = true;
		String miniColumn = null;

		if (firstTime == true) {
			for (String tempColumnName : unChooseCoeList) {

				stepwiseAnalyzer = new LogisticRegressionTrainerGeneral();
				stepwiseAnalyzer.setListeners(getListeners());

				dynamicColumnName = tempColumnName;

				tempConfig.setColumnNames(dynamicColumnName);
				tempsource.setAnalyticConfiguration(tempConfig);
				if (firstLoop == true) {
					result = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					miniMD = result.getModelDeviance();
					firstLoop = false;
					miniColumn = tempColumnName;
				} else {
					LogisticRegressionModelDB tempResult = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					double tempMD = tempResult.getModelDeviance();
					if (tempMD < miniMD) {
						miniMD = tempMD;
						result = tempResult;
						miniColumn = tempColumnName;
					}
				}
			}
			if (unChooseCoeList.contains(miniColumn)) {
				unChooseCoeList.remove(miniColumn);
			}
			chooseCoeList.add(miniColumn);
			tempConfig.setColumnNames(miniColumn);
		} else {
			StringBuffer dynamicColumn = new StringBuffer();
			for (String tempColumnName : chooseCoeList) {

				dynamicColumn.append(tempColumnName);
				dynamicColumn.append(",");

			}
			String choosedColumn = dynamicColumn.deleteCharAt(
					dynamicColumn.length() - 1).toString();

			for (String tempColumn : unChooseCoeList) {
				stepwiseAnalyzer = new LogisticRegressionTrainerGeneral();
				stepwiseAnalyzer.setListeners(getListeners());
				dynamicColumnName = choosedColumn + "," + tempColumn;

				tempConfig.setColumnNames(dynamicColumnName);
				tempsource.setAnalyticConfiguration(tempConfig);
				if (firstLoop == true) {
					result = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					miniMD = result.getModelDeviance();

					firstLoop = false;
					miniColumn = tempColumn;
				} else {
					LogisticRegressionModelDB tempResult = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					double tempS = tempResult.getModelDeviance();
					if (tempS < miniMD) {
						miniMD = tempS;
						result = tempResult;
						miniColumn = tempColumn;
					}
				}
			}
			if (unChooseCoeList.contains(miniColumn))
				unChooseCoeList.remove(miniColumn);
			chooseCoeList.add(miniColumn);
			tempConfig.setColumnNames(choosedColumn.toString() + ","
					+ miniColumn);
		}
		return result;

	}

	protected Model dropOneColumn(AbstractModelTrainerConfig tempConfig,
			List<String> chooseCoeList, List<String> unChooseCoeList,
			DataBaseAnalyticSource tempsource, long rowNumber)
			throws AnalysisException {

		double miniS = MAXDOUBLE;
		LogisticRegressionModelDB result = null;
		boolean firstLoop = true;
		String miniColumn = null;
		String miniColumns = null;

		for (String tempColumn : chooseCoeList) {
			stepwiseAnalyzer = new LogisticRegressionTrainerGeneral();
			stepwiseAnalyzer.setListeners(getListeners());

			StringBuffer dynamicColumn = new StringBuffer();
			for (String tempColumnName : chooseCoeList) {
				if (!tempColumnName.equalsIgnoreCase(tempColumn)) {
					dynamicColumn.append(tempColumnName);
					dynamicColumn.append(",");
				}
			}
			dynamicColumn = dynamicColumn
					.deleteCharAt(dynamicColumn.length() - 1);
			tempConfig.setColumnNames(dynamicColumn.toString());
			tempsource.setAnalyticConfiguration(tempConfig);
			if (firstLoop == true) {
				result = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
						.doAnalysis(tempsource)).getEngineModel().getModel();
				miniS = result.getModelDeviance();
				firstLoop = false;
				miniColumn = tempColumn;
				miniColumns = dynamicColumn.toString();
			} else {
				LogisticRegressionModelDB tempResult = (LogisticRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
						.doAnalysis(tempsource)).getEngineModel().getModel();
				double tempS = tempResult.getModelDeviance();
				if (tempS < miniS) {
					miniS = tempS;
					result = tempResult;
					miniColumn = tempColumn;
					miniColumns = dynamicColumn.toString();
				}
			}
		}
		if (chooseCoeList.contains(miniColumn)) {
			chooseCoeList.remove(miniColumn);
		}

		unChooseCoeList.add(miniColumn);
		changedString = miniColumn;
		tempConfig.setColumnNames(miniColumns);

		return result;
	}

	protected void setNumericalLabelCategory(Column label) {
		if (label.isNumerical()) {
			((NumericColumn) label).setCategory(true);
		}
	}

	protected void setSpecifyColumn(DataSet dataSet,
			AnalyticConfiguration config) {
		String columnNames = config.getColumnNames();
		ArrayList<Column> column_list = new ArrayList<Column>();
		if (!StringUtil.isEmpty(columnNames)) {
			String[] columnNamesArray = columnNames.split(",");
			for (String s : columnNamesArray) {
				Column att = dataSet.getColumns().get(s);
				column_list.add(att);
			}
		}
		if (((LogisticRegressionConfigGeneral) config).getInterActionModel() != null) {
			List<AnalysisInterActionItem> interActionItems = ((LogisticRegressionConfigGeneral) config)
					.getInterActionModel().getInterActionItems();
			if (interActionItems != null) {
				for (int i = 0; i < interActionItems.size(); i++) {
					Column att = dataSet.getColumns().get(
							interActionItems.get(i).getFirstColumn());
					column_list.add(att);
					att = dataSet.getColumns().get(
							interActionItems.get(i).getSecondColumn());
					column_list.add(att);
				}
			}
		}
		Columns atts = dataSet.getColumns();
		Columns atts_clone = (Columns) atts.clone();
		Iterator<Column> i = atts_clone.iterator();
		while (i.hasNext()) {
			Column att = i.next();
			if (!column_list.contains(att)) {
				dataSet.getColumns().remove(att);
			}
		}

	}

	/**
	 * @param para
	 * @param dataSet
	 */
	protected void handColumns(DataBaseAnalyticSource para, DataSet dataSet) {
		List<String> columns = new ArrayList<String>();
		String columnNames = para.getAnalyticConfig().getColumnNames();
		if (!StringUtil.isEmpty(columnNames)) {
			String[] columnNamesArray = columnNames.split(",");
			for (String s : columnNamesArray) {
				columns.add(s);
			}
		}
		if (((LogisticRegressionConfigGeneral) para.getAnalyticConfig())
				.getInterActionModel() != null) {
			List<AnalysisInterActionItem> interActionItems = ((LogisticRegressionConfigGeneral) para
					.getAnalyticConfig()).getInterActionModel()
					.getInterActionItems();
			if (interActionItems != null) {
				for (int i = 0; i < interActionItems.size(); i++) {
					columns.add(interActionItems.get(i).getFirstColumn());
					columns.add(interActionItems.get(i).getSecondColumn());
				}
			}
		}
		filerColumens(dataSet, columns);
	}

	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) throws OperatorException {
		((DatabaseSourceParameter) dataSource.getParameter())
				.setLabel(((LogisticRegressionConfigGeneral) config)
						.getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo
				.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.LOGISTIC_REGRESSION_TRAIN_NAME,locale));
		nodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.LOGISTIC_REGRESSION_TRAIN_DESCRIPTION,locale));

		return nodeMetaInfo;
	}

}
