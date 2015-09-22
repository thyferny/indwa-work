/**
 * ClassName ROCOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.model.evaluator;

import com.alpine.miner.inter.resources.LanguagePack;

/**
 * @author zhao yong
 *
 */
public class ROCOperator extends VerificationOperator {

	public ROCOperator() {
		super(VerificationOperator.parameterNames);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ROC_OPERATOR,locale);
	}
 
}
