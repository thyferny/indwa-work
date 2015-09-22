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
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitOutPut;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelValidator;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;

public class HadoopGoodnessOfFitEvaluator extends AbstractHadoopModelValidator{

	protected void fillSpecialDataSource(Operator dataSource,
			AnalyticConfiguration config) {
		((DatabaseSourceParameter)dataSource.getParameter()).setLabel(((EvaluatorConfig)config).getDependentColumn());
	}
	protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
		AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
		nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.GOODNESSOFFit_NAME,locale));
		nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.GOODNESSOFFit_DESCRIPTION,locale));
	 
		return nodeMetaInfo;
	}
	@Override
	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		EvaluatorConfig config=(EvaluatorConfig) source.getAnalyticConfig();
		List<GoodnessOfFit> resultList=new ArrayList<GoodnessOfFit>();
 
		boolean isLocalMode = false;

		try {
			//((HadoopAnalyticSource)source).getFileName() is the predicted file name and file struct
			for(EngineModel eModel:config.getTrainedModel()){
				String modelType =eModel.getModelType();
				adpter = EvaluatorAdapterFactory.getAdapater(modelType,getContext(),getName()) ;
				if(adpter!=null){
					resultList.add(adpter.generateGoFData(eModel,source));
					if(adpter.isLocalMode()==true){
						isLocalMode = true ;
					}
				}
 			}
		 
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnalysisException(e.getLocalizedMessage());
		}
	  		
		 
		GoodnessOfFitOutPut out= new GoodnessOfFitOutPut(resultList);
		if(isLocalMode ==true){
			
			out.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
		}
		out.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
		return out;
	}
}
