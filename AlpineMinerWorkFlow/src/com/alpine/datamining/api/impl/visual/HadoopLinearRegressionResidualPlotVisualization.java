/**
 * ClassName HadoopLinearRegressionResidualPlotVisualization.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-27
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.List;

import org.jfree.chart.JFreeChart;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;

/**
 * @author Jeff Dong
 *
 */
public class HadoopLinearRegressionResidualPlotVisualization extends
		ImageVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		if(obj == null)return null;
		EngineModel engineModel = (EngineModel) obj;
		
		LinearRegressionHadoopModel model=(LinearRegressionHadoopModel)engineModel.getModel();
		
		List<double[]> residuals = model.getResiduals();
		
		String dependentColumn = model.getSpecifyColumn();
		//avoid the null point
		if(residuals==null||residuals.size()==0){
			return null;
		}
		JFreeChart chart = LinearRegressionResidualPlotVisualization.generateResidualPlot(dependentColumn , residuals);

		JFreeChartImageVisualizationOutPut visualizationOutput = new JFreeChartImageVisualizationOutPut(
				chart);
		
		visualizationOutput.setName(VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUALPLOT_TITLE,locale));
		
		return visualizationOutput;
	}
}
