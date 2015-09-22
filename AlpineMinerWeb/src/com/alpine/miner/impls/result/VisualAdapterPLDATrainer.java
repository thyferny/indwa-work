/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * VisualAdapterPLDATrainer
 * 
 * Author:Will
 * 
 * Data:2012-3-27
 * 
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPLDATrainModel;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TableVisualizationType;
import com.alpine.datamining.operator.plda.PLDAModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterPLDATrainer extends AbstractOutPutVisualAdapter {

	private static final VisualAdapterPLDATrainer INSTANCE = new VisualAdapterPLDATrainer();
	static TableVisualizationType tableVType= new TableVisualizationType();
	
	private final String DOC_TOPIC_OUT_TABLE= "docTopicOutTable";
	private final String TOPIC_OUT_TABLE= "topicOutTable";
	private final String PLDA_MODEL_OUTPUT_TABLE= "PLDAModelOutputTable";
	
	
	public static VisualAdapterPLDATrainer getInstance(){
		return INSTANCE;
	}

	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,
			Locale locale) throws Exception {
		if (analyzerOutPut==null|| analyzerOutPut instanceof AnalyzerOutPutPLDATrainModel == false) {
			return null;
		}
		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
		AnalyzerOutPutPLDATrainModel pldaModel = ((AnalyzerOutPutPLDATrainModel) analyzerOutPut);
		
		String docTopicSchema = pldaModel.getPLDADocTopicOutTable().getSchemaName();
		String docTopicTable = pldaModel.getPLDADocTopicOutTable().getTableName();
		DataTable table = tableVType.getResultTableSampleRow(analyzerOutPut,docTopicSchema, docTopicTable);
		VisualizationModel docTopicTableVisualModel = new VisualizationModelDataTable(
				LanguagePack.getMessage(DOC_TOPIC_OUT_TABLE, locale),
				table);
		models.add(docTopicTableVisualModel);
		
		String wordTopicSchema = pldaModel.getPLDAWordTopicOutTable()
				.getSchemaName();
		String wordTopicTable = pldaModel.getPLDAWordTopicOutTable()
				.getTableName();
		table = tableVType.getResultTableSampleRow(analyzerOutPut, wordTopicSchema,
				wordTopicTable);
		VisualizationModel wordTopicTableVisualModel = new VisualizationModelDataTable(
				LanguagePack.getMessage(TOPIC_OUT_TABLE, locale),
				table);
		models.add(wordTopicTableVisualModel);
		
		 
		EngineModel model = (EngineModel) pldaModel.getEngineModel();
		String modelTable = ((PLDAModel) model.getModel()).getModelTable();
		String modelSchema = ((PLDAModel) model.getModel()).getModelSchema();
		DataTable dataTable = tableVType.getResultTableSampleRow(analyzerOutPut, modelSchema,
				modelTable);
		VisualizationModel modelTableVisualModel  = new VisualizationModelDataTable(
				LanguagePack.getMessage(PLDA_MODEL_OUTPUT_TABLE, locale), dataTable);  
		models.add(modelTableVisualModel );
		
		VisualizationModelComposite visualModel = new VisualizationModelComposite(analyzerOutPut.getAnalyticNode().getName(), 
				models );
		
		return visualModel;
	}
		
}


