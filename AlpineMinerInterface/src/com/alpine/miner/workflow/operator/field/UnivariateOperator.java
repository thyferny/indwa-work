/**
 * ClassName UnivariateOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhao yong
 *
 */
public class UnivariateOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_goodValue,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_max_generations,
			OperatorParameter.NAME_epislon,
			OperatorParameter.NAME_columnNames	
	});

	public UnivariateOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.UNIVARIATE_OPERATOR,locale);
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
			}else if(paraName.equals(OperatorParameter.NAME_goodValue)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_max_generations)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_epislon)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, false, Double.MAX_VALUE, true,variableModel);
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
		if(paraName.equals(OperatorParameter.NAME_max_generations)){
			return "25";
		}else if (paraName.equals(OperatorParameter.NAME_epislon)){
			return "0.00000001";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
