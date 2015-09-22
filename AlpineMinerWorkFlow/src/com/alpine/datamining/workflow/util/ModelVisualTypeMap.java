/**
 * ClassName ModelVisualTypeMap.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.HashMap;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.algoconf.ARIMAConfig;
import com.alpine.datamining.api.impl.algoconf.AbstractSVMConfig;
import com.alpine.datamining.api.impl.algoconf.AdaboostConfig;
import com.alpine.datamining.api.impl.algoconf.CartConfig;
import com.alpine.datamining.api.impl.algoconf.DecisionTreeConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopDecisionTrainConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopLinearTrainConfig;
import com.alpine.datamining.api.impl.algoconf.HadoopLogisticRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.LinearRegressionConfig;
import com.alpine.datamining.api.impl.algoconf.LogisticRegressionConfigGeneral;
import com.alpine.datamining.api.impl.algoconf.NaiveBayesConfig;
import com.alpine.datamining.api.impl.algoconf.NeuralNetworkConfig;
import com.alpine.datamining.api.impl.algoconf.PLDAConfig;
import com.alpine.datamining.api.impl.algoconf.WeightOfEvidenceConfig;

public class ModelVisualTypeMap {
	
	public static void initModelVisualTypeMap(HashMap<String, String> modelVisualTypeMap) {
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_LIR,
				LinearRegressionConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_LOR,
				LogisticRegressionConfigGeneral.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_NB,
				NaiveBayesConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_NEU,
				NeuralNetworkConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_SVM_C,
				AbstractSVMConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_SVM_ND,
				AbstractSVMConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_SVM_R,
				AbstractSVMConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_TIMESERIES,
				ARIMAConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_TREE_CLASSIFICATION,
				DecisionTreeConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_TREE_REGRESSION,
				CartConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_ADABOOST,
				AdaboostConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_WOE,
				WeightOfEvidenceConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_PLDA,
				PLDAConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_LR_SPLITMODEL,
				LogisticRegressionConfigGeneral.SPLIT_MODEL_VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_LIR_SPLITMODEL,
				LinearRegressionConfig.SPLITMODEL_VISUALIZATION_TYPE+","+
						LinearRegressionConfig.ResidualPlot_VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_HADOOP_LIR,
				HadoopLinearTrainConfig.VISUALIZATION_TYPE);
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_HADOOP_LOR,
				HadoopLogisticRegressionConfig.VISUALIZATION_TYPE);
		 
		modelVisualTypeMap.put(EngineModel.MPDE_TYPE_HADOOP_TREE_CLASSIFICATION,
				HadoopDecisionTrainConfig.VISUALIZATION_TYPE);
				
	}
}
