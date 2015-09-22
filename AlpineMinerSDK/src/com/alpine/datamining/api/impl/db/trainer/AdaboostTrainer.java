/**
 * ClassName AdaboostTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-20
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.db.trainer;

/**
 * @author Shawn
 *
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.AdaboostConfig;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.DecisionTreeConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NaiveBayesConfig;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.algoconf.SVMClassificationConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.AbstractDBModelTrainer;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayer;
import com.alpine.datamining.api.impl.db.attribute.model.neuralnetwork.AnalysisHiddenLayersModel;
import com.alpine.datamining.api.impl.db.predictor.CartPredictor;
import com.alpine.datamining.api.impl.db.predictor.DecisionTreePredictor;
import com.alpine.datamining.api.impl.db.predictor.LogisticRegressionPredictorGeneral;
import com.alpine.datamining.api.impl.db.predictor.NaiveBayesPredictor;
import com.alpine.datamining.api.impl.db.predictor.NeuralNetworkPredictor;
import com.alpine.datamining.api.impl.db.predictor.SVMPredictor;
import com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostDB2;
import com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostGreenplum;
import com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostIMP;
import com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostNZ;
import com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostOracle;
import com.alpine.datamining.api.impl.db.trainer.adaboost.AdaboostPostgres;
import com.alpine.datamining.api.impl.output.AdaBoostAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.AlpineMinerConfig;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.db.NumericColumn;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.AbstractModel;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.adboost.AdaboostModel;
import com.alpine.datamining.operator.adboost.AdaboostModelDB2;
import com.alpine.datamining.operator.adboost.AdaboostModelGreenplum;
import com.alpine.datamining.operator.adboost.AdaboostModelNZ;
import com.alpine.datamining.operator.adboost.AdaboostModelOracle;
import com.alpine.datamining.operator.adboost.AdaboostModelPostgres;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceItem;
import com.alpine.datamining.operator.adboost.AnalysisAdaboostPersistenceModel;
import com.alpine.datamining.operator.adboost.AdaboostSingleModel;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoFactory;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IDataSourceInfo;
import com.alpine.utility.file.StringUtil;

public class AdaboostTrainer extends AbstractDBModelTrainer {
	private static Logger logger= Logger.getLogger(AdaboostTrainer.class);
	
	private String pnewTable = null;

	private String sampleTable = null;

	private DataSet dataSet = null;
	private AbstractAnalyzer analyzer = null;
	private AbstractDBModelPredictor predictor = null;
	static String dropIfExists = "yes";
	static String alpine_adaboost_id = "alpine_adaboost_id";
	private String dbtype = null;
	private Map<String, DataAnalyzer> dataAnalyzerMap;

	@Override
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
					throw new AnalysisException(
							SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_TOO_MANY_TRAINER,config.getLocale()));
				}
			}

			AdaBoostAnalyzerOutPutTrainModel analyzerOutPutModel = new AdaBoostAnalyzerOutPutTrainModel(
					model);
			String modelName = getName();
			analyzerOutPutModel.getEngineModel().setName(modelName);
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutModel.setDataAnalyzerMap(dataAnalyzerMap);
			analyzerOutPutModel.setComeFromRetrain(true);

			return analyzerOutPutModel;
		} else {// need not train the model agian, UI have the reused model

			AdaBoostAnalyzerOutPutTrainModel analyzerOutPutModel = new AdaBoostAnalyzerOutPutTrainModel(
					config.getTrainedModel().getModel());
			analyzerOutPutModel.getEngineModel().setName(getName());
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
//			analyzerOutPutModel.setDataAnalyzerMap(dataAnalyzerMap);
			analyzerOutPutModel.setComeFromRetrain(false);

			return analyzerOutPutModel;
		}
	}

	protected Model train(AnalyticSource analyticSource)
			throws AnalysisException {
		ResultSet rs = null;
		Statement st = null;
		try {
			IDataSourceInfo dataSourceInfo = DataSourceInfoFactory
					.createConnectionInfo(analyticSource.getDataSourceType());
			dbtype = dataSourceInfo.getDBType();
			AdaboostModel lastResult = null;
			AdaboostIMP adaboostTrainer = null;
			if (dbtype.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {
				adaboostTrainer = new AdaboostOracle();

			} else if (dbtype.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)) {
				adaboostTrainer = new AdaboostGreenplum();

			} else if (dbtype.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {
				adaboostTrainer = new AdaboostPostgres();

			} else if (dbtype.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
				adaboostTrainer = new AdaboostDB2();
				((AdaboostDB2) adaboostTrainer).setConnection(((DataBaseAnalyticSource) analyticSource)
						.getConnection());
			}else if (dbtype.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				adaboostTrainer = new AdaboostNZ();
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

			AdaboostConfig adaConfig = (AdaboostConfig) analyticSource
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
					.getDataBaseInfo().getUseSSL ();
			long timeStamp = System.currentTimeMillis();
			pnewTable = "pnew" + timeStamp;
			sampleTable = "s" + timeStamp;
			String dependentColumn = adaConfig.getDependentColumn();
			String dependentColumnReplaceQ = dependentColumn.replace("\"",
					"\"\"");
			String columnNames = adaConfig.getColumnNames();
			Connection conncetion = null;
			if (dbtype.equalsIgnoreCase(DataSourceInfoOracle.dBType)) {

				lastResult = new AdaboostModelOracle(dataSet);
			} else if (dbtype.equalsIgnoreCase(DataSourceInfoGreenplum.dBType)) {

				lastResult = new AdaboostModelGreenplum(dataSet);
			} else if (dbtype.equalsIgnoreCase(DataSourceInfoPostgres.dBType)) {

				lastResult = new AdaboostModelPostgres(dataSet);
			} else if (dbtype.equalsIgnoreCase(DataSourceInfoDB2.dBType)) {
				lastResult = new AdaboostModelDB2(dataSet);
			}else if (dbtype.equalsIgnoreCase(DataSourceInfoNZ.dBType)){
				lastResult = new AdaboostModelNZ(dataSet);
			}
			lastResult.setColumnNames(columnNames);
			lastResult.setDependentColumn(dependentColumn);
			lastResult.setTableNames(tableName);

			DataBaseAnalyticSource tempPredictsource = new DataBaseAnalyticSource(
					dbSystem, url, userName, password, inputSchema, sampleTable,useSSL);
			conncetion = ((DataBaseAnalyticSource) analyticSource)
					.getConnection();

			AnalyzerOutPutTrainModel result = null;
			
			try {
				st = conncetion.createStatement();
			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}

			

			Iterator<String> dependvalueIterator = dataSet.getColumns()
					.getLabel().getMapping().getValues().iterator();

			if (dataSet.getColumns().getLabel().getMapping().getValues().size() <= 1) {
				String e = SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_SAMPLE_ERRINFO,adaConfig.getLocale());
				logger.error(e);
				throw new AnalysisException(e);
			}
			if (dataSet.getColumns().getLabel().getMapping().getValues().size() > AlpineMinerConfig.ADABOOST_MAX_DEPENDENT_COUNT) {
				String e = SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_MAX_DEPENDENT_COUNT_ERRINFO,adaConfig.getLocale());
				logger.error(e);
				throw new AnalysisException(e);
			}

			try {

				adaboostTrainer.adaboostTrainInit(inputSchema, tableName,
						timeStamp, dependentColumn, st, dependvalueIterator,
						dataSet);

			} catch (SQLException e) {
				logger.error(e);
				throw new AnalysisException(e);
			}

			dataAnalyzerMap = new HashMap<String, DataAnalyzer>();

			AnalysisAdaboostPersistenceModel adaboostPersistenceModel = adaConfig
					.getAdaboostUIModel();

			List<AnalysisAdaboostPersistenceItem> items = adaboostPersistenceModel
					.getAdaboostUIItems();
			boolean firstTime = true;

			for (AnalysisAdaboostPersistenceItem item : items) {
				AbstractModelTrainerConfig config = adaConfig
						.getNameConfigMap().get(item.getAdaName());

				if (config instanceof NeuralNetworkConfig) {
					AnalysisHiddenLayersModel hiddenLayersModel = new AnalysisHiddenLayersModel();
					List<AnalysisHiddenLayer> hiddenLayerList = new ArrayList<AnalysisHiddenLayer>();
					String hiddenLayers = item.getParameterMap().get(
							NeuralNetworkConfig.ConstHidden_layers);
					if (!StringUtil.isEmpty(hiddenLayers)) {
						String[] temp = hiddenLayers.split(";");
						for (String s : temp) {
							String[] hiddenLayer = s.split(",");
							AnalysisHiddenLayer analysisHiddenLayer = new AnalysisHiddenLayer(
									hiddenLayer[0], Integer
											.parseInt(hiddenLayer[1]));
							hiddenLayerList.add(analysisHiddenLayer);
						}
					}
					hiddenLayersModel.setHiddenLayers(hiddenLayerList);
					((NeuralNetworkConfig) config)
							.setHiddenLayersModel(hiddenLayersModel);
				}
				if (firstTime == false) {

					adaboostTrainer.adaboostTrainSample(inputSchema, timeStamp,
							dependentColumn, st, rs, pnewTable,adaConfig.getLocale());

				} else {
					firstTime = false;
				}
				config.setDependentColumn(dependentColumn);
				config.setColumnNames(columnNames);
				tempPredictsource.setAnalyticConfiguration(config);
				generateAnalyzer(config);
				tempPredictsource.setConenction(conncetion);

				result = (AnalyzerOutPutTrainModel) analyzer
						.doAnalysis(tempPredictsource);// use the weak alg ,
				// train the sample
				// dataset

				AdaboostSingleModel adaresult = new AdaboostSingleModel();
				adaresult.setModel(result.getEngineModel().getModel());
				String paraName = item.getAdaName();
				adaresult.setName(paraName);
				adaresult.setType(analyzer.getClass().getCanonicalName());
				dataAnalyzerMap.put(paraName, analyzer);
				DataBaseAnalyticSource tempsource = new DataBaseAnalyticSource(
						dbSystem, url, userName, password, inputSchema,
						pnewTable,useSSL);
				EngineModel em = new EngineModel();
				em.setModel(result.getEngineModel().getModel());
				PredictorConfig tempconfig = new PredictorConfig(em);
				tempconfig.setDropIfExist(dropIfExists);
				tempconfig.setOutputSchema(inputSchema);

				adaboostTrainer.setOutputTable(tempconfig);// according to the
															// dbtype ,set
				// different out table

				tempsource.setAnalyticConfiguration(tempconfig);
				tempsource.setConenction(conncetion);
				predictor.doAnalysis(tempsource);// use the weak alg , do
				// predict
				Iterator<String> sampleDvalueIterator = ((AbstractModel) result
						.getEngineModel().getModel()).getTrainingHeader()
						.getColumns().getLabel().getMapping().getValues()
						.iterator();
				double changedPeosoC=0;
				changedPeosoC = adaboostTrainer.adaboostChangePeoso(inputSchema,
						tableName, timeStamp, dependentColumn,
						dependentColumnReplaceQ, st, rs, sampleDvalueIterator);

						adaresult.setPeoso(changedPeosoC);
		
				lastResult.addModel(adaresult);
			}

			adaboostTrainer.clearTrainResult(inputSchema, timeStamp, st,
					pnewTable);
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
				logger.error(e);
			throw new AnalysisException(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * @param inputSchema
	 * @param timeStamp
	 * @param st
	 * @param rs
	 * @return
	 * @throws SQLException
	 */

	protected void generateAnalyzer(AbstractModelTrainerConfig config) {
		if (config instanceof CartConfig) {
			analyzer = new CartTrainer();
			predictor = new CartPredictor();

		} else if (config instanceof SVMClassificationConfig) {
			analyzer = new SVMClassificationTrainer();
			predictor = new SVMPredictor();

		} else if (config instanceof NeuralNetworkConfig) {
			analyzer = new NeuralNetworkTrainer();
			predictor = new NeuralNetworkPredictor();

		} else if (config instanceof NaiveBayesConfig) {
			analyzer = new NaiveBayesTrainer();
			predictor = new NaiveBayesPredictor();

		} else if (config instanceof LogisticRegressionConfigGeneral) {
			analyzer = new LogisticRegressionTrainerGeneral();
			predictor = new LogisticRegressionPredictorGeneral();

		} else if (config instanceof DecisionTreeConfig) {
			analyzer = new DecisionTreeTrainer();
			predictor = new DecisionTreePredictor();

		}
		analyzer.setListeners(getListeners());
		predictor.setListeners(getListeners());

	}

	protected void setNumericalLabelCategory(Column paramColumn) {
		if (!(paramColumn.isNumerical()))
			return;
		((NumericColumn) paramColumn).setCategory(true);
	}

	protected void fillSpecialDataSource(Operator paramOperator,
			AnalyticConfiguration paramAnalyticConfiguration) {
		((DatabaseSourceParameter) paramOperator.getParameter())
				.setLabel(((AdaboostConfig) paramAnalyticConfiguration)
						.getDependentColumn());
	}

	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo localAnalyticNodeMetaInfo = new AnalyticNodeMetaInfo();
		localAnalyticNodeMetaInfo
				.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_TRAIN_NAME,locale));
		localAnalyticNodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_TRAIN_DESCRIPTION,locale));
		return localAnalyticNodeMetaInfo;
	}


}
