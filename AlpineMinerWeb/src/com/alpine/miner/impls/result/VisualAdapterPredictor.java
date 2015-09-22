/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterPredictor.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterPredictor extends DBUpdateOutPutVisualAdapter
		implements OutPutVisualAdapter {

	public static final VisualAdapterPredictor INSTANCE = new VisualAdapterPredictor();

	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {

		DataTable dataTable = new DataTable();

		if (analyzerOutPut instanceof AnalyzerOutPutDataBaseUpdate) {
			AnalyzerOutPutDataBaseUpdate outPut = (AnalyzerOutPutDataBaseUpdate) analyzerOutPut;
			fillDataTable(dataTable, outPut);
		}

		VisualizationModelDataTable visualModel = new VisualizationModelDataTable(
				analyzerOutPut.getDataAnalyzer().getName(), dataTable);

		return visualModel;
	}


	

}
