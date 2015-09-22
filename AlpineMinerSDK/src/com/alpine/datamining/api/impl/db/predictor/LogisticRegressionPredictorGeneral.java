/**
 * ClassName LogisticRegressionPredictorGeneral.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.predictor;

import java.util.ArrayList;
import java.util.List;
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
import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.training.Prediction;

/**
 * @author Eason
 *
 */
public class LogisticRegressionPredictorGeneral extends AbstractDBModelPredictor {
	private static Logger logger= Logger.getLogger(LogisticRegressionPredictorGeneral.class);
	
	private DataSet dataSet;
	private DataSet exmapleSetResult;

	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(DataBaseAnalyticSource source,PredictorConfig config) throws AnalysisException {
		
		logisticRegressionPredict(source,config.getTrainedModel().getModel());
		
		//here create  a AnlyticOutPutDataBaseUpdate to tell the user you change what table's what column
		AnalyzerOutPutDataBaseUpdate result=new AnalyzerOutPutDataBaseUpdate();
		
//		 set url user pwd ,schema, table
		fillDBInfo(result, (DataBaseAnalyticSource)source);
		
		result.setDataset(dataSet);
		
		List<String> updatedColumns=new ArrayList<String>();
		
		updatedColumns.add(exmapleSetResult.getColumns().getPredictedLabel().getName());
		updatedColumns.add(exmapleSetResult.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + ((Prediction)config.getTrainedModel().getModel()).getLabel().getMapping().mapIndex(1)).getName());
		updatedColumns.add(exmapleSetResult.getColumns().getSpecial(Column.CONFIDENCE_NAME + "_" + ((Prediction)config.getTrainedModel().getModel()).getLabel().getMapping().mapIndex(0)).getName());
		result.setUpdatedColumns(updatedColumns);
		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return result;
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.LOGISTIC_REGRESSION_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.LOGISTIC_REGRESSION_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
	private   void logisticRegressionPredict(DataBaseAnalyticSource source, Model model )
			throws AnalysisException {
		try {
			dataSet = getDataSet(source,source.getAnalyticConfig());
			exmapleSetResult = model.apply(dataSet);
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

}
