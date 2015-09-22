/**
 * ClassName NaiveBayesPredictor.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
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
import com.alpine.datamining.operator.bayes.NBModel;

/**
 * @author John Zhao
 * 
 */
public class NaiveBayesPredictor extends AbstractDBModelPredictor {
	private static Logger logger= Logger.getLogger(NaiveBayesPredictor.class);
	
	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(DataBaseAnalyticSource source,PredictorConfig config) throws AnalysisException {
		DataSet dataSet = null;
		try {
			dataSet = getDataSet(source, source
					.getAnalyticConfig());
			config.getTrainedModel().getModel().apply(dataSet);
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
		AnalyzerOutPutDataBaseUpdate result=new AnalyzerOutPutDataBaseUpdate();
		
		result.setDataset(dataSet);
		 
//		 set url user pwd ,schema, table
		fillDBInfo(result, (DataBaseAnalyticSource)source);

		Model model = config.getTrainedModel().getModel();
		result.setUpdatedColumns(((NBModel)model).getUpdateColumns());
		//good, bad ,
 
		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return result;
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.NAIVE_BAYES_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.NAIVE_BAYES_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
	
}
