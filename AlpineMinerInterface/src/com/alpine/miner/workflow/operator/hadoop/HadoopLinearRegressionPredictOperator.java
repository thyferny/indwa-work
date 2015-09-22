/**
 * ClassName HadoopLinearRegressionPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-20
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;

/**
 * @author Jeff Dong
 *
 */
public class HadoopLinearRegressionPredictOperator extends
		HadoopPredictOperator {

	public HadoopLinearRegressionPredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_LIR);
		addOutputClass(OperatorInputFileInfo.class.getName());
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LINEARREGRESSION_PREDICT_OPERATOR,locale);
	}

}
