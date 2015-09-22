/**
 * ClassName LogisticRegressionPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.logisticregression;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;

public class LogisticRegressionPredictOperator extends PredictOperator {
	public LogisticRegressionPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_LOR);
	}
 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LOGISTICREGRESSION_PREDICT_OPERATOR,locale);
	}
}
