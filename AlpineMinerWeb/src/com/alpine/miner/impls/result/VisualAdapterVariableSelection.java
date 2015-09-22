/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterInformationValue.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.attribute.variableselection.VariableSelectionResult;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterVariableSelection extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 

	public static final VisualAdapterVariableSelection INSTANCE = new VisualAdapterVariableSelection();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
		if(!(outPut instanceof AnalyzerOutPutObject))return null;
		Object objR=((AnalyzerOutPutObject)outPut).getOutPutObject();
		if(!(objR instanceof VariableSelectionResult))return null;
		VariableSelectionResult result=(VariableSelectionResult)objR;
		 
		VisualizationModelComposite visualModel = generateCompositeModel(outPut.getDataAnalyzer().getName(),result,  locale);
		return visualModel;
	}

	private String getVTextText(VariableSelectionResult result,Locale locale) {
		double thresholdCategory =result.getThresholdCategory();
		double thresholdNumber = result.getThresholdNumber();
		StringBuffer str = new StringBuffer();  
		
		str.append(Tools.getLineSeparator() + VisualNLS.getMessage(VisualNLS.VS_THRESHOLD_CATEGORY,locale)+": " + String.valueOf(thresholdCategory) + Tools.getLineSeparator());
		str.append(Tools.getLineSeparator() + VisualNLS.getMessage(VisualNLS.VS_THRESHOLD_NUMBER,locale)+": " + String.valueOf(thresholdNumber) + Tools.getLineSeparator());
		return str.toString();
	}


	/**
	 * @param result
	 * @return
	 */
	private VisualizationModelComposite generateCompositeModel(String name,VariableSelectionResult result,Locale locale) {
		List<VisualizationModel> models= new ArrayList<VisualizationModel> (); 
		VisualizationModelText textModel= new VisualizationModelText(VisualNLS.getMessage(VisualNLS.THRESHOLD,locale),
				getVTextText(result,  locale)); 
		models.add(textModel) ;
	 
		String[] attributeNames=result.getColumnNames();
		double[] scores=result.getScores();
		
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.VS_COLUMN_NAME,locale), DBUtil.TYPE_CATE)) ;
		columns.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.VS_SCORE,locale), DBUtil.TYPE_NUMBER)) ;
		
		List<DataRow> rows = new ArrayList<DataRow>();
		for(int i=0;i<attributeNames.length;i++){
			
			DataRow row= new DataRow();
			row.setData(new String[]{attributeNames[i],
					AlpineMath.powExpression(scores[i])});
			rows.add(row) ; 
 
		
		}
		DataTable dataTable = new DataTable();		
		dataTable.setColumns(columns) ;	
		dataTable.setRows(rows) ;
		
		VisualizationModelDataTable tableModel=new VisualizationModelDataTable(VisualNLS.getMessage(VisualNLS.SCORE,locale),
				dataTable) ;
		
		models.add(tableModel);
		
		VisualizationModelComposite model= new VisualizationModelComposite(name,models);
		return model;
	}
	 
 	 
}
