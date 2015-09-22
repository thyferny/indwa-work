package com.alpine.miner.impls;



/**
 * ClassName:Resources
 * 
 * Author kemp zhang
 * 
 * Version Ver 1.0
 * 
 * Date 2011-3-29
 * 
 * COPYRIGHT 2010 Alpine Solutions. All Rights Reserved.
 */
public class Resources {

	// FlowTree attribute
	public static final String TREE_ID = "id";
	public static final String TREE_NAME = "name";
	public static final String TREE_TYPE = "type";

	// WorkFlow attribute
	public static final String WORKFLOW_UID = "uid";
	public static final String WORKFLOW_NAME = "name";
	public static final String WORKFLOW_CLASSNAME = "classname";
	public static final String WORKFLOW_X = "x";
	public static final String WORKFLOW_Y = "y";
	public static final String WORKFLOW_ICON = "icon";
	public static final String WORKFLOW_ICONS = "icons";

	public static final String WORKFLOW_LINKS = "links";

	public static final String ICON_PATH = "/image/operators/";

	public static final String[][] OPERATOR_CLASSNAME_ICONS = {
        { "DbTableOperator", "dbTable.png", "dbTable_s.png" },
        { "BarChartAnalysisOperator", "dbtableAnalysis.png", "dbtableAnalysis_s.png" },
        { "CorrelationAnalysisOperator", "correlation.png", "correlation_s.png" },
        { "FrequencyAnalysisOperator", "frequencyAnalysis.png", "frequencyAnalysis_s.png" },
        { "HistogramOperator", "histogram.png", "histogram_s.png" },
        { "InformationValueAnalysisOperator", "informationValue.png", "informationValue_s.png" },
        { "UnivariateOperator", "univariate.png", "univariate_s.png" },
        { "ValueAnalysisOperator", "valueAnalysis.png", "valueAnalysis_s.png" },
        { "AggregateOperator", "aggregate.png", "aggregate_s.png" },
        { "CustomizedOperator", "udf.png", "udf_s.png" },
        { "NormalizationOperator", "normalization.png", "normalization_s.png" },
        { "ReplaceNullOperator", "NullReplacement.png", "NullReplacement_s.png" },
        { "IntegerToTextOperator", "integerToText.png", "integerToText_s.png" },
        { "PivotOperator", "pivot.png", "pivot_s.png" },
        { "FilterOperator", "filter.png", "filter_s.png" },
        { "TableJoinOperator", "join.png", "join_s.png" },
        { "VariableOperator", "variable.png", "variable_s.png" },
        { "RandomSamplingOperator", "randomsample.png", "randomsample_s.png" },
        { "StratifiedSamplingOperator", "stratifiedsample.png", "stratifiedsample_s.png" },
        { "SampleSelectorOperator", "sampleSelector.png", "sampleSelector_s.png" },
        { "AssociationOperator", "association.png", "association_s.png" },
        { "CartOperator", "cartTree.png", "cartTree_s.png" },
        { "DecisionTreeOperator", "decisionTree.png", "decisionTree_s.png" },
        { "KMeansOperator", "clustering.png", "clustering_s.png" },
        {"EMClusteringOperator","clustering.png","clustering_s.png"},
        {"EMClusteringPredictOperator","clusteringpredict.png","clusteringpredict_s.png"},
        { "LinearRegressionOperator", "linearRegression.png", "linearRegression_s.png" },
        { "LogisticRegressionOperator", "logisticRegression.png", "logisticRegression_s.png" },
        { "ModelOperator", "model.png", "model_s.png" },
        { "NaiveBayesOperator", "naiveBayes.png", "naiveBayes_s.png" },
        { "NeuralNetworkOperator", "neuralNetwork.png", "neuralNetwork_s.png" },
        { "TimeSeriesOperator", "timeseries.png", "timeseries_s.png" },
        { "LinearRegressionPredictOperator", "linearRegressionPredict.png", "linearRegressionPredict_s.png" },
        { "LogisticRegressionPredictOperator", "logisticRegressionPredict.png", "logisticRegressionPredict_s.png" },
        { "NaiveBayesPredictOperator", "naiveBayesPredict.png", "naiveBayesPredict_s.png" },
        { "NeuralNetworkPredictOperator", "neuralNetworkPredict.png", "neuralNetworkPredict_s.png" },
        { "TimeSeriesPredictOperator", "timeseriespredict.png", "timeseriespredict_s.png" },
        { "TreePredictOperator", "decisionTreePredict.png", "decisionTreePredict_s.png" },
        { "GoodnessOfFitOperator", "goodnessOfFit.png", "goodnessOfFit_s.png" },
        { "LIFTOperator", "lift.png", "lift_s.png" },
        { "ROCOperator", "roc.png", "roc_s.png" },
        { "ProductRecommendationOperator", "productRecommendation.png", "productRecommendation_s.png" },
        { "ProductRecommendationEvaluationOperator",
                "productRecommendationEvaluation.png",
                "productRecommendationEvaluation_s.png" },
        { "SVMNoveltyDetectionOperator", "svm_nd.png", "svm_nd_s.png" },
        { "SVMClassificationOperator", "svm_c.png", "svm_c_s.png" },
        { "SVMRegressionOperator", "svm_r.png", "svm_r_s.png" },
        { "SVMPredictOperator", "svm_p.png", "svm_p_s.png" },
             
        // just added.
		{ "SVDOperator", "svd.png", "svd_s.png" },
		{ "SVDLanczosOperator", "svd.png", "svd_s.png" },
		{ "SVDCalculatorOperator", "svdCalculator.png", "svdCalculator_s.png" },
		{ "SVDLanczosCalculatorOperator", "svdCalculator.png", "svdCalculator_s.png" },
		{ "ColumnFilterOperator", "columnfilter.png", "columnfilter_s.png" },
		{ "VariableSelectionAnalysisOperator", "VariableSelection.png", "VariableSelection_s.png" },
        { "SQLExecuteOperator" , "sqlExecute.png" , "sqlExecute_s.png"  },
        
        // added web3.1
        { "AdaboostOperator" , "adaboost.png" , "adaboost_s.png"  },
        { "AdaboostPredictOperator" , "adaboostpredict.png" , "adaboostpredict_s.png"  },
        { "PCAOperator" , "pca.png" , "pca_s.png"  },
        { "WOEOperator", "woe.png", "woe_s.png"},
        { "WOETableGeneratorOperator", "woepredict.png", "woepredict_s.png"},
        //scatter plot matrix Add by Will
        {"ScatterMatrixOperator","scatterMatrix.png","scatterMatrix_s.png"},
        {"TableSetOperator", "tableset.png","tableset_s.png"},
        {"PLDATrainerOperator", "plda.png","plda_s.png"},
        {"PLDAPredictOperator", "pldapredictor.png","pldapredictor_s.png"},
        //subflow
        {"SubFlowOperator", "subflow.png","subflow_s.png"},
        
        { "ScatterPlotOperator", "histogram.png", "histogram_s.png" },
        { "UnivariateExplorerOperator", "histogram.png", "histogram_s.png" },
        { "BoxAndWhiskerOperator", "histogram.png", "histogram_s.png" },
        //hadoop
		{ "HadoopFileOperator", "hdTable.png", "hdTable_s.png" },
		{ "HadoopRowFilterOperator", "filter.png", "filter_s.png" },
		{ "HadoopVariableOperator", "variable.png", "variable_s.png" },
		{ "HadoopAggregateOperator", "aggregate.png", "aggregate_s.png" },
		{ "HadoopBarChartOperator", "dbtableAnalysis.png", "dbtableAnalysis_s.png" }
    };


	public static final String PARAMETER_NAME = "name";
	public static final String PARAMETER_VALUE = "value";
	public static final String[] PARAMETER_VIEW_FILTER = { "system",
			"userName", "password", "url" };

	public static final String OPERATOR_FINISHED = "operator_finished";
	public static final String OPERATOR_IS_START = "operator_start";
	public static final String PROCESS_FINISHED = "process_finished";
	public static final String PROCESS_STOP = "process_stop";
	public static final String PROCESS_START = "process_start";
	public static final String PROCESS_ERROR = "process_error";
	public static final String AFM = ".afm";
	
	public static final String NO_USER = "NO_USER";
	public static final String BAD_PASSWORD = "BAD_PASSWORD";
	public static final String SESSION_USER = "USER_INFO";
	public static final String SESSION_TIME_STAMP = "TIME_STAMP";
	public static final String SESSION_PERMISSION = "PERMISSION_BLACKLIST";
	public static final String AUTH_TYPE = "AUTH_TYPE";

}
