/**
 * ClassName KSImageVisualizationType.java
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
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.operator.evaluator.DoubleListAndDoubleData;

public class KSImageVisualizationType extends ImageVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		List<double[]> xyCoordinateSet1 = null;
		double value = 0d;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof DoubleListAndDoubleData){
				xyCoordinateSet1 =  ((DoubleListAndDoubleData)obj).getDoubleList();
				value = ((DoubleListAndDoubleData)obj).getDouble();
			}
		}
		
		XYSeries series1 = new XYSeries("Default");
		for (int i=0; i<xyCoordinateSet1.size(); i++) {
			series1.add(xyCoordinateSet1.get(i)[0], xyCoordinateSet1.get(i)[1]);
		}
		XYSeries series2 = new XYSeries("Non-default");
		for (int i=0; i<xyCoordinateSet1.size(); i++) {
			series2.add(xyCoordinateSet1.get(i)[0], xyCoordinateSet1.get(i)[2]);
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		
		JFreeChart chart = ChartFactory.createXYLineChart(
				"K-S Measure Chart", // chart title
				"Confidence", // x axis label
				"Rate", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				true, // include legend
				true, // tooltips
				false // urls
				);
//		TextTitle valueLabel = new TextTitle(String.format("K-S = %s", Double.toString(value)));
		TextTitle valueLabel = new TextTitle("K-S = "+Double.toString(value));
		chart.addSubtitle(valueLabel);
		
		JFreeChartImageVisualizationOutPut output = new JFreeChartImageVisualizationOutPut(chart);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
}
