/**
 * ClassName NomarlizationPredictor.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.predictor;

import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.algoconf.ARIMARPredictorConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutARIMARPredict;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.timeseries.ARIMAModel;
import com.alpine.datamining.operator.timeseries.ARIMARPredictResult;

/**
 * 
 */
public class ARIMAPredictor extends AbstractAnalyzer {

	

	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.ARIMA_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		ARIMARPredictorConfig config = (ARIMARPredictorConfig)source.getAnalyticConfig();
		int aheadNumber = Integer.parseInt(config.getAheadNumber());
		ARIMAModel aRIMAmodel=(ARIMAModel)config.getTrainedModel().getModel();
		ARIMARPredictResult ret = null;
		try{
			ret = aRIMAmodel.prediction(aheadNumber, null, null, null);
		}catch (Exception e) {
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
		AnalyzerOutPutARIMARPredict output = new AnalyzerOutPutARIMARPredict(ret);
		output.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return output;
	}
}
