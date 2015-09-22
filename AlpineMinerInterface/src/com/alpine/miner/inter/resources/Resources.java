/**
 * ClassName Resources.java
 *
 * Version information: 1.00
 *
 * Data: 2011/04/06
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.inter.resources;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.adaboost.AdaboostPredictOperator;
import com.alpine.miner.workflow.operator.association.AssociationOperator;
import com.alpine.miner.workflow.operator.clustering.EMClusteringOperator;
import com.alpine.miner.workflow.operator.clustering.EMClusteringPredictOperator;
import com.alpine.miner.workflow.operator.clustering.KMeansOperator;
import com.alpine.miner.workflow.operator.customize.CustomizedOperator;
import com.alpine.miner.workflow.operator.datasource.DbTableOperator;
import com.alpine.miner.workflow.operator.datasource.TableJoinOperator;
import com.alpine.miner.workflow.operator.decisiontree.CartOperator;
import com.alpine.miner.workflow.operator.decisiontree.DecisionTreeOperator;
import com.alpine.miner.workflow.operator.decisiontree.RandomForestOperator;
import com.alpine.miner.workflow.operator.decisiontree.RandomForestPredictOperator;
import com.alpine.miner.workflow.operator.decisiontree.TreePredictOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.field.AggregateOperator;
import com.alpine.miner.workflow.operator.field.BarChartAnalysisOperator;
import com.alpine.miner.workflow.operator.field.BoxAndWhiskerOperator;
import com.alpine.miner.workflow.operator.field.ColumnFilterOperator;
import com.alpine.miner.workflow.operator.field.CorrelationAnalysisOperator;
import com.alpine.miner.workflow.operator.field.FilterOperator;
import com.alpine.miner.workflow.operator.field.FrequencyAnalysisOperator;
import com.alpine.miner.workflow.operator.field.HistogramOperator;
import com.alpine.miner.workflow.operator.field.InformationValueAnalysisOperator;
import com.alpine.miner.workflow.operator.field.IntegerToTextOperator;
import com.alpine.miner.workflow.operator.field.NormalizationOperator;
import com.alpine.miner.workflow.operator.field.PivotOperator;
import com.alpine.miner.workflow.operator.field.ReplaceNullOperator;
import com.alpine.miner.workflow.operator.field.ScatterMatrixOperator;
import com.alpine.miner.workflow.operator.field.ScatterPlotOperator;
import com.alpine.miner.workflow.operator.field.TableSetOperator;
import com.alpine.miner.workflow.operator.field.UnivariateExplorerOperator;
import com.alpine.miner.workflow.operator.field.UnivariateOperator;
import com.alpine.miner.workflow.operator.field.ValueAnalysisOperator;
import com.alpine.miner.workflow.operator.field.VariableOperator;
import com.alpine.miner.workflow.operator.field.variableselection.VariableSelectionAnalysisOperator;
import com.alpine.miner.workflow.operator.hadoop.*;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionOperator;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionPredictOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionPredictOperator;
import com.alpine.miner.workflow.operator.logisticregression.woe.WOEOperator;
import com.alpine.miner.workflow.operator.logisticregression.woe.WOETableGeneratorOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.model.evaluator.GoodnessOfFitOperator;
import com.alpine.miner.workflow.operator.model.evaluator.LIFTOperator;
import com.alpine.miner.workflow.operator.model.evaluator.ROCOperator;
import com.alpine.miner.workflow.operator.naivebayes.NaiveBayesOperator;
import com.alpine.miner.workflow.operator.naivebayes.NaiveBayesPredictOperator;
import com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkOperator;
import com.alpine.miner.workflow.operator.neuralNetwork.NeuralNetworkPredictOperator;
import com.alpine.miner.workflow.operator.pca.PCAOperator;
import com.alpine.miner.workflow.operator.plda.PLDAPredictOperator;
import com.alpine.miner.workflow.operator.plda.PLDATrainerOperator;
import com.alpine.miner.workflow.operator.sampling.RandomSamplingOperator;
import com.alpine.miner.workflow.operator.sampling.SampleSelectorOperator;
import com.alpine.miner.workflow.operator.sampling.StratifiedSamplingOperator;
import com.alpine.miner.workflow.operator.solutions.ProductRecommendationEvaluationOperator;
import com.alpine.miner.workflow.operator.solutions.ProductRecommendationOperator;
import com.alpine.miner.workflow.operator.structual.NoteOperator;
import com.alpine.miner.workflow.operator.structual.SubFlowOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosCalculatorOperator;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import com.alpine.miner.workflow.operator.svm.SVMClassificationOperator;
import com.alpine.miner.workflow.operator.svm.SVMNoveltyDetectionOperator;
import com.alpine.miner.workflow.operator.svm.SVMPredictOperator;
import com.alpine.miner.workflow.operator.svm.SVMRegressionOperator;
import com.alpine.miner.workflow.operator.timeseries.TimeSeriesOperator;
import com.alpine.miner.workflow.operator.timeseries.TimeSeriesPredictOperator;

public class Resources {
	public static final HashMap<String,EngineModel> retrainHash = new LinkedHashMap<String,EngineModel>();
	private static final HashMap<String,String> operatorMap = new HashMap<String,String>();
	private static final HashMap<String,String> operatorNamingMap = new HashMap<String,String>();
	private static final HashMap<String,String> hadoopOperatorMap = new HashMap<String,String>();
	static{
		
		operatorMap.put(getLastPart(HadoopBoxAndWiskerOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopBoxAndWiskerOperator");

		operatorMap.put(getLastPart(SubFlowOperator.class.getName()), "com.alpine.miner.workflow.runoperator.structual.SubFlowOperator");

		operatorMap.put(getLastPart(RandomForestOperator.class.getName()), "com.alpine.miner.workflow.runoperator.decisiontree.RandomForestOperator");

		operatorMap.put(getLastPart(RandomForestPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.decisiontree.RandomForestPredictOperator");

		operatorMap.put(getLastPart(NoteOperator.class.getName()), "com.alpine.miner.workflow.runoperator.structual.NoteOperator");

		operatorMap.put(getLastPart(DbTableOperator.class.getName()), "com.alpine.miner.workflow.runoperator.datasource.DbTableOperator");

		operatorMap.put(getLastPart(PLDATrainerOperator.class.getName()), "com.alpine.miner.workflow.runoperator.plda.PLDATrainerOperator");		
		operatorMap.put(getLastPart(PLDAPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.plda.PLDAPredictOperator");		

		operatorMap.put(getLastPart(TableSetOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.TableSetOperator");		
		operatorMap.put(getLastPart(AggregateOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.AggregateOperator");
		operatorMap.put(getLastPart(PivotOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.PivotOperator");
		operatorMap.put(getLastPart(TableJoinOperator.class.getName()), "com.alpine.miner.workflow.runoperator.datasource.TableJoinOperator");
		operatorMap.put(getLastPart(FilterOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.FilterOperator");
		operatorMap.put(getLastPart(ColumnFilterOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.ColumnFilterOperator");	
		operatorMap.put(getLastPart(NormalizationOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.NormalizationOperator");
		operatorMap.put(getLastPart(IntegerToTextOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.IntegerToTextOperator");
		operatorMap.put(getLastPart(ReplaceNullOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.ReplaceNullOperator");
		operatorMap.put(getLastPart(VariableOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.VariableOperator");

		operatorMap.put(getLastPart(BarChartAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.BarChartAnalysisOperator");
		operatorMap.put(getLastPart(CorrelationAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.CorrelationAnalysisOperator");
		operatorMap.put(getLastPart(FrequencyAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.FrequencyAnalysisOperator");
		operatorMap.put(getLastPart(HistogramOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.HistogramOperator");
		operatorMap.put(getLastPart(InformationValueAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.InformationValueAnalysisOperator");
		operatorMap.put(getLastPart(UnivariateOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.UnivariateOperator");
		operatorMap.put(getLastPart(ValueAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.ValueAnalysisOperator");
		operatorMap.put(getLastPart(VariableSelectionAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.variableselection.VariableSelectionAnalysisOperator");

		operatorMap.put(getLastPart(AssociationOperator.class.getName()), "com.alpine.miner.workflow.runoperator.association.AssociationOperator");

		operatorMap.put(getLastPart(KMeansOperator.class.getName()), "com.alpine.miner.workflow.runoperator.clustering.KMeansOperator");
		operatorMap.put(getLastPart(CartOperator.class.getName()), "com.alpine.miner.workflow.runoperator.decisiontree.CartOperator");
		operatorMap.put(getLastPart(DecisionTreeOperator.class.getName()), "com.alpine.miner.workflow.runoperator.decisiontree.DecisionTreeOperator");
		operatorMap.put(getLastPart(TreePredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.decisiontree.TreePredictOperator");
		operatorMap.put(getLastPart(LinearRegressionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.linearregression.LinearRegressionOperator");
		operatorMap.put(getLastPart(LinearRegressionPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.linearregression.LinearRegressionPredictOperator");
		operatorMap.put(getLastPart(LogisticRegressionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.logisticregression.LogisticRegressionOperator");
		operatorMap.put(getLastPart(LogisticRegressionPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.logisticregression.LogisticRegressionPredictOperator");
		operatorMap.put(getLastPart(ModelOperator.class.getName()), "com.alpine.miner.workflow.runoperator.model.ModelOperator");
		operatorMap.put(getLastPart(NaiveBayesOperator.class.getName()), "com.alpine.miner.workflow.runoperator.naivebayes.NaiveBayesOperator");
		operatorMap.put(getLastPart(NaiveBayesPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.naivebayes.NaiveBayesPredictOperator");
		operatorMap.put(getLastPart(NeuralNetworkOperator.class.getName()), "com.alpine.miner.workflow.runoperator.neuralNetwork.NeuralNetworkOperator");
		operatorMap.put(getLastPart(NeuralNetworkPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.neuralNetwork.NeuralNetworkPredictOperator");
		operatorMap.put(getLastPart(SVMClassificationOperator.class.getName()), "com.alpine.miner.workflow.runoperator.svm.SVMClassificationOperator");
		operatorMap.put(getLastPart(SVMNoveltyDetectionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.svm.SVMNoveltyDetectionOperator");
		operatorMap.put(getLastPart(SVMRegressionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.svm.SVMRegressionOperator");
		operatorMap.put(getLastPart(SVMPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.svm.SVMPredictOperator");

		operatorMap.put(getLastPart(SVDLanczosOperator.class.getName()), "com.alpine.miner.workflow.runoperator.svd.SVDLanczosOperator");
		operatorMap.put(getLastPart(SVDLanczosCalculatorOperator.class.getName()), "com.alpine.miner.workflow.runoperator.svd.SVDLanczosCalculatorOperator");

		operatorMap.put(getLastPart(GoodnessOfFitOperator.class.getName()), "com.alpine.miner.workflow.runoperator.model.evaluator.GoodnessOfFitOperator");
		operatorMap.put(getLastPart(LIFTOperator.class.getName()), "com.alpine.miner.workflow.runoperator.model.evaluator.LIFTOperator");
		operatorMap.put(getLastPart(ROCOperator.class.getName()), "com.alpine.miner.workflow.runoperator.model.evaluator.ROCOperator");

		operatorMap.put(getLastPart(RandomSamplingOperator.class.getName()), "com.alpine.miner.workflow.runoperator.sampling.RandomSamplingOperator");
		operatorMap.put(getLastPart(SampleSelectorOperator.class.getName()), "com.alpine.miner.workflow.runoperator.sampling.SampleSelectorOperator");
		operatorMap.put(getLastPart(StratifiedSamplingOperator.class.getName()), "com.alpine.miner.workflow.runoperator.sampling.StratifiedSamplingOperator");

		operatorMap.put(getLastPart(ProductRecommendationOperator.class.getName()), "com.alpine.miner.workflow.runoperator.solutions.ProductRecommendationOperator");
		operatorMap.put(getLastPart(ProductRecommendationEvaluationOperator.class.getName()), "com.alpine.miner.workflow.runoperator.solutions.ProductRecommendationEvaluationOperator");

		operatorMap.put(getLastPart(TimeSeriesOperator.class.getName()), "com.alpine.miner.workflow.runoperator.timeseries.TimeSeriesOperator");
		operatorMap.put(getLastPart(TimeSeriesPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.timeseries.TimeSeriesPredictOperator");

		operatorMap.put(getLastPart(SQLExecuteOperator.class.getName()), "com.alpine.miner.workflow.runoperator.execute.SQLExecuteOperator");

		operatorMap.put(getLastPart(AdaboostOperator.class.getName()), "com.alpine.miner.workflow.runoperator.adaboost.AdaboostOperator");
		operatorMap.put(getLastPart(AdaboostPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.adaboost.AdaboostPredictOperator");

		operatorMap.put(getLastPart(PCAOperator.class.getName()), "com.alpine.miner.workflow.runoperator.pca.PCAOperator");
		operatorMap.put(getLastPart(WOEOperator.class.getName()), "com.alpine.miner.workflow.runoperator.logisticregression.woe.WOEOperator");
		operatorMap.put(getLastPart(WOETableGeneratorOperator.class.getName()), "com.alpine.miner.workflow.runoperator.logisticregression.woe.WOETableGeneratorOperator");
		operatorMap.put(getLastPart(CustomizedOperator.class.getName()), "com.alpine.miner.workflow.runoperator.customize.CustomizedOperator");
		operatorMap.put(getLastPart(ScatterMatrixOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.ScatterMatrixOperator");
		operatorMap.put(getLastPart(UnivariateExplorerOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.UnivariateExplorerOperator");
		operatorMap.put(getLastPart(ScatterPlotOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.ScatterPlotOperator");
		operatorMap.put(getLastPart(BoxAndWhiskerOperator.class.getName()), "com.alpine.miner.workflow.runoperator.field.BoxAndWhiskerOperator");
		operatorMap.put(getLastPart(EMClusteringOperator.class.getName()), "com.alpine.miner.workflow.runoperator.clustering.EMClusteringOperator");
		operatorMap.put(getLastPart(EMClusteringPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.clustering.EMClusteringPredictOperator");
		
		operatorMap.put(getLastPart(HadoopReplaceNullOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopReplaceNullOperator");

		operatorMap.put(getLastPart(CopytoHadoopOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.CopytoHadoopOperator");
		operatorMap.put(getLastPart(CopyToDBOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.CopyToDBOperator");
		operatorMap.put(getLastPart(HadoopFileOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopFileOperator");
		operatorMap.put(getLastPart(HadoopRowFilterOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopRowFilterOperator");
		operatorMap.put(getLastPart(HadoopAggregateOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopAggregateOperator");
		operatorMap.put(getLastPart(HadoopUnionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopUnionOperator");
		operatorMap.put(getLastPart(HadoopVariableOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopVariableOperator");
		operatorMap.put(getLastPart(HadoopBarChartOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopBarChartOperator");

		operatorMap.put(getLastPart(HadoopBoxAndWiskerOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopBoxAndWiskerOperator");
        operatorMap.put(getLastPart(HadoopVariableSelectionAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopVariableSelectionAnalysisOperator");


        operatorMap.put(getLastPart(HadoopHistogramOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopHistogramOperator");
		operatorMap.put(getLastPart(HadoopJoinOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopJoinOperator");
		operatorMap.put(getLastPart(HadoopValueAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopValueAnalysisOperator");
		operatorMap.put(getLastPart(HadoopScatterPlotMatrixOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopScatterPlotMatrixOperator");
		operatorMap.put(getLastPart(HadoopRandomSamplingOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopRandomSamplingOperator");
		operatorMap.put(getLastPart(HadoopFrequencyAnalysisOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopFrequencyAnalysisOperator");
		operatorMap.put(getLastPart(HadoopNormalizationOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopNormalizationOperator");
		operatorMap.put(getLastPart(HadoopKmeansOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopKmeansOperator");
		operatorMap.put(getLastPart(HadoopSampleSelectorOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopSampleSelectorOperator");
		operatorMap.put(getLastPart(HadoopLinearRegressionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopLinearRegressionOperator");
		operatorMap.put(getLastPart(HadoopLinearRegressionPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopLinearRegressionPredictOperator");
		operatorMap.put(getLastPart(HadoopLogisticRegressionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopLogisticRegressionOperator");
		operatorMap.put(getLastPart(HadoopDecisionTreeOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopDecisionTreeOperator");
		operatorMap.put(getLastPart(HadoopDecisionTreePredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopDecisionTreePredictOperator");

		operatorMap.put(getLastPart(HadoopLogisticRegressionPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopLogisticRegressionPredictOperator");
		operatorMap.put(getLastPart(HadoopROCOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopROCOperator");
		operatorMap.put(getLastPart(HadoopLiftOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopLiftOperator");
		operatorMap.put(getLastPart(HadoopGoodnessOfFitOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopGoodnessOfFitOperator");
		operatorMap.put(getLastPart(HadoopPigExecuteOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopPigExecuteOperator");
		operatorMap.put(getLastPart(HadoopPivotOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopPivotOperator");
		operatorMap.put(getLastPart(HadoopColumnFilterOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopColumnFilterOperator");
		operatorMap.put(getLastPart(HadoopTimeSeriesOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopTimeSeriesOperator");
		operatorMap.put(getLastPart(HadoopTimeSeriesPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopTimeSeriesPredictOperator");
        operatorMap.put(getLastPart(HadoopNaiveBayesOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopNaiveBayesOperator");
        operatorMap.put(getLastPart(HadoopNaiveBayesPredictOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopNaiveBayesPredictOperator");
        operatorMap.put(getLastPart(HadoopConfusionOperator.class.getName()), "com.alpine.miner.workflow.runoperator.hadoop.HadoopConfusionOperator");



        operatorNamingMap.put(KMeansOperator.class.getCanonicalName(), "kmean");
		operatorNamingMap.put(AssociationOperator.class.getCanonicalName(), "assoc");
		operatorNamingMap.put(LinearRegressionPredictOperator.class.getCanonicalName(), "linrp");
		operatorNamingMap.put(LogisticRegressionPredictOperator.class.getCanonicalName(), "logrp");
		operatorNamingMap.put(EMClusteringPredictOperator.class.getCanonicalName(), "emcp");
		operatorNamingMap.put(NaiveBayesPredictOperator.class.getCanonicalName(), "nbp");
		operatorNamingMap.put(NeuralNetworkPredictOperator.class.getCanonicalName(), "nnp");
		operatorNamingMap.put(TreePredictOperator.class.getCanonicalName(), "treep");
		operatorNamingMap.put(HadoopDecisionTreePredictOperator.class.getCanonicalName(), "treep");


		operatorNamingMap.put(SVMPredictOperator.class.getCanonicalName(), "svmp");
		operatorNamingMap.put(AdaboostPredictOperator.class.getCanonicalName(), "adabp");
		operatorNamingMap.put(PCAOperator.class.getCanonicalName(), "pca");
		operatorNamingMap.put(SVDLanczosOperator.class.getCanonicalName(), "svd");
		operatorNamingMap.put(SVDLanczosCalculatorOperator.class.getCanonicalName(), "svdc");
		operatorNamingMap.put(RandomSamplingOperator.class.getCanonicalName(), "rsamp");
		operatorNamingMap.put(StratifiedSamplingOperator.class.getCanonicalName(), "ssamp");
		operatorNamingMap.put(TimeSeriesPredictOperator.class.getCanonicalName(), "timep");
        operatorNamingMap.put(HadoopTimeSeriesPredictOperator.class.getCanonicalName(), "timep");
		operatorNamingMap.put(AggregateOperator.class.getCanonicalName(), "agg");
		operatorNamingMap.put(IntegerToTextOperator.class.getCanonicalName(), "n2t");
		operatorNamingMap.put(ColumnFilterOperator.class.getCanonicalName(), "colfil");
		operatorNamingMap.put(ReplaceNullOperator.class.getCanonicalName(), "repl");
		operatorNamingMap.put(NormalizationOperator.class.getCanonicalName(), "norm");
		operatorNamingMap.put(PivotOperator.class.getCanonicalName(), "pivot");
		operatorNamingMap.put(FilterOperator.class.getCanonicalName(), "rowfil");
		operatorNamingMap.put(VariableOperator.class.getCanonicalName(), "var");
		operatorNamingMap.put(TableJoinOperator.class.getCanonicalName(), "join");
		operatorNamingMap.put(ProductRecommendationOperator.class.getCanonicalName(), "rec");
		operatorNamingMap.put(PLDATrainerOperator.class.getCanonicalName(), "plda");
		operatorNamingMap.put(PLDAPredictOperator.class.getCanonicalName(), "pldap");
		operatorNamingMap.put(TableSetOperator.class.getCanonicalName(), "tset");
		operatorNamingMap.put(WOETableGeneratorOperator.class.getCanonicalName(), "woet");
		operatorNamingMap.put(CustomizedOperator.class.getCanonicalName(), "udf");

		operatorNamingMap.put(CopyToDBOperator.class.getCanonicalName(), "todb");
		operatorNamingMap.put(HadoopAggregateOperator.class.getCanonicalName(), "agg");
		operatorNamingMap.put(HadoopRowFilterOperator.class.getCanonicalName(), "rowfil");
		operatorNamingMap.put(HadoopReplaceNullOperator.class.getCanonicalName(), "repl");
		operatorNamingMap.put(HadoopVariableOperator.class.getCanonicalName(), "var");
		operatorNamingMap.put(HadoopJoinOperator.class.getCanonicalName(), "join");
		operatorNamingMap.put(HadoopUnionOperator.class.getCanonicalName(), "tset");
		operatorNamingMap.put(HadoopRandomSamplingOperator.class.getCanonicalName(), "rsamp");
		operatorNamingMap.put(HadoopNormalizationOperator.class.getCanonicalName(), "norm");
		operatorNamingMap.put(HadoopKmeansOperator.class.getCanonicalName(), "kmean");
		operatorNamingMap.put(HadoopLogisticRegressionPredictOperator.class.getCanonicalName(), "logrp");
		operatorNamingMap.put(HadoopLinearRegressionPredictOperator.class.getCanonicalName(), "linrp");
        operatorNamingMap.put(HadoopConfusionOperator.class.getCanonicalName(), "hconf");

		operatorNamingMap.put(RandomForestPredictOperator.class.getCanonicalName(), "rfp");
		operatorNamingMap.put(HadoopPigExecuteOperator.class.getCanonicalName(), "pigexec");
		operatorNamingMap.put(HadoopPivotOperator.class.getCanonicalName(), "pivot");
		operatorNamingMap.put(HadoopColumnFilterOperator.class.getCanonicalName(), "colfil");



		hadoopOperatorMap.put(getLastPart(AggregateOperator.class.getName()), getLastPart(HadoopAggregateOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(FilterOperator.class.getName()), getLastPart(HadoopRowFilterOperator.class.getName()));

		hadoopOperatorMap.put(getLastPart(ReplaceNullOperator.class.getName()), getLastPart(HadoopReplaceNullOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(VariableOperator.class.getName()), getLastPart(HadoopVariableOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(BarChartAnalysisOperator.class.getName()), getLastPart(HadoopBarChartOperator.class.getName()));
        hadoopOperatorMap.put(getLastPart(BoxAndWhiskerOperator.class.getName()), getLastPart(HadoopBoxAndWiskerOperator.class.getName()));
        hadoopOperatorMap.put(getLastPart(HistogramOperator.class.getName()), getLastPart(HadoopHistogramOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(TableJoinOperator.class.getName()), getLastPart(HadoopJoinOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(ValueAnalysisOperator.class.getName()), getLastPart(HadoopValueAnalysisOperator.class.getName()));		
		hadoopOperatorMap.put(getLastPart(FrequencyAnalysisOperator.class.getName()), getLastPart(HadoopFrequencyAnalysisOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(ScatterMatrixOperator.class.getName()), getLastPart(HadoopScatterPlotMatrixOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(TableSetOperator.class.getName()), getLastPart(HadoopUnionOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(RandomSamplingOperator.class.getName()), getLastPart(HadoopRandomSamplingOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(NormalizationOperator.class.getName()), getLastPart(HadoopNormalizationOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(KMeansOperator.class.getName()), getLastPart(HadoopKmeansOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(SampleSelectorOperator.class.getName()), getLastPart(HadoopSampleSelectorOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(PivotOperator.class.getName()), getLastPart(HadoopPivotOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(ColumnFilterOperator.class.getName()), getLastPart(HadoopColumnFilterOperator.class.getName()));

		hadoopOperatorMap.put(getLastPart(LogisticRegressionOperator.class.getName()), getLastPart(HadoopLogisticRegressionOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(DecisionTreeOperator.class.getName()), getLastPart(HadoopDecisionTreeOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(TreePredictOperator.class.getName()), getLastPart(HadoopDecisionTreePredictOperator.class.getName()));
        hadoopOperatorMap.put(getLastPart(VariableSelectionAnalysisOperator.class.getName()), getLastPart(HadoopVariableSelectionAnalysisOperator.class.getName()));

        hadoopOperatorMap.put(getLastPart(TimeSeriesPredictOperator.class.getName()), getLastPart(HadoopTimeSeriesPredictOperator.class.getName()));

		hadoopOperatorMap.put(getLastPart(LogisticRegressionPredictOperator.class.getName()), getLastPart(HadoopLogisticRegressionPredictOperator.class.getName()));

		hadoopOperatorMap.put(getLastPart(LinearRegressionOperator.class.getName()), getLastPart(HadoopLinearRegressionOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(LinearRegressionPredictOperator.class.getName()), getLastPart(HadoopLinearRegressionPredictOperator.class.getName()));

		hadoopOperatorMap.put(getLastPart(ROCOperator.class.getName()), getLastPart(HadoopROCOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(LIFTOperator.class.getName()), getLastPart(HadoopLiftOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(GoodnessOfFitOperator.class.getName()), getLastPart(HadoopGoodnessOfFitOperator.class.getName()));
		hadoopOperatorMap.put(getLastPart(TimeSeriesOperator.class.getName()), getLastPart(HadoopTimeSeriesOperator.class.getName()));
        hadoopOperatorMap.put(getLastPart(NaiveBayesOperator.class.getName()), getLastPart(HadoopNaiveBayesOperator.class.getName()));
        hadoopOperatorMap.put(getLastPart(NaiveBayesPredictOperator.class.getName()), getLastPart(HadoopNaiveBayesPredictOperator.class.getName()));

    }

	public static String getOperatorNaming(String operatorName){
		return operatorNamingMap.get(operatorName);
	}

	public static String getHadoopNewOperatorName(String operatorName){
		return hadoopOperatorMap.get(operatorName);
	}

	public static boolean isHadoopOperatorNameExists(String operatorName){
		return hadoopOperatorMap.containsKey(operatorName);
	}

	public static String getOperator(String operatorName){
		return operatorMap.get(operatorName);
	}

	private static String getLastPart(String str){
		String[] temp=str.split("\\.");
		if(temp.length==0)return null;
		return temp[temp.length-1];
	}
}