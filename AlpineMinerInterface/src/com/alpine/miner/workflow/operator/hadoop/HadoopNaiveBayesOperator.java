package com.alpine.miner.workflow.operator.hadoop;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HadoopNaiveBayesOperator extends HadoopLearnerOperator {

    public static final List<String> parameterNames = Arrays.asList(new String[]{
            OperatorParameter.NAME_dependentColumn,
            OperatorParameter.NAME_forceRetrain,
//            OperatorParameter.NAME_isCalculateDeviance,
            OperatorParameter.NAME_columnNames
    });

    public HadoopNaiveBayesOperator() {
        super(parameterNames);
        addInputClass(OperatorInputFileInfo.class.getName());
        addOutputClass(EngineModel.MPDE_TYPE_HADOOP_NB);
    }


    @Override
    public String getToolTipTypeName() {
        return LanguagePack.getMessage(LanguagePack.NAIVE_BAYES_OPERATOR, locale);
    }

    @Override
    public boolean isVaild(VariableModel variableModel) {
        List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
                false);

        List<String> invalidParameterList=new ArrayList<String>();
        List<OperatorParameter> paraList=getOperatorParameterList();
        for(OperatorParameter para:paraList){
            String paraName=para.getName();
            String paraValue=(String)para.getValue();
            if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
                validateNull(invalidParameterList, paraName, paraValue);
                validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
            }else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
                validateNull(invalidParameterList, paraName, paraValue);
            }else if(paraName.equals(OperatorParameter.NAME_columnNames)){
                validateNull(invalidParameterList, paraName, paraValue);
                validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
            }
        }
        invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
        if(invalidParameterList.size()==0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public String getOperatorParameterDefaultValue(String paraName) {
        if (paraName.equals(OperatorParameter.NAME_isCalculateDeviance)){
            return Resources.FalseOpt;
        }else {
            return super.getOperatorParameterDefaultValue(paraName);
        }
    }
}
