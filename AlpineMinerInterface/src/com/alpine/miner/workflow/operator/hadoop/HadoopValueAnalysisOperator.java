/**
 * ClassName HadoopValueAnalysisOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhao yong
 *
 */
public class HadoopValueAnalysisOperator extends HadoopExplorationOperator  {
	 
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_columnNames,
	});
	
	public HadoopValueAnalysisOperator() {
		super(parameterNames);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.VALUEANALYSIS_OPERATOR,locale);
	}


}
