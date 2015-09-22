/**
 * ClassName OperatorAnalyzerMap.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.HashMap;

import com.alpine.datamining.api.impl.db.DBTableSelector;
import com.alpine.datamining.api.impl.db.association.FPGrowthAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.AggregateAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.ColumnFilterAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.CopyToDBAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.CorrelationAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.FilterAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.FrequencyAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.HistogramAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.InformationValueAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.IntegerToTextTransformationAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.NormalizationAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.PCAAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.PivotTableAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.RandomSamplingAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.ReplaceNullAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.SampleSelectorAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.StratifiedSamplingAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.TableSetAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.ValueAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.VariableAnalyzer;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionAnalyzer;
import com.alpine.datamining.api.impl.db.cluster.KMeansAnalyzer;
import com.alpine.datamining.api.impl.db.customize.CustomizedOperationAnalyzer;
import com.alpine.datamining.api.impl.db.evaluator.GoodnessOfFitEvaluator;
import com.alpine.datamining.api.impl.db.evaluator.LiftGeneralEvaluator;
import com.alpine.datamining.api.impl.db.evaluator.ROCGeneralEvaluator;
import com.alpine.datamining.api.impl.db.execute.SQLAnalyzer;
import com.alpine.datamining.api.impl.db.predictor.ARIMARPredictor;
import com.alpine.datamining.api.impl.db.predictor.AdaboostPredictor;
import com.alpine.datamining.api.impl.db.predictor.CartPredictor;
import com.alpine.datamining.api.impl.db.predictor.EMClusterPredictor;
import com.alpine.datamining.api.impl.db.predictor.FPGrowthPredictor;
import com.alpine.datamining.api.impl.db.predictor.LinearRegressionPredictor;
import com.alpine.datamining.api.impl.db.predictor.LogisticRegressionPredictorGeneral;
import com.alpine.datamining.api.impl.db.predictor.NaiveBayesPredictor;
import com.alpine.datamining.api.impl.db.predictor.NeuralNetworkPredictor;
import com.alpine.datamining.api.impl.db.predictor.PLDAPredictor;
import com.alpine.datamining.api.impl.db.predictor.RandomForestPredictor;
import com.alpine.datamining.api.impl.db.predictor.SVDCalculator;
import com.alpine.datamining.api.impl.db.predictor.SVDLanczosCalculator;
import com.alpine.datamining.api.impl.db.predictor.SVDPredictor;
import com.alpine.datamining.api.impl.db.predictor.SVMPredictor;
import com.alpine.datamining.api.impl.db.predictor.WOETableGenerator;
import com.alpine.datamining.api.impl.db.recommendation.RecommendationAnalyzer;
import com.alpine.datamining.api.impl.db.recommendation.RecommendationEvaluationAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableAnalysisAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableBoxAndWhiskerAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableScatterAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableScatterMatrixAnalyzer;
import com.alpine.datamining.api.impl.db.table.TableUnivariateAnalyzer;
import com.alpine.datamining.api.impl.db.tablejoin.TableJoinAnalyzer;
import com.alpine.datamining.api.impl.db.trainer.ARIMARTrainer;
import com.alpine.datamining.api.impl.db.trainer.AdaboostTrainer;
import com.alpine.datamining.api.impl.db.trainer.CartTrainer;
import com.alpine.datamining.api.impl.db.trainer.DecisionTreeTrainer;
import com.alpine.datamining.api.impl.db.trainer.EMTrainer;
import com.alpine.datamining.api.impl.db.trainer.EngineModelWrapperAnalyzer;
import com.alpine.datamining.api.impl.db.trainer.LinearRegressionTrainer;
import com.alpine.datamining.api.impl.db.trainer.LogisticRegressionTrainerGeneral;
import com.alpine.datamining.api.impl.db.trainer.NaiveBayesTrainer;
import com.alpine.datamining.api.impl.db.trainer.NeuralNetworkTrainer;
import com.alpine.datamining.api.impl.db.trainer.PLDATrainer;
import com.alpine.datamining.api.impl.db.trainer.RandomForestTrainer;
import com.alpine.datamining.api.impl.db.trainer.SVDLanczosTrainer;
import com.alpine.datamining.api.impl.db.trainer.SVDTrainer;
import com.alpine.datamining.api.impl.db.trainer.SVMClassificationTrainer;
import com.alpine.datamining.api.impl.db.trainer.SVMNoveltyDetectionTrainer;
import com.alpine.datamining.api.impl.db.trainer.SVMRegressionTrainer;
import com.alpine.datamining.api.impl.db.trainer.WOETrainer;
import com.alpine.datamining.api.impl.db.variableOptimization.UnivariateVariable;
import com.alpine.datamining.api.impl.hadoop.CopytoHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.HadoopFileSelector;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopAggregaterAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopColumnFilterAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopJoinAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopNormalizationAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopNullValueReplaceAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopPigExecuteAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopPivotAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopRandomSamplingAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopRowFilterAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopSampleSelectorAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopUnionAnalyzer;
import com.alpine.datamining.api.impl.hadoop.attribute.HadoopVariableAnalyzer;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopConfusionEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopGoodnessOfFitEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopLiftEvaluator;
import com.alpine.datamining.api.impl.hadoop.evaluator.HadoopRocEvaluator;
import com.alpine.datamining.api.impl.hadoop.explorer.*;
import com.alpine.datamining.api.impl.hadoop.kmeans.HadoopKmeansAnalyzer;
import com.alpine.datamining.api.impl.hadoop.predictor.*;
import com.alpine.datamining.api.impl.hadoop.trainer.*;

public class OperatorAnalyzerMap {
	public static void initOperatorAnalyzerMap(HashMap<String, String>  operatorAnalyzerMap) { 
		// DataOperationAnalyzer
 
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.datasource.TableJoinOperator",
						TableJoinAnalyzer.class.getName());
		
		
		operatorAnalyzerMap
		.put(
				"com.alpine.miner.gef.runoperator.decisiontree.RandomForestOperator",
				RandomForestTrainer.class.getName());
		operatorAnalyzerMap
		.put(
				"com.alpine.miner.gef.runoperator.decisiontree.RandomForestPredictOperator",
				RandomForestPredictor.class.getName());

		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.VariableOperator",
				VariableAnalyzer.class.getName());

		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.FilterOperator",
				FilterAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.ColumnFilterOperator",
				ColumnFilterAnalyzer.class.getName());

		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.AggregateOperator",
				AggregateAnalyzer.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.sampling.RandomSamplingOperator",
						RandomSamplingAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.sampling.StratifiedSamplingOperator",
						StratifiedSamplingAnalyzer.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.sampling.SampleSelectorOperator",
						SampleSelectorAnalyzer.class.getName());

		// -> model!!! EngineModel...
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.model.ModelOperator",
				EngineModelWrapperAnalyzer.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.association.AssociationOperator",
						FPGrowthAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.association.AssociationPredictOperator",
						FPGrowthPredictor.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.field.BarChartAnalysisOperator",
						TableAnalysisAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.field.CorrelationAnalysisOperator",
						CorrelationAnalysisAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.datasource.DbTableOperator",
				DBTableSelector.class.getName());

		// operatorAnalyzerMap.put("com.alpine.miner.gef.runoperator.field.FieldSelectOperator",TableFieldSelector.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.field.FrequencyAnalysisOperator",
						FrequencyAnalysisAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.HistogramOperator",
				HistogramAnalysisAnalyzer.class.getName());

		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.clustering.KMeansOperator",
				KMeansAnalyzer.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.neuralNetwork.NeuralNetworkOperator",
						NeuralNetworkTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.NormalizationOperator",
				NormalizationAnalyzer.class.getName());

	
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.decisiontree.DecisionTreeOperator",
						DecisionTreeTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.decisiontree.CartOperator",
				CartTrainer.class.getName());
		// operatorAnalyzerMap.put("com.alpine.miner.gef.runoperator.cart.CartPredictOperator",CartPredictor.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.linearregression.LinearRegressionOperator",
						LinearRegressionTrainer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.logisticregression.LogisticRegressionOperator",
						LogisticRegressionTrainerGeneral.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.naivebayes.NaiveBayesOperator",
						NaiveBayesTrainer.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.decisiontree.TreePredictOperator",
						CartPredictor.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.linearregression.LinearRegressionPredictOperator",
						LinearRegressionPredictor.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.logisticregression.LogisticRegressionPredictOperator",
						LogisticRegressionPredictorGeneral.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.naivebayes.NaiveBayesPredictOperator",
						NaiveBayesPredictor.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.neuralNetwork.NeuralNetworkPredictOperator",
						NeuralNetworkPredictor.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.timeseries.TimeSeriesPredictOperator",
						ARIMARPredictor.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.svd.SVDPredictOperator",
				SVDPredictor.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.svd.SVDCalculatorOperator",
				SVDCalculator.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.svd.SVDLanczosCalculatorOperator",
						SVDLanczosCalculator.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.model.evaluator.LIFTOperator",
						LiftGeneralEvaluator.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.model.evaluator.ROCOperator",
				ROCGeneralEvaluator.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.model.evaluator.GoodnessOfFitOperator",
						GoodnessOfFitEvaluator.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.ValueAnalysisOperator",
				ValueAnalysisAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.clustering.EMClusteringOperator",
				EMTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.clustering.EMClusteringPredictOperator", 
				EMClusterPredictor.class.getName());
		operatorAnalyzerMap
		.put("com.alpine.miner.gef.runoperator.hadoop.CopytoHadoopOperator",
				CopytoHadoopAnalyzer.class.getName());
		
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopValueAnalysisOperator",
				HadoopValueAnalysisAnalyzer.class.getName());
     
		operatorAnalyzerMap.put(
						"com.alpine.miner.gef.runoperator.hadoop.HadoopFrequencyAnalysisOperator",
						HadoopFrequencyAnalysisAnalyzer.class.getName());
		
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopReplaceNullOperator",
				HadoopNullValueReplaceAnalyzer.class.getName());
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.UnivariateOperator",
				UnivariateVariable.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.IntegerToTextOperator",
				IntegerToTextTransformationAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.PivotOperator",
				PivotTableAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.solutions.ProductRecommendationOperator",
						RecommendationAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.solutions.ProductRecommendationEvaluationOperator",
						RecommendationEvaluationAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.ReplaceNullOperator",
				ReplaceNullAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.field.InformationValueAnalysisOperator",
						InformationValueAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.timeseries.TimeSeriesOperator",
						ARIMARTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.execute.SQLExecuteOperator",
				SQLAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.svm.SVMClassificationOperator",
						SVMClassificationTrainer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.svm.SVMNoveltyDetectionOperator",
						SVMNoveltyDetectionTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.svm.SVMRegressionOperator",
				SVMRegressionTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.svm.SVMPredictOperator",
				SVMPredictor.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.customize.CustomizedOperator",
						CustomizedOperationAnalyzer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.field.variableselection.VariableSelectionAnalysisOperator",
						VariableSelectionAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.svd.SVDOperator",
				SVDTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.svd.SVDLanczosOperator",
				SVDLanczosTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.adaboost.AdaboostOperator",
				AdaboostTrainer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.adaboost.AdaboostPredictOperator",
						AdaboostPredictor.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.pca.PCAOperator",
				PCAAnalyzer.class.getName());

		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.logisticregression.woe.WOEOperator",
						WOETrainer.class.getName());
		operatorAnalyzerMap
				.put(
						"com.alpine.miner.gef.runoperator.logisticregression.woe.WOETableGeneratorOperator",
						WOETableGenerator.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.ScatterMatrixOperator",
				TableScatterMatrixAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.TableSetOperator",
				TableSetAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.plda.PLDATrainerOperator",
				PLDATrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.plda.PLDAPredictOperator",
				PLDAPredictor.class.getName());
 
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopFileOperator",
				HadoopFileSelector.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopRowFilterOperator",
				HadoopRowFilterAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopAggregateOperator",
				HadoopAggregaterAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopBoxAndWiskerOperator",
				HadoopBoxPlotAnalyzer.class.getName());
        operatorAnalyzerMap.put(
                "com.alpine.miner.gef.runoperator.hadoop.HadoopBarChartOperator",
                HadoopBarChartAnalyzer.class.getName());
        //zy
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopHistogramOperator",
				HadoopHistogramAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopJoinOperator",
				HadoopJoinAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopUnionOperator",
				HadoopUnionAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopRandomSamplingOperator",
				HadoopRandomSamplingAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopNormalizationOperator",
				HadoopNormalizationAnalyzer.class.getName());
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopFrequencyAnalysisOperator",
				HadoopFrequencyAnalysisAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopVariableOperator",
				HadoopVariableAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopScatterPlotMatrixOperator",
				HadoopScatterPlotMatrixAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopKmeansOperator",
				HadoopKmeansAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopSampleSelectorOperator",
				HadoopSampleSelectorAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopLinearRegressionOperator",
				HadoopLinearRegressionTrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopTimeSeriesOperator",
				HadoopARIMATrainer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopLinearRegressionPredictOperator",
				HadoopLinearRegressionPredictor.class.getName());
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopLogisticRegressionOperator",
				HadoopLogisticRegressionTrainer.class.getName());
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopDecisionTreeOperator",
				HadoopDecisionTreeTrainer.class.getName()); 
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopDecisionTreePredictOperator",
				HadoopDecisionTreePredictor.class.getName()); 
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopTimeSeriesPredictOperator",
				HadoopTimeSeriesPredictor.class.getName()); 
		
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopLogisticRegressionPredictOperator",
				HadoopLogisticRegressionPredictor.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopPigExecuteOperator",
				HadoopPigExecuteAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopPivotOperator",
				HadoopPivotAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopColumnFilterOperator",
				HadoopColumnFilterAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.CopyToDBOperator",
				CopyToDBAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopROCOperator",
				HadoopRocEvaluator.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopLiftOperator",
				HadoopLiftEvaluator.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.hadoop.HadoopGoodnessOfFitOperator",
				HadoopGoodnessOfFitEvaluator.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.UnivariateExplorerOperator",
				TableUnivariateAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.ScatterPlotOperator",
				TableScatterAnalyzer.class.getName());
		operatorAnalyzerMap.put(
				"com.alpine.miner.gef.runoperator.field.BoxAndWhiskerOperator",
				TableBoxAndWhiskerAnalyzer.class.getName());
        operatorAnalyzerMap.put(
                "com.alpine.miner.gef.runoperator.hadoop.HadoopVariableSelectionAnalysisOperator",
                HadoopVariableSelectionAnalyzer.class.getName());
        operatorAnalyzerMap.put(
                "com.alpine.miner.gef.runoperator.hadoop.HadoopNaiveBayesOperator",
                HadoopNaiveBayesTrainer.class.getName());
        operatorAnalyzerMap.put(
                "com.alpine.miner.gef.runoperator.hadoop.HadoopNaiveBayesPredictOperator",
                HadoopNaiveBayesPredictor.class.getName());
        operatorAnalyzerMap.put(
                "com.alpine.miner.gef.runoperator.hadoop.HadoopConfusionOperator", HadoopConfusionEvaluator.class.getName());


    }
}
