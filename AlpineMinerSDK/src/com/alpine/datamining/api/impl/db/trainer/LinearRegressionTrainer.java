/**
 * ClassName LinearRegressionTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-7
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.LinearRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.predictor.LinearRegressionPredictor;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionAIC;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionAICLinear;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionIMP;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionSBC;
import com.alpine.datamining.api.impl.trainer.stepwise.CriterionSBCLinear;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.Columns;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.regressions.AnalysisInterActionItem;
import com.alpine.datamining.operator.regressions.LinearRegressionDB;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.operator.regressions.LinearRegressionParameter;
import com.alpine.datamining.operator.regressions.ResidualDataGenarator;
import com.alpine.datamining.operator.regressions.ResidualDataGenaratorDB2;
import com.alpine.datamining.operator.regressions.ResidualDataGenaratorNZ;
import com.alpine.datamining.operator.regressions.ResidualDataGenaratorOracle;
import com.alpine.datamining.operator.training.Training;
import com.alpine.datamining.utility.OperatorUtil;
import com.alpine.datamining.utility.StatisticsFTest;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.Resources;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Eason
 */

public class LinearRegressionTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(LinearRegressionTrainer.class);
	
	private double MAXDOUBLE = Double.MAX_VALUE;
	private double checkValue = 0.95;
	private String trueString = Resources.TrueOpt;

	private String no = Resources.NoOpt;
	private String forward = "FORWARD";
	private String backward = "BACKWARD";
	private String stepwise = "STEPWISE";

	private String changedString = null;
	boolean noCriterion = false;
	protected LinearRegressionTrainer stepwiseAnalyzer = null;
	private boolean inStepWise=false;
	
	
	protected Model train(AnalyticSource source) throws AnalysisException {

		try {
			LinearRegressionConfig config = (LinearRegressionConfig) source
					.getAnalyticConfig();
			DataSet dataSet = getDataSet(((DataBaseAnalyticSource) source),
					config);
			setSpecifyColumn(dataSet, config);
			dataSet.computeAllColumnStatistics();
			Operator learner = OperatorUtil
					.createOperator(LinearRegressionDB.class);

			LinearRegressionParameter parameter = new LinearRegressionParameter();
			if (!StringUtil.isEmpty(config.getColumnNames())) {
				parameter.setColumnNames(config.getColumnNames());
			}
			if (config.getInterActionModel() != null) {
				parameter.setAnalysisInterActionModel(config
						.getInterActionModel());
			}
			
			if (!StringUtil.isEmpty(config.getSplitModelGroupByColumn())
					&&config.getIsStepWise().equals(Resources.FalseOpt))
			{
				parameter.setGroupBy(true);
				parameter.setGroupByColumn(config.getSplitModelGroupByColumn());
				config.setVisualizationTypeClass(LinearRegressionConfig.SPLITMODEL_VISUALIZATION_TYPE);
			}
			
			
			warnTooManyValue(dataSet, Integer
					.parseInt(AlpineMinerConfig.C2N_WARNING), config
					.getLocale());
			learner.setParameter(parameter);
			Model model = null;
			if (!StringUtil.isEmpty(config.getIsStepWise())
						&& config.getIsStepWise().equalsIgnoreCase(trueString)) {
				model = stepWise(source);
				if(inStepWise==false&&(StringUtil.isEmpty(config.getAddResidualPlot())
						||config.getAddResidualPlot().equals(Resources.TrueOpt))){
					calculateResidual(source, config, model);
				}
			} else {
				model = ((Training) learner).train(dataSet);
				if(config.getSplitModelGroupByColumn()!=null&&inStepWise==false
						&& (StringUtil.isEmpty(config.getAddResidualPlot())
								||config.getAddResidualPlot().equals(Resources.TrueOpt)))
					{
						calculateResidualGroup(source, config, model);
					}else if(inStepWise==false&&(StringUtil.isEmpty(config.getAddResidualPlot())
							||config.getAddResidualPlot().equals(Resources.TrueOpt))){
						calculateResidual(source, config, model);
					}
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

	private void calculateResidual(AnalyticSource source,
			LinearRegressionConfig config, Model model) throws Exception {
	
		config.setVisualizationTypeClass(LinearRegressionConfig.VISUALIZATION_TYPE+","+
				LinearRegressionConfig.ResidualPlot_VISUALIZATION_TYPE);
		ResultSet rs = null;
		PreparedStatement ps = null;
		Statement st=null;
		String outputSchema = null;
		String outputTable = null;
		try {
			LinearRegressionPredictor predictor = new LinearRegressionPredictor();

			outputSchema = ((DataBaseAnalyticSource) source).getTableInfo()
					.getSchema();
			outputTable = "alpine" + System.currentTimeMillis();
			String oldTableName = ((DataBaseAnalyticSource) source)
					.getTableInfo().getTableName();

			EngineModel em = new EngineModel();
			em.setModel(model);

			PredictorConfig predictorConfig = new PredictorConfig(em);

			predictorConfig.setOutputSchema(outputSchema);
			predictorConfig.setOutputTable(outputTable);
			predictorConfig.setDropIfExist(Resources.YesOpt);
			source.setAnalyticConfiguration(predictorConfig);

			AnalyzerOutPutDataBaseUpdate output = (AnalyzerOutPutDataBaseUpdate) predictor
					.doAnalysis(source);

			((DataBaseAnalyticSource) source).getTableInfo().setTableName(
					oldTableName);
			source.setAnalyticConfiguration(config);

			String fitColumn = output.getUpdatedColumns().get(0);

			String schemaName = StringHandler.doubleQ(outputSchema);
			String tableName = StringHandler.doubleQ(outputTable);

			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append(" WHERE ");
			sbWhere.append(StringHandler.doubleQ(fitColumn));
			sbWhere.append(" is not null");
			sbWhere.append(" and ").append(
					StringHandler.doubleQ(config.getDependentColumn()));
			sbWhere.append(" is not null ");

			// construct the SQL to make sure you get the right set of data
			// if the
			// dataset is huge
			
			long count = 0;
			String countSql = " select count(*) from " + schemaName + "."
					+ tableName + " " + sbWhere.toString();
			Connection connection = ((DataBaseAnalyticSource) source)
					.getConnection();
			ps = connection.prepareStatement(countSql);
			rs = ps.executeQuery();
			rs.next();
			count = rs.getLong(1);

			String maxRows = ProfileReader.getInstance().getParameter(
					ProfileUtility.UI_TABLE_LIMIT);
			ResidualDataGenarator residualGenarator=residualDataFactory(source);
			StringBuffer sover = residualGenarator.getResidualString(config.getDependentColumn(), fitColumn,
					schemaName, tableName, sbWhere, count, maxRows);

			String sql = sover.toString();//TODO
			logger.debug("LinearRegressionTrainer.calculateResidual(): sql = "
									+ sql);
 
			st=connection.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				double[] items = new double[2];
				for (int n = 0; n < items.length; n++) {
					items[n] = rs.getDouble(n + 1);
				}
				((LinearRegressionModelDB) model).addResidual(items);
			}
		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			dropTempTable(source, outputSchema, outputTable);
		}

	}

 	private void calculateResidualGroup(AnalyticSource source,
			LinearRegressionConfig config, Model model) throws Exception {
		config.setVisualizationTypeClass(LinearRegressionConfig.SPLITMODEL_VISUALIZATION_TYPE+","+
				LinearRegressionConfig.ResidualPlot_VISUALIZATION_TYPE);
		ResultSet rs = null;
		PreparedStatement ps = null;
		String outputSchema = null;
		String outputTable = null;
		try {
			LinearRegressionPredictor predictor = new LinearRegressionPredictor();

			outputSchema = ((DataBaseAnalyticSource) source).getTableInfo()
					.getSchema();
			outputTable = "alpine" + System.currentTimeMillis();
			String oldTableName = ((DataBaseAnalyticSource) source)
					.getTableInfo().getTableName();

			EngineModel em = new EngineModel();
			em.setModel(model);

			PredictorConfig predictorConfig = new PredictorConfig(em);

			predictorConfig.setOutputSchema(outputSchema);
			predictorConfig.setOutputTable(outputTable);
			predictorConfig.setDropIfExist(Resources.YesOpt);
			source.setAnalyticConfiguration(predictorConfig);

			AnalyzerOutPutDataBaseUpdate output = (AnalyzerOutPutDataBaseUpdate) predictor
					.doAnalysis(source);

			((DataBaseAnalyticSource) source).getTableInfo().setTableName(
					oldTableName);
			source.setAnalyticConfiguration(config);

			String fitColumn = output.getUpdatedColumns().get(0);

			String schemaName = StringHandler.doubleQ(outputSchema);
			String tableName = StringHandler.doubleQ(outputTable);

			StringBuffer sbWhere = new StringBuffer();
			sbWhere.append(" WHERE ");
			sbWhere.append(StringHandler.doubleQ(fitColumn));
			sbWhere.append(" is not null");
			sbWhere.append(" and ").append(
					StringHandler.doubleQ(config.getDependentColumn()));
			sbWhere.append(" is not null ");

			// construct the SQL to make sure you get the right set of data
			// if the
			// dataset is huge
			HashMap<String,Integer> groupCount =new HashMap<String,Integer>();

			String countSql = " select count(*),"+StringHandler.doubleQ(((LinearRegressionGroupGPModel) model ).getGroupByColumn())+" from " + schemaName + "."
					+ tableName + " " + sbWhere.toString()+" group by "+StringHandler.doubleQ(((LinearRegressionGroupGPModel) model ).getGroupByColumn());
			Connection connection = ((DataBaseAnalyticSource) source)
					.getConnection();
			ps = connection.prepareStatement(countSql);
			rs = ps.executeQuery();
			while(rs.next())
			{
				String groupValue="";
				groupValue=rs.getString(2);
				if(groupValue== null)
				{
					continue;
				}
				groupCount.put(groupValue, rs.getInt(1));
			}
			
			for(String groupValue:((LinearRegressionGroupGPModel) model ).getModelList().keySet())
			{
				StringBuffer sover = new StringBuffer();
				long count = groupCount.get(groupValue);
				String maxRows = ProfileReader.getInstance().getParameter(
					ProfileUtility.UI_TABLE_LIMIT);

				sover.append("select ").append(StringHandler.doubleQ(fitColumn))
					.append(", alpine_residual ");
				sover.append(" from (select ").append(
					StringHandler.doubleQ(fitColumn)).append(",");
				sover.append(StringHandler.doubleQ(config.getDependentColumn())
					+ "-" + StringHandler.doubleQ(fitColumn));
				sover.append(" as alpine_residual,row_number() over (order by ");
				sover.append(StringHandler.doubleQ(fitColumn)
					+ ") as myrow_number from " + schemaName + "." + tableName
					+ " " + sbWhere + " and "+StringHandler.doubleQ(((LinearRegressionGroupGPModel) model ).getGroupByColumn())+"="+StringHandler.singleQ(groupValue)+" ) foo where").append("  ");
				sover.append("   mod(( myrow_number-1)*" + maxRows + "," + count
					+ ")<" + maxRows);

				String sql = sover.toString();
				logger.debug(
							"LinearRegressionTrainer.calculateResidual(): sql = "
									+ sql);
				ps = connection.prepareStatement(sql);
				rs = ps.executeQuery();

				while (rs.next()) {
					double[] items = new double[2];
					for (int n = 0; n < items.length; n++) {
						items[n] = rs.getDouble(n + 1);
					}
					((LinearRegressionGroupGPModel) model).getOneModel(groupValue).addResidual(items);
				}
			}
		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			dropTempTable(source, outputSchema, outputTable);
		}

	}

	
	
	
	
	private void dropTempTable(AnalyticSource source, String outputSchema,
			String outputTable) throws Exception {
		Connection connection = ((DataBaseAnalyticSource) source)
				.getConnection();
		ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory
				.createConnectionInfo(((DataBaseAnalyticSource) source)
						.getDataSourceType());
		StringBuilder sql = new StringBuilder();
		sql.append("drop table ");
		sql.append(getQuotaedTableName(outputSchema, outputTable)).append(" ")
				.append(sqlGenerator.cascade());
		Statement st = null;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			logger.debug(
					"LinearRegressionTrainer.dropTempTable():sql=" + sql);
			st.execute(sql.toString());
		} catch (SQLException e) {
			logger.error(e);
		} finally {
			if (st != null) {
				st.close();
			}
		}
	}

	protected Model stepWise(AnalyticSource source) throws AnalysisException,
			OperatorException {
		Statement st = null;
		ResultSet rs = null;
		try {

			LinearRegressionConfig config = (LinearRegressionConfig) source
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

			logger.debug(sql);
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

				criterion = new CriterionAICLinear();
				noCriterion = true;

			} else if (criterionType
					.equalsIgnoreCase(CriterionAIC.criterionType)) {
				criterion = new CriterionAICLinear();

			} else if (criterionType
					.equalsIgnoreCase(CriterionSBC.criterionType)) {
				criterion = new CriterionSBCLinear();
			}
			String columnNames = config.getColumnNames();
			LinearRegressionConfig tempConfig = new LinearRegressionConfig();
			tempConfig.setDependentColumn(config.getDependentColumn());
			tempConfig.setForceRetrain(config.getForceRetrain());
			tempConfig.setIsStepWise(no);
			tempConfig.setLocale(config.getLocale());

			tempsource.setAnalyticConfiguration(tempConfig);
			tempsource.setConenction(conncetion);
			String[] columnNamesArray = columnNames.split(",");
			List<String> chooseCoeList = new ArrayList<String>();
			List<String> unChooseCoeList = new ArrayList<String>();
			boolean firstTime = true;
			LinearRegressionModelDB bestModel = null;
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
		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (rs != null) {
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
	private LinearRegressionModelDB stepwise(long rowNumber,
			DataBaseAnalyticSource tempsource, CriterionIMP criterion,
			LinearRegressionConfig tempConfig, String[] columnNamesArray,
			List<String> chooseCoeList, List<String> unChooseCoeList,
			boolean firstTime) throws AnalysisException {
		LinearRegressionModelDB bestModel;
		LinearRegressionModelDB preModel = null;

		double bestCreiterion;

		for (String tempColumnName : columnNamesArray) {
			unChooseCoeList.add(tempColumnName);
		}

		preModel = (LinearRegressionModelDB) addOneColumn(tempConfig,
				chooseCoeList, unChooseCoeList, firstTime, tempsource,
				rowNumber);
		bestModel = preModel;
		bestCreiterion = criterion.getCriterion(preModel, rowNumber);
		firstTime = false;
		while (!unChooseCoeList.isEmpty()) {

			LinearRegressionModelDB afterModel = (LinearRegressionModelDB) addOneColumn(
					tempConfig, chooseCoeList, unChooseCoeList, firstTime,
					tempsource, rowNumber);
			if (afterModel.getS() == Double.NaN
					|| preModel.getS() == Double.NaN) {
				String e = SDKLanguagePack.getMessage(
						SDKLanguagePack.STEPWISE_LINEAR_SQUARE, tempConfig
								.getLocale());
				logger.error(e);
				throw new AnalysisException(e);
			}
			double afterQ = Math.pow(afterModel.getS(), 2)
					* (rowNumber - afterModel.getColumnNames().length - 1);
			double preQ = Math.pow(preModel.getS(), 2)
					* (rowNumber - preModel.getColumnNames().length - 1);
			double p = StatisticsFTest.fTest((rowNumber
					- preModel.getColumnNames().length - 2)
					* (preQ - afterQ) / afterQ, 1, rowNumber
					- preModel.getColumnNames().length - 2);
			if (p > 1 - checkValue) {
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
				LinearRegressionModelDB dropPreModel = afterModel;
				LinearRegressionModelDB dropBestModel = afterModel;
				double dropBestCreiterion = criterion.getCriterion(afterModel,
						rowNumber);
				while (chooseCoeList.size() > 1) {
					LinearRegressionModelDB dropAfterModel = (LinearRegressionModelDB) dropOneColumn(
							tempConfig, chooseCoeList, unChooseCoeList,
							tempsource, rowNumber);
					if (dropAfterModel.getS() == Double.NaN
							|| dropPreModel.getS() == Double.NaN) {
						String e = SDKLanguagePack.getMessage(
								SDKLanguagePack.STEPWISE_LINEAR_SQUARE,
								tempConfig.getLocale());
						logger.error(e);
						throw new AnalysisException(e);
					}

					double dropAfterQ = Math.pow(dropAfterModel.getS(), 2)
							* (rowNumber
									- dropAfterModel.getColumnNames().length - 1);
					double dropPreQ = Math.pow(dropPreModel.getS(), 2)
							* (rowNumber - dropPreModel.getColumnNames().length - 1);
					double dropP = StatisticsFTest.fTest((rowNumber
							- dropPreModel.getColumnNames().length - 1)
							* (dropAfterQ - dropPreQ) / dropPreQ, 1, rowNumber
							- dropPreModel.getColumnNames().length - 1);
					if (dropP < 1 - checkValue) {
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
				preModel = dropPreModel;
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
	private LinearRegressionModelDB backward(long rowNumber,
			DataBaseAnalyticSource tempsource, CriterionIMP criterion,
			String columnNames, LinearRegressionConfig tempConfig,
			String[] columnNamesArray, List<String> chooseCoeList,
			List<String> unChooseCoeList) throws AnalysisException {
		LinearRegressionModelDB bestModel;
		LinearRegressionModelDB preModel = null;

		double bestCreiterion;

		for (String tempColumnName : columnNamesArray) {
			chooseCoeList.add(tempColumnName);
		}
		tempConfig.setColumnNames(columnNames);
		tempsource.setAnalyticConfiguration(tempConfig);
		stepwiseAnalyzer = new LinearRegressionTrainer();
		stepwiseAnalyzer.setInStepWise(true);
		stepwiseAnalyzer.setListeners(getListeners());
		preModel = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
				.doAnalysis(tempsource)).getEngineModel().getModel();
		bestModel = preModel;
		bestCreiterion = criterion.getCriterion(preModel, rowNumber);
		while (chooseCoeList.size() > 1) {
			LinearRegressionModelDB afterModel = (LinearRegressionModelDB) dropOneColumn(
					tempConfig, chooseCoeList, unChooseCoeList, tempsource,
					rowNumber);
			if (afterModel.getS() == Double.NaN
					|| preModel.getS() == Double.NaN) {
				String e = SDKLanguagePack.getMessage(
						SDKLanguagePack.STEPWISE_LINEAR_SQUARE, tempConfig
								.getLocale());
				logger.error(e);
				throw new AnalysisException(e);
			}

			double afterQ = Math.pow(afterModel.getS(), 2)
					* (rowNumber - afterModel.getColumnNames().length - 1);
			double preQ = Math.pow(preModel.getS(), 2)
					* (rowNumber - preModel.getColumnNames().length - 1);
			double p = StatisticsFTest.fTest((rowNumber
					- preModel.getColumnNames().length - 1)
					* (afterQ - preQ) / preQ, 1, rowNumber
					- preModel.getColumnNames().length - 1);
			if (p < 1 - checkValue) {
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
	private LinearRegressionModelDB forward(long rowNumber,
			DataBaseAnalyticSource tempsource, CriterionIMP criterion,
			LinearRegressionConfig tempConfig, String[] columnNamesArray,
			List<String> chooseCoeList, List<String> unChooseCoeList,
			boolean firstTime) throws AnalysisException {
		LinearRegressionModelDB bestModel;
		LinearRegressionModelDB preModel = null;
		tempConfig.setColumnNames("");
		double bestCreiterion;

		for (String tempColumnName : columnNamesArray) {
			unChooseCoeList.add(tempColumnName);
		}

		preModel = (LinearRegressionModelDB) addOneColumn(tempConfig,
				chooseCoeList, unChooseCoeList, firstTime, tempsource,
				rowNumber);
		bestModel = preModel;
		bestCreiterion = criterion.getCriterion(preModel, rowNumber);
		firstTime = false;
		while (!unChooseCoeList.isEmpty()) {
			LinearRegressionModelDB afterModel = (LinearRegressionModelDB) addOneColumn(
					tempConfig, chooseCoeList, unChooseCoeList, firstTime,
					tempsource, rowNumber);
			if (afterModel.getS() == Double.NaN
					|| preModel.getS() == Double.NaN) {
				String e = SDKLanguagePack.getMessage(
						SDKLanguagePack.STEPWISE_LINEAR_SQUARE, tempConfig
								.getLocale());
				logger.error(e);
				throw new AnalysisException(e);
			}

			double afterQ = Math.pow(afterModel.getS(), 2)
					* (rowNumber - afterModel.getColumnNames().length - 1);
			double preQ = Math.pow(preModel.getS(), 2)
					* (rowNumber - preModel.getColumnNames().length - 1);

			double p = StatisticsFTest.fTest((rowNumber
					- preModel.getColumnNames().length - 2)
					* (preQ - afterQ) / afterQ, 1, rowNumber
					- preModel.getColumnNames().length - 2);
			if (p > 1 - checkValue) {
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

		double miniS = MAXDOUBLE;
		LinearRegressionModelDB result = null;
		boolean firstLoop = true;
		String miniColumn = null;

		if (firstTime == true) {
			for (String tempColumnName : unChooseCoeList) {

				stepwiseAnalyzer = new LinearRegressionTrainer();
				stepwiseAnalyzer.setListeners(getListeners());
				stepwiseAnalyzer.setInStepWise(true);

				dynamicColumnName = tempColumnName;

				tempConfig.setColumnNames(dynamicColumnName);
				tempsource.setAnalyticConfiguration(tempConfig);
				if (firstLoop == true) {
					result = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					if (result.getS() == Double.NaN) {
						String e = SDKLanguagePack.getMessage(
								SDKLanguagePack.STEPWISE_LINEAR_SQUARE,
								tempConfig.getLocale());
						logger.error(e);
						throw new AnalysisException(e);
					}
					miniS = result.getS();
					firstLoop = false;
					miniColumn = tempColumnName;
				} else {
					LinearRegressionModelDB tempResult = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					if (tempResult.getS() == Double.NaN) {
						String e = SDKLanguagePack.getMessage(
								SDKLanguagePack.STEPWISE_LINEAR_SQUARE,
								tempConfig.getLocale());
						logger.error(e);
						throw new AnalysisException(e);
					}
					double tempS = tempResult.getS();

					if (tempS < miniS) {
						miniS = tempS;
						result = tempResult;
						miniColumn = tempColumnName;
					}
				}
			}
			if (unChooseCoeList.contains(miniColumn)) {
				unChooseCoeList.remove(miniColumn);
			}
			chooseCoeList.add(miniColumn);
			changedString = miniColumn;
			tempConfig.setColumnNames(miniColumn);
		} else {
			StringBuffer dynamicColumnNames = new StringBuffer();

			for (String tempColumnName : chooseCoeList) {

				dynamicColumnNames.append(tempColumnName);
				dynamicColumnNames.append(",");

			}
			String choosedColumn = dynamicColumnNames.deleteCharAt(
					dynamicColumnNames.length() - 1).toString();

			for (String tempColumnName : unChooseCoeList) {
				stepwiseAnalyzer = new LinearRegressionTrainer();
				stepwiseAnalyzer.setInStepWise(true);
				stepwiseAnalyzer.setListeners(getListeners());
				dynamicColumnName = choosedColumn + "," + tempColumnName;

				tempConfig.setColumnNames(dynamicColumnName);
				tempsource.setAnalyticConfiguration(tempConfig);
				if (firstLoop == true) {
					result = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					if (result.getS() == Double.NaN) {
						String e = SDKLanguagePack.getMessage(
								SDKLanguagePack.STEPWISE_LINEAR_SQUARE,
								tempConfig.getLocale());
						logger.error(e);
						throw new AnalysisException(e);
					}
					miniS = result.getS();

					firstLoop = false;
					miniColumn = tempColumnName;
				} else {
					LinearRegressionModelDB tempResult = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
							.doAnalysis(tempsource)).getEngineModel()
							.getModel();
					if (tempResult.getS() == Double.NaN) {
						String e = SDKLanguagePack.getMessage(
								SDKLanguagePack.STEPWISE_LINEAR_SQUARE,
								tempConfig.getLocale());
						logger.error(e);
						throw new AnalysisException(e);
					}
					double tempS = tempResult.getS();
					if (tempS < miniS) {
						miniS = tempS;
						result = tempResult;
						miniColumn = tempColumnName;
					}
				}
			}
			if (unChooseCoeList.contains(miniColumn)) {
				unChooseCoeList.remove(miniColumn);
			}
			chooseCoeList.add(miniColumn);
			changedString = miniColumn;
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
		LinearRegressionModelDB result = null;
		boolean firstLoop = true;
		String miniColumn = null;
		String miniColumns = null;

		for (String tempColumnName : chooseCoeList) {
			stepwiseAnalyzer = new LinearRegressionTrainer();
			stepwiseAnalyzer.setInStepWise(true);
			stepwiseAnalyzer.setListeners(getListeners());

			StringBuffer dynamicColumnNames = new StringBuffer();
			for (String oneColumn : chooseCoeList) {
				if (!oneColumn.equalsIgnoreCase(tempColumnName)) {
					dynamicColumnNames.append(oneColumn);
					dynamicColumnNames.append(",");
				}
			}
			dynamicColumnNames = dynamicColumnNames
					.deleteCharAt(dynamicColumnNames.length() - 1);
			tempConfig.setColumnNames(dynamicColumnNames.toString());
			tempsource.setAnalyticConfiguration(tempConfig);
			if (firstLoop == true) {
				result = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
						.doAnalysis(tempsource)).getEngineModel().getModel();
				if (result.getS() == Double.NaN) {
					String e = SDKLanguagePack.getMessage(
							SDKLanguagePack.STEPWISE_LINEAR_SQUARE, tempConfig
									.getLocale());
					logger.error(e);
					throw new AnalysisException(e);
				}
				miniS = result.getS();
				firstLoop = false;
				miniColumn = tempColumnName;
				miniColumns = dynamicColumnNames.toString();
			} else {
				LinearRegressionModelDB tempResult = (LinearRegressionModelDB) ((AnalyzerOutPutTrainModel) stepwiseAnalyzer
						.doAnalysis(tempsource)).getEngineModel().getModel();
				if (tempResult.getS() == Double.NaN) {
					String e = SDKLanguagePack.getMessage(
							SDKLanguagePack.STEPWISE_LINEAR_SQUARE, tempConfig
									.getLocale());
					logger.error(e);
					throw new AnalysisException(e);
				}
				double tempS = tempResult.getS();
				if (tempS < miniS) {
					miniS = tempS;
					result = tempResult;
					miniColumn = tempColumnName;
					miniColumns = dynamicColumnNames.toString();
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
		if (((LinearRegressionConfig) config).getInterActionModel() != null) {
			List<AnalysisInterActionItem> interActionItems = ((LinearRegressionConfig) config)
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
		if (((LinearRegressionConfig) para.getAnalyticConfig())
				.getInterActionModel() != null) {
			List<AnalysisInterActionItem> interActionItems = ((LinearRegressionConfig) para
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
			AnalyticConfiguration config) {

		((DatabaseSourceParameter) dataSource.getParameter())
				.setLabel(((LinearRegressionConfig) config)
						.getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
				SDKLanguagePack.LINEAR_REGRESSION_TRAIN_NAME, locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
				SDKLanguagePack.LINEAR_REGRESSION_TRAIN_DESCRIPTION, locale));

		return nodeMetaInfo;
	}

	public boolean isInStepWise() {
		return inStepWise;
	}

	public void setInStepWise(boolean inStepWise) {
		this.inStepWise = inStepWise;
	}

	private ResidualDataGenarator residualDataFactory(AnalyticSource source)
	{
		ResidualDataGenarator residualGenarator=null;
		if (((DataBaseAnalyticSource) source).getDataSourceType()
				.equalsIgnoreCase(DataSourceInfoOracle.dBType))
    	{
			residualGenarator = new ResidualDataGenaratorOracle();
    	}else if (((DataBaseAnalyticSource) source).getDataSourceType()
				.equalsIgnoreCase(DataSourceInfoNZ.dBType))
    	{
    		residualGenarator = new ResidualDataGenaratorNZ();
    	}else if (((DataBaseAnalyticSource) source).getDataSourceType()
				.equalsIgnoreCase(DataSourceInfoDB2.dBType))
    	{
    		residualGenarator = new ResidualDataGenaratorDB2();
    	}else if (((DataBaseAnalyticSource) source).getDataSourceType()
				.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)||
				((DataBaseAnalyticSource) source).getDataSourceType()
				.equalsIgnoreCase(DataSourceInfoPostgres.dBType))
    	{
    		residualGenarator = new ResidualDataGenarator();
    	}else{
    		residualGenarator = new ResidualDataGenarator();
    	}
		return residualGenarator;
	}
}
