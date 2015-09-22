/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterARIMARPredictor.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.sql.Types;
import java.util.*;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutARIMARPredict;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.db.Column;
import com.alpine.datamining.operator.timeseries.ARIMARPredictResult;
import com.alpine.datamining.operator.timeseries.SingleARIMARPredictResult;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualLine;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;
import com.alpine.miner.workflow.output.visual.VisualizationModelLine;
import com.alpine.utility.file.StringUtil;

public class VisualAdapterARIMARPredictor extends VisualAdapterBaseARIMARPredictor
		implements OutPutVisualAdapter {


	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		
		if(!(analyzerOutPut instanceof AnalyzerOutPutARIMARPredict)){
			return null;
		}
		
		AnalyzerOutPutARIMARPredict model=(AnalyzerOutPutARIMARPredict)analyzerOutPut;
		ARIMARPredictResult aResult=model.getRet();
		int maxLines=OutPutVisualAdapterFactory.getInstance().getMaxChartElements();
		List<String> errorMessages = new ArrayList<String>();
		List<SingleARIMARPredictResult> aRsults = aResult.getResults();
		if(aRsults.size()>maxLines){
		 
			errorMessages.add(VisualNLS.getMessage(VisualNLS.BARS_EXCEED_LIMIT, locale, maxLines)); 
	 		aRsults=aRsults.subList(0, maxLines-1) ;
		}
		
	  	
		List<VisualizationModel> models= new ArrayList<VisualizationModel>(); 
		addTableResult(aRsults, models,  locale, aResult.getGroupColumnName());
		VisualizationModelLine  lineModel =createLineModels(aRsults,  locale); 
		models.add( lineModel);
		VisualizationModelComposite  visualModel= new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName()	,models);
		if(errorMessages.size()>0){
			lineModel.setErrorMessage(errorMessages);
		}
		
		return visualModel;
	}



	private void addTableResult(		List<SingleARIMARPredictResult> results,
			List<VisualizationModel> models,Locale locale, String groupByColumnName) {
		if(results!=null&&results.size()>0){
			VisualizationModel model = null;
			String outPutTitle = VisualNLS.getMessage(VisualNLS.TIMESERIES_PREDICTION_PREDICT,locale);
			if(StringUtil.isEmpty(groupByColumnName)){
				SingleARIMARPredictResult aRresult = (SingleARIMARPredictResult) results.get(0);
				DataTable dataTable = getTableOutPut(aRresult); 
				  
				model = new VisualizationModelDataTable(outPutTitle, dataTable);
				
			}else{
 
				List<String> keys = new ArrayList<String>() ;
				HashMap<String, VisualizationModel> modelMap = new HashMap<String, VisualizationModel>(); 
				
				for (Iterator iterator = results.iterator(); iterator.hasNext();) {
					SingleARIMARPredictResult singleARIMARPredictResult = (SingleARIMARPredictResult) iterator
							.next();
					DataTable dataTable = getTableOutPut(singleARIMARPredictResult); 
		  
					String key = singleARIMARPredictResult.getGroupByValue();
					VisualizationModelDataTable  tableModel=new VisualizationModelDataTable(singleARIMARPredictResult.getGroupByValue(), dataTable);
					keys.add(key);
					modelMap.put(key, tableModel) ;
					
				}
				 
				model = new VisualizationModelLayered(outPutTitle, groupByColumnName, keys, modelMap) ;
			}
			models.add( model);
		}

	}
 
	private DataTable getTableOutPut(SingleARIMARPredictResult aResult) {
 
		Object[] idDataArray = aResult.getIDData();
		double[] predictArray = aResult.getPredict();
		double[] seArray = aResult.getSe();
		TextTable table = new TextTable();
		String[] titleArray = new String[] { ID, RESULT, SE };
		String[] columnTypes = new String[]{DBUtil.TYPE_CATE,
				DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER};
	 
		table.addLine(titleArray);

		for (int i = 0; i < predictArray.length; i++) {
			table.addLine(new String[] { idDataArray[i].toString(),
					AlpineMath.powExpression(predictArray[i]),// String.valueOf(i+1)
					AlpineMath.powExpression(seArray[i]) });
		}
		
		DataTable dataTable=getDataTable4TimeSeries(table,columnTypes);
		return dataTable;
	}
 

	private VisualizationModelLine createLineModels(List<SingleARIMARPredictResult> aResults ,Locale locale) {
		List<VisualLine> lines = new ArrayList<VisualLine>();
		List<String[]> xLabels = new ArrayList<String[]> ();

		int index =0;
		boolean isDate=false;
		List<float[]> precisionList = new ArrayList<float[]>();
		for (Iterator iterator = aResults.iterator(); iterator.hasNext();) {
			SingleARIMARPredictResult singleARIMARPredictResult = (SingleARIMARPredictResult) iterator
					.next();
			List<String[]> xlabel = addLinesForSingleResult(lines, singleARIMARPredictResult,index,  locale,precisionList);
			xLabels.addAll(xlabel) ;
			index=index+1;
			int idType = singleARIMARPredictResult.getType();
			if (idType == Types.DATE || idType == Types.TIME
					|| idType == Types.TIMESTAMP) {
				isDate=true;
			}
		}
	 	VisualizationModelLine  lineModel=new VisualizationModelLine(
	 			VisualNLS.getMessage(VisualNLS.TIMESERIES_PREDICTION_SHARP,locale) , lines);

	 	
		lineModel.setMarkers(true);
		String[] precisionTitles = getPrecisionTitles(precisionList);
		lineModel.setxAxisTitle(aResults.get(0).getIdColumn().getName()+precisionTitles[0]);
		lineModel.setyAxisTitle(VisualNLS.getMessage(VisualNLS.VALUE,locale)+precisionTitles[1]) ;
		lineModel.setWidth(900) ;
		lineModel.setHeight(600) ;
	
 
		 if(isDate == true){
			 	handleDateLineModelLabel(lineModel,xLabelMap);			 	
			 }
		 else{
			 	handleNumericModelLabel(lineModel,xLabels);
		 }
	
		return lineModel;
	}
 



	//max index =29 (max_count =30)
	protected List<String[]> addLinesForSingleResult(List<VisualLine> lines,
			SingleARIMARPredictResult aResult,int index,Locale locale,List<float[]> precisionList) {
		List<String[]> xLabels = new ArrayList<String[]> ();
		List<String[]> yLabels = new ArrayList<String[]> ();

		  Column idAttribute = aResult.getIdColumn();
		Object[] trainedIDs = aResult.getTrainLastIDData();
		Object[] predictIDs = aResult.getIDData();
		int idType = aResult.getType();

		double[] trainLastArray = aResult.getTrainLastData();
		double[] predictArray = aResult.getPredict();

		String suffix ="";
		if(aResult.getGroupByValue()!=null&&aResult.getGroupByValue().trim().length()>0)
		{
			suffix = "_"+aResult.getGroupByValue();
		}
		VisualLine trainedLine = new VisualLine(
				VisualNLS.getMessage(VisualNLS.TIMESERIES_PREDICTION_ORIGIN,locale)
				+suffix);
		VisualLine predictedLine = new VisualLine(
				VisualNLS.getMessage(VisualNLS.TIMESERIES_PREDICTION_PREDICT,locale)
				+suffix);

		// all from java.util.Date
		if (idType == Types.DATE || idType == Types.TIME
				|| idType == Types.TIMESTAMP) {
			fillTimeLines(xLabels, yLabels, trainedIDs, trainLastArray,		trainedLine);
			fillTimeLines(xLabels, yLabels, predictIDs, predictArray,	predictedLine);
		} else {
            fillNoneTimeLines(xLabels,yLabels,trainedIDs,trainLastArray,predictIDs,predictArray,trainedLine,predictedLine,precisionList);
			//fillNoneTimeLine(xLabels, yLabels, trainedIDs, trainLastArray, trainedLine,precisionList);
			//fillNoneTimeLine(xLabels, yLabels, predictIDs, predictArray, predictedLine,precisionList);
		}

		lines.add(trainedLine);
		String color = getRandomColor(index) ;
		trainedLine.setColor(color);

		lines.add(predictedLine);
		predictedLine.setColor(color) ;
		return xLabels;
	}






}
