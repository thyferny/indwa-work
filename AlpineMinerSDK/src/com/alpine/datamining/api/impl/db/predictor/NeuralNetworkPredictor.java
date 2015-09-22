/**
 * ClassName NeuralNetworkPredictor.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.predictor;

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
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;

/**
 * @author Eason
 *
 */
public class NeuralNetworkPredictor extends AbstractDBModelPredictor {
	private static Logger logger= Logger.getLogger(NeuralNetworkPredictor.class);
	
	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.AbstractPredictor#doPredict(com.alpine.datamining.api.impl.config.PredictorConfig)
	 */
	@Override
	protected AnalyzerOutPutDataBaseUpdate doPredict(DataBaseAnalyticSource source, PredictorConfig config) throws AnalysisException {
		Model model = config.getTrainedModel().getModel();
		DataSet dataSet;
		try {	
			dataSet = getDataSet(source, source.getAnalyticConfig());
//			source.getAnalyticConfig().setColumnNames(((NNModel)model).getSpecifyColumn());
//			setSpecifyColumn(dataSet, source.getAnalyticConfig());
//			dataSet.recalculateAllcolumnStatistics();
			dataSet = model.apply(dataSet);
		} catch ( Exception e) {
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
		
		List<String> updatedColumns=((NNModel)model).getUpdateColumns();

		result.setUpdatedColumns(updatedColumns);
		result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return result;
	}
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.NEURALNETWORK_PREDICT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.NEURALNETWORK_PREDICT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
}
