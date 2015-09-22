package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutScatter;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.StringHandler;

public class ScatterImageVisualizationType extends ImageVisualizationType {
	
	private final static String SPLITCHAR=":;:";

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		AnalyzerOutPutScatter obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutScatter){
			obj = (AnalyzerOutPutScatter)analyzerOutPut;
		}
		
		DataSourceType stype = DataSourceType.getDataSourceType(
				analyzerOutPut.getAnalyticNode().getSource().getDataSourceType());
		
		
		if(obj != null){
			JFreeChart chart = null;
			DataTable dt = obj.getDataTable();
			String dependentColumn = obj.getDependentColumn();
			String referenceColumn = obj.getReferenceColumn();
			String categoryColumn = obj.getCategoryColumn();
			Hashtable<String,List<double[]>> categoryDataHt = new Hashtable<String, List<double[]>>();
			
			String columnType = obj.getReferenceColumnType();
			DefaultXYDataset xydataset = new DefaultXYDataset();
			if(StringUtil.isEmpty(columnType)||stype.isNumberColumnType(columnType)){
			     chart = generateNumChart(dt, dependentColumn, referenceColumn,
						categoryColumn, categoryDataHt, xydataset);
			}else{
				chart = generateCategoryChart(dt, dependentColumn,
						referenceColumn, categoryColumn);
			}
			chart.getLegend().setItemFont(VisualResource.getChartFont());
			JFreeChartImageVisualizationOutPut output = new JFreeChartImageVisualizationOutPut(chart);
			output.setName(analyzerOutPut.getAnalyticNode().getName());
			return output;
		}
		return null;
	}

	private JFreeChart generateCategoryChart(DataTable dt,
			String dependentColumn, String referenceColumn,
			String categoryColumn) {
		JFreeChart chart;
		DefaultCategoryDataset dataset =new DefaultCategoryDataset();
			if(StringUtil.isEmpty(categoryColumn)){
				Map<String,List<Double>> valueMap=new HashMap<String,List<Double>>();
				for(DataRow dr:dt.getRows()){
					if(!valueMap.keySet().contains(dr.getData(1))){
						ArrayList<Double> list = new ArrayList<Double>();
						list.add(Double.parseDouble(dr.getData(0)));
						valueMap.put(dr.getData(1),list);
					}else{
						valueMap.get(dr.getData(1)).add(Double.parseDouble(dr.getData(0)));
					}
				}		
				Iterator<Entry<String, List<Double>>> iter = valueMap.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String, List<Double>> entry = iter.next();
					List<Double> values = entry.getValue();
					Double[] valueArray=new Double[values.size()];
					valueArray=values.toArray(valueArray);
					Arrays.sort(valueArray);
					dataset.addValue(valueArray[0],entry.getKey(),"");
				}
			}else{
				Map<String,List<Double>> valueMap=new HashMap<String,List<Double>>();
				for(DataRow dr:dt.getRows()){
					String combin=dr.getData(1)+SPLITCHAR+dr.getData(2);
					if(!valueMap.keySet().contains(combin)){
						ArrayList<Double> list = new ArrayList<Double>();
						list.add(Double.parseDouble(dr.getData(0)));
						valueMap.put(combin,list);
					}else{
						valueMap.get(combin).add(Double.parseDouble(dr.getData(0)));
					}
				}		
				Iterator<Entry<String, List<Double>>> iter = valueMap.entrySet().iterator();
				while(iter.hasNext()){
					Entry<String, List<Double>> entry = iter.next();
					List<Double> values = entry.getValue();
					Double[] valueArray=new Double[values.size()];
					valueArray=values.toArray(valueArray);
					Arrays.sort(valueArray);
					String[] combin=entry.getKey().split(SPLITCHAR);
					dataset.addValue(valueArray[0],combin[0],combin[1]);
				}
			}
			
			chart=ChartFactory.createBarChart3D("",referenceColumn,dependentColumn, dataset, PlotOrientation.VERTICAL,true,true,true);
			CategoryPlot   categoryplot   =   (CategoryPlot)chart.getPlot();
			categoryplot.getDomainAxis().setTickLabelFont(VisualResource.getChartFont());
			CategoryItemRenderer   categoryitemrenderer   =   categoryplot.getRenderer(); 
			BarRenderer3D   custombarrenderer3d   =  (BarRenderer3D)categoryitemrenderer;
			custombarrenderer3d.setMaximumBarWidth(0.04D);
		return chart;
	}

	private JFreeChart generateNumChart(DataTable dt, String dependentColumn,
			String referenceColumn, String categoryColumn,
			Hashtable<String, List<double[]>> categoryDataHt,
			DefaultXYDataset xydataset) {
		JFreeChart chart;
		if(categoryColumn != null && !categoryColumn.equals("")){
			 List<Double> xValueList=new ArrayList<Double>();
			 List<Double> yValueList=new ArrayList<Double>();
			 for(int i=0;i<dt.getRows().size();i++){
				 DataRow dr = dt.getRows().get(i);
				 double f1 = Float.valueOf(dr.getData(0));
		    	 double f2 = Float.valueOf(dr.getData(1));
		    	 xValueList.add(f2);
		    	 yValueList.add(f1);
		    	 double[] data = new double[2];
		    	 data[0] = f2;
		    	 data[1] = f1;
				 if(categoryDataHt.containsKey(dr.getData(2))){
					 categoryDataHt.get(dr.getData(2)).add(data);
				 }else{
					 List<double[]> list = new ArrayList<double[]>();
					 list.add(data);
					 categoryDataHt.put(dr.getData(2),list);
				 }
			 }
			Double[] xValueArray=xValueList.toArray(new Double[xValueList.size()]);
			Arrays.sort(xValueArray);
			Double[] yValueArray=yValueList.toArray(new Double[yValueList.size()]);
			Arrays.sort(yValueArray);
			double minX=xValueArray[0];
			double maxX=xValueArray[xValueArray.length-1];
			double minY=yValueArray[0];
			double maxY=yValueArray[yValueArray.length-1];
			long n = AlpineMath.adjustUnits(minX, maxX);
			long m = AlpineMath.adjustUnits(minY, maxY);
			 Enumeration<String> set = categoryDataHt.keys();
			 while(set.hasMoreElements()){
				 String key = set.nextElement();
				 List<double[]> list = categoryDataHt.get(key);
				 double[][] data = new double[2][list.size()];
				 for(int i=0;i<list.size();i++){
					 data[0][i] = list.get(i)[0]/n;
					 data[1][i] = list.get(i)[1]/m;
				 }
				 xydataset.addSeries(key, data);
			 }

			 String xLabel=n==1?referenceColumn:referenceColumn
						+" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
			 String yLabel=m==1?dependentColumn:dependentColumn
					 +" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(m)+")";
			 
			 chart = ChartFactory.createScatterPlot("", xLabel,yLabel,  xydataset, PlotOrientation.VERTICAL, true, true, false);
			
		     XYPlot  plot   =   chart.getXYPlot();
		     Color[] colors = VisualUtility.getRandomColor(categoryDataHt.size());
		     int count =0;
		     Enumeration<String> setnew = categoryDataHt.keys();
		     while(setnew.hasMoreElements()){
		    	 String key = setnew.nextElement();
		    	 List<double[]> list = categoryDataHt.get(key);
		    	 DefaultXYDataset xyLinedataset = new DefaultXYDataset();
				 double[][] data = new double[2][list.size()];
				 for(int i=0;i<list.size();i++){
					 data[0][i] = list.get(i)[0]/n;
					 data[1][i] = list.get(i)[1]/m;
				 }
				 double[] xArray = Arrays.copyOf(data[0], data[0].length);
				 Arrays.sort(xArray);
				 double min=xArray[0];
				 double max=xArray[xArray.length-1];
				 if(min>=max){
					 count++;
					 continue;
				 }
				 xyLinedataset.addSeries(key, data);
				 double ad[] =Regression.getOLSRegression(xyLinedataset, 0);
				 LineFunction2D linefunction2d = new LineFunction2D(ad[0], ad[1]);
				 XYDataset xyLinedataset1 = DatasetUtilities.sampleFunction2D(linefunction2d, min,max, 100, key);
				 plot.setDataset(count+1, xyLinedataset1);
				 StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
				 standardxyitemrenderer.setSeriesPaint(0,colors[count]);
				 standardxyitemrenderer.setSeriesShapesFilled(count+1, true);
				 plot.setRenderer(count+1, standardxyitemrenderer);
				 standardxyitemrenderer.setLegendItemLabelGenerator(new ScatterPlotXYSeriesLabelGenerator(ScatterPlotXYSeriesLabelGenerator.LINE,true));
				 count++;
		     }
			 plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
			 plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
			 
		     XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();    
			 Shape shapeScatter = VisualUtility.getScatterPoint();
			 for(int i=0;i<categoryDataHt.size();i++){
				renderer.setSeriesShape(i,shapeScatter);
				renderer.setSeriesPaint(i,colors[i]); 
				renderer.setLegendItemLabelGenerator(new ScatterPlotXYSeriesLabelGenerator(ScatterPlotXYSeriesLabelGenerator.VALUE,true));
			 }
		 }else{
			 double[][] data = new double[2][dt.getRows().size()];
		     for(int i=0;i<dt.getRows().size();i++){
		    	 DataRow dr = dt.getRows().get(i);
		    	 double f1 = Float.valueOf(dr.getData(0));
		    	 double f2 = Float.valueOf(dr.getData(1));
		    	 data[0][i] = f2;
		    	 data[1][i] = f1;
		     }
			 double[] xArray = Arrays.copyOf(data[0], data[0].length);
			 double[] yArray = Arrays.copyOf(data[1], data[1].length);
				
			 Arrays.sort(xArray);
			 double minX=xArray[0];
			 double maxX=xArray[xArray.length-1];
			 long n = AlpineMath.adjustUnits(minX, maxX);
			 for(int i=0;i<data[0].length;i++){
				 data[0][i]=data[0][i]/n;
			 }
			 
			 Arrays.sort(yArray);
			 double minY=yArray[0];
			 double maxY=yArray[yArray.length-1];
			 long m = AlpineMath.adjustUnits(minY, maxY);
			 for(int i=0;i<data[1].length;i++){
				 data[1][i]=data[1][i]/m;
			 }
			 
		     xydataset.addSeries(dependentColumn, data);
		     
			 String xLabel=n==1?referenceColumn:referenceColumn
						+" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
			 String yLabel=m==1?dependentColumn:dependentColumn
					 +" "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(m)+")";
			 
			 
		     chart = ChartFactory.createScatterPlot(null, xLabel,yLabel,  xydataset, PlotOrientation.VERTICAL, true, true, false);
		     XYPlot  plot   =   chart.getXYPlot();
		     
			 plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
			 plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());

			 if(minX<maxX){
			     double ad[] =Regression.getOLSRegression(xydataset, 0);
			     LineFunction2D linefunction2d = new LineFunction2D(ad[0], ad[1]);
			     XYDataset xyLinedataset1 = DatasetUtilities.sampleFunction2D(linefunction2d, minX/n,maxX/n, 100, dependentColumn);
			     plot.setDataset(1, xyLinedataset1);
			     StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
				 standardxyitemrenderer.setSeriesPaint(0,Color.blue);
				 standardxyitemrenderer.setSeriesShapesFilled(0, true);
				 plot.setRenderer(1, standardxyitemrenderer);
			 }
		     
		     XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();	 				 
			 Shape shapeScatter = VisualUtility.getScatterPoint();
			 renderer.setSeriesShape(0,shapeScatter);
			 renderer.setSeriesPaint(0,Color.blue);	
			 
			 renderer.setLegendItemLabelGenerator(new ScatterPlotXYSeriesLabelGenerator(ScatterPlotXYSeriesLabelGenerator.VALUE,false));
			 plot.getRenderer(1).setLegendItemLabelGenerator(new ScatterPlotXYSeriesLabelGenerator(ScatterPlotXYSeriesLabelGenerator.LINE,false));
		 }
		return chart;
	}
	
	class ScatterPlotXYSeriesLabelGenerator implements XYSeriesLabelGenerator {
		
		public static final int VALUE = 0;
		public static final int LINE = 1;
		private int type;
		private boolean addColumnName;
		
		public ScatterPlotXYSeriesLabelGenerator(int type,boolean addColumnName){
			this.type=type;
			this.addColumnName=addColumnName;
		}
	
		@Override
		public String generateLabel(XYDataset arg0, int arg1) {
			String label=null;	
			if(type==VALUE){		 
				label=VisualLanguagePack.getMessage(VisualLanguagePack.SCATTERMATRIX_VALUE,locale);
			}else if(type==LINE){
				label=VisualLanguagePack.getMessage(VisualLanguagePack.SCATTERMATRIX_LINE,locale);
			}
			if(addColumnName){
				String columnName = arg0.getSeriesKey(arg1).toString();
				label=StringHandler.doubleQ(columnName)+" "+label;
			}
			return label;
		}		
	}
}
