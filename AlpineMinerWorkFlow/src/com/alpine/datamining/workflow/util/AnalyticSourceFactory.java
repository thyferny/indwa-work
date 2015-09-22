/**
 * ClassName AnalyticSourceFactory.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.AnalyticSource;
import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;
import com.alpine.datamining.api.impl.hadoop.HadoopAnalyticSource;
/**
 * @author zhao yong
 *
 */
public class AnalyticSourceFactory {
	
	public static List<String> hadoopOperatorList=new ArrayList<String>();

	static {
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopPROperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopFileOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopRowFilterOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopAggregateOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopVariableOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopBarChartOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopHistogramOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopValueAnalysisOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopJoinOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopUnionOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopFrequencyAnalysisOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopScatterPlotMatrixOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopReplaceNullOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopRandomSamplingOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopNormalizationOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopKmeansOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopSampleSelectorOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopLinearRegressionOperator");
		
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopDecisionTreeOperator"); 
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopDecisionTreePredictOperator"); 
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopPigExecuteOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopPivotOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopColumnFilterOperator");
		
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopTimeSeriesOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopLogisticRegressionOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.CopyToDBOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopLinearRegressionPredictOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopLogisticRegressionPredictOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopTimeSeriesPredictOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopROCOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopLiftOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopGoodnessOfFitOperator");
		hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopBoxAndWiskerOperator");
        hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopVariableSelectionAnalysisOperator");
        hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopNaiveBayesOperator");
        hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopNaiveBayesPredictOperator");
        hadoopOperatorList.add("com.alpine.miner.gef.runoperator.hadoop.HadoopConfusionOperator");

    }
	public static AnalyticSource createAnalyticSource(String operatorName){
		//isntanceof HadoopOperator !!!
		
		if(hadoopOperatorList.contains(operatorName)){
			return new HadoopAnalyticSource();
		}else{
			return new DataBaseAnalyticSource();
		}
	}

}
