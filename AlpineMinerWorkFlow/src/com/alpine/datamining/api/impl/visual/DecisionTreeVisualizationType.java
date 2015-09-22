/**
 * ClassName DecisionTreeVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.tree.VisualizationTreeChart;
import com.alpine.datamining.operator.tree.threshold.DecisionTreeModel;

public class DecisionTreeVisualizationType extends TreeVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		DecisionTreeModel  model = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
			if(obj instanceof DecisionTreeModel){
				model = (DecisionTreeModel) obj;
			}
		}
		if(model == null) {
			return null;
		}
//		TreeChart tree = new TreeChart();
		return generateVOut(analyzerOutPut.getAnalyticNode().getName(), model);
	}

	public VisualizationOutPut generateVOut(String name,
			DecisionTreeModel model) {
		VisualizationTreeChart tree = new VisualizationTreeChart();
		tree.createChart(model.getRoot());
		
		TreeVisualizationOutPut output = new TreeVisualizationOutPut(tree.getChart());
		output.setName(name);
		output.setType(TreeVisualizationOutPut.TYPE_DECISION_TREE);
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
