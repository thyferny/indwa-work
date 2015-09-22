/**
 * NeuralNetworkTextVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.tree.NeuralChart;
import com.alpine.datamining.operator.neuralnet.sequential.NNModel;

/**
 * @author Jimmy
 *
 */
public class NeuralNetworkTreeVisualizationType extends 
TreeVisualizationType {
		@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
		}
		NNModel model = (NNModel) obj;
		NeuralChart chart = new NeuralChart();
		chart.createChart(model);
		
		 TreeVisualizationOutPut output = new  TreeVisualizationOutPut(chart.getChart());
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		output.setImg(chart.getImage());
		output.setType(TreeVisualizationOutPut.TYPE_NEAURAL_NETWORK);
		
		output.setShowHeight(chart.getShowHeight());
		output.setShowWidth(chart.getShowWidth());
		
		output.setVisualizationChart(chart);
		
		return output;
	}
}
