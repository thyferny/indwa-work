package com.alpine.miner.workflow.operator.adaboost;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;

public class AdaboostPredictOperator extends PredictOperator {

	public AdaboostPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_ADABOOST);
 
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ADABOOST_PREDICTION_OPERATOR,locale);
	}

	
}
