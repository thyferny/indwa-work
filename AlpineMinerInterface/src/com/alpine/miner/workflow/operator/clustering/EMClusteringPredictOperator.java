/**
 * 

* ClassName EMClusteringPredictOperator.java
*
* Version information: 1.00
*
* Date: Nov 22, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.miner.workflow.operator.clustering;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;

/**
 * @author Shawn
 *
 *  
 */

public class EMClusteringPredictOperator extends PredictOperator{

	
	public EMClusteringPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_EMCLUSTER);
 
	}
	
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.EM_CLUSTERING_PREDICTION_OPERATOR,locale);
	}

}
