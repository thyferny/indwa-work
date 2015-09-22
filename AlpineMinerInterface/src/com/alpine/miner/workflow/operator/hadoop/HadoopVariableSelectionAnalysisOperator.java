package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

public class HadoopVariableSelectionAnalysisOperator extends HadoopOperator {

    public static final List<String> parameterNames = Arrays.asList(new String[]{
            OperatorParameter.NAME_dependentColumn,
            OperatorParameter.NAME_columnNames
    });


    public HadoopVariableSelectionAnalysisOperator() {
        super(parameterNames);
        addInputClass(OperatorInputFileInfo.class.getName());
    }

    @Override
    public String validateInputLink(Operator precedingOperator) {
        String message = super.validateInputLink(precedingOperator);
        if(StringUtil.isEmpty(message) ==true){
            message = super.validateStoreResult(precedingOperator);
        }
        return message;
    }

    @Override
    public String getToolTipTypeName() {
        return LanguagePack.getMessage(LanguagePack.VARIABLE_SELECTION_OPERATOR, locale);
    }


    @Override
    public boolean isVaild(VariableModel variableModel) {
        List<String> fieldList = OperatorUtility.getAvailableColumnsList(this, false);
        List<String> numFieldList = OperatorUtility.getAvailableNumColumnsList(this, false);

        List<String> invalidParameterList=new ArrayList<String>();
        List<OperatorParameter> paraList=getOperatorParameterList();
        for(OperatorParameter para:paraList){
            String paraName=para.getName();
            String paraValue=(String)para.getValue();
            if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
                validateNull(invalidParameterList, paraName, paraValue);
                validateContainColumns(numFieldList,invalidParameterList, paraName, paraValue);
//            }else if(paraName.equals(OperatorParameter.NAME_scoreType)){
//                validateNull(invalidParameterList, paraName, paraValue);
            }else if(paraName.equals(OperatorParameter.NAME_columnNames)){
                validateNull(invalidParameterList, paraName, paraValue);
                validateColumnNames(fieldList, invalidParameterList, paraName, paraValue);
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
    public List<Object> getOperatorOutputList() {
        return null;
    }
}
