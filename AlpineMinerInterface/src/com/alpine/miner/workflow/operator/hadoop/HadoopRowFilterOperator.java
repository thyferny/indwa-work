package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

public class HadoopRowFilterOperator extends HadoopDataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_Condition,
			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
	});
	
	public HadoopRowFilterOperator() {
		super(parameterNames);
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList=new ArrayList<String>();
		
		List<OperatorParameter> paraList=getOperatorParameterList();
		

		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_HD_Condition)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else{
				validateHadoopStorageParameter(paraName,paraValue,invalidParameterList);
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
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ROWFILTER_OPERATOR,locale);
	}




}
