/**
 * 

* ClassName EMClusterPredictor.java
*
* Version information: 1.00
*
* Data: May 11, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.predictor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.EMCluster.EMModel;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.resources.AlpineDataAnalysisConfig;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Shawn
 *
 */
public class EMClusterPredictor extends AbstractDBModelPredictor{
	private static Logger logger= Logger.getLogger(EMClusterPredictor.class);
	
	private DataSet dataSet;
	public AnalyticOutPut doAnalysis(AnalyticSource source)
	throws AnalysisException {

		PredictorConfig config = (PredictorConfig) source.getAnalyticConfig();

		AnalyticOutPut result = doPredict((DataBaseAnalyticSource) source,
				config);

		return result;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.AbstractDBModelPredictor#doPredict(com.alpine.datamining.api.impl.db.DataBaseAnalyticSource, com.alpine.datamining.api.impl.algoconf.PredictorConfig)
	 */
	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(
			DataBaseAnalyticSource source, PredictorConfig config)
			throws AnalysisException {
		
		doEMClusterPredict(source,config.getTrainedModel().getModel());
		AnalyzerOutPutDataBaseUpdate result=new AnalyzerOutPutDataBaseUpdate();
		
		fillDBInfo(result, (DataBaseAnalyticSource)source);
		result.setDataset(dataSet);
		EMModel emModel=(EMModel)config.getTrainedModel().getModel();
		List<String> updatedColumns=new ArrayList<String>();
		updatedColumns.add(AlpineDataAnalysisConfig.ALPINE_MINER_EMCLUSTER);
		result.setUpdatedColumns(updatedColumns);

		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));

		return result;
	}
	
	/**
 * @param source
 * @param model
	 * @throws AnalysisException 
 */
	private void doEMClusterPredict(DataBaseAnalyticSource source, Model model) throws AnalysisException {
	
		try{
			Connection conncetion = ((DataBaseAnalyticSource) source).getConnection();

			PredictorConfig emPredictConfig=(PredictorConfig)source.getAnalyticConfig();
			String predictTable=StringHandler.doubleQ(source.getTableInfo().getSchema())
			+"."+StringHandler.doubleQ(source.getTableInfo().getTableName());
			String dropIfExist = emPredictConfig.getDropIfExist();
			String schemaName = emPredictConfig.getOutputSchema();
			String tableName = emPredictConfig.getOutputTable();
			try {
				dataSet = getDataSet(source,source.getAnalyticConfig());
			} catch (Exception e) {
				logger.error(e) ;
				if(e instanceof WrongUsedException){
					throw new AnalysisError(this,(WrongUsedException)e);
				} 
				else if(e instanceof AnalysisError){
					throw (AnalysisError)e;
				} 
				else{
					throw new AnalysisException(e );
				}
			}
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory
			.createConnectionInfo(source.getDataSourceType());
			
			AnalysisStorageParameterModel outputTableStorageParameters = null;
			String appendOnlyString = "";
			String endingString = "";
			outputTableStorageParameters = emPredictConfig.getStorageParameters();
			if (outputTableStorageParameters == null
					|| !outputTableStorageParameters.isAppendOnly()) {
				appendOnlyString = " ";
			} else {
				appendOnlyString = sqlGenerator.getStorageString(
						outputTableStorageParameters.isAppendOnly(),
						outputTableStorageParameters.isColumnarStorage(),
						outputTableStorageParameters.isCompression(),
						outputTableStorageParameters.getCompressionLevel());
			}

			endingString = sqlGenerator
					.setCreateTableEndingSql(outputTableStorageParameters == null ? null
							: outputTableStorageParameters
									.getSqlDistributeString());
			
			((EMModel)model).EMClusterPredict(predictTable, appendOnlyString,endingString,
			  conncetion,dropIfExist,schemaName,tableName);
		} catch (Exception e) {
			logger.error(e) ;
			if(e instanceof WrongUsedException){
				throw new AnalysisError(this,(WrongUsedException)e);
			} 
			else if(e instanceof AnalysisError){
				throw (AnalysisError)e;
			} 
			else{
				throw new AnalysisException(e );
			}
		}
	
}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo localAnalyticNodeMetaInfo = new AnalyticNodeMetaInfo();
		localAnalyticNodeMetaInfo
				.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.EM_PREDICT_NAME,locale));
		localAnalyticNodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.EM_PREDICT_DESCRIPTION,locale));
		return localAnalyticNodeMetaInfo;
	}

	
	
	
}
