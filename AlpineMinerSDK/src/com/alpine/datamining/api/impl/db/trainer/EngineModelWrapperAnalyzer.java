/**
 * ClassName  EngineModelWrapperAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-2
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db.trainer;

import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.AbstractAnalyzer;
import com.alpine.datamining.api.impl.ModelWrapperConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.resources.SDKLanguagePack;

/**
 * @author John Zhao
 *
 */
public class EngineModelWrapperAnalyzer extends AbstractAnalyzer {
 
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException  {
	 
		ModelWrapperConfig config=(ModelWrapperConfig)source.getAnalyticConfig();
		AnalyzerOutPutTrainModel model=new AnalyzerOutPutTrainModel(config.getTrainedModel());
		String modelName= getName();
		model.getEngineModel().setName(modelName);
		model.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return model;
	}
	
	private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.EngineModelWrapper_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.EngineModelWrapper_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}

}
