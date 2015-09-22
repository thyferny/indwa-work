/**
 * ClassName SVMPredictor.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
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

/**
 * @author Eason
 * 
 */
public class SVMPredictor extends AbstractDBModelPredictor {
	private static Logger logger= Logger.getLogger(SVMPredictor.class);
	
	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(DataBaseAnalyticSource source,PredictorConfig config) throws AnalysisException {
		DataSet dataSet = null;
		try {
			dataSet = getDataSet(source, source
					.getAnalyticConfig());
//			dataSet.recalculateAllcolumnStatistics();
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

		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return result;
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.SVM_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.SVM_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
	
}
