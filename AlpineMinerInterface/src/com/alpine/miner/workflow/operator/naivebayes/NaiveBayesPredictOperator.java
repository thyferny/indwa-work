/**
 * ClassName NaiveBayesPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.naivebayes;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;

public class NaiveBayesPredictOperator extends PredictOperator {

	public NaiveBayesPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_NB);
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NAIVE_BAYES_PREDICTION_OPERATOR,locale);
	}
 
}
