package com.alpine.datamining.api.impl.visual;

import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueAnalysisResult;
import com.alpine.miner.view.ui.dataset.DropDownListEntity;
import com.alpine.utility.db.DataSourceType;

public class ValueShapeVisualizationType extends TableVisualizationType {
	static DataSourceType stype ;
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		stype = DataSourceType.getDataSourceType(
				analyzerOutPut.getAnalyticNode().getSource().getDataSourceType());
		Object obj = null;
		List<ColumnValueAnalysisResult> list = null;
		if(analyzerOutPut instanceof AnalyzerOutPutObject){
			obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
			if(obj instanceof ValueAnalysisResult){
				 
				list = ((ValueAnalysisResult)obj).getValueAnalysisResult();
			}
		}
		if(list == null)return null;
		List<ColumnValueAnalysisResult> colAnalysisResultList = list;
//		List<ColumnValueAnalysisResult> colAnalysisResultList_del=new ArrayList<ColumnValueAnalysisResult>();

		DropDownListVisualizationOutPut output=null;
//		Iterator<ColumnValueAnalysisResult> iter=colAnalysisResultList.iterator();
//		while(iter.hasNext()){
//			ColumnValueAnalysisResult first=iter.next();
//			if(stype.isNumberColumnType(first.getColumnType().toUpperCase())){
//				colAnalysisResultList_del.add(first);
//			}	
//		}
//		colAnalysisResultList.removeAll(colAnalysisResultList_del);
		Iterator<ColumnValueAnalysisResult> iter_r=colAnalysisResultList.iterator();
		while(iter_r.hasNext()){
			ColumnValueAnalysisResult first=iter_r.next();
			JFreeChart chart = generateChart(first);
			DropDownListEntity entity=new DropDownListEntity();
			entity.setJfreechart(chart);
			entity.setObj(obj);
			entity.setResult(colAnalysisResultList);
			entity.setType("count");
			output = new DropDownListVisualizationOutPut(entity);
			output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.COUNT_SHAPE_ANALYSIS,locale));
			output.setList(colAnalysisResultList);
			break;
		}

		return output;
	}
	public   JFreeChart generateChart(ColumnValueAnalysisResult first) {
		DefaultCategoryDataset dataset1 = addValue(first);
		JFreeChart chart = createChart(dataset1);
		chart.setTitle(first.getColumnName());
		return chart;
	}
	protected   DefaultCategoryDataset addValue(ColumnValueAnalysisResult result) {
		DefaultCategoryDataset dataset =new DefaultCategoryDataset();
		dataset.addValue(result.getCount(), VisualLanguagePack.getMessage(VisualLanguagePack.TOTAL_VALUE_COUNT,locale), "");
		dataset.addValue(result.getUniqueValueCount(), VisualLanguagePack.getMessage(VisualLanguagePack.UNIQUE_VALUE_COUNT,locale), "");
		dataset.addValue(result.getNullCount(), VisualLanguagePack.getMessage(VisualLanguagePack.NULL_COUNT,locale), "");
		dataset.addValue(result.getEmptyCount(), VisualLanguagePack.getMessage(VisualLanguagePack.EMPTY_COUNT,locale), "");
		dataset.addValue(result.getZeroCount(), VisualLanguagePack.getMessage(VisualLanguagePack.ZERO_COUNT,locale), "");
		dataset.addValue(result.getPositiveValueCount(), VisualLanguagePack.getMessage(VisualLanguagePack.POS_VALUE_COUNT,locale), "");
		dataset.addValue(result.getNegativeValueCount(), VisualLanguagePack.getMessage(VisualLanguagePack.NEG_VALUE_COUNT,locale), "");
//		if(stype.isNumberColumnType(result.getColumnType().toUpperCase())){
//			long n=AlpineMath.adjustUnits(result.getMin(), result.getMax());
//			dataset.addValue(result.getMin()/n, VisualLanguagePack.getMessage(VisualLanguagePack.MIN_VALUE, "");
//			dataset.addValue(result.getAvg()/n, VisualLanguagePack.getMessage(VisualLanguagePack.AVERAGE, "");
//			dataset.addValue(result.getDeviation()/n, VisualLanguagePack.getMessage(VisualLanguagePack.STANDARD_DEVIATION, "");
//			dataset.addValue(result.getMax()/n, VisualLanguagePack.getMessage(VisualLanguagePack.MAX_VALUE, "");
//		}
		return dataset;
	}
	public   JFreeChart createChart(String columnName,DropDownListEntity entity){
		List<ColumnValueAnalysisResult> colAnalysisResultList=(List<ColumnValueAnalysisResult>)(entity.getResult());
		Iterator<ColumnValueAnalysisResult> iter=colAnalysisResultList.iterator();
		while(iter.hasNext()){
			ColumnValueAnalysisResult result=iter.next();
			if(!result.getColumnName().equals(columnName))continue;
			JFreeChart chart = generateChart(result);
			return chart;
		}
		
		return null;
		
	}
	
	private   JFreeChart createChart(
			DefaultCategoryDataset dataset) {
		JFreeChart chart=ChartFactory.createBarChart("","",VisualLanguagePack.getMessage(VisualLanguagePack.VALUE,locale), dataset, PlotOrientation.VERTICAL,true,true,true);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.getDomainAxis().setLabelFont(VisualResource.getChartFont());
		plot.getRangeAxis().setLabelFont(VisualResource.getChartFont());
		chart.getLegend().setItemFont(VisualResource.getChartFont());
		CategoryPlot   categoryplot   =   (CategoryPlot)chart.getPlot();
		CategoryItemRenderer   categoryitemrenderer   =   categoryplot.getRenderer(); 
		BarRenderer   custombarrenderer3d   =  (BarRenderer)categoryitemrenderer;
		custombarrenderer3d.setMaximumBarWidth(0.008D);

		return chart;
	}
}
