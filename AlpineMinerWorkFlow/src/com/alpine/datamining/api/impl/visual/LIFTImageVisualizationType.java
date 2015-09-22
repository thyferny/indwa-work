/**
 * ClassName LIFTImageVisualizationType.java
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
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.operator.evaluator.DoubleListData;

public class LIFTImageVisualizationType extends ImageVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		List<String> nameList = new ArrayList<String>();
		List<List<double[]>> xyCoordinateSet1List = new ArrayList<List<double[]>>();
		
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof List){
				Iterator liftlist = ((List) obj).iterator();
				while(liftlist.hasNext()){
					Object oo = liftlist.next();
					nameList.add(((DoubleListData)oo).getSourceName());
					List<double[]> xyCoordinateSet1 =  ((DoubleListData)oo).getDoubleList();
					xyCoordinateSet1List.add(xyCoordinateSet1);
				}
			}
		}
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for(int j=0;j<nameList.size();j++){
			XYSeries series1 = new XYSeries(nameList.get(j)+" "+VisualLanguagePack.getMessage(
					VisualLanguagePack.LIFT_CURVE,locale));
			for (int i=0; i<xyCoordinateSet1List.get(j).size(); i++) {
				series1.add(xyCoordinateSet1List.get(j).get(i)[0], xyCoordinateSet1List.get(j).get(i)[1]);
			}
			
			
			dataset.addSeries(series1);
			
		}
		
		XYSeries series2 = new XYSeries(VisualLanguagePack.getMessage(VisualLanguagePack.RANDOM,locale));
		series2.add(0.0,1.0);
		series2.add(1.0,1.0);
		dataset.addSeries(series2);	
		JFreeChart chart = ChartFactory.createXYLineChart(
				"", 
				VisualLanguagePack.getMessage(VisualLanguagePack.PERCENTAGE_UPPER,locale), 
				"LIFT", 
				dataset, 
				PlotOrientation.VERTICAL, 
				true, true, false);
		
		XYPlot  plot   =   chart.getXYPlot();
		plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
		plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		chart.getLegend().setItemFont(VisualResource.getChartFont());
		XYLineAndShapeRenderer lineandshaperenderer = (XYLineAndShapeRenderer)plot.getRenderer();
		Color[] colors = VisualUtility.getRandomColor(nameList.size()+1);
		for(int i=0;i<nameList.size()+1;i++){
			lineandshaperenderer.setSeriesStroke(i,new BasicStroke(2));
			lineandshaperenderer.setSeriesPaint(i,colors[i]);
		}
		
		JFreeChartImageVisualizationOutPut output = new JFreeChartImageVisualizationOutPut(chart);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
}
