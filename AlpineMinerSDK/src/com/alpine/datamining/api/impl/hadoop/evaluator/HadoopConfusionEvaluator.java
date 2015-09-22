package com.alpine.datamining.api.impl.hadoop.evaluator;

import com.alpine.datamining.api.*;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.EvaluatorConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopPredictorConfig;
import com.alpine.datamining.api.impl.db.evaluator.ConfusionOutput;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelValidator;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.models.DecisionTreeHadoopModel;
import com.alpine.datamining.api.impl.hadoop.models.NaiveBayesHadoopModel;
import com.alpine.datamining.api.impl.hadoop.predictor.HadoopLogisticRegressionPredictor;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopNaiveBayesConfusionRunner;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.evaluator.ConfusionMatrix;
import com.alpine.datamining.operator.evaluator.GoodnessOfFit;
import com.alpine.hadoop.AlpineHadoopConfKeySet;
import com.alpine.hadoop.NaiveBayesKeySet;
import com.alpine.hadoop.naivebayes.NaiveBayesConfusionMapReduce;
import com.alpine.hadoop.naivebayes.NaiveBayesPredictorMapper;
import com.alpine.utility.common.VariableModelUtility;
import com.alpine.utility.db.Resources;
import com.alpine.utility.hadoop.HadoopConstants;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HadoopConfusionEvaluator extends AbstractHadoopModelValidator {
    private AnalyticContext context =null;

    private String operatorName;


    @Override
    public AnalyticOutPut doAnalysis(AnalyticSource source) throws AnalysisException {
        EvaluatorConfig config=(EvaluatorConfig) source.getAnalyticConfig();
       List<ConfusionMatrix> resultList=new ArrayList<ConfusionMatrix>();

        HadoopNaiveBayesConfusionRunner runner = new HadoopNaiveBayesConfusionRunner(getContext(),getName());
        boolean isLocalMode = false;

        try {
            //((HadoopAnalyticSource)source).getFileName() is the predicted file name and file struct
            for(EngineModel eModel:config.getTrainedModel()){
                String modelType =eModel.getModelType();
                //FOr now, this is just NB.
                resultList.add(createConfusionMatrix(eModel, source, runner));

//                adpter = EvaluatorAdapterFactory.getAdapater(modelType,getContext(),getName()) ;
//                if(adpter!=null){
//                    resultList.add(adpter.generateGoFData(eModel,source));
//                    if(adpter.isLocalMode()==true){
//                        isLocalMode = true ;
//                    }
//                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new AnalysisException(e.getLocalizedMessage());
        }


        ConfusionOutput out= new ConfusionOutput(resultList);
        if(isLocalMode ==true){

            out.setExtraLogMessage( SDKLanguagePack.getMessage(SDKLanguagePack.LOCAL_MODE, config.getLocale()));
        }
        out.setAnalyticNodeMetaInfo(createNodeMetaInfo(config.getLocale()));
        return out;
    }

    private ConfusionMatrix createConfusionMatrix(EngineModel model, AnalyticSource source, HadoopNaiveBayesConfusionRunner runner) throws Exception, AnalysisException {
        runner.createConfusionMatrix(source, model);
        return runner.getResults();
    }




    protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo =new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(SDKLanguagePack.CONFUSION_NAME,locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(SDKLanguagePack.CONFUSION_DESCRIPTION,locale));

        return nodeMetaInfo;
    }
}
