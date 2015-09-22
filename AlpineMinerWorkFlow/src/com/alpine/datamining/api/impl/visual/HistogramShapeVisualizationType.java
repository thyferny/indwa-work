package com.alpine.datamining.api.impl.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.impl.visual.resource.VisualResource;
import com.alpine.datamining.api.impl.visual.resource.VisualUtility;
import com.alpine.datamining.operator.attributeanalysisresult.BinHistogramAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.HistogramAnalysisResult;
import com.alpine.miner.view.ui.dataset.DropDownListEntity;
import com.alpine.utility.db.DataSourceType;

public class HistogramShapeVisualizationType extends TableVisualizationType {
	static DataSourceType stype ;
	
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		stype = DataSourceType.getDataSourceType(
				analyzerOutPut.getAnalyticNode().getSource().getDataSourceType());
		Object obj = null;
		List<BinHistogramAnalysisResult> list = null;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof HistogramAnalysisResult){
				 
				list = ((HistogramAnalysisResult)obj).getResult();
			}
		}
		if(list == null)return null;
		List<BinHistogramAnalysisResult> colAnalysisResultList = list;
		DropDownListVisualizationOutPut output=null;
		Iterator<BinHistogramAnalysisResult> iter=colAnalysisResultList.iterator();
		while(iter.hasNext()){
			BinHistogramAnalysisResult first=iter.next();
			Map<String, List<DefaultCategoryDataset>> dataSets = getDataSets(colAnalysisResultList);
			List<DefaultCategoryDataset> dataSetList = dataSets.get(first.getColumnName());
			JFreeChart chart = createChart(dataSetList.get(0),dataSetList.get(1),dataSetList.get(2));
			chart.setTitle(first.getColumnName());
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
	
	@SuppressWarnings("unchecked")
	public  JFreeChart createChart(String columnName,DropDownListEntity entity){
		List<BinHistogramAnalysisResult> colAnalysisResultList=(List<BinHistogramAnalysisResult>)(entity.getResult());
		Map<String, List<DefaultCategoryDataset>> dataSets = getDataSets(colAnalysisResultList);
		List<DefaultCategoryDataset> dataSetList = dataSets.get(columnName);
		JFreeChart chart = createChart(dataSetList.get(0),dataSetList.get(1),dataSetList.get(2));
		chart.setTitle(columnName);
		return chart;		
	}
	
	private static Map<String,List<DefaultCategoryDataset>> getDataSets(List<BinHistogramAnalysisResult> list) {
		Map<String,List<DefaultCategoryDataset>> dataSets=new HashMap<String,List<DefaultCategoryDataset>>();
		HashMap<String,ArrayList<BinHistogramAnalysisResult>> analysisMap = new HashMap<String,ArrayList<BinHistogramAnalysisResult>>();
		String columnName = null;
		ArrayList<BinHistogramAnalysisResult> analysisList = null;
		for (BinHistogramAnalysisResult histogramResult: list) {
			if (!histogramResult.getColumnName().equals(columnName)) {
				if (columnName!=null) {
					analysisMap.put(columnName, analysisList);
				}
				columnName = histogramResult.getColumnName();
				analysisList = new ArrayList<BinHistogramAnalysisResult>();
			}
			analysisList.add(histogramResult);
		}
		if (columnName!=null) {
			analysisMap.put(columnName, analysisList);
		}
		
		
		Set<String> keySet = analysisMap.keySet();
		for (String key:keySet) {
			analysisList = analysisMap.get(key);
			columnName  = analysisList.get(0).getColumnName();
			DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
			DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
			DefaultCategoryDataset dataset3 = new DefaultCategoryDataset();
			for (BinHistogramAnalysisResult analysisResult:analysisList) {
				dataset1.addValue(
						analysisResult.getCount(), 
						columnName, 
						analysisResult.getBegin()+"-"+analysisResult.getEnd()
						);
				dataset2.addValue(
						analysisResult.getAccumCount(), 
						columnName, 
						analysisResult.getBegin()+"-"+analysisResult.getEnd()
						);
				dataset3.addValue(
						analysisResult.getPercentage(), 
						columnName, 
						analysisResult.getBegin()+"-"+analysisResult.getEnd()
						);
			}
			List<DefaultCategoryDataset> defaultCategoryDatasetList=new ArrayList<DefaultCategoryDataset>();
			defaultCategoryDatasetList.add(dataset1);
			defaultCategoryDatasetList.add(dataset2);
			defaultCategoryDatasetList.add(dataset3);
			dataSets.put(columnName, defaultCategoryDatasetList);
		}
		
		return dataSets;
	}
	
	private   JFreeChart createChart(
			DefaultCategoryDataset dataset1,DefaultCategoryDataset dataset2,DefaultCategoryDataset dataset3) {	
		NumberAxis rangeAxis1 = new NumberAxis(VisualLanguagePack.getMessage(VisualLanguagePack.COUNT,locale));
		rangeAxis1.setLabelFont(VisualResource.getChartFont());
		BarRenderer renderer1 = new BarRenderer();
		renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		renderer1.setMaximumBarWidth(0.008D);
//		renderer1.setShadowVisible(false);
		renderer1.setDrawBarOutline(false);
		CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
		subplot1.setForegroundAlpha(0.9f);
		
		NumberAxis rangeAxis2 = new NumberAxis(VisualLanguagePack.getMessage(VisualLanguagePack.ACC_COUNT,locale));
		rangeAxis2.setLabelFont(VisualResource.getChartFont());
		LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
		renderer2.setSeriesShape(0,VisualUtility.getCommonElliipseShape());
		renderer2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
		
		NumberAxis rangeAxis3 = new NumberAxis(VisualLanguagePack.getMessage(VisualLanguagePack.DENSITY,locale));
		rangeAxis3.setLabelFont(VisualResource.getChartFont());
		LineAndShapeRenderer renderer3 = new LineAndShapeRenderer();
		renderer3.setSeriesShape(0,VisualUtility.getCommonElliipseShape());
		renderer3.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
		CategoryPlot subplot3 = new CategoryPlot(dataset3, null, rangeAxis3, renderer3);
		
		
		CategoryAxis domainAxis = new CategoryAxis(VisualLanguagePack.getMessage(VisualLanguagePack.BIN,locale));
		domainAxis.setLabelFont(VisualResource.getChartFont());
		CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
		
		plot.add(subplot2);
		plot.add(subplot3);
		plot.add(subplot1);
		
		CategoryAxis dox  = plot.getDomainAxis();
		dox.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		
		JFreeChart chart = new JFreeChart(plot);
		chart.removeLegend();

		return chart;
	}
}
