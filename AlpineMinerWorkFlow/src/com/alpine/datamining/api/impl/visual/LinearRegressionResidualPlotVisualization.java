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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.utility.tools.AlpineMath;

public class LinearRegressionResidualPlotVisualization extends ImageVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {

		EngineModel obj = null;
		if (analyzerOutPut instanceof AnalyzerOutPutTrainModel) {
			obj = ((AnalyzerOutPutTrainModel) analyzerOutPut).getEngineModel();
		}

		String dependentColumn = obj.getDependentColumn();
		LinearRegressionModelDB model=null;
		
		boolean isGroupBy=false;
		String ditinctValue="";
		if(obj.getModel() instanceof LinearRegressionGroupGPModel){
			Map<String, LinearRegressionModelDB> modelList = ((LinearRegressionGroupGPModel)obj.getModel()).getModelList();
			Iterator<Entry<String, LinearRegressionModelDB>> iter = modelList.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, LinearRegressionModelDB> entry = iter.next();
				ditinctValue=entry.getKey();
				model = entry.getValue();
				break;
			}
			isGroupBy=true;
		}else{
			model=(LinearRegressionModelDB)obj.getModel();
		}
		
		List<double[]> residuals = model.getResiduals();
		
		JFreeChart chart = generateResidualPlot(dependentColumn, residuals);

		JFreeChartImageVisualizationOutPut visualizationOutput = new JFreeChartImageVisualizationOutPut(
				chart);
		
		if(isGroupBy){
			visualizationOutput.setName(VisualLanguagePack.getMessage(VisualLanguagePack.SPLITMODEL_GROUP,locale)+":"+ditinctValue
					+":"+VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUALPLOT_TITLE,locale));
		}else{
			visualizationOutput.setName(VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUALPLOT_TITLE,locale));
		}
		return visualizationOutput;

	}

	public static JFreeChart generateResidualPlot(String dependentColumn,
			List<double[]> residuals) {
		double[][] data = new double[2][residuals.size()];
		for (int i = 0; i < residuals.size(); i++) {
			double[] row = residuals.get(i);
			double f1 = row[0];
			double f2 = row[1];
			if(Math.abs(f2)<Math.pow(10, -5)){
				f2=0.0d;
			}
			data[0][i] = f1;
			data[1][i] = f2;
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
				 dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.FITVALUE,Locale.getDefault()):
				 dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.FITVALUE,Locale.getDefault())
					+"   "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,Locale.getDefault())+
					" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
		 String yLabel=m==1?
				 dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUAL,Locale.getDefault()):
				dependentColumn+ " "+ VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUAL,Locale.getDefault())
						+"   "+VisualLanguagePack.getMessage(VisualLanguagePack.UNITS,Locale.getDefault())+
						" ("+com.alpine.datamining.api.utility.AlpineMath.powExpression(n)+")";
		 
		DefaultXYDataset xydataset = new DefaultXYDataset();
		xydataset.addSeries(dependentColumn, data);
		JFreeChart chart = ChartFactory.createScatterPlot(null, xLabel, yLabel, xydataset, PlotOrientation.VERTICAL, false,
				true, false);
		XYPlot plot = chart.getXYPlot();

		plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());

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
