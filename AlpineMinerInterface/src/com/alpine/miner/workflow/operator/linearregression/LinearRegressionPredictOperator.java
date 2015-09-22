/**
 * ClassName LinearRegressionPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.linearregression;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.PredictOperator;

public class LinearRegressionPredictOperator extends PredictOperator {
	
	public LinearRegressionPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_LIR);
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LINEARREGRESSION_PREDICT_OPERATOR,locale);
	}

}
