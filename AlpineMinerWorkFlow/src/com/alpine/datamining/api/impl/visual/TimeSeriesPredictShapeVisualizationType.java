package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutARIMARPredict;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.operator.timeseries.ARIMARPredictResult;
import com.alpine.datamining.operator.timeseries.SingleARIMARPredictResult;
import com.alpine.datamining.utility.DataType;
import com.alpine.miner.view.ui.dataset.DropDownListEntity;
import com.alpine.utility.tools.AlpineMath;

public class TimeSeriesPredictShapeVisualizationType extends
		TextVisualizationType {

	private static final double CHARTMAXWIDTH = 10000;
	
	private static String dataTypeFormat="MM-dd-yyyy";
	private static String timeTypeFormat="HH-mm-ss";
	private static String dataTimeTypeFormat="MM-dd-yyyy-HH-mm-ss";
	
	private static ARIMARPredictResult aResult;

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		if(!(analyzerOutPut instanceof AnalyzerOutPutARIMARPredict))return null;
		AnalyzerOutPutARIMARPredict model=(AnalyzerOutPutARIMARPredict)analyzerOutPut;
		aResult=model.getRet();
		Column idAttribute =aResult.getResults().get(0).getIdColumn();
		Object[] lastDataObjs=aResult.getResults().get(0).getTrainLastIDData();
		Object[] predictObjs=aResult.getResults().get(0).getIDData();
		int idType=aResult.getResults().get(0).getType();
		JFreeChart chart=null;
		double[] trainLastData=aResult.getResults().get(0).getTrainLastData();
		double[] predictArray=aResult.getResults().get(0).getPredict();
		XYSeries trainLastDataSeries= new XYSeries(
				VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_ORIGIN,locale));
		chart = genereateChart(idAttribute, lastDataObjs, predictObjs, idType,
				trainLastData, predictArray, trainLastDataSeries,aResult.getResults().get(0).getGroupByValue());
		
		VisualizationOutPut vout=null;
		if(aResult.getResults().size()==1){//no groupby
			vout = new JFreeChartImageVisualizationOutPut(chart);
			vout.setName(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_SHARP,locale));
		}else{
			DropDownListEntity entity=new DropDownListEntity();
			entity.setJfreechart(chart);
			entity.setObj(aResult);
			entity.setResult(aResult.getResults());
			vout = new DropDownListVisualizationOutPut(entity);
			vout.setName(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_SHARP,locale));
			((DropDownListVisualizationOutPut)vout).setList(aResult.getResults());
		}
		return vout;
	}

	private   JFreeChart genereateChart(Column idAttribute,
			Object[] lastDataObjs, Object[] predictObjs, int idType,
			double[] trainLastData, double[] predictArray,
			XYSeries trainLastDataSeries, String columnValue) {
		JFreeChart chart;
		if(idType==Types.DATE){
			TimeSeries lastDataTimeseries = new TimeSeries(VisualLanguagePack.getMessage(
					VisualLanguagePack.TIMESERIES_PREDICTION_ORIGIN,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_ORIGIN,locale),"a");
			for(int i=1;i<lastDataObjs.length+1;i++){
				Date d=(Date)lastDataObjs[i-1];
				lastDataTimeseries.add(new Day(d), trainLastData[i-1]);			
			}
			TimeSeries predictDataTimeseries = new TimeSeries(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_PREDICT,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_PREDICT,locale),"b");
			for(int i=1;i<predictObjs.length+1;i++){
				Date d=(Date)predictObjs[i-1];
				predictDataTimeseries.add(new Day(d), predictArray[i-1]);			
			}
			chart = generateTimeSeriesChart(lastDataTimeseries,predictDataTimeseries,dataTypeFormat,columnValue);
		}else if(idType==Types.TIME){
			TimeSeries timeseries = new TimeSeries(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_ORIGIN,locale),VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale),"");
			for(int i=1;i<lastDataObjs.length+1;i++){
				Time d=(Time)lastDataObjs[i-1];
				timeseries.add(new Second(d), trainLastData[i-1]);			
			}
			TimeSeries predictDataTimeseries = new TimeSeries(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_PREDICT,locale),VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale),"");
			for(int i=1;i<predictObjs.length+1;i++){
				Time d=(Time)predictObjs[i-1];
				predictDataTimeseries.add(new Second(d), predictArray[i-1]);			
			}
			chart = generateTimeSeriesChart(timeseries,predictDataTimeseries,timeTypeFormat,columnValue);
		}else if(idType==Types.TIMESTAMP){
			TimeSeries timeseries = new TimeSeries(
					VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_ORIGIN,locale),VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale),"");
			for(int i=1;i<lastDataObjs.length+1;i++){
				Timestamp d=(Timestamp)lastDataObjs[i-1];
				timeseries.add(new Second(d), trainLastData[i-1]);			
			}
			TimeSeries predictDataTimeseries = new TimeSeries(
					VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_PREDICT,locale),VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale),"");
			for(int i=1;i<predictObjs.length+1;i++){
				Timestamp d=(Timestamp)predictObjs[i-1];
				predictDataTimeseries.add(new Second(d), predictArray[i-1]);			
			}
			chart = generateTimeSeriesChart(timeseries,predictDataTimeseries,dataTimeTypeFormat,columnValue);
		}else if(idAttribute.isNumerical()){
			if(idAttribute.getValueType() == DataType.INTEGER){
						chart = generateIntegerTypeChart(predictArray, lastDataObjs,
				predictObjs, trainLastData, trainLastDataSeries);
			}else{
						chart = generateDoubleTypeChart(predictArray, lastDataObjs,
				predictObjs, trainLastData, trainLastDataSeries);
			}
		}else{
			chart = generateIntegerTypeChart(predictArray, lastDataObjs,
			predictObjs, trainLastData, trainLastDataSeries);
		}
		return chart;
	}

	@SuppressWarnings("unchecked")
	private   JFreeChart generateTimeSeriesChart(TimeSeries lastDataTimeseries,TimeSeries predictTimeseries,String dataFormat,String columnName) {
		JFreeChart chart;
					TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
					timeseriescollection.addSeries(lastDataTimeseries);
					timeseriescollection.addSeries(predictTimeseries);
					
					List<Double> valueList=new ArrayList<Double>();
					List<TimeSeries> series = timeseriescollection.getSeries();
					for(TimeSeries timeSeries:series){
						for(int i=0;i<timeSeries.getItemCount();i++){
							Number value = timeSeries.getValue(i);
							valueList.add(value.doubleValue());		
						}
					}
					Double[] valueArray=valueList.toArray(new Double[valueList.size()]);
					Arrays.sort(valueArray);
					
					double minY=valueArray[0];
					double maxY=valueArray[valueArray.length-1];
					
					long n = AlpineMath.adjustUnits(minY, maxY);
					if(n!=1){
						for(TimeSeries timeSeries:series){
							for(int i=0;i<timeSeries.getItemCount();i++){
								Number value = timeSeries.getValue(i);
								timeSeries.update(i, value.doubleValue()/n);
							}
						}
					}
					
					 String yLabel=n==1?
							 " ":VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,Locale.getDefault())+
									" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
					
					chart = ChartFactory.createTimeSeriesChart(
							VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES,locale), // title
							VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale), // x-axis label
							yLabel, // y-axis label
							timeseriescollection, // data
							true, // create legend?
							true, // generate tooltips?
							false // generate URLs?
							);
					chart.getLegend().setItemFont(VisualResource.getChartFont());
					chart.setBackgroundPaint(Color.white);
					chart.setTitle(columnName);
					XYPlot xyplot = (XYPlot) chart.getPlot();
					xyplot.setBackgroundPaint(Color.lightGray);
					xyplot.setDomainGridlinePaint(Color.white);
					xyplot.setRangeGridlinePaint(Color.white);
					xyplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
					xyplot.setDomainCrosshairVisible(true);
					xyplot.setRangeCrosshairVisible(true);
					xyplot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
					xyplot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
					org.jfree.chart.renderer.xy.XYItemRenderer xyitemrenderer = xyplot
							.getRenderer();
					if (xyitemrenderer instanceof XYLineAndShapeRenderer) {
						XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyitemrenderer;
						xylineandshaperenderer.setBaseShapesVisible(true);
						xylineandshaperenderer.setBaseShapesFilled(true);
					}
					DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
					dateaxis.setDateFormatOverride(new SimpleDateFormat(dataFormat));
		return chart;
	}

	private   JFreeChart generateIntegerTypeChart(double[] predictArray,
			Object[] lastDataObjs, Object[] predictObjs,
			double[] trainLastData, XYSeries trainLastDataSeries) {
		double[] trainLastDataCopy = Arrays.copyOf(trainLastData, trainLastData.length);
		double[] predictArrayCopy = Arrays.copyOf(predictArray, predictArray.length);
		
		String yLabel = doPowExpression(trainLastDataCopy,predictArrayCopy);
				 
		XYSeries predictSeries= new XYSeries(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_PREDICT,locale));
		
		for(int i=1;i<trainLastDataCopy.length+1;i++){
			Long d=(Long)lastDataObjs[i-1];
			trainLastDataSeries.add(d.longValue(), trainLastDataCopy[i-1]);	
		}
		
		for(int i=1;i<predictArrayCopy.length+1;i++){
			if((i-1)%(predictArrayCopy.length/CHARTMAXWIDTH)<1){
				Long d=(Long)predictObjs[i-1];
				predictSeries.add(d.longValue(), predictArrayCopy[i-1]);
			}		
		}
		JFreeChart chart = generateXYlineChart(trainLastDataSeries,
				predictSeries,yLabel);
		return chart;
	}

	private String doPowExpression(double[] trainLastData,double[] predictArray) {	
		double[] trainLastDataCopy = Arrays.copyOf(trainLastData, trainLastData.length);
		double[] predictArrayCopy = Arrays.copyOf(predictArray, predictArray.length);
		
		Arrays.sort(trainLastDataCopy);
		double minT=trainLastDataCopy[0];
		double maxT=trainLastDataCopy[trainLastDataCopy.length-1];
		
		Arrays.sort(predictArrayCopy);
		double minP=predictArrayCopy[0];
		double maxP=predictArrayCopy[predictArrayCopy.length-1];
		
		double minY=0;
		double maxY=0;
		
		if(minT<minP){
			minY=minT;
		}else{
			minY=minP;
		}
		
		if(maxT>maxP){
			maxY=maxT;
		}else{
			maxY=maxP;
		}
		
		long n = AlpineMath.adjustUnits(minY, maxY);
		for(int i=0;i<trainLastData.length;i++){
			trainLastData[i]=trainLastData[i]/n;
		}	
		
		for(int i=0;i<predictArray.length;i++){
			predictArray[i]=predictArray[i]/n;
		}
		
		 String yLabel=n==1?VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale):
			 VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale)
				 +"   "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,locale)+
					" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
		return yLabel;
	}

	private   JFreeChart generateDoubleTypeChart(double[] predictArray,
			Object[] lastDataObjs, Object[] predictObjs,
			double[] trainLastData, XYSeries trainLastDataSeries) {
		double[] trainLastDataCopy = Arrays.copyOf(trainLastData, trainLastData.length);
		double[] predictArrayCopy = Arrays.copyOf(predictArray, predictArray.length);
		
		String yLabel = doPowExpression(trainLastDataCopy,predictArrayCopy);
		
		XYSeries predictSeries= new XYSeries(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_PREDICT,locale));
		
		for(int i=1;i<trainLastDataCopy.length+1;i++){
			Double d=(Double)trainLastDataCopy[i-1];
			trainLastDataSeries.add(d.doubleValue(), trainLastDataCopy[i-1]);	
		}
		
		for(int i=1;i<predictArrayCopy.length+1;i++){
			if((i-1)%(predictArrayCopy.length/CHARTMAXWIDTH)<1){
				Double d=(Double)predictObjs[i-1];
				predictSeries.add(d.doubleValue(), predictArrayCopy[i-1]);
			}		
		}
		JFreeChart chart = generateXYlineChart(trainLastDataSeries,
					predictSeries,yLabel);
		return chart;
	}
	private   JFreeChart generateXYlineChart(XYSeries trainLastDataSeries,
			XYSeries predictSeries, String yLabel) {
		final XYSeriesCollection[] dataset = new XYSeriesCollection[2];
		 dataset[0]= new XYSeriesCollection();
		 dataset[0].addSeries(trainLastDataSeries);
		 dataset[1]= new XYSeriesCollection();
		 dataset[1].addSeries(predictSeries);
		 
		 JFreeChart chart = ChartFactory.createXYLineChart("", "",
				 yLabel, null,
				 PlotOrientation.VERTICAL, true, true, true);
		 chart.getLegend().setItemFont(VisualResource.getChartFont());
			final XYPlot plot = chart.getXYPlot();
			plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
			plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
			
			 XYLineAndShapeRenderer[] renderer = new XYLineAndShapeRenderer[dataset.length];
			 
			 plot.setDataset(1, dataset[0]);
			 renderer[0] = new XYLineAndShapeRenderer();
			 renderer[0].setSeriesPaint(0, Color.BLUE);
			 plot.setRenderer(1, renderer[0]);
			 
			 plot.setDataset(2, dataset[1]);
			 renderer[1] = new XYLineAndShapeRenderer();
			 renderer[1].setSeriesPaint(1, Color.RED);
			 plot.setRenderer(2, renderer[1]);
		return chart;
	}

	@SuppressWarnings("unchecked")
	public   JFreeChart createChart(String groupByValue,
			DropDownListEntity entity) {
		List<SingleARIMARPredictResult> results=(List<SingleARIMARPredictResult>)(entity.getResult());
		for(SingleARIMARPredictResult result:results){
			if(!result.getGroupByValue().equals(groupByValue)){
				continue;
			}
			XYSeries trainLastDataSeries= new XYSeries(VisualLanguagePack.getMessage(VisualLanguagePack.TIMESERIES_PREDICTION_ORIGIN,locale));
			return genereateChart(result.getIdColumn(), result.getTrainLastIDData(), 
					result.getIDData(), result.getType(),
					result.getTrainLastData(), result.getPredict(), trainLastDataSeries,groupByValue);
		}
		return null;
	}

}
