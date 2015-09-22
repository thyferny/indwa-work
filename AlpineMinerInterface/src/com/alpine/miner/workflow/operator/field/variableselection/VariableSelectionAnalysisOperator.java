/**
 * ClassName VariableSelectionOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field.variableselection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

public class VariableSelectionAnalysisOperator extends AbstractOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{			 
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_scoreType,
			OperatorParameter.NAME_columnNames,
	});
	
	public VariableSelectionAnalysisOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());	
	}

 
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.VARIABLE_SELECTION_OPERATOR,locale);
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
			}else if(paraName.equals(OperatorParameter.NAME_scoreType)){
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
	public List<Object> getOperatorOutputList() {
		return null;
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_scoreType)){
			return "Info gain";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
		
	}	
}
