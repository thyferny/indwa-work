/**
 * ClassName SVMPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.svm;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;

public class SVMPredictOperator extends PredictOperator {

	public SVMPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_SVM_R);
		addInputClass(EngineModel.MPDE_TYPE_SVM_ND);
		addInputClass(EngineModel.MPDE_TYPE_SVM_C);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SVM_PREDICTION,locale);
	}

}
