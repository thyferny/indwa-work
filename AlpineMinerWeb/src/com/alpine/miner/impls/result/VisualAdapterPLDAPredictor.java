/**
 * ClassName :DataExplorerManagerImpl.java
 *
 * Version information: 3.0
 * 
 * Author:Will
 *
 * Data: 2012-03-27
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdatePLDA;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TableVisualizationType;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterPLDAPredictor extends AbstractOutPutVisualAdapter {

	private static final VisualAdapterPLDAPredictor INSTANCE = new VisualAdapterPLDAPredictor();
	static TableVisualizationType tableVType= new TableVisualizationType();
	private final String PREDICT_TABLE="PredictTable";
	private final String PLDA_DOC_TOPIC_OUTPUT_TABLE="PLDADocTopicOutputTable";
	public static VisualAdapterPLDAPredictor getInstance(){
		return INSTANCE;
	}

	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,
			Locale locale) throws Exception {
		if (analyzerOutPut==null|| analyzerOutPut instanceof AnalyzerOutPutDataBaseUpdatePLDA == false) {
			return null;
		}
		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
		AnalyzerOutPutDataBaseUpdatePLDA pldaOutPut = ((AnalyzerOutPutDataBaseUpdatePLDA) analyzerOutPut);
		
		String docTopicSchema = pldaOutPut.getSchemaName();
		String docTopicTable = pldaOutPut.getTableName();
		DataTable table = tableVType.getResultTableSampleRow(analyzerOutPut,
				docTopicSchema, docTopicTable);
		VisualizationModel docTopicTableVisualModel = new VisualizationModelDataTable(
				LanguagePack.getMessage(PREDICT_TABLE, locale),
				table);
		models.add(docTopicTableVisualModel);
		
		String wordTopicSchema = pldaOutPut.getDocTopicOutSchema();
		String wordTopicTable = pldaOutPut.getDocTopicOutTable();
		table = tableVType.getResultTableSampleRow(analyzerOutPut, wordTopicSchema,
				wordTopicTable);
		VisualizationModel wordTopicTableVisualModel = new VisualizationModelDataTable(
				VisualNLS.getMessage("PLDA_DOC_TOPIC_OUTPUT_TABLE", locale),
				table);
		models.add(wordTopicTableVisualModel);
	
		VisualizationModelComposite visualModel = new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName(), 
				models );
		
		return visualModel;
	}
		
}


