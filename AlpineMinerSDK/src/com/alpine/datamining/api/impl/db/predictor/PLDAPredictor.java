/**
 * ClassName PLDAPredictor.java
 *
 * Version information: 1.00
 *
 * Data: 2012-2-6
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.predictor;

import java.sql.Connection;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.algoconf.PLDAPredictConfig;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdatePLDA;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.configure.AnalysisStorageParameterModel;
import com.alpine.datamining.operator.plda.PLDAModel;
import com.alpine.utility.db.ISqlGeneratorMultiDB;
import com.alpine.utility.db.SqlGeneratorMultiDBFactory;
import com.alpine.utility.tools.StringHandler;
/**
 * @author Shawn
 *
 */

public class PLDAPredictor extends AbstractDBModelPredictor{
	private static Logger logger= Logger.getLogger(PLDAPredictor.class);
	
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {

		PredictorConfig config = (PredictorConfig) source.getAnalyticConfig();

		AnalyticOutPut result = doPredict((DataBaseAnalyticSource) source,
				config);

		return result;
	}
	
	@Override
	public AnalyzerOutPutDataBaseUpdate doPredict(DataBaseAnalyticSource source, PredictorConfig config)
			throws AnalysisException {

			PLDAPredict((DataBaseAnalyticSource)source,config.getTrainedModel().getModel());
			AnalyzerOutPutDataBaseUpdatePLDA analyzerOutPutDataBaseUpdate = new AnalyzerOutPutDataBaseUpdatePLDA();
			fillDBInfo(analyzerOutPutDataBaseUpdate, (DataBaseAnalyticSource)source);
			analyzerOutPutDataBaseUpdate
					.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutDataBaseUpdate.setSchemaName(((PLDAPredictConfig)config).getOutputSchema());
			analyzerOutPutDataBaseUpdate.setTableName(((PLDAPredictConfig)config).getOutputTable());
			analyzerOutPutDataBaseUpdate.setDocTopicOutTable(((PLDAPredictConfig)config).getPLDADocTopicOutputTable());
			analyzerOutPutDataBaseUpdate.setDocTopicOutSchema(((PLDAPredictConfig)config).getPLDADocTopicOutputSchema());

			return analyzerOutPutDataBaseUpdate;
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo localAnalyticNodeMetaInfo = new AnalyticNodeMetaInfo();
		localAnalyticNodeMetaInfo
				.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.PLDA_PREDICT_NAME,locale));
		localAnalyticNodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.PLDA_PREDICT_DESCRIPTION,locale));
		return localAnalyticNodeMetaInfo;
	}
	
	private void PLDAPredict(DataBaseAnalyticSource source, Model model)throws AnalysisException
	{
		try {

			Connection conncetion = ((DataBaseAnalyticSource) source)
					.getConnection();

			PLDAPredictConfig pldaConfig=(PLDAPredictConfig)source.getAnalyticConfig();
			String predictTable=StringHandler.doubleQ(source.getTableInfo().getSchema())
			+"."+StringHandler.doubleQ(source.getTableInfo().getTableName());
			long iterationNumber=Long.parseLong(pldaConfig.getIterationNumber());
	
			String dropIfExist = pldaConfig.getDropIfExist();
			String dropIfExistRTable=pldaConfig.getPLDADocTopicDropIfExist();
			String docOutTable=pldaConfig.getOutputTable();
			String docOutSchema=pldaConfig.getOutputSchema();
			String docTopicOutSchema=pldaConfig.getPLDADocTopicOutputSchema();
			String docTopicOutTable = pldaConfig.getPLDADocTopicOutputTable();
			ISqlGeneratorMultiDB sqlGenerator = SqlGeneratorMultiDBFactory
					.createConnectionInfo(source.getDataSourceType());

			AnalysisStorageParameterModel docTopicOutputTableStorageParameters = null;
			String docTopicAppendOnlyString = "";
			String docTopicEndingString = "";
			docTopicOutputTableStorageParameters = pldaConfig
					.getPLDADocTopicOutputTableStorageParameters();
			if (docTopicOutputTableStorageParameters == null
					|| !docTopicOutputTableStorageParameters.isAppendOnly()) {
				docTopicAppendOnlyString = "";
			} else {
				docTopicAppendOnlyString = sqlGenerator.getStorageString(
						docTopicOutputTableStorageParameters.isAppendOnly(),
						docTopicOutputTableStorageParameters
								.isColumnarStorage(),
						docTopicOutputTableStorageParameters.isCompression(),
						docTopicOutputTableStorageParameters
								.getCompressionLevel());
			}

			docTopicEndingString = sqlGenerator
					.setCreateTableEndingSql(docTopicOutputTableStorageParameters == null ? null
							: docTopicOutputTableStorageParameters
									.getSqlDistributeString());

			AnalysisStorageParameterModel outputTableStorageParameters = null;
			String appendOnlyString = "";
			String endingString = "";
			outputTableStorageParameters = pldaConfig.getStorageParameters();
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

			((PLDAModel)model).PLDAPredict(predictTable,iterationNumber,
					docOutSchema,docOutTable,appendOnlyString, endingString, docTopicOutSchema,
					docTopicOutTable,docTopicAppendOnlyString, docTopicEndingString,conncetion,dropIfExist,dropIfExistRTable
					);

		} catch (Exception exception) {
			logger.error(exception);

			if (exception instanceof WrongUsedException)
				throw new AnalysisError(this, (WrongUsedException) exception);
			if (exception instanceof AnalysisError)
				throw ((AnalysisError) exception);

			throw new AnalysisException(exception);
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.db.AbstractDBModelPredictor#doPredict(com.alpine.datamining.api.impl.db.DataBaseAnalyticSource, com.alpine.datamining.api.impl.algoconf.PredictorConfig)
	 */
//	@Override
//	protected AnalyzerOutPutDataBaseUpdate doPredict(
//			DataBaseAnalyticSource source, PredictorConfig config)
//			throws AnalysisException {
//		// TODO Auto-generated method stub
//		return null;
//	}


}
