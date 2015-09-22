/**
 * ClassName LiftOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.model.evaluator;

import com.alpine.miner.inter.resources.LanguagePack;

/**
 * @author zhao yong
 *
 */
public class LIFTOperator extends VerificationOperator {

	public LIFTOperator() {
		super(VerificationOperator.parameterNames);
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LIFT_OPERATOR,locale);
	}

}
