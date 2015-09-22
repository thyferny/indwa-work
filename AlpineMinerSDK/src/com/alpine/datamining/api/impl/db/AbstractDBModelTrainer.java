/**
 * ClassName AbstractModelTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;

import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.operator.Model;

/**
 * @author John Zhao
 *
 */
public abstract class AbstractDBModelTrainer extends AbstractDBAnalyzer{

 

	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
 
		AbstractModelTrainerConfig config = (AbstractModelTrainerConfig) source
				.getAnalyticConfig();
		if (config.getTrainedModel() == null||config.getForceRetrain().equals("Yes")) {
			
			Model model = train(source);
			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(
					model);
			String modelName= getName();
			analyzerOutPutModel.getEngineModel().setName(modelName);
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutModel.setComeFromRetrain(true);
		 
			return analyzerOutPutModel;
		} else {// need not train the model agian, UI have the reused model			
			AnalyzerOutPutTrainModel analyzerOutPutModel = new AnalyzerOutPutTrainModel(config.getTrainedModel().getModel());
			analyzerOutPutModel.getEngineModel().setName(getName());
			analyzerOutPutModel.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
			analyzerOutPutModel.setComeFromRetrain(false);
			
			return analyzerOutPutModel;
		}
	}

	protected abstract Model train(AnalyticSource source) throws AnalysisException;
	protected abstract AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale);

}
