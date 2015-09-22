/**
 * ClassName  ModelNeededConfig.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf;

import com.alpine.datamining.api.impl.EngineModel;

/**
 * @author John Zha0
 *
 */
public interface ModelNeededConfig {
	public EngineModel getTrainedModel();

	public void setTrainedModel(EngineModel trainedModel);
}
