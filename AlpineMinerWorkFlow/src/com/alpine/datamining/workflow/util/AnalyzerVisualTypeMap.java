/**
 * ClassName AnalyzerVisualTypeMap.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.HashMap;

import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionAnalyzer;
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitEvaluator;
import com.alpine.datamining.api.impl.db.evaluator.LiftGeneralEvaluator;
import com.alpine.datamining.api.impl.db.evaluator.ROCGeneralEvaluator;
import com.alpine.datamining.api.impl.db.predictor.ARIMARPredictor;
import com.alpine.datamining.api.impl.db.predictor.AdaboostPredictor;
import com.alpine.datamining.api.impl.db.predictor.CartPredictor;
import com.alpine.datamining.api.impl.db.predictor.DecisionTreePredictor;
import com.alpine.datamining.api.impl.db.predictor.FPGrowthPredictor;
import com.alpine.datamining.api.impl.db.predictor.LinearRegressionPredictor;
import com.alpine.datamining.api.impl.db.predictor.LogisticRegressionPredictorGeneral;
import com.alpine.datamining.api.impl.db.predictor.NaiveBayesPredictor;
import com.alpine.datamining.api.impl.db.predictor.NeuralNetworkPredictor;
import com.alpine.datamining.api.impl.db.predictor.RandomForestPredictor;
import com.alpine.datamining.api.impl.db.predictor.SVDCalculator;
import com.alpine.datamining.api.impl.db.predictor.SVDLanczosCalculator;
import com.alpine.datamining.api.impl.db.predictor.SVDPredictor;
import com.alpine.datamining.api.impl.db.predictor.SVMPredictor;
import com.alpine.datamining.api.impl.db.predictor.WOETableGenerator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopGoodnessOfFitEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopLiftEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopRocEvaluator;

public class AnalyzerVisualTypeMap {

	public static void initAnalyzerVisualTypeMap(HashMap<String, String> analyzerVisualTypeMap) {
		analyzerVisualTypeMap
				.put(LiftGeneralEvaluator.class.getName(),
						"com.alpine.datamining.api.impl.visual.LIFTImageVisualizationType");
		analyzerVisualTypeMap
				.put(ROCGeneralEvaluator.class.getName(),
						"com.alpine.datamining.api.impl.visual.ROCImageVisualizationType");
		analyzerVisualTypeMap
				.put(GoodnessOfFitEvaluator.class.getName(),
						"com.alpine.datamining.api.impl.visual.GoodnessOfFitTableVisualizationType");

		analyzerVisualTypeMap
				.put(
						DecisionTreePredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.DecisionTreePredictTableVisualizationType");
		
		analyzerVisualTypeMap
		.put(
				RandomForestPredictor.class.getName(),
				"com.alpine.datamining.api.impl.visual.DecisionTreePredictTableVisualizationType");
		
		analyzerVisualTypeMap
				.put(
						LinearRegressionPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.LinearRegressionPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(
						LogisticRegressionPredictorGeneral.class.getName(),
						"com.alpine.datamining.api.impl.visual.LogisticRegressionPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(NaiveBayesPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.NaiveBayesPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(
						NeuralNetworkPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.NeuralNetworkPredictionTableVisualizationType");
		analyzerVisualTypeMap
				.put(CartPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.CartTreePredictVisualizationType");
		analyzerVisualTypeMap
				.put(
						FPGrowthPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.AssociationPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(
						ARIMARPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.TimeSeriesPredictShapeVisualizationType"
								+ ","
								+ "com.alpine.datamining.api.impl.visual.TimeSeriesPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(SVMPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.SVMPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(SVDPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.SVDPredictTableVisualizationType");
		analyzerVisualTypeMap
				.put(
						VariableSelectionAnalyzer.class.getName(),
						"com.alpine.datamining.api.impl.visual.VariableSelectionTextAndTableVisualizationType");
		analyzerVisualTypeMap
				.put(SVDCalculator.class.getName(),
						"com.alpine.datamining.api.impl.visual.SVDCalculatorTableVisualizationType");
		analyzerVisualTypeMap
				.put(SVDLanczosCalculator.class.getName(),
						"com.alpine.datamining.api.impl.visual.SVDCalculatorTableVisualizationType");
		analyzerVisualTypeMap
				.put(AdaboostPredictor.class.getName(),
						"com.alpine.datamining.api.impl.visual.AdaboostTableVisualizationType");
		analyzerVisualTypeMap
				.put(WOETableGenerator.class.getName(),
						"com.alpine.datamining.api.impl.visual.WOETableVisualizationType");
		analyzerVisualTypeMap
		.put(HadoopRocEvaluator.class.getName(),
				"com.alpine.datamining.api.impl.visual.ROCImageVisualizationType");
		analyzerVisualTypeMap
		.put(HadoopLiftEvaluator.class.getName(),
				"com.alpine.datamining.api.impl.visual.LIFTImageVisualizationType");
		analyzerVisualTypeMap
		.put(HadoopGoodnessOfFitEvaluator.class.getName(),
				"com.alpine.datamining.api.impl.visual.GoodnessOfFitTableVisualizationType");
	}

}
