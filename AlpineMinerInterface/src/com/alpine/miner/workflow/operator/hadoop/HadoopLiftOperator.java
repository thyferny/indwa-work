/**
 * ClassName HadoopLiftOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-19
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import com.alpine.miner.inter.resources.LanguagePack;

/**
 * @author Jeff Dong
 *
 */
public class HadoopLiftOperator extends HadoopVerificationOperator {

	public HadoopLiftOperator() {
		super(HadoopVerificationOperator.parameterNames);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LIFT_OPERATOR,locale);
	}

}
