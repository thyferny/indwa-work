/**
 * ClassName AnalyzerConfigMap.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.HashMap;

import com.alpine.datamining.api.impl.DBTableSelectorConfig;
import com.alpine.datamining.api.impl.ModelWrapperConfig;
import com.alpine.datamining.api.impl.algoconf.*;
import com.alpine.datamining.api.impl.algoconf.hadoop.CopytoHadoopConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
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
import com.alpine.datamining.api.impl.db.predictor.DecisionTreePredictor;
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

public class AnalyzerConfigMap {

	public static void initAnalyzerConfigMap(HashMap<String,String> analyzerConfigMap) {
		// need know the analyzer and config class
		// will fill parameter use relection
		analyzerConfigMap.put(TableJoinAnalyzer.class.getName(),
				TableJoinConfig.class.getName());

		analyzerConfigMap.put(RandomForestTrainer.class.getName(),
				RandomForestConfig.class.getName());


		analyzerConfigMap.put(VariableAnalyzer.class.getName(),
				VariableConfig.class.getName());
		analyzerConfigMap.put(FilterAnalyzer.class.getName(),
				FilterConfig.class.getName());
		analyzerConfigMap.put(ColumnFilterAnalyzer.class.getName(),
				ColumnFilterConfig.class.getName());
		analyzerConfigMap.put(AggregateAnalyzer.class.getName(),
				AggregateConfig.class.getName());
		analyzerConfigMap.put(RandomSamplingAnalyzer.class.getName(),
				RandomSamplingConfig.class.getName());
		analyzerConfigMap.put(StratifiedSamplingAnalyzer.class.getName(),
				StratifiedSamplingConfig.class.getName());
		analyzerConfigMap.put(SampleSelectorAnalyzer.class.getName(),
				SampleSelectorConfig.class.getName());

		analyzerConfigMap.put(EngineModelWrapperAnalyzer.class.getName(),
				ModelWrapperConfig.class.getName());
		analyzerConfigMap.put(DBTableSelector.class.getName(),
				DBTableSelectorConfig.class.getName());
		analyzerConfigMap.put(IntegerToTextTransformationAnalyzer.class
				.getName(), IntegerToTextTransformConfig.class.getName());
		analyzerConfigMap.put(FPGrowthAnalyzer.class.getName(),
				FPGrowthConfig.class.getName());
		analyzerConfigMap.put(CorrelationAnalysisAnalyzer.class.getName(),
				CorrelationAnalysisConfig.class.getName());
		analyzerConfigMap.put(FrequencyAnalysisAnalyzer.class.getName(),
				FrequencyAnalysisConfig.class.getName());
		analyzerConfigMap.put(HistogramAnalysisAnalyzer.class.getName(),
				HistogramAnalysisConfig.class.getName());
		analyzerConfigMap.put(PivotTableAnalyzer.class.getName(),
				PivotTableConfig.class.getName());
		analyzerConfigMap.put(RecommendationAnalyzer.class.getName(),
				RecommendationConfig.class.getName());
		analyzerConfigMap.put(RecommendationEvaluationAnalyzer.class.getName(),
				RecommendationEvaluationConfig.class.getName());
		analyzerConfigMap.put(ReplaceNullAnalyzer.class.getName(),
				ReplaceNullConfig.class.getName());
		analyzerConfigMap.put(HadoopNullValueReplaceAnalyzer .class.getName(),
				HadoopReplaceNullConfig.class.getName());
		analyzerConfigMap.put(InformationValueAnalyzer.class.getName(),
				InformationValueAnalysisConfig.class.getName());
		analyzerConfigMap.put(KMeansAnalyzer.class.getName(),
				ClusterKMeansConfig.class.getName());

		analyzerConfigMap.put(NormalizationAnalyzer.class.getName(),
				NormalizationConfig.class.getName());
		analyzerConfigMap.put(ValueAnalysisAnalyzer.class.getName(),
				ValueAnalysisConfig.class.getName());
		analyzerConfigMap.put(HadoopValueAnalysisAnalyzer.class.getName(),
				ValueAnalysisConfig.class.getName());
		analyzerConfigMap.put(HadoopFrequencyAnalysisAnalyzer.class.getName(),
				FrequencyAnalysisConfig.class.getName());

		analyzerConfigMap.put(TableAnalysisAnalyzer.class.getName(),
				BarChartAnalysisConfig.class.getName());

		analyzerConfigMap.put(DecisionTreePredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(CartPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(RandomForestPredictor.class.getName(),
				PredictorConfig.class.getName());

		analyzerConfigMap.put(LinearRegressionPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(LogisticRegressionPredictorGeneral.class
				.getName(), PredictorConfig.class.getName());
		analyzerConfigMap.put(NaiveBayesPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(NeuralNetworkPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(FPGrowthPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(ARIMARPredictor.class.getName(),
				ARIMARPredictorConfig.class.getName());
		analyzerConfigMap.put(SVMPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(SVDPredictor.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(SVDCalculator.class.getName(),
				SVDCalculatorConfig.class.getName());
		analyzerConfigMap.put(SVDLanczosCalculator.class.getName(),
				SVDLanczosCalculatorConfig.class.getName());
		analyzerConfigMap.put(AdaboostPredictor.class.getName(),
				PredictorConfig.class.getName());

		analyzerConfigMap.put(DecisionTreeTrainer.class.getName(),
				DecisionTreeConfig.class.getName());
		analyzerConfigMap.put(CartTrainer.class.getName(), CartConfig.class
				.getName());
		analyzerConfigMap.put(LinearRegressionTrainer.class.getName(),
				LinearRegressionConfig.class.getName());
		analyzerConfigMap.put(LogisticRegressionTrainerGeneral.class.getName(),
				LogisticRegressionConfigGeneral.class.getName());

		analyzerConfigMap.put(NaiveBayesTrainer.class.getName(),
				NaiveBayesConfig.class.getName());
		analyzerConfigMap.put(UnivariateVariable.class.getName(),
				LogisticRegressionConfigGeneral.class.getName());
		analyzerConfigMap.put(NeuralNetworkTrainer.class.getName(),
				NeuralNetworkConfig.class.getName());
		analyzerConfigMap.put(ARIMARTrainer.class.getName(), ARIMAConfig.class
				.getName());

		analyzerConfigMap.put(LiftGeneralEvaluator.class.getName(),
				EvaluatorConfig.class.getName());
		analyzerConfigMap.put(ROCGeneralEvaluator.class.getName(),
				EvaluatorConfig.class.getName());
		analyzerConfigMap.put(GoodnessOfFitEvaluator.class.getName(),
				EvaluatorConfig.class.getName());

		analyzerConfigMap.put(SQLAnalyzer.class.getName(),
				SQLAnalysisConfig.class.getName());

		analyzerConfigMap.put(SVMClassificationTrainer.class.getName(),
				SVMClassificationConfig.class.getName());
		analyzerConfigMap.put(SVMNoveltyDetectionTrainer.class.getName(),
				SVMNoveltyDetectionConfig.class.getName());
		analyzerConfigMap.put(SVMRegressionTrainer.class.getName(),
				SVMRegressionConfig.class.getName());
		analyzerConfigMap.put(VariableSelectionAnalyzer.class.getName(),
				VariableSelectionConfig.class.getName());
		analyzerConfigMap.put(CustomizedOperationAnalyzer.class.getName(),
				CustomziedConfig.class.getName());
		analyzerConfigMap.put(SVDTrainer.class.getName(), SVDConfig.class
				.getName());
		analyzerConfigMap.put(SVDLanczosTrainer.class.getName(),
				SVDLanczosConfig.class.getName());
		analyzerConfigMap.put(AdaboostTrainer.class.getName(),
				AdaboostConfig.class.getName());
		analyzerConfigMap.put(PCAAnalyzer.class.getName(), PCAConfig.class
				.getName());
		analyzerConfigMap.put(WOETrainer.class.getName(),
				WeightOfEvidenceConfig.class.getName());
		analyzerConfigMap.put(WOETableGenerator.class.getName(),
				PredictorConfig.class.getName());
		analyzerConfigMap.put(TableScatterMatrixAnalyzer.class.getName(),
				TableScatterMatrixConfig.class.getName());
		analyzerConfigMap.put(TableSetAnalyzer.class.getName(),
				TableSetConfig.class.getName());
		analyzerConfigMap.put(PLDATrainer.class.getName(), PLDAConfig.class
				.getName());
		analyzerConfigMap.put(PLDAPredictor.class.getName(),
				PLDAPredictConfig.class.getName());
		analyzerConfigMap.put(EMTrainer.class.getName(),
				EMConfig.class.getName());
		analyzerConfigMap.put(EMClusterPredictor.class.getName(),
				PredictorConfig.class.getName());

		analyzerConfigMap.put(HadoopFileSelector.class.getName(),
				HadoopFileSelectorConfig.class.getName());
		analyzerConfigMap.put(CopytoHadoopAnalyzer.class.getName(),
				CopytoHadoopConfig.class.getName());
		analyzerConfigMap.put(HadoopRowFilterAnalyzer.class.getName(),
				HadoopRowFilterConfig.class.getName());
		analyzerConfigMap.put(HadoopAggregaterAnalyzer.class.getName(),
				HadoopAggregaterConfig.class.getName());
		analyzerConfigMap.put(HadoopVariableAnalyzer.class.getName(),
				HadoopVariableConfig.class.getName());
		analyzerConfigMap.put(HadoopBarChartAnalyzer.class.getName(),
				BarChartAnalysisConfig.class.getName());
        analyzerConfigMap.put(HadoopBoxPlotAnalyzer.class.getName(),
                TableBoxAndWhiskerConfig.class.getName());
        analyzerConfigMap.put(HadoopHistogramAnalyzer.class.getName(),
				HistogramAnalysisConfig.class.getName());
		analyzerConfigMap.put(HadoopJoinAnalyzer.class.getName(),
				HadoopJoinConfig.class.getName());
		analyzerConfigMap.put(HadoopUnionAnalyzer.class.getName(),
				HadoopUnionConfig.class.getName());
		analyzerConfigMap.put(HadoopScatterPlotMatrixAnalyzer.class.getName(),
				TableScatterMatrixConfig.class.getName());
		analyzerConfigMap.put(HadoopRandomSamplingAnalyzer.class.getName(),
				HadoopRandomSamplingConfig.class.getName());
		analyzerConfigMap.put(HadoopNormalizationAnalyzer.class.getName(),
				HadoopNormalizationConfig.class.getName());
		analyzerConfigMap.put(HadoopKmeansAnalyzer.class.getName(),
				HadoopKMeansConfig.class.getName());
		analyzerConfigMap.put(HadoopSampleSelectorAnalyzer.class.getName(),
				HadoopSampleSelectorConfig.class.getName());
		analyzerConfigMap.put(HadoopLinearRegressionTrainer.class.getName(),
				HadoopLinearTrainConfig.class.getName());
		analyzerConfigMap.put(HadoopLinearRegressionPredictor.class.getName(),
				HadoopPredictorConfig.class.getName());
		analyzerConfigMap.put(CopyToDBAnalyzer.class.getName(),
				CopyToDBConfig.class.getName());
		analyzerConfigMap.put(HadoopRocEvaluator.class.getName(),
				EvaluatorConfig.class.getName());
		analyzerConfigMap.put(HadoopLiftEvaluator.class.getName(),
				EvaluatorConfig.class.getName());
        analyzerConfigMap.put(HadoopConfusionEvaluator.class.getName(),
                EvaluatorConfig.class.getName());
		analyzerConfigMap.put(HadoopPigExecuteAnalyzer.class.getName(),
				HadoopPigExecuteConfig.class.getName());
		analyzerConfigMap.put(HadoopPivotAnalyzer.class.getName(),
				HadoopPivotConfig.class.getName());
		analyzerConfigMap.put(HadoopColumnFilterAnalyzer.class.getName(),
				HadoopColumnFilterConfig.class.getName());

		analyzerConfigMap.put(TableUnivariateAnalyzer.class.getName(), TableUnivariateConfig.class
				.getName());
		analyzerConfigMap.put(TableScatterAnalyzer.class.getName(), TableScatterConfig.class
				.getName());
		analyzerConfigMap.put(TableBoxAndWhiskerAnalyzer.class.getName(), TableBoxAndWhiskerConfig.class
				.getName());
		analyzerConfigMap.put(HadoopDecisionTreeTrainer.class.getName(),
				HadoopDecisionTrainConfig.class.getName());
		analyzerConfigMap.put(HadoopDecisionTreePredictor.class.getName(),
				HadoopPredictorConfig.class.getName());

		analyzerConfigMap.put(HadoopARIMATrainer.class.getName(),
				HadoopTimeSeriesConfig.class.getName());
		analyzerConfigMap.put(HadoopLogisticRegressionTrainer.class.getName(),
				HadoopLogisticRegressionConfig.class.getName());
		analyzerConfigMap.put(HadoopLogisticRegressionPredictor.class.getName(),
				HadoopPredictorConfig.class.getName());
		analyzerConfigMap.put(HadoopTimeSeriesPredictor.class.getName(),
				TimeSeriesHadoopPredictorConfig.class.getName());
		analyzerConfigMap.put(HadoopGoodnessOfFitEvaluator.class.getName(),
				EvaluatorConfig.class.getName());
        analyzerConfigMap.put(HadoopVariableSelectionAnalyzer.class.getName(),
                HadoopVariableSelectionConfig.class.getName());
        analyzerConfigMap.put(HadoopNaiveBayesTrainer.class.getName(),
                NaiveBayesConfig.class.getName());
        analyzerConfigMap.put(HadoopNaiveBayesPredictor.class.getName(),
                HadoopPredictorConfig.class.getName());

    }

}