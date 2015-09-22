/**
 * 

 * ClassName RandomForestTrainer.java
 *
 * Version information: 1.00
 *
 * Date: 2012-10-9
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.trainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.algoconf.RandomForestConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.predictor.CartPredictor;
import com.alpine.datamining.api.impl.db.trainer.randomforest.RandomForestDB2;
import com.alpine.datamining.api.impl.db.trainer.randomforest.RandomForestGreenplum;
import com.alpine.datamining.api.impl.db.trainer.randomforest.RandomForestIMP;
import com.alpine.datamining.api.impl.db.trainer.randomforest.RandomForestNZ;
import com.alpine.datamining.api.impl.db.trainer.randomforest.RandomForestOracle;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NominalColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.randomforest.RandomForestModel;
import com.alpine.datamining.operator.randomforest.RandomForestModelDB2;
import com.alpine.datamining.operator.randomforest.RandomForestModelGreenplum;
import com.alpine.datamining.operator.randomforest.RandomForestModelNZ;
import com.alpine.datamining.operator.randomforest.RandomForestModelOracle;
import com.alpine.datamining.operator.training.SingleModel;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.db.Resources;

/**
 * @author Shawn
 * 
 * 
 */

public class RandomForestTrainer extends AbstractDBModelTrainer {

	private static Logger logger = Logger.getLogger(RandomForestTrainer.class);
	private String dbtype = null;
	private DataSet dataSet = null;
	private String pnewTable = null;
	static String dropIfExists = "yes";
	private String sampleTable = null;
	static String alpine_randomforest_id = "alpine_randomforest_id";

	@Override
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo localAnalyticNodeMetaInfo = new AnalyticNodeMetaInfo();
		localAnalyticNodeMetaInfo
				.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.RANDOME_FOREST_TRAIN_NAME,locale));
		localAnalyticNodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.RANDOME_FOREST_TRAIN_DESCRIPTION,locale));
		return localAnalyticNodeMetaInfo;
	}

	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		AbstractModelTrainerConfig config = (AbstractModelTrainerConfig) source
				.getAnalyticConfig();
		Model model = null;
		if (config.getTrainedModel() == null
				|| config.getForceRetrain().equals("Yes")) {
			try {
				model = train(source);
			} catch (Error e) {
				logger.error(e);
				if (e instanceof OutOfMemoryError) {
					throw new AnalysisException(SDKLanguagePack.getMessage(
							SDKLanguagePack.ADABOOST_TOO_MANY_TRAINER,
							config.getLocale()));
				}
			}

			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(
					model);
			String modelName = getName();
			analyzerOutPutModel.getEngineModel().setName(modelName);
			analyzerOutPutModel
					.setAnalyticNodeMetaInfo(createNodeMetaInfo(config
							.getLocale()));
			// analyzerOutPutModel.setDataAnalyzerMap(dataAnalyzerMap);
			analyzerOutPutModel.setComeFromRetrain(true);

			return analyzerOutPutModel;
		} else {// need not train the model agian, UI have the reused model

			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(
					config.getTrainedModel().getModel());
			analyzerOutPutModel.getEngineModel().setName(getName());
			analyzerOutPutModel
					.setAnalyticNodeMetaInfo(createNodeMetaInfo(config
							.getLocale()));
			// analyzerOutPutModel.setDataAnalyzerMap(dataAnalyzerMap);
			analyzerOutPutModel.setComeFromRetrain(false);

			return analyzerOutPutModel;
		}
	}

	@Override
	protected Model train(AnalyticSource analyticSource)
			throws AnalysisException {
		ResultSet rs = null;
		Statement st = null;
		try {
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory
					.createConnectionInfo(analyticSource.getDataSourceType());
			dbtype = dataSourceInfo.getDBType();
			RandomForestModel lastResult = null;
			RandomForestIMP randomForestImpl = null;
			// if (dbtype.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
			// randomForestTrainer = new AdaboostOracle();
			//
			// } else
			if (dbtype.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)||dbtype.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
				randomForestImpl = new RandomForestGreenplum();

			} else if (dbtype.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
				randomForestImpl = new RandomForestOracle();

			}else if (dbtype.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
				randomForestImpl = new RandomForestDB2();
				 ((RandomForestDB2)
						 randomForestImpl).setConnection(((DataBaseAnalyticSource)
				 analyticSource)
				 .getConnection());
			}
			   else if (dbtype.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				 randomForestImpl = new RandomForestNZ();
			 }
			else {
				throw new AnalysisException("Databse type is not supported for Random Forest:"+dbtype);
//				return null;
			}
			
		

			try {
				dataSet = getDataSet((DataBaseAnalyticSource) analyticSource,
						analyticSource.getAnalyticConfig());
			} catch (OperatorException e1) {
				logger.error(e1);
				throw new OperatorException(e1.getLocalizedMessage());
			}
			setSpecifyColumn(dataSet, analyticSource.getAnalyticConfig());
			dataSet.computeAllColumnStatistics();

			RandomForestConfig rfConfig = (RandomForestConfig) analyticSource
					.getAnalyticConfig();

			String dbSystem = ((DataBaseAnalyticSource) analyticSource)
					.getDataBaseInfo().getSystem();
		
			String url = ((DataBaseAnalyticSource) analyticSource)
					.getDataBaseInfo().getUrl();
			String userName = ((DataBaseAnalyticSource) analyticSource)
					.getDataBaseInfo().getUserName();
			String password = ((DataBaseAnalyticSource) analyticSource)
					.getDataBaseInfo().getPassword();
			String inputSchema = ((DataBaseAnalyticSource) analyticSource)
					.getTableInfo().getSchema();
			String tableName = ((DataBaseAnalyticSource) analyticSource)
					.getTableInfo().getTableName();
			String useSSL = ((DataBaseAnalyticSource) analyticSource)
					.getDataBaseInfo().getUseSSL();
			String sampleWithReplacement= rfConfig.getSampleWithReplacement();
			long timeStamp = System.currentTimeMillis();
			pnewTable = "pnew" + timeStamp;
			sampleTable = "s" + timeStamp;
			String dependentColumn = rfConfig.getDependentColumn();
 			String columnNames = rfConfig.getColumnNames();
			String[] totalColumns = columnNames.split(",");
			int subSize = Integer.parseInt(rfConfig.getNodeColumnNumber());
			int forestSize = Integer.parseInt(rfConfig.getForestSize());

			Connection conncetion = null;
 
			if (dbtype.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)||dbtype.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {

				lastResult = new RandomForestModelGreenplum(dataSet);
			}else if (dbtype.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
				lastResult = new RandomForestModelOracle(dataSet);

			}else if (dbtype.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
				lastResult = new RandomForestModelDB2(dataSet);
			 }
			else if (dbtype.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				lastResult = new RandomForestModelNZ(dataSet);
			 }
			lastResult.setColumnNames(columnNames);
			lastResult.setDependColumn(dependentColumn);
			lastResult.setTableName(tableName);

			conncetion = ((DataBaseAnalyticSource) analyticSource)
					.getConnection();

			Model result = null;

			try {
				st = conncetion.createStatement();
			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}

//			Iterator<String> dependvalueIterator = dataSet.getColumns()
//					.getLabel().getMapping().getValues().iterator();
			if(dataSet.getColumns().getLabel() instanceof NominalColumn){
				if (dataSet.getColumns().getLabel().getMapping().getValues().size() <= 1) {
					String e = SDKLanguagePack.getMessage(
							SDKLanguagePack.ADABOOST_SAMPLE_ERRINFO,
							rfConfig.getLocale());
					logger.error(e);
					throw new AnalysisException(e);
				}
				if (dataSet.getColumns().getLabel().getMapping().getValues().size() > AlpineMinerConfig.ADABOOST_MAX_DEPENDENT_COUNT) {
					String e = SDKLanguagePack.getMessage(
							SDKLanguagePack.ADABOOST_MAX_DEPENDENT_COUNT_ERRINFO,
							rfConfig.getLocale());
					logger.error(e);
					throw new AnalysisException(e);
				}
			}
 
			try {

				randomForestImpl.randomForestTrainInit(inputSchema, tableName,
						timeStamp, dependentColumn, st,  
						dataSet);

			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}

			CartConfig config = new CartConfig();
			config.setDependentColumn(dependentColumn);
			config.setConfidence(rfConfig.getConfidence());
			config.setMaximal_depth(rfConfig.getMaximal_depth());
			config.setMinimal_leaf_size(rfConfig.getMinimal_leaf_size());
			config.setMinimal_size_for_split(rfConfig
					.getMinimal_size_for_split());
			config.setNo_pre_pruning("true");
			config.setNo_pruning("true");
 
			for (int i = 0; i < forestSize; i++) {
				CartTrainer analyzer = new CartTrainer();
				
				
				if(sampleWithReplacement==Resources.TrueOpt)
				{
					randomForestImpl.randomForestSample(inputSchema, timeStamp+""
						+ i, dependentColumn, st, rs, pnewTable, sampleTable
						+ i, rfConfig.getLocale());
				}
				else{
					randomForestImpl.randomForestSampleNoReplace(inputSchema,timeStamp+""+i
							,dependentColumn,st,rs,pnewTable,sampleTable+i,rfConfig.getLocale(),dataSet.size());
				}
				String subColumns = getSubColumns(totalColumns, subSize);
 
				config.setColumnNames(subColumns);
				DataBaseAnalyticSource tempsource = new DataBaseAnalyticSource(
						dbSystem, url, userName, password, inputSchema,
						sampleTable + i, useSSL);
				tempsource.setAnalyticConfiguration(config);
				tempsource.setConenction(conncetion);
				result = ((AnalyzerOutPutTrainModel) analyzer
						.doAnalysis(tempsource)).getEngineModel().getModel();  
				String OOBTable="OOB"+sampleTable+i;
				
				randomForestImpl.generateOOBTable(inputSchema, OOBTable, pnewTable, sampleTable
						+ i, st, rs);
				
				DataBaseAnalyticSource tempPredictSource = new DataBaseAnalyticSource(
						dbSystem, url, userName, password, inputSchema,
						OOBTable,useSSL);
				
				String predictOutTable="OOBPredict"+sampleTable;
				EngineModel em = new EngineModel();
				em.setModel(result);
				PredictorConfig tempconfig = new PredictorConfig(em);
				tempconfig.setDropIfExist(dropIfExists);
				tempconfig.setOutputSchema(inputSchema);
				tempconfig.setOutputTable(predictOutTable);
 				tempPredictSource.setAnalyticConfiguration(tempconfig);
				tempPredictSource.setConenction(conncetion);
				AbstractDBModelPredictor  predictor = new CartPredictor();
				predictor.doAnalysis(tempPredictSource);// use the weak alg , do
				
				double OOBError=0.0;
				if(result instanceof DecisionTreeModel)
				{
					OOBError=randomForestImpl.getOOBError(tempPredictSource,dependentColumn,"P("+dependentColumn+")");
					lastResult.getOobEstimateError().add(OOBError);
				}
				else if (result instanceof RegressionTreeModel)
				{
					OOBError=randomForestImpl.getMSE(tempPredictSource,"P("+dependentColumn+")");
					lastResult.getOobLoss().add(OOBError);
					double OOBMape=randomForestImpl.getMAPE(tempPredictSource, dependentColumn, "P("+dependentColumn+")");
					lastResult.getOobMape().add(OOBMape);
				}else
				{
					OOBError=Double.NaN;
					lastResult.getOobLoss().add(OOBError);
				}
			 

				lastResult.addModel((SingleModel) result);
				randomForestImpl.clearTrainResult(inputSchema, sampleTable+i);
				randomForestImpl.clearTrainResult(inputSchema, predictOutTable);
				randomForestImpl.clearTrainResult(inputSchema, OOBTable);
			}
			return lastResult;

		} catch (Exception e) {
			logger.error(e);
			if (e instanceof WrongUsedException) {
				throw new AnalysisError(this, (WrongUsedException) e);
			} else if (e instanceof AnalysisError) {
				throw (AnalysisError) e;
			} else {
				throw new AnalysisException(e);
			}
		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e.getLocalizedMessage());
			}
		}
	}

	private String getSubColumns(String[] totalColumns, int subSize) {
		ArrayList<String> tempColumns = new ArrayList<String>();
		for (String tempColumn : totalColumns) {
			tempColumns.add(tempColumn);
		}
		StringBuffer subColumns = new StringBuffer();
		for (int i = 0; i < subSize; i++) {
			double temp = Math.random();
			int index = (int) Math.floor(temp * tempColumns.size());
			subColumns.append(tempColumns.get(index)).append(",");
			tempColumns.remove(index);
			// tempColumns.size()
		}

		return subColumns.deleteCharAt(subColumns.length() - 1).toString();
	}

	protected void fillSpecialDataSource(Operator paramOperator,
			AnalyticConfiguration paramAnalyticConfiguration) {
		((DatabaseSourceParameter) paramOperator.getParameter())
				.setLabel(((RandomForestConfig) paramAnalyticConfiguration)
						.getDependentColumn());
	}
}
