
package com.alpine.datamining.api.impl.hadoop.explorer;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionResult;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopMRJobAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopVariableSelectionRunner;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.utility.hadoop.HadoopConstants;


public class HadoopVariableSelectionAnalyzer extends AbstractHadoopMRJobAnalyzer {

    private static Logger itsLogger = Logger.getLogger(HadoopVariableSelectionAnalyzer.class);

    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source) throws AnalysisException {
        if(itsLogger.isDebugEnabled()){
            itsLogger.debug("Starting Hadoop Variable Selection Analyser");
        }

         HadoopAnalyticSource hadoopSource = (HadoopAnalyticSource) source;

        try {

            hadoopRunner = new HadoopVariableSelectionRunner(getContext(), getName()) ;
            VariableSelectionResult variableSelectionResult = (VariableSelectionResult) hadoopRunner.runAlgorithm(hadoopSource);


            AnalyzerOutPutObject output= new AnalyzerOutPutObject(variableSelectionResult);
            output.setAnalyticNodeMetaInfo(createNodeMetaInfo(source.getAnalyticConfig().getLocale()));
			super.reportBadDataCount(hadoopRunner.getBadCounter(), HadoopConstants.Flow_Call_Back_URL, getName(), getFlowRunUUID());
			if(hadoopRunner!=null && hadoopRunner.isLocalMode()  ==true){
				output.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE,source.getAnalyticConfig().getLocale()));
			}
            return output;

        } catch (Exception e) {
            itsLogger.error(e.getMessage(), e);
            throw new AnalysisException(e.getMessage(), e);
        }
    }


    private AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_NAME,locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.VARIABLE_SELECTION_DESCRIPTION,locale));

        return nodeMetaInfo;
    }

}
