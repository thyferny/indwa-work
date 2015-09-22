/**
 * ClassName AbstractHadoopModelPredictor.java
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
import com.alpine.datamining.api.impl.AbstractAnalyzerOutPut;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.algoconf.TimeSeriesHadoopPredictorConfig;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.utility.hadoop.HadoopConstants;

/***
 * 
 * @author Eason
 * 
 */

public abstract class AbstractHadoopModelPredictor extends AbstractHadoopMRJobAnalyzer {
//	protected AlpineHadoopRunner hadoopRunner =null;

	public AnalyticOutPut doAnalysis(AnalyticSource source)
			throws AnalysisException {
		AnalyticOutPut result = null;
		HadoopPredictorConfig config = (HadoopPredictorConfig) source.getAnalyticConfig();
		try {
			if(config instanceof TimeSeriesHadoopPredictorConfig){
				((TimeSeriesHadoopPredictorConfig)config).setHadoopInfo(((HadoopAnalyticSource)source).getHadoopInfo());
				result = doPredict(config);
			}
			else{
				result = doPredict((HadoopAnalyticSource) source,
					config);
				 super.reportBadDataCount(hadoopRunner.getBadCounter(), HadoopConstants.Flow_Call_Back_URL, getName(), getFlowRunUUID());
				 if( hadoopRunner.isLocalMode()  ==true){
						result.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,config.getLocale()));
					}
			}
			result.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));

		
		} catch (Exception e) {
			throw new AnalysisException(e);
		}
		return result;
	}


/**@param config 
 * @return
 * @throws AnalysisException 
 * @throws Exception 
 * @throws OperatorException 
	 */
	protected abstract HadoopMultiAnalyticFileOutPut doPredict(
			HadoopAnalyticSource source,HadoopPredictorConfig config) throws AnalysisException, Exception;
	
	protected abstract AbstractAnalyzerOutPut doPredict(HadoopPredictorConfig config) throws AnalysisException, Exception;

	protected abstract AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale);

	
 
}
