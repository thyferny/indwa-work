/**
 * ClassName RowFilterOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

public class FilterOperator extends DataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_whereClause,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist
 
	});
	
	public FilterOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ROWFILTER_OPERATOR,locale);
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
			if(paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_whereClause)){
				validateNull(invalidParameterList, paraName, paraValue);
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
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
}
