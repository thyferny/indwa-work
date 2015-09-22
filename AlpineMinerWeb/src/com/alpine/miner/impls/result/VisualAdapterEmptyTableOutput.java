/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterIntegerToText.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

//result is an output with empty table, need fertch data from db
public class VisualAdapterEmptyTableOutput extends DBUpdateOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterEmptyTableOutput INSTANCE = new VisualAdapterEmptyTableOutput();
 
	//xx no sql any more 
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws  Exception {
		if(!(outPut instanceof AnalyzerOutPutTableObject)){
			return null;
		}
		AnalyzerOutPutTableObject tableObject = (AnalyzerOutPutTableObject)outPut;
		DataTable dataTable =tableVType.getResultTableSampleRow(outPut, tableObject.getSchemaName(),tableObject.getTableName()) ;
		tableObject.setColumns(dataTable.getColumns()) ;
		
		String dbType = outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType();
		DBUtil.reSetColumnType(dbType, dataTable) ;
		
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(outPut.getDataAnalyzer().getName(),dataTable);
			
 
		return visualModel;
	}
  
 	 
}
