/**
 * ClassName ValueAnalysisOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhao yong
 *
 */
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
