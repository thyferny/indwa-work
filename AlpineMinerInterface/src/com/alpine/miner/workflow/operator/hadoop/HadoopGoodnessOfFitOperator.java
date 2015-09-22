/**
 * ClassName HadoopGoodnessOfFitOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-19
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author Jeff Dong
 *
 */
public class HadoopGoodnessOfFitOperator extends HadoopVerificationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn
			
	});
	
	public HadoopGoodnessOfFitOperator() {
		super(parameterNames);
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.GOODNESSOFFIT_OPERATOR,locale);
	}

}
