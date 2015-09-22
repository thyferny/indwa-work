/**
 * ClassName HadoopDecesionTreeVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-13
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.hadoop.models.DecisionTreeHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.tree.VisualizationTreeChart;

/**
 * @author John
 *
 */
public class HadoopDecesionTreeVisualizationType extends TreeVisualizationType {
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		DecisionTreeHadoopModel  model = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel().getModel();
			if(obj instanceof DecisionTreeHadoopModel){
				model = (DecisionTreeHadoopModel) obj;
			}
		}
		if(model == null) return null;
//		TreeChart tree = new TreeChart();
		VisualizationTreeChart tree = new VisualizationTreeChart();
		tree.createChart(model.toVisualTree());
		
		TreeVisualizationOutPut output = new TreeVisualizationOutPut(tree.getChart());
		output.setName(analyzerOutPut.getAnalyticNode().getName());
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
