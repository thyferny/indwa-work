/**
 * ClassName LinearRegressionTextVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPLDATrainModel;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.datamining.operator.plda.PLDAModel;
import com.alpine.miner.view.ui.dataset.TableEntity;
import org.apache.log4j.Logger;

public class PLDATrainerVisualizationType extends
PredictTableVisualizationType {
    private static final Logger itsLogger =Logger.getLogger(PLDATrainerVisualizationType.class);

    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		 
		if (analyzerOutPut==null|| analyzerOutPut instanceof AnalyzerOutPutPLDATrainModel == false) {
			return null;
		}
		AnalyzerOutPutPLDATrainModel pldaModel = ((AnalyzerOutPutPLDATrainModel) analyzerOutPut);
 
		try {
			CompositeVisualizationOutPut output = new CompositeVisualizationOutPut();
			String docTopicSchema = pldaModel.getPLDADocTopicOutTable()
					.getSchemaName();
			String docTopicTable = pldaModel.getPLDADocTopicOutTable()
					.getTableName();
			DataTable table = getResultTableSampleRow(analyzerOutPut,
					docTopicSchema, docTopicTable);
			TableEntity tableEntity = new TableEntity();
			
			generateTableEntity(table, tableEntity);
//			tableEntity.setTableName("DocTopicTable") ;
			DataTableVisualizationOutPut docTopicTableOutPut = new DataTableVisualizationOutPut(
					tableEntity);
			docTopicTableOutPut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.docTopicOutTable, locale)) ;
			output.addChildOutPut(docTopicTableOutPut);

			String wordTopicSchema = pldaModel.getPLDAWordTopicOutTable()
					.getSchemaName();
			String wordTopicTable = pldaModel.getPLDAWordTopicOutTable()
					.getTableName();
			table = getResultTableSampleRow(analyzerOutPut, wordTopicSchema,
					wordTopicTable);
//			tableEntity.setTableName("WordTopicTable") ;
			tableEntity = new TableEntity();
			generateTableEntity(table, tableEntity);
			 
			DataTableVisualizationOutPut wordTopicTableOutPut = new DataTableVisualizationOutPut(
					tableEntity);
			wordTopicTableOutPut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.topicOutTable, locale)) ;
			output.addChildOutPut(wordTopicTableOutPut);

			EngineModel model = (EngineModel) pldaModel.getEngineModel();
			String modelTable = ((PLDAModel) model.getModel()).getModelTable();
			String modelSchema = ((PLDAModel) model.getModel())
					.getModelSchema();
			table = getResultTableSampleRow(analyzerOutPut, modelSchema,
					modelTable);

			tableEntity = new TableEntity();
//			tableEntity.setTableName("ModelTable") ;
			generateTableEntity(table, tableEntity);
			 
			DataTableVisualizationOutPut modelTableOutPut = new DataTableVisualizationOutPut(
					tableEntity);
			modelTableOutPut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.PLDAModelOutputTable, locale)) ;
			output.addChildOutPut(modelTableOutPut);

			output.setName(analyzerOutPut.getAnalyticNode().getName());

			return output;
		} catch (AnalysisException e) {
			itsLogger.error(e.getMessage(),e);
			return null;
		}

	}
 
}
