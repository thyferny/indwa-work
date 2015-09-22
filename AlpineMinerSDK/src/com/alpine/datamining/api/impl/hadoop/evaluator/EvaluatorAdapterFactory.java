/**
 * ClassName EvaluatorAdapterFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2012-9-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/

package com.alpine.datamining.api.impl.hadoop.evaluator;

import com.alpine.datamining.api.AnalyticContext;
import com.alpine.datamining.api.impl.EngineModel;

public class EvaluatorAdapterFactory {

	public static EvaluatorAdapter getAdapater(String modelType,AnalyticContext context,String operatorName) {
		if(EngineModel.MPDE_TYPE_HADOOP_LOR.equals(modelType)){
			return new LoREvaluatorAdapter(context,operatorName);
		}
		else if(EngineModel.MPDE_TYPE_HADOOP_TREE_CLASSIFICATION.equals(modelType)){
			return new DTEvaluatorAdapter(context,operatorName);
		}//
		else{
			return null;
		}
	}

}
