package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

public class HadoopPivotOperator extends HadoopDataOperationOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_pivotColumn,
			OperatorParameter.NAME_groupByColumn,
			OperatorParameter.NAME_aggregateColumn,
			OperatorParameter.NAME_aggregateType,
			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
			
	});
	
	public HadoopPivotOperator() {
		super(parameterNames);
		getOutputClassList().clear();
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String pivotColumn=null;
		String groupByColumn=null;
		for(OperatorParameter para:paraList){
			if(para.getName().equals(OperatorParameter.NAME_pivotColumn)){
				pivotColumn=(String)para.getValue();
			}else if(para.getName().equals(OperatorParameter.NAME_groupByColumn)){
				groupByColumn=(String)para.getValue();
			}
		}
		
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}			
			if(paraName.equals(OperatorParameter.NAME_aggregateColumn)){
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_pivotColumn)){
				if(!StringUtil.isEmpty(pivotColumn)&&!StringUtil.isEmpty(groupByColumn)
						&&pivotColumn.equals(groupByColumn)&&!invalidParameterList.contains(paraName)){
					invalidParameterList.add(paraName);
				}
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_groupByColumn)){
				if(!StringUtil.isEmpty(pivotColumn)&&!StringUtil.isEmpty(groupByColumn)
						&&pivotColumn.equals(groupByColumn)&&!invalidParameterList.contains(paraName)){
					invalidParameterList.add(paraName);
				}
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_aggregateType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else {
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
		return LanguagePack.getMessage(LanguagePack.PIVOT_OPERATOR,locale);
	}

}
