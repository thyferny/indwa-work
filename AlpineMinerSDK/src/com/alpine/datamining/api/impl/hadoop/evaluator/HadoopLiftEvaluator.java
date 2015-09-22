/**
 * ClassName HadoopLiftEvaluator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.hadoop.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticConfiguration;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelValidator;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.evaluator.DoubleListData;

public class HadoopLiftEvaluator extends AbstractHadoopModelValidator{

	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((EvaluatorConfig)config).getDependentColumn());
	}
	
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.LIFT_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.LIFT_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
	
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		
		EvaluatorConfig config=(EvaluatorConfig) source.getAnalyticConfig();
		List<DoubleListData> resultList=new ArrayList<DoubleListData>();
		boolean isLocalMode = false;
	
		try {
			//((HadoopAnalyticSource)source).getFileName() is the predicted file name and file struct
			for(EngineModel eModel:config.getTrainedModel()){
				String modelType =eModel.getModelType();
				adpter = EvaluatorAdapterFactory.getAdapater(modelType,getContext(),getName()) ;
				if(adpter!=null){
					
					resultList.add(adpter.generateLiftData(eModel,source));
					if(adpter.isLocalMode()==true){
						isLocalMode = true ;
					}
				}
			
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnalysisException(e.getLocalizedMessage());
		}
	 
		AnalyzerOutPutObject out= new AnalyzerOutPutObject(resultList);
		
		if(isLocalMode ==true){
			
			out.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
		}

		out.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return out;
	}
	
	
}

