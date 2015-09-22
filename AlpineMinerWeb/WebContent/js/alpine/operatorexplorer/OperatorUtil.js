/**
 * User: sasher
 * Date: 7/5/12
 * Time: 1:02 PM
 */

define([
    "dojo/i18n!../nls/labels"
],

    function(i18nLabels) {


    var OP_ALL = -1;
    var OP_DATASOURCE = 0;
    var OP_EXPLORATION = 1;
    var OP_TRANFORMATION = 2;
    var OP_SAMPLING = 3;
    var OP_MODEL = 4;
    var OP_SCORING = 5;
    var OP_OTHER = 6;
    var OP_DONTSHOW = 7;

    var IMG_TYPE_SQUARE = 0;
    var IMG_TYPE_HEX = 1;
    var IMG_TYPE_OCT = 2;
    var IMG_TYPE_TRAP_DOWN = 3;
    var IMG_TYPE_TRAP_UP = 4;
    var IMG_TYPE_TRAP_LEFT = 5;
    var IMG_TYPE_TRAP_RIGHT = 6;
    var IMG_TYPE_JEWEL = 7;
    var IMG_TYPE_TRIANGLE = 8;
    var IMG_TYPE_DIAMOND = 9;
    var IMG_TYPE_PENT = 10;
    var IMG_TYPE_TRIANGLE_UPSIDEDOWN = 11;

    var OP_HASH = {};

    dojo.ready(function(){
        /*
        * showhadoop: used for filtering in the operator explorer should be set true for db-type operators with hadoop counterparts, also true for actual hadoop operators for future consideration
        *
        * */
        OP_HASH["DbTableOperator"] ={ key:"DbTableOperator", icon:"dbTable.png", optype: OP_DATASOURCE, imgtype: IMG_TYPE_SQUARE, terminal: false, showhadoop: false};
        OP_HASH["CopyToDBOperator"] ={ key:"CopyToDBOperator", icon:"copyToDB.png", optype: OP_DATASOURCE, imgtype: IMG_TYPE_SQUARE, terminal: false, showhadoop: true};
        OP_HASH["CopytoHadoopOperator"] ={ key:"CopytoHadoopOperator", icon:"copyToHD.png", optype: OP_DATASOURCE, imgtype: IMG_TYPE_SQUARE, terminal: false, showhadoop: true};
        OP_HASH["BarChartAnalysisOperator"] ={ key:"BarChartAnalysisOperator", icon:"dbtableAnalysis.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["CorrelationAnalysisOperator"] ={ key:"CorrelationAnalysisOperator", icon:"correlation.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: false};
        OP_HASH["FrequencyAnalysisOperator"] ={ key:"FrequencyAnalysisOperator",icon: "frequencyAnalysis.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HistogramOperator"] ={ key:"HistogramOperator", icon:"histogram.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["InformationValueAnalysisOperator"] ={ key:"InformationValueAnalysisOperator",icon: "informationValue.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: false};
        OP_HASH["UnivariateOperator"] ={ key:"UnivariateOperator", icon:"univariate.png",  optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT, terminal: true, showhadoop: false};
        OP_HASH["ValueAnalysisOperator"] ={ key:"ValueAnalysisOperator", icon:"valueAnalysis.png", optype: OP_EXPLORATION , imgtype: IMG_TYPE_OCT, terminal: true, showhadoop: true};
        OP_HASH["AggregateOperator"] ={ key:"AggregateOperator", icon:"aggregate.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_UP , terminal: false, showhadoop: true};
        OP_HASH["CustomizedOperator"] ={ key:"CustomizedOperator",icon: "udf.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_DIAMOND, terminal: false, showhadoop: false};
        OP_HASH["NormalizationOperator"] ={ key:"NormalizationOperator", icon:"normalization.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["ReplaceNullOperator"] ={ key:"ReplaceNullOperator",icon: "NullReplacement.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_DOWN , terminal: false, showhadoop: true};
        OP_HASH["IntegerToTextOperator"] ={ key:"IntegerToTextOperator",icon: "integerToText.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_DOWN, terminal: false, showhadoop: false};
        OP_HASH["PivotOperator"] ={ key:"PivotOperator", icon:"pivot.png", optype: OP_TRANFORMATION, imgtype:IMG_TYPE_TRAP_LEFT , terminal: false, showhadoop: true};
        OP_HASH["FilterOperator"] ={ key:"FilterOperator",icon: "filter.png", optype: OP_TRANFORMATION, imgtype:IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["TableJoinOperator"] ={ key:"TableJoinOperator",icon: "join.png", optype: OP_TRANFORMATION,imgtype: IMG_TYPE_TRAP_UP, terminal: false, showhadoop: true};
        OP_HASH["VariableOperator"] ={ key:"VariableOperator", icon:"variable.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_DOWN, terminal: false, showhadoop: true};
        OP_HASH["RandomSamplingOperator"] ={ key:"RandomSamplingOperator",icon: "randomsample.png", optype: OP_SAMPLING, imgtype: IMG_TYPE_JEWEL  , terminal: false, showhadoop: true};
        OP_HASH["StratifiedSamplingOperator"] ={ key:"StratifiedSamplingOperator",icon: "stratifiedsample.png",optype: OP_SAMPLING, imgtype: IMG_TYPE_JEWEL , terminal: false, showhadoop: false};
        OP_HASH["SampleSelectorOperator"] ={ key:"SampleSelectorOperator",icon: "sampleSelector.png",optype: OP_SAMPLING, imgtype: IMG_TYPE_JEWEL  , terminal: false, showhadoop: true};
        OP_HASH["AssociationOperator"] ={ key:"AssociationOperator",icon: "association.png", optype: OP_MODEL, imgtype: IMG_TYPE_HEX, terminal: true, showhadoop: false};
        OP_HASH["CartOperator"] ={ key:"CartOperator",icon: "cartTree.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["DecisionTreeOperator"] ={ key:"DecisionTreeOperator", icon:"decisionTree.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["KMeansOperator"] ={ key:"KMeansOperator", icon:"clustering.png", optype: OP_MODEL, imgtype: IMG_TYPE_HEX, terminal: false, showhadoop: true};
        OP_HASH["LinearRegressionOperator"] ={ key:"LinearRegressionOperator", icon:"linearRegression.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["LogisticRegressionOperator"] ={ key:"LogisticRegressionOperator",icon: "logisticRegression.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX, terminal: false, showhadoop: true};
        OP_HASH["ModelOperator"] ={ key:"ModelOperator", icon:"model.png", optype: OP_OTHER, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["NaiveBayesOperator"] ={ key:"NaiveBayesOperator", icon:"naiveBayes.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["NeuralNetworkOperator"] ={ key:"NeuralNetworkOperator", icon:"neuralNetwork.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["TimeSeriesOperator"] ={ key:"TimeSeriesOperator", icon:"timeseries.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["LinearRegressionPredictOperator"] ={ key:"LinearRegressionPredictOperator",icon: "linearRegressionPredict.png",optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: false, showhadoop: true};
        OP_HASH["LogisticRegressionPredictOperator"] ={ key:"LogisticRegressionPredictOperator", icon:"logisticRegressionPredict.png", optype: OP_SCORING , imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: true};
        OP_HASH["NaiveBayesPredictOperator"] ={ key:"NaiveBayesPredictOperator", icon:"naiveBayesPredict.png", optype: OP_SCORING , imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: true};
        OP_HASH["NeuralNetworkPredictOperator"] ={ key:"NeuralNetworkPredictOperator", icon:"neuralNetworkPredict.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: false};
        OP_HASH["TimeSeriesPredictOperator"] ={ key:"TimeSeriesPredictOperator", icon:"timeseriespredict.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["TreePredictOperator"] ={ key:"TreePredictOperator", icon:"decisionTreePredict.png",optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["GoodnessOfFitOperator"] ={ key:"GoodnessOfFitOperator", icon:"goodnessOfFit.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["LIFTOperator"] ={ key:"LIFTOperator", icon:"lift.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["ROCOperator"] ={ key:"ROCOperator",icon: "roc.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
//    OP_HASH["ProductRecommendationOperator"] ={ key:"ProductRecommendationOperator", icon:"productRecommendation.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_PENT , terminal: false};
//    OP_HASH["ProductRecommendationEvaluationOperator"] ={ key:"ProductRecommendationEvaluationOperator",
//        icon:"productRecommendationEvaluation.png",
//        optype: OP_DONTSHOW, imgtype: IMG_TYPE_PENT , terminal: false};
        OP_HASH["SVMNoveltyDetectionOperator"] ={ key:"SVMNoveltyDetectionOperator",icon: "svm_nd.png", optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["SVMClassificationOperator"] ={ key:"SVMClassificationOperator", icon:"svm_c.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["SVMRegressionOperator"] ={ key:"SVMRegressionOperator", icon:"svm_r.png", optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["SVMPredictOperator"] ={ key:"SVMPredictOperator", icon:"svm_p.png",optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: false};

        OP_HASH["SVDOperator"] ={ key:"SVDOperator", icon:"svd.png",optype: OP_DONTSHOW, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["SVDLanczosOperator"] ={ key:"SVDLanczosOperator", icon:"svd.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["SVDCalculatorOperator"] ={ key:"SVDCalculatorOperator", icon:"svdCalculator.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: false};
        OP_HASH["SVDLanczosCalculatorOperator"] ={ key:"SVDLanczosCalculatorOperator", icon:"svdCalculator.png",optype: OP_SCORING , imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: false};
        OP_HASH["ColumnFilterOperator"] ={ key:"ColumnFilterOperator", icon:"columnfilter.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["VariableSelectionAnalysisOperator"] ={ key:"VariableSelectionAnalysisOperator", icon:"VariableSelection.png",  optype: OP_EXPLORATION, imgtype:IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["SQLExecuteOperator"] ={ key:"SQLExecuteOperator" , icon:"sqlExecute.png" , optype: OP_OTHER, imgtype: IMG_TYPE_TRIANGLE_UPSIDEDOWN , terminal: false, showhadoop: false};

        OP_HASH["AdaboostOperator"] ={ key:"AdaboostOperator" , icon:"adaboost.png" ,  optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["AdaboostPredictOperator"] ={ key:"AdaboostPredictOperator" , icon:"adaboostpredict.png" , optype: OP_SCORING , imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: false};
        OP_HASH["PCAOperator"] ={ key:"PCAOperator" , icon:"pca.png" , optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: true, showhadoop: false};
        OP_HASH["WOEOperator"] ={ key:"WOEOperator", icon:"woe.png",  optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_UP, terminal: false, showhadoop: false};
        OP_HASH["WOETableGeneratorOperator"] ={ key:"WOETableGeneratorOperator", icon:"woepredict.png", optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_UP, terminal: false, showhadoop: false};
        OP_HASH["ScatterMatrixOperator"] ={ key:"ScatterMatrixOperator",icon:"scatterMatrix.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT, terminal: true, showhadoop: true};
        OP_HASH["TableSetOperator"] ={ key:"TableSetOperator", icon:"tableset.png",  optype: OP_TRANFORMATION, imgtype: IMG_TYPE_TRAP_UP, terminal: false, showhadoop: true};
        OP_HASH["PLDATrainerOperator"] ={ key:"PLDATrainerOperator", icon:"plda.png",  optype: OP_MODEL, imgtype:IMG_TYPE_HEX, terminal: false, showhadoop: false};
        OP_HASH["PLDAPredictOperator"] ={ key:"PLDAPredictOperator", icon:"pldapredictor.png",  optype: OP_SCORING, imgtype:IMG_TYPE_SQUARE, terminal: true, showhadoop: false};
        OP_HASH["SubFlowOperator"] ={ key:"SubFlowOperator", icon:"subflow.png",  optype: OP_OTHER, imgtype:IMG_TYPE_SQUARE, terminal: false, showhadoop: true};
    	OP_HASH["EMClusteringPredictOperator"] ={key:"EMClusteringPredictOperator", icon:"emclusteringpredict.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: false};
        OP_HASH["EMClusteringOperator"] ={ key:"EMClusteringOperator", icon:"emclustering.png", optype: OP_MODEL, imgtype: IMG_TYPE_HEX, terminal: false, showhadoop: false};
//    OP_HASH["ScatterPlotOperator"] ={ key:"ScatterPlotOperator", icon:"histogram.png", optype: OP_EXPLORATION, imgtype:IMG_TYPE_OCT, terminal: true};
//    OP_HASH["UnivariateExplorerOperator"] ={ key:"UnivariateExplorerOperator", icon:"histogram.png",  optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT, terminal: true};
//    OP_HASH["BoxAndWhiskerOperator"] ={ key:"BoxAndWhiskerOperator", icon:"histogram.png",  optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT, terminal: true};
        OP_HASH["HadoopFileOperator"] ={ key:"HadoopFileOperator", icon:"hdTable.png",  optype: OP_DATASOURCE , imgtype: IMG_TYPE_SQUARE, terminal: false, showhadoop: true};
        OP_HASH["HadoopRowFilterOperator"] ={ key:"HadoopRowFilterOperator", icon:"filter.png",  optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopVariableOperator"] ={ key:"HadoopVariableOperator", icon:"variable.png",  optype: OP_DONTSHOW , imgtype: IMG_TYPE_TRAP_DOWN, terminal: false, showhadoop: true};
        OP_HASH["HadoopAggregateOperator"] ={ key:"HadoopAggregateOperator", icon:"aggregate.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_UP , terminal: false, showhadoop: true};
        OP_HASH["HadoopBarChartOperator"] ={ key:"HadoopBarChartOperator", icon:"dbtableAnalysis.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopJoinOperator"] ={ key:"HadoopJoinOperator", icon:"join.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_UP , terminal: false, showhadoop: true};
        OP_HASH["HadoopFrequencyAnalysisOperator"] ={ key:"HadoopFrequencyAnalysisOperator", icon:"frequencyAnalysis.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopValueAnalysisOperator"] ={ key:"HadoopValueAnalysisOperator", icon:"valueAnalysis.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopHistogramOperator"] ={ key:"HadoopHistogramOperator", icon:"histogram.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopUnionOperator"] ={ key:"HadoopUnionOperator", icon:"tableset.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_UP , terminal: false, showhadoop: true};
        OP_HASH["HadoopScatterPlotMatrixOperator"] ={ key:"HadoopScatterPlotMatrixOperator", icon:"scatterMatrix.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopReplaceNullOperator"] ={ key:"HadoopReplaceNullOperator", icon:"NullReplacement.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_DOWN , terminal: false, showhadoop: true};
        OP_HASH["HadoopNormalizationOperator"] ={ key:"HadoopNormalizationOperator", icon:"normalization.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopKmeansOperator"] ={ key:"HadoopKmeansOperator", icon:"clustering.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopRandomSamplingOperator"] ={ key:"HadoopRandomSamplingOperator", icon:"randomsample.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopSampleSelectorOperator"] ={ key:"HadoopSampleSelectorOperator", icon:"sampleSelector.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopLinearRegressionOperator"] ={ key:"HadoopLinearRegressionOperator", icon:"linearRegression.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopLinearRegressionPredictOperator"] ={ key:"HadoopLinearRegressionPredictOperator", icon:"linearRegressionPredict.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: true, showhadoop: true};
        OP_HASH["HadoopLogisticRegressionOperator"] ={ key:"HadoopLogisticRegressionOperator", icon:"logisticRegression.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
        OP_HASH["HadoopLogisticRegressionPredictOperator"] ={ key:"HadoopLogisticRegressionPredictOperator", icon:"logisticRegressionPredict.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: true, showhadoop: true};
        OP_HASH["NoteOperator"] ={ key:"NoteOperator", icon:"note.png", optype: OP_OTHER, imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: true};
        OP_HASH["HadoopROCOperator"] ={ key:"HadoopROCOperator",icon: "roc.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["HadoopLiftOperator"] ={ key:"HadoopLiftOperator",icon: "lift.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["HadoopGoodnessOfFitOperator"] ={ key:"HadoopGoodnessOfFitOperator",icon: "goodnessOfFit.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["HadoopPigExecuteOperator"] ={ key:"HadoopPigExecuteOperator",icon: "sqlExecute.png", optype: OP_OTHER, imgtype: IMG_TYPE_TRIANGLE_UPSIDEDOWN , terminal: false, showhadoop: true};
        OP_HASH["HadoopTimeSeriesOperator"] ={ key:"HadoopTimeSeriesOperator", icon:"timeseries.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_HEX, terminal: false, showhadoop: true};
        OP_HASH["HadoopTimeSeriesPredictOperator"] ={ key:"HadoopTimeSeriesPredictOperator", icon:"timeseriespredict.png",optype: OP_DONTSHOW, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        
        OP_HASH["HadoopDecisionTreeOperator"] ={ key:"HadoopDecisionTreeOperator", icon:"decisionTree.png",optype: OP_DONTSHOW, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["HadoopDecisionTreePredictOperator"] ={ key:"HadoopDecisionTreePredictOperator", icon:"decisionTreePredict.png",optype: OP_DONTSHOW, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};
        OP_HASH["RandomForestOperator"] ={ key:"RandomForestOperator", icon:"randomForest.png",optype: OP_MODEL, imgtype: IMG_TYPE_HEX , terminal: false, showhadoop: false};
        OP_HASH["RandomForestPredictOperator"] ={ key:"RandomForestPredictOperator", icon:"randomForestPredict.png",optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE, terminal: true, showhadoop: false};
        OP_HASH["HadoopPivotOperator"] ={ key:"HadoopPivotOperator", icon:"pivot.png", optype: OP_DONTSHOW, imgtype:IMG_TYPE_TRAP_LEFT , terminal: false, showhadoop: true};
        OP_HASH["HadoopColumnFilterOperator"] ={ key:"HadoopColumnFilterOperator", icon:"columnfilter.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_TRAP_RIGHT, terminal: false, showhadoop: true};
      
        OP_HASH["BoxAndWhiskerOperator"] ={ key:"BoxAndWhiskerOperator", icon:"boxplot.png", optype: OP_EXPLORATION, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopBoxAndWiskerOperator"] ={ key:"HadoopBoxAndWiskerOperator", icon:"boxplot.png", optype: OP_DONTSHOW, imgtype: IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopVariableSelectionAnalysisOperator"] ={ key:"HadoopVariableSelectionAnalysisOperator", icon:"VariableSelection.png",  optype: OP_DONTSHOW, imgtype:IMG_TYPE_OCT , terminal: true, showhadoop: true};
        OP_HASH["HadoopNaiveBayesOperator"] ={ key:"HadoopNaiveBayesOperator", icon:"naiveBayes.png",  optype: OP_DONTSHOW, imgtype:IMG_TYPE_HEX , terminal: false, showhadoop: true};
        OP_HASH["HadoopNaiveBayesPredictOperator"] ={ key:"HadoopNaiveBayesPredictOperator", icon:"naiveBayesPredict.png",  optype: OP_DONTSHOW, imgtype:IMG_TYPE_HEX , terminal: true, showhadoop: false};
        OP_HASH["HadoopConfusionOperator"] ={ key:"HadoopConfusionOperator",icon: "roc.png", optype: OP_SCORING, imgtype: IMG_TYPE_SQUARE , terminal: true, showhadoop: true};

        _setAllOperatorObjects();

    });

    function _getOperatorObjectByKey(theKey)
    {
        if (theKey && /^CustomizedOperator/.test(theKey))   
        	theKey = "CustomizedOperator";
        return OP_HASH[theKey];
    }

    function _getOperatorObjectsByKeys(keyArray)
    {
        var opArray = [];
        for (var count = 0; count < keyArray.length; count++)
        {
            if  (OP_HASH[keyArray[count]].optype != OP_DONTSHOW )
                opArray.push(dojo.clone(OP_HASH[keyArray[count]]));
        }
        return opArray;
    }

    function _setAllOperatorObjects()
    {
        var keys = [];
        for(var i in OP_HASH) if (OP_HASH.hasOwnProperty(i))
        {
            OP_HASH[i].label = i18nLabels[i];
        }
    }
    function _getAllOperatorObjects()
    {
        var opArray = [];
        var keys = [];
        for(var i in OP_HASH) if (OP_HASH.hasOwnProperty(i))
        {
            keys.push(i);
        }
        var opArray = _getOperatorObjectsByKeys(keys);
        opArray.sort(function compare(a,b) {
            if (a.label.toLowerCase()  < b.label.toLowerCase())
                return -1;
            if (a.label.toLowerCase() > b.label.toLowerCase())
                return 1;
            return 0;
        });

        return opArray;

    }

    function _getAllOperatorsIncludingCustom(customops)
    {
        console.log("have the custom ops");
        var opArray = _getAllOperatorObjects();
        if (customops == null || customops.length < 1) return opArray;
        for (var count=0; count< customops.length;count++)
        {
            var co = customops[count];

            var lastIndex = co.operatorName.lastIndexOf("_");
            var operatorLabel = (lastIndex > -1)? co.operatorName.substring(0,lastIndex) : co.operatorName;

            var customOperator = {};
            customOperator.key = "CustomizedOperator_" + co.operatorName;
            customOperator.actualName = co.operatorName;
            customOperator.label = operatorLabel;
            customOperator.icon ="udf.png";
            customOperator.optype = OP_OTHER;
            customOperator.imgtype = IMG_TYPE_DIAMOND;
            customOperator.terminal = false;
            customOperator.showhadoop = false;
            opArray.push(customOperator);
        }

        opArray.sort(function compare(a,b) {
            if (a.label.toLowerCase()  < b.label.toLowerCase())
                return -1;
            if (a.label.toLowerCase() > b.label.toLowerCase())
                return 1;
            return 0;
        });

        return opArray;
    }

    function _getTargetImageSourceByKey(key)
    {
        var op = _getOperatorObjectByKey(key);
        if (op)
        {
            return  baseURL +  "/images/target_icons/" + op.icon;
        }
    }


    function _getFadedImageSourceByKey(key)
    {
        var op = _getOperatorObjectByKey(key);
        if (op)
        {
            return  baseURL +  "/images/faded_icons/" + op.icon;
        }
    }

    function _getSelectedImageSourceByKey(key)
    {
        var op = _getOperatorObjectByKey(key);
        if (op)
        {
            return  baseURL +  "/images/selected_icons/" + op.icon;
        }
    }

    function _getStandardImageSourceByKey(key)
    {
        var op = _getOperatorObjectByKey(key);
        if (op)
        {
            return  baseURL +  "/images/icons/" + op.icon;
        }
    }

    function _isTerminalByKey(key)
    {
        var op = _getOperatorObjectByKey(key);
        if (op)
        {
            return op.terminal;
        }
        throw "Cannot find Operator for " + key;
    }

    function _getImageSourceByImageType(imgType)
    {
        var imgSrc = "datasource";
        switch (imgType)
        {
            case IMG_TYPE_SQUARE:
                imgSrc = "datasource";
                break;
            case IMG_TYPE_HEX:
                imgSrc = "hex";
                break;
            case IMG_TYPE_OCT:
                imgSrc = "oct";
                break;
            case IMG_TYPE_TRAP_DOWN:
                imgSrc = "trap_down";
                break;
            case IMG_TYPE_TRAP_UP:
                imgSrc = "trap_up";
                break;
            case IMG_TYPE_TRAP_LEFT:
                imgSrc = "trap_left";
                break;
            case IMG_TYPE_TRAP_RIGHT:
                imgSrc = "trap_right";
                break;
            case IMG_TYPE_JEWEL:
                imgSrc = "jewel";
                break;
            case IMG_TYPE_TRIANGLE:
                imgSrc = "tri_up";
                break;
            case IMG_TYPE_DIAMOND:
                imgSrc = "diamond";
                break;
            case IMG_TYPE_PENT:
                imgSrc = "pent";
                break;
            case IMG_TYPE_TRIANGLE_UPSIDEDOWN:
                imgSrc = "tri_down";
                break;
            default:
                imgSrc = "datasource";
                break;

        }
        var src = baseURL +  "/images/workbench_icons/" + imgSrc + ".png";
        return src;
    }



    return {
        getImageSourceByImageType: _getImageSourceByImageType,
        getAllOperatorObjects: _getAllOperatorObjects,
        getOperatorObjectsByKeys: _getOperatorObjectsByKeys,
        getOperatorObjectByKey: _getOperatorObjectByKey ,
        getAllOperatorsIncludingCustom: _getAllOperatorsIncludingCustom,
        getTargetImageSourceByKey: _getTargetImageSourceByKey,
        getFadedImageSourceByKey: _getFadedImageSourceByKey,
        getSelectedImageSourceByKey: _getSelectedImageSourceByKey,
        getStandardImageSourceByKey: _getStandardImageSourceByKey,
        isTerminalByKey: _isTerminalByKey,
        OP_ALL:OP_ALL,
        OP_HASH: OP_HASH
    };


});
