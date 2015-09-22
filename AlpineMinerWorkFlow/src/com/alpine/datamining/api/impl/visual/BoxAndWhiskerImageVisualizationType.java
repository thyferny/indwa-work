/**
 * BoxAndWhiskerImageVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.awt.Paint;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.ChartColor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutBoxWhisker;
import com.alpine.datamining.api.impl.output.BoxAndWhiskerItem;
import com.alpine.datamining.api.impl.visual.dataset.BoxAndWhiskerDataset;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.utility.tools.AlpineMath;

/**
 * @author Jimmy
 *
 */
public class BoxAndWhiskerImageVisualizationType extends ImageVisualizationType {

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.impl.visual.ImageVisualizationType#generateOutPut(com.alpine.datamining.api.AnalyticOutPut)
	 */
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		AnalyzerOutPutBoxWhisker obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutBoxWhisker){
			obj = (AnalyzerOutPutBoxWhisker)analyzerOutPut;
		}
		List<BoxAndWhiskerItem> list = obj.getItemList();
		BoxAndWhiskerDataset dataset = new BoxAndWhiskerDataset();
		String variableName = null;
		String seriesName = null;
		String typeName = null;
		
		double[] maxArray=new double[list.size()];
		double[] minArray=new double[list.size()];
		//find max and min
		for(int i=0;i<list.size();i++){
			maxArray[i]=list.get(i).getMax().doubleValue();
			minArray[i]=list.get(i).getMin().doubleValue();
		}

		Arrays.sort(maxArray);
		Arrays.sort(minArray);
		
		double max=maxArray[maxArray.length-1];
		double min=minArray[0];
		
		long n = AlpineMath.adjustUnits(min, max);
		 
		for(BoxAndWhiskerItem item:list){
			dataset.add(item.getMean().doubleValue()/n,
					item.getMedian().doubleValue()/n,
					item.getQ1().doubleValue()/n,
					item.getQ3().doubleValue()/n,
					item.getMin().doubleValue()/n,
					item.getMax().doubleValue()/n, 0, 0, null, 
					item.getSeries(),item.getType());
		}
		if(list != null && list.size()>0){
			variableName = list.get(0).getVariableName();
			if(variableName == null)variableName="";
			seriesName = list.get(0).getSeriesName();
			if(seriesName == null)seriesName="";
			typeName = list.get(0).getTypeName();
			if(typeName == null)typeName="";
		}
		
		 String yLabel=n==1?variableName:variableName
					+" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
					" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
		 
		final CategoryAxis xAxis = new CategoryAxis(typeName);
		xAxis.setLabelFont(VisualResource.getChartFont());
        final NumberAxis yAxis = new NumberAxis(yLabel);
        yAxis.setLabelFont(VisualResource.getChartFont());
        yAxis.setAutoRangeIncludesZero(false);
        
       final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();  
       if(list!=null&&list.size()<5){
    	   renderer.setMaximumBarWidth(0.2);
    	   renderer.setItemMargin(0.7);
       }

        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        
        DrawingSupplier supplier=new DefaultDrawingSupplier(createPaintArray(), 
        		DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, 
        		DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, 
        		DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, 
        		DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
		plot.setDrawingSupplier(supplier);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);
		
        final JFreeChart chart = new JFreeChart(
                " ",
                VisualResource.getChartFont(),
                plot,
                true
            );
        chart.removeLegend();
        if(!seriesName.trim().equals("")){
        final TextTitle subtitle = new TextTitle(seriesName,VisualResource.getChartFont());
        chart.addSubtitle(subtitle);
        chart.addLegend(new LegendTitle(plot));
        chart.getLegend().setItemFont(VisualResource.getChartFont());
        		
        }
        JFreeChartImageVisualizationOutPut output = new JFreeChartImageVisualizationOutPut(chart);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		return output;
	}
	
	private Paint[] createPaintArray(){
        return new Paint[] {
                new Color(0x55, 0x55, 0xFF),
                new Color(0x55, 0xFF, 0x55),
                new Color(0xFF, 0x55, 0x55),
                new Color(0xFF, 0xFF, 0x55),
                new Color(0xFF, 0x55, 0xFF),
                new Color(0x55, 0xFF, 0xFF),
                Color.pink,
                Color.gray,
                ChartColor.DARK_RED,
                ChartColor.DARK_BLUE,
                ChartColor.DARK_GREEN,
                ChartColor.DARK_YELLOW,
                ChartColor.DARK_MAGENTA,
                ChartColor.DARK_CYAN,
                Color.darkGray,
                ChartColor.LIGHT_RED,
                ChartColor.LIGHT_BLUE,
                ChartColor.LIGHT_GREEN,
                ChartColor.LIGHT_YELLOW,
                ChartColor.LIGHT_MAGENTA,
                ChartColor.LIGHT_CYAN,
                Color.lightGray,
                ChartColor.VERY_DARK_RED,
                ChartColor.VERY_DARK_BLUE,
                ChartColor.VERY_DARK_GREEN,
                ChartColor.VERY_DARK_YELLOW,
                ChartColor.VERY_DARK_MAGENTA,
                ChartColor.VERY_DARK_CYAN,
                ChartColor.VERY_LIGHT_RED,
                ChartColor.VERY_LIGHT_BLUE,
                ChartColor.VERY_LIGHT_GREEN,
                ChartColor.VERY_LIGHT_YELLOW,
                ChartColor.VERY_LIGHT_MAGENTA,
                ChartColor.VERY_LIGHT_CYAN
            };
	}
}
