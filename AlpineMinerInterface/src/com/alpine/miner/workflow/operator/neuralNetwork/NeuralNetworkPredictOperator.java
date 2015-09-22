/**
 * NeuralNetworkPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.miner.workflow.operator.neuralNetwork;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;

/**
 * @author Jimmy
 *
 */
public class NeuralNetworkPredictOperator extends PredictOperator {

	public NeuralNetworkPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_NEU);
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NEURAL_NETWORK_PREDICTION_OPERATOR,locale);
	}
 
}
