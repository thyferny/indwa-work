/**
 * 

* ClassName RandomForestPredictor.java
*
* Version information: 1.00
*
* Date: 2012-10-10
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.db.predictor;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.algoconf.PredictorConfig;
import com.alpine.datamining.api.impl.db.AbstractDBModelPredictor;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;

/**
 * @author Shawn
 *
 *  
 */

public class RandomForestPredictor extends AbstractDBModelPredictor{
	private static Logger logger= Logger.getLogger(RandomForestPredictor.class);
	private DataSet dataSet;
	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(
			DataBaseAnalyticSource dataBaseAnalyticSource, PredictorConfig predictorConfig)
			throws AnalysisException {
		Model model = predictorConfig.getTrainedModel().getModel();
		dataBaseAnalyticSource.getAnalyticConfig();

		try {
			dataSet = getDataSet(dataBaseAnalyticSource, dataBaseAnalyticSource
					.getAnalyticConfig());
	
			model.apply(dataSet);
		} catch (Exception exception) {
			logger.error(exception);

			if (exception instanceof WrongUsedException)
				throw new AnalysisError(this, (WrongUsedException) exception);
			if (exception instanceof AnalysisError)
				throw ((AnalysisError) exception);

			throw new AnalysisException(exception);
		}
		AnalyzerOutPutDataBaseUpdate analyzerOutPutDataBaseUpdate = new AnalyzerOutPutDataBaseUpdate();
		fillDBInfo(analyzerOutPutDataBaseUpdate, dataBaseAnalyticSource);
		analyzerOutPutDataBaseUpdate.setDataset(this.dataSet);
		analyzerOutPutDataBaseUpdate
				.setAnalyticNodeMetaInfo(createNodeMetaInfo(predictorConfig.getLocale()));
		return analyzerOutPutDataBaseUpdate;
	}

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo localAnalyticNodeMetaInfo = new AnalyticNodeMetaInfo();
		localAnalyticNodeMetaInfo
				.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_PREDICT_NAME,locale));
		localAnalyticNodeMetaInfo
				.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ADABOOST_PREDICT_DESCRIPTION,locale));
		return localAnalyticNodeMetaInfo;
	}
}
