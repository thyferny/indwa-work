/**
 * CartTreeVisualizationType.java
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
import com.alpine.datamining.api.impl.visual.tree.VisualizationTreeChart;
import com.alpine.datamining.operator.tree.cartregression.RegressionTreeModel;

/**
 * @author Jimmy
 *
 */
public class CartTreeVisualizationType extends TreeVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
			if(obj == null) {
				return null;
			}
			return generateVOut(analyzerOutPut.getAnalyticNode().getName(), obj);
		}
		
		return null;
	}

	public VisualizationOutPut generateVOut(String name,
			Object obj) {
		VisualizationTreeChart tree = new VisualizationTreeChart();
//			TreeChart tree = new TreeChart();
		if(obj instanceof RegressionTreeModel){
			tree.createChart(((RegressionTreeModel) obj).getRoot());
		}else if(obj instanceof com.alpine.datamining.operator.tree.threshold.DecisionTreeModel){
			tree.createChart(((com.alpine.datamining.operator.tree.threshold.DecisionTreeModel) obj).getRoot());
		}else{
			return null;
		}
		
		TreeVisualizationOutPut output = new TreeVisualizationOutPut(tree.getChart());
		output.setName(name);
		output.setType(TreeVisualizationOutPut.TYPE_CART_TREE);
		output.setDepth(tree.getDepth());
		output.setImg(tree.getImage());
		output.setSaveWidth(tree.getSaveWidth());
		output.setSaveHeight(tree.getSaveHeight());
		output.setShowHeight(tree.getShowHeight());
		output.setShowWidth(tree.getShowWidth());
		output.setVisualizationChart(tree);
		
		return output;
	}
}
