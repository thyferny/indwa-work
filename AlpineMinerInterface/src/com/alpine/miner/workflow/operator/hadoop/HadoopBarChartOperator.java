/**
 * ClassName HadoopBarChartOperator.java
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
public class HadoopBarChartOperator extends HadoopExplorationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_valueDomain,
			OperatorParameter.NAME_categoryType,
			OperatorParameter.NAME_scopeDomain,
		
	});

	public HadoopBarChartOperator() {
		super(parameterNames);
	}
 
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.BARCHART_OPERATOR,locale);
	}

	 
 
}
