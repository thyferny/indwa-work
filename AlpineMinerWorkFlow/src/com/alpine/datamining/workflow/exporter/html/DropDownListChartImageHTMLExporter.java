package com.alpine.datamining.workflow.exporter.html;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.visual.DropDownListVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.FrequencyShapeVisualizationType;
import com.alpine.datamining.api.impl.visual.HistogramShapeVisualizationType;
import com.alpine.datamining.api.impl.visual.TimeSeriesPredictShapeVisualizationType;
import com.alpine.datamining.api.impl.visual.ValueNumericVisualizationType;
import com.alpine.datamining.api.impl.visual.ValueShapeVisualizationType;
import com.alpine.datamining.operator.attributeanalysisresult.BinHistogramAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ColumnValueAnalysisResult;
import com.alpine.datamining.operator.attributeanalysisresult.ValueFrequencyAnalysisResult;
import com.alpine.datamining.operator.timeseries.SingleARIMARPredictResult;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.view.ui.dataset.DropDownListEntity;
import org.apache.log4j.Logger;


public class DropDownListChartImageHTMLExporter implements VisualOutPutHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(DropDownListChartImageHTMLExporter.class);


    @Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList, String rootPath) throws Exception {
		  ValueShapeVisualizationType valueShapeVType = new ValueShapeVisualizationType();
		  ValueNumericVisualizationType valueNumVType=new ValueNumericVisualizationType();
		  FrequencyShapeVisualizationType frequencyShapeVType=new FrequencyShapeVisualizationType();
		 HistogramShapeVisualizationType histogramShapeVType=new HistogramShapeVisualizationType();
		  TimeSeriesPredictShapeVisualizationType tspShapeVType=new TimeSeriesPredictShapeVisualizationType();
//		StringBuffer resultList= new StringBuffer();
		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		List<String> temList=new ArrayList<String>();
		DropDownListVisualizationOutPut out=(DropDownListVisualizationOutPut)visualizationOutPut;
		List list = out.getList();
		if(list!=null){
			Iterator iter=list.iterator();
			String type=out.getEntity().getType();
			List neednotList=out.getEntity().getList();
			while(iter.hasNext()){
				Object obj=iter.next();
					JFreeChart chart=null;
				if(obj instanceof ColumnValueAnalysisResult){
					ColumnValueAnalysisResult result=(ColumnValueAnalysisResult)obj;
					if(type!=null&&type.equals("count")){
						chart=valueShapeVType.generateChart(result);
					}else{
						if(neednotList!=null&&!neednotList.contains(result.getColumnName())){				
							chart=valueNumVType.generateChart(result);
						}				
					}			
				}
				else if(obj instanceof ValueFrequencyAnalysisResult){
					ValueFrequencyAnalysisResult result=(ValueFrequencyAnalysisResult)obj;
					if(temList.contains(result.getColumnName()))continue;
					temList.add(result.getColumnName());		
					chart=frequencyShapeVType.createChart(result.getColumnName(), out.getEntity());
				}else if(obj instanceof BinHistogramAnalysisResult){
					BinHistogramAnalysisResult result=(BinHistogramAnalysisResult)obj;
					if(temList.contains(result.getColumnName()))continue;
					temList.add(result.getColumnName());
					chart=histogramShapeVType.createChart(result.getColumnName(), out.getEntity());
				}else if(obj instanceof SingleARIMARPredictResult){
					SingleARIMARPredictResult result=(SingleARIMARPredictResult)obj;
					chart=tspShapeVType.createChart(result.getGroupByValue(), out.getEntity());
				}
				if(chart==null)continue;
				String imageFile = getImage(chart,tempFileList, rootPath);

				htmlWriter.writeImg(imageFile);
			}
		}else{
			DropDownListEntity entity = out.getEntity();
			JFreeChart jfreeChart= (JFreeChart)entity.getJfreechart();
			
			int i = rootPath.lastIndexOf(File.separator);
			String curdir = rootPath.substring(0, i);
			String name = System.currentTimeMillis()+".jpg";
			String fileName=curdir+File.separator+name;
			//500*350 is the fit
			int defaultWidth=500;
			int defaultHeight=350;
			//always use this 
			ChartUtilities.saveChartAsJPEG(new File(fileName),jfreeChart, defaultWidth, defaultHeight);
			itsLogger.debug("JFreeChartImageHTMLExporter export to:"+fileName);
			String imageFile = "."+File.separator+name;
			htmlWriter.writeImg(imageFile);
		}

		return  htmlWriter.toStringBuffer();
	}
	private String getImage(JFreeChart chart, List<String> tempFileList, String rootPath )   throws  Exception {
		int i = rootPath.lastIndexOf(File.separator);
		String curdir = rootPath.substring(0, i);
		String name = System.currentTimeMillis()+".jpg";
		String fileName=curdir+File.separator+name;
		int defaultWidth=500;
		int defaultHeight=350;
		ChartUtilities.saveChartAsJPEG(new File(fileName),chart, defaultWidth, defaultHeight);
		itsLogger.debug("DropDownListChartImageHTMLExporter export to:"+fileName);
		String imageFile = "."+File.separator+name;
		return imageFile;
	}
}
