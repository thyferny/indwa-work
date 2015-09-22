package com.alpine.datamining.api.impl.visual;

import java.awt.Color;
import java.awt.Shape;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.utility.NormDistributionUtility;
import com.alpine.utility.tools.AlpineMath;

public class LinearRegressionNormalProbabilityPlotVisualization extends ImageVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {

		EngineModel obj = null;
		if (analyzerOutPut instanceof AnalyzerOutPutTrainModel) {
			obj = ((AnalyzerOutPutTrainModel) analyzerOutPut).getEngineModel();
		}

		String dependentColumn = obj.getDependentColumn();
		
		LinearRegressionModelDB model=null;
		
		boolean isGroupBy=false;
		String distinctValue="";
		
		if(obj.getModel() instanceof LinearRegressionGroupGPModel){
			Map<String, LinearRegressionModelDB> modelList = ((LinearRegressionGroupGPModel)obj.getModel()).getModelList();
			Iterator<Entry<String, LinearRegressionModelDB>> iter = modelList.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, LinearRegressionModelDB> entry = iter.next();
				distinctValue=entry.getKey();
				model = entry.getValue();
				break;
			}
			isGroupBy=true;
		}else{
			model=(LinearRegressionModelDB)obj.getModel();
		}

		List<double[]> residuals = model.getResiduals();
		//avoid null point
		if(residuals==null||residuals.size()==0){
			return null;
		}
		double sValue = model.getS();;
		
		JFreeChart chart = generateNormalProbabilityPlot(dependentColumn,
				residuals, sValue);

		JFreeChartImageVisualizationOutPut visualizationOutput = new JFreeChartImageVisualizationOutPut(
				chart);
		
		if(isGroupBy){
			visualizationOutput.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+distinctValue
					+":"+VisualLanguagePack.getMessage(VisualLanguagePack.Q_Q_PLOT_TITLE,locale));
		}else{
			visualizationOutput.setName(VisualLanguagePack.getMessage(VisualLanguagePack.Q_Q_PLOT_TITLE,locale));
		}

		return visualizationOutput;
	}

	public static JFreeChart generateNormalProbabilityPlot(String dependentColumn,
			List<double[]> residuals, double sValue) {
		double[][] data = new double[2][residuals.size()];
		for (int i = 0; i < residuals.size(); i++) {
			double[] row = residuals.get(i);
			double f1 = row[1];
			if(Math.abs(f1)<Math.pow(10, -5)){
				f1=0.0d;
			}
			data[0][i] = f1;
		}
		Arrays.sort(data[0]);
		for (int i = 0; i < residuals.size(); i++) {
			data[1][i] = NormDistributionUtility.normDistributionQuantile((i+0.5)/residuals.size(),sValue);
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
		 
		 String xLabel=n==1?
				 dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.SAMPLE_QUANTILE,Locale.getDefault()):
				 dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.SAMPLE_QUANTILE,Locale.getDefault())
					+"   "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,Locale.getDefault())+
					" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
		 String yLabel=m==1?
				 dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.THEORY_QUANTILE,Locale.getDefault()):
				dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.THEORY_QUANTILE,Locale.getDefault())
						+"   "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,Locale.getDefault())+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
				 
		DefaultXYDataset xydataset = new DefaultXYDataset();
		xydataset.addSeries(dependentColumn, data);
		JFreeChart chart = ChartFactory.createScatterPlot(null, xLabel, yLabel, xydataset, PlotOrientation.VERTICAL, false,
				true, false);
		XYPlot plot = chart.getXYPlot();

		plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
			 		 
		if(minX<maxX){
		     double ad[] =Regression.getOLSRegression(xydataset, 0);
		     LineFunction2D linefunction2d = new LineFunction2D(ad[0], ad[1]);
		     XYDataset xyLinedataset1 = DatasetUtilities.sampleFunction2D(linefunction2d, minX/n,maxX/n, 100, dependentColumn);
		     plot.setDataset(1, xyLinedataset1);
		     StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
    		 standardxyitemrenderer.setSeriesPaint(0,Color.BLUE);
    		 standardxyitemrenderer.setSeriesShapesFilled(0, true);
    		 plot.setRenderer(1, standardxyitemrenderer);
		 }
		 
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot
				.getRenderer();
		Shape shapeScatter = VisualUtility.getScatterPoint();
		renderer.setSeriesShape(0, shapeScatter);
		renderer.setSeriesPaint(0, Color.black);

		plot.setRangeZeroBaselinePaint(Color.red);
		plot.setRangeZeroBaselineVisible(true);
		return chart;
	}
}
