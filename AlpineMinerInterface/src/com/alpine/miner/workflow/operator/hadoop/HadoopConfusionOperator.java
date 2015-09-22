package com.alpine.miner.workflow.operator.hadoop;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

import java.util.Arrays;
import java.util.List;


public class HadoopConfusionOperator extends HadoopVerificationOperator  {
    public static final List<String> parameterNames = Arrays.asList(new String[]{
            OperatorParameter.NAME_dependentColumn

    });

    public HadoopConfusionOperator() {
        super(parameterNames);
        addInputClass(EngineModel.MPDE_TYPE_HADOOP_NB);

    }

    @Override
    public String getToolTipTypeName() {
        return LanguagePack.getMessage(LanguagePack.CONFUSION_OPERATOR, locale);
    }
}
