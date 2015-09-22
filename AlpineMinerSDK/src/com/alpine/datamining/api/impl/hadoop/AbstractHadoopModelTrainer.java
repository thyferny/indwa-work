/**
 * ClassName AbstractHadoopModelTrainer.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-25
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop;

import java.util.Locale;

import org.apache.pig.builtin.PigUtil;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractModelTrainerConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;
import com.alpine.utility.hadoop.HadoopConstants;

/**
 * @author Eason
 *
 */
public abstract class AbstractHadoopModelTrainer extends AbstractHadoopMRJobAnalyzer{
//	protected AlpineHadoopRunner hadoopRunner;  

 

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
			if(hadoopRunner.isLocalMode()==true){
				analyzerOutPutModel.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
			}
			super.reportBadDataCount(hadoopRunner.getBadCounter(), HadoopConstants.Flow_Call_Back_URL, getName(), getFlowRunUUID());
			return analyzerOutPutModel;
		} else {
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
