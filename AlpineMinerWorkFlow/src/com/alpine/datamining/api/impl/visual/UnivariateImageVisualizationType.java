package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutUnivariate;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.utility.tools.AlpineMath;

public class UnivariateImageVisualizationType extends ImageVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		AnalyzerOutPutUnivariate obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutUnivariate){
			obj = (AnalyzerOutPutUnivariate)analyzerOutPut;
		}
		if(obj != null){
			JFreeChart chart = null;
			DataTable dt = obj.getDataTable();
			List<String> allSelectedColumns = obj.getAllSelectedColumn();
			List<String> analysisColumns = obj.getAnalysisColumn();
			String referenceColumn = obj.getReferenceColumn();
			XYSeries[] xYSeriess=new XYSeries[allSelectedColumns.size()-1];
			for (int i = 0; i < xYSeriess.length; i++) {
				xYSeriess[i]=    new XYSeries(analysisColumns.get(i)); 
			}
			//sequence is very important
			
			 for(int i=0;i<dt.getRows().size();i++){
				 DataRow dr = dt.getRows().get(i);
				 double referenceValue = Float.valueOf(dr.getData(0));
				 
				 for (int j = 1; j <dr.getData().length; j++) {
					 double coulunmValue=Float.valueOf(dr.getData(j));
					 xYSeriess[j-1].add(referenceValue,coulunmValue);
				}
				 
			 }
			 
	 
			 final XYSeriesCollection[] dataset = new XYSeriesCollection[xYSeriess.length];
			 for (int i = 0; i < xYSeriess.length; i++) {
				 dataset[i] = new XYSeriesCollection();
				  dataset[i].addSeries(xYSeriess[i]);
			}
			 
			 Map<String,Long> unitMap=new HashMap<String,Long>();
			 String xLabel=referenceColumn;
			 if(dataset.length>0){
				 double minX = dataset[0].getSeries(0).getMinX();
				 double maxX = dataset[0].getSeries(0).getMaxX();
				 
				 long n = AlpineMath.adjustUnits(minX, maxX);
				 
				 xLabel=n==1?referenceColumn:referenceColumn
							+" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
							" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
				 
				 
				 for(int i=0;i<dataset.length;i++){
					 int count = dataset[i].getSeries(0).getItemCount();
					 double maxY = dataset[i].getSeries(0).getMaxY();
					 double minY = dataset[i].getSeries(0).getMinY();
					 
					 long m = AlpineMath.adjustUnits(minY, maxY);
					 unitMap.put(dataset[i].getSeries(0).getKey().toString(), m);
					 List<Double> xList=new ArrayList<Double>();
					 List<Double> yList=new ArrayList<Double>();
					 for(int j=0;j<count;j++){
						 Number x = dataset[i].getSeries(0).getX(j);
						 Number y = dataset[i].getSeries(0).getY(j);
						 xList.add(x.doubleValue()/n);
						 yList.add(y.doubleValue()/m);
					 }
					 dataset[i].getSeries(0).clear();
					 for(int j=0;j<count;j++){
						 dataset[i].getSeries(0).add(xList.get(j), yList.get(j));
					 }
				 }
			 }
			 chart = ChartFactory.createXYLineChart("", xLabel,
					 VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale), null,
					 PlotOrientation.VERTICAL, true, true, false);
			 final XYPlot plot = chart.getXYPlot();
			 plot.getRangeAxis().setVisible(false);
			 
			 XYLineAndShapeRenderer[] renderer = new XYLineAndShapeRenderer[dataset.length];
			 Color[] colors = VisualUtility.getRandomColor(dataset.length);
			 for(int i=0;i<dataset.length;i++){
				 String yLabel = dataset[i].getSeries(0).getKey().toString();
				 if(unitMap.containsKey(yLabel)){
					 Long unit = unitMap.get(yLabel);				 
					 yLabel=unit==1?yLabel+" "
							 +VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale)
							 :yLabel+" "
							 +VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale)
								+" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
								" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(unit)+")";
				 }else{
					 yLabel=  yLabel + " "
					 +VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale);
				 }
				 NumberAxis axis = new NumberAxis(yLabel);
				 axis.setAxisLinePaint(colors[i]);
				 axis.setLabelPaint(colors[i]);
				 axis.setTickLabelPaint(colors[i]);
				 double maxY = dataset[i].getSeries(0).getMaxY();
				 double minY = dataset[i].getSeries(0).getMinY();
				 
				 axis.setLowerBound(minY-Math.abs(maxY-minY)/10);
				 axis.setUpperBound(maxY+Math.abs(maxY-minY)/10);
				 
				 axis.setAutoRangeIncludesZero(false);
				 
				 plot.setRangeAxis(i+1, axis);
				 plot.setDataset(i+1, dataset[i]);
				 plot.mapDatasetToRangeAxis(i+1, i+1);
				 renderer[i] = new XYLineAndShapeRenderer();
				 renderer[i].setSeriesPaint(0, colors[i]);
				 plot.setRenderer(i+1, renderer[i]);
			 }
			 
			 
			 Font yfont = new Font(VisualLanguagePack.getMessage(VisualLanguagePack.CHARACTER_FONT,locale),Font.PLAIN,12) ;  
			 ValueAxis rangeAxis = plot.getRangeAxis(); 
			 rangeAxis.setLabelFont(yfont);  
			 chart.getLegend().setItemFont(VisualResource.getChartFont());   
			 JFreeChartImageVisualizationOutPut output = new JFreeChartImageVisualizationOutPut(chart);
			 output.setName(analyzerOutPut.getAnalyticNode().getName());
			 return output;
		}
		return null;
	}
}
