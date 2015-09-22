/**
 * ClassName VariableSelectionTextAndTableVisualizationType
 *
 * Version information: 1.00
 *
 * Data: 2011-4-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionResult;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.miner.view.ui.dataset.TextAndTableListEntity;
import com.alpine.utility.db.DataTypeConverterUtil;

public class VariableSelectionTextAndTableVisualizationType extends TextVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = analyzerOutPut;
		
		if(!(obj instanceof AnalyzerOutPutObject))return null;
		Object objR=((AnalyzerOutPutObject)obj).getOutPutObject();
		if(!(objR instanceof VariableSelectionResult))return null;
		VariableSelectionResult result=(VariableSelectionResult)objR;
		
		TextTable table=getVTextTable(result);
		TableEntity te = new TableEntity();
		te.setSystem(((AnalyzerOutPutObject)analyzerOutPut).getDataAnalyzer().getAnalyticSource().getDataSourceType());
		
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				te.setColumn(table.getLines().get(i));
				for(int j=0;j<table.getLines().get(i).length;j++){
					if(j==0){
						te.addSortColumn(table.getLines().get(i)[j],DataTypeConverterUtil.textType);
					}else{
						te.addSortColumn(table.getLines().get(i)[j],DataTypeConverterUtil.numberType);
					}
				}
			}else{
				te.addItem(table.getLines().get(i));
			}
		}
		TextAndTableListEntity tableList = new TextAndTableListEntity();
		tableList.setText(getVTextText(result));
		tableList.addTableEntity(te);
		DataTextAndTableListVisualizationOutPut outputTable = new DataTextAndTableListVisualizationOutPut(tableList);
		outputTable.setName(analyzerOutPut.getAnalyticNode().getName());
		return outputTable;
	}

	private String getVTextText(VariableSelectionResult result) {
		double thresholdCategory =result.getThresholdCategory();
		double thresholdNumber = result.getThresholdNumber();
		StringBuffer str = new StringBuffer();  
		
		str.append(Tools.getLineSeparator() + VisualLanguagePack.getMessage(VisualLanguagePack.VS_THRESHOLD_CATEGORY,locale)
				+": " + String.valueOf(thresholdCategory) + Tools.getLineSeparator());
		str.append(Tools.getLineSeparator() + VisualLanguagePack.getMessage(VisualLanguagePack.VS_THRESHOLD_NUMBER,locale)
				+": " + String.valueOf(thresholdNumber) + Tools.getLineSeparator());
		return str.toString();
	}

	private TextTable getVTextTable(VariableSelectionResult result) {
		TextTable table= new TextTable();
		String[] attributeNames=result.getColumnNames();
		double[] scores=result.getScores();

		int colSize=2;
		String[] header=new String[colSize];
		header[0] = VisualLanguagePack.getMessage(VisualLanguagePack.VS_COLUMN_NAME,locale);
		header[1]= VisualLanguagePack.getMessage(VisualLanguagePack.VS_SCORE,locale);
		table.addLine(header);
		
		for(int i=0;i<attributeNames.length;i++){
			String[] line=new String[colSize];
			line[0]=attributeNames[i];
			line[1]=AlpineMath.powExpression(scores[i]);
			table.addLine(line);
		}
		return table;
	}
}
