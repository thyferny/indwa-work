/**
 * ClassName HadoopROCOperator.java
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
public class HadoopROCOperator extends HadoopVerificationOperator {

	public HadoopROCOperator() {
		super(HadoopVerificationOperator.parameterNames);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ROC_OPERATOR,locale);
	}

}
