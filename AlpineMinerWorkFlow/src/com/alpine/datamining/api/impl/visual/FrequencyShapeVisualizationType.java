package com.alpine.datamining.api.impl.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.operator.attributeanalysisresult.FrequencyAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueFrequencyAnalysisResult;
import com.alpine.miner.view.ui.dataset.DropDownListEntity;

public class FrequencyShapeVisualizationType extends TableVisualizationType {
	private static final double MAX_SHOW = 30.0;
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		List<ValueFrequencyAnalysisResult> list = null;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof FrequencyAnalysisResult){
				 
				list = ((FrequencyAnalysisResult)obj).getFrequencyAnalysisResult();
			}
		}
		if(list == null)return null;
		List<ValueFrequencyAnalysisResult> colAnalysisResultList = list;
		Map<String, DefaultCategoryDataset> dataSets = getDataSets(colAnalysisResultList);
		
		DropDownListVisualizationOutPut output=null;
		Iterator<ValueFrequencyAnalysisResult> iter=colAnalysisResultList.iterator();
		while(iter.hasNext()){
			ValueFrequencyAnalysisResult first=iter.next();
			DefaultCategoryDataset dataSet = dataSets.get(first.getColumnName());
			JFreeChart chart = createChart(dataSet);
			chart.setTitle(first.getColumnName());
//			JFreeChart chart = generateChart(first);
			DropDownListEntity entity=new DropDownListEntity();
			entity.setJfreechart(chart);
			entity.setObj(obj);
			entity.setResult(colAnalysisResultList);
			output = new DropDownListVisualizationOutPut(entity);
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.COUNT_SHAPE_ANALYSIS,locale));
			break;
		}
		output.setList(colAnalysisResultList);
		return output;
	}
	private static Map<String,DefaultCategoryDataset>  getDataSets(List<ValueFrequencyAnalysisResult> list) {
		Map<String,DefaultCategoryDataset> dataSetMap=new HashMap<String,DefaultCategoryDataset>();
		HashMap<String,List<ValueFrequencyAnalysisResult>> resultMap=new HashMap<String,List<ValueFrequencyAnalysisResult>>();
		for (ValueFrequencyAnalysisResult freqAnalysisResult: list){
			if(!resultMap.containsKey(freqAnalysisResult.getColumnName())){
				List<ValueFrequencyAnalysisResult> resultList=new ArrayList<ValueFrequencyAnalysisResult>();
				resultList.add(freqAnalysisResult);
				resultMap.put(freqAnalysisResult.getColumnName(), resultList);
			}else{
				resultMap.get(freqAnalysisResult.getColumnName()).add(freqAnalysisResult);
			}
		}
		long count=0;
		String lastColumn="";
		for (ValueFrequencyAnalysisResult freqAnalysisResult: list) {
			if(freqAnalysisResult.getColumnName().equals(lastColumn)){
				count++;
			}else{
				count=0;
			}
			if(freqAnalysisResult.getColumnValue() != null){
				long total=resultMap.get(freqAnalysisResult.getColumnName()).size();
				if(total<=10||(count%(total/MAX_SHOW))<1){
					if(!dataSetMap.containsKey(freqAnalysisResult.getColumnName())){
						DefaultCategoryDataset dataset =new DefaultCategoryDataset();
						dataset.addValue(freqAnalysisResult.getCount(), freqAnalysisResult.getColumnName(), freqAnalysisResult.getColumnValue());
						dataSetMap.put(freqAnalysisResult.getColumnName(), dataset);
					}else{
						dataSetMap.get(freqAnalysisResult.getColumnName()).addValue(freqAnalysisResult.getCount(), freqAnalysisResult.getColumnName(), freqAnalysisResult.getColumnValue());
					}
				}
			}
		}
		return dataSetMap;
	}
	
	@SuppressWarnings("unchecked")
	public   JFreeChart createChart(String columnName,DropDownListEntity entity){
		List<ValueFrequencyAnalysisResult> colAnalysisResultList=(List<ValueFrequencyAnalysisResult>)(entity.getResult());
		Map<String, DefaultCategoryDataset> dataSets = getDataSets(colAnalysisResultList);
		DefaultCategoryDataset dataSet = dataSets.get(columnName);
		JFreeChart chart = createChart(dataSet);
		chart.setTitle(columnName);
		return chart;	
	}
	
	private   JFreeChart createChart(
			DefaultCategoryDataset dataset) {	
		JFreeChart chart=ChartFactory.createBarChart("",VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale),VisualLanguagePack.getMessage(VisualLanguagePack.COUNT,locale), dataset, PlotOrientation.VERTICAL,true,true,true);
		CategoryPlot   categoryplot   =   (CategoryPlot)chart.getPlot();
		chart.getLegend().setItemFont(VisualResource.getChartFont());
		CategoryAxis dox  = categoryplot.getDomainAxis();
		dox.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		
		categoryplot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
		categoryplot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		categoryplot.getDomainAxis().setTickLabelFont(VisualResource.getChartFont());
		CategoryItemRenderer   categoryitemrenderer   =   categoryplot.getRenderer(); 
		BarRenderer   custombarrenderer3d   =  (BarRenderer)categoryitemrenderer;
		custombarrenderer3d.setMaximumBarWidth(0.008D);

		return chart;
	}
}
