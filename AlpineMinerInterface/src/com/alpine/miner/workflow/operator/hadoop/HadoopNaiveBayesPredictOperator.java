package com.alpine.miner.workflow.operator.hadoop;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;

public class HadoopNaiveBayesPredictOperator extends HadoopPredictOperator {

    public HadoopNaiveBayesPredictOperator() {
        super();
        addInputClass(EngineModel.MPDE_TYPE_HADOOP_NB);
        addOutputClass(OperatorInputFileInfo.class.getName());
    }

    @Override
    public String getToolTipTypeName() {
        return LanguagePack.getMessage(LanguagePack.NAIVE_BAYES_PREDICTION_OPERATOR, locale);
    }

}
