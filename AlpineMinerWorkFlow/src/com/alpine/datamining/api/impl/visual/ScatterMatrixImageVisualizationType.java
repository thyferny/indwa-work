package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.awt.Shape;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.table.ScatterMatrixColumnPairs;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatterMatrix;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.api.impl.visual.widgets.ScatterMatrixPlotChart;
import com.alpine.utility.tools.AlpineMath;

public class ScatterMatrixImageVisualizationType extends ImageVisualizationType {
	

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		AnalyzerOutPutScatterMatrix obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutScatterMatrix){
			obj = (AnalyzerOutPutScatterMatrix)analyzerOutPut;
		}
		if(obj==null){
			return null;
		}
		
		Map<ScatterMatrixColumnPairs, DataTable> dataTableMap = obj.getDataTableMap();
		
		Map<ScatterMatrixColumnPairs,JFreeChart> imageMap=new HashMap<ScatterMatrixColumnPairs,JFreeChart>();
		
		Iterator<Entry<ScatterMatrixColumnPairs, DataTable>> iter = dataTableMap.entrySet().iterator();
		
		while(iter.hasNext()){
			Entry<ScatterMatrixColumnPairs, DataTable> entry = iter.next();
			ScatterMatrixColumnPairs columnPairs = entry.getKey();
			String columnY = columnPairs.getColumnY();
			DataTable dt = entry.getValue();
			
			//must be an array with length 2, containing two arrays of equal length, 
			//the first containing the x-values and the second containing the y-values
			 double[][] data = new double[2][dt.getRows().size()];
		     for(int i=0;i<dt.getRows().size();i++){
		    	 DataRow dr = dt.getRows().get(i);
		    	 double x = Float.valueOf(dr.getData(0));
		    	 double y = Float.valueOf(dr.getData(1));
		    	 data[0][i] = x;
		    	 data[1][i] = y;
		     }
		     DefaultXYDataset xydataset = new DefaultXYDataset();
		     xydataset.addSeries(columnY, data);
		     JFreeChart chart = ChartFactory.createScatterPlot(null, null,null, xydataset, PlotOrientation.VERTICAL, false, false, false);
		     XYPlot  plot   =   chart.getXYPlot();
		     
		     //Special for pre-view image.
		     plot.getRangeAxis().setVisible(false);
		     plot.getDomainAxis().setVisible(false);
		     plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		     plot.setInsets(new RectangleInsets(0, 0, 0, 0));
		     
		     
			 double[] xArray = Arrays.copyOf(data[0], data[0].length);
			 double[] yArray = Arrays.copyOf(data[1], data[1].length);
			 
			if (data[0].length > 0) {
				// Sort by X-axis
				Arrays.sort(xArray);
				double minX = xArray[0];
				double maxX = xArray[xArray.length - 1];

				long n = AlpineMath.adjustUnits(minX, maxX);
				for (int i = 0; i < data[0].length; i++) {
					data[0][i] = data[0][i] / n;
				}

				Arrays.sort(yArray);
				double minY = yArray[0];
				double maxY = yArray[yArray.length - 1];
				long m = AlpineMath.adjustUnits(minY, maxY);
				for (int i = 0; i < data[1].length; i++) {
					data[1][i] = data[1][i] / m;
				}

				if (minX < maxX) {
					double para[] = Regression.getOLSRegression(xydataset, 0);
					// para1 is intercept
					// para2 is slope.
					LineFunction2D linefunction2d = new LineFunction2D(para[0],
							para[1]);
					XYDataset xyLinedataset1 = DatasetUtilities
							.sampleFunction2D(linefunction2d, minX / n, maxX
									/ n, 100, columnY);
					plot.setDataset(1, xyLinedataset1);
					StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
					standardxyitemrenderer.setSeriesPaint(0, Color.red);
					standardxyitemrenderer.setSeriesShapesFilled(0, true);
					plot.setRenderer(1, standardxyitemrenderer);
				}
			}
		     XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();	 				 
				 Shape shapeScatter = VisualUtility.getScatterPoint();
				 renderer.setSeriesShape(0,shapeScatter);
				 renderer.setSeriesPaint(0,Color.black);
				 
			imageMap.put(columnPairs, chart);
		}
		
		ScatterMatrixPlotChart visualChart = new ScatterMatrixPlotChart();
		visualChart.createChart(obj,imageMap);
			
		MultiChartImageVisualizationOutput output=new MultiChartImageVisualizationOutput(visualChart);
		output.setImage(visualChart.getImage());
		output.setShowHeight(visualChart.getShowHeight());
		output.setShowWidth(visualChart.getShowWidth());
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}

}
