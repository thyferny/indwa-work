
package com.alpine.miner.workflow.operator.field;

import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;


public class ValueAnalysisOperator extends AbstractOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_columnNames
	});

	public ValueAnalysisOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.VALUEANALYSIS_OPERATOR,locale);
	}
 

	@Override
	public List<Object> getOperatorOutputList() {
		return null;
	}
}
