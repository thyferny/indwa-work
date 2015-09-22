/**
 * ClassName NaiveBayesOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.naivebayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;

/**
 * @author zhao yong
 *
 */
public class NaiveBayesOperator extends LearnerOperator {
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_isCalculateDeviance,
			OperatorParameter.NAME_columnNames  
	});

	public NaiveBayesOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_NB);
	}

	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NAIVE_BAYES_OPERATOR,locale);
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
