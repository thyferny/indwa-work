package com.alpine.datamining.api.impl.hadoop.trainer;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopModelTrainer;
import com.alpine.datamining.api.impl.hadoop.runner.HadoopNaiveBayesTrainRunner;
import com.alpine.datamining.api.resources.SDKLanguagePack;
import com.alpine.datamining.operator.Model;

import java.util.Locale;

public class HadoopNaiveBayesTrainer extends AbstractHadoopModelTrainer {
    @Override
    protected Model train(AnalyticSource source) throws AnalysisException {
        hadoopRunner= new HadoopNaiveBayesTrainRunner(getContext(),getName());
        Model model = null;
        try {
            model =(Model)	hadoopRunner.runAlgorithm(source);
        } catch (Exception e) {
            throw new AnalysisException(e);
        }
//        //set real column name for visualization
//        if(model instanceof LinearRegressionHadoopModel){
//            AnalysisFileStructureModel fileStructureModel = ((HadoopAnalyticSource)source).getHadoopFileStructureModel();
//            List<String> columnNameList = fileStructureModel.getColumnNameList();
//            ((LinearRegressionHadoopModel)model).setRealColumnNames(columnNameList);
//        }
        return model;
    }

    @Override
    protected AnalyticNodeMetaInfo createNodeMetaInfo(Locale locale) {
        AnalyticNodeMetaInfo nodeMetaInfo = new AnalyticNodeMetaInfo();
        nodeMetaInfo.setAlgorithmName(SDKLanguagePack.getMessage(
                SDKLanguagePack.NAIVE_BAYES_TRAIN_NAME, locale));
        nodeMetaInfo.setAlgorithmDescription(SDKLanguagePack.getMessage(
                SDKLanguagePack.NAIVE_BAYES_TRAIN_DESCRIPTION, locale));

        return nodeMetaInfo;
    }
}
