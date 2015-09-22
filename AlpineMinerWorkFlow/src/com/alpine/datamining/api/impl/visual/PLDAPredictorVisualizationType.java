/**
 * ClassName PLDAPredictorVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

/**
 * zhaoyong
 */
import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdatePLDA;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.miner.view.ui.dataset.TableEntity;
import org.apache.log4j.Logger;

public class PLDAPredictorVisualizationType extends
		PredictTableVisualizationType {
    private static final Logger itsLogger =Logger.getLogger(PLDAPredictorVisualizationType.class);

    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		 
		 
		if (analyzerOutPut==null|| analyzerOutPut instanceof AnalyzerOutPutDataBaseUpdatePLDA == false) {
			return null;
		}
		AnalyzerOutPutDataBaseUpdatePLDA pldaOutPut = ((AnalyzerOutPutDataBaseUpdatePLDA) analyzerOutPut);
 
		try {
			CompositeVisualizationOutPut output = new CompositeVisualizationOutPut();
			String docTopicSchema = pldaOutPut.getSchemaName();
			String docTopicTable = pldaOutPut.getTableName();
			DataTable table = getResultTableSampleRow(analyzerOutPut,
					docTopicSchema, docTopicTable);
			TableEntity tableEntity = new TableEntity();
			generateTableEntity(table, tableEntity);
			 
			DataTableVisualizationOutPut predictionTableOutPut = new DataTableVisualizationOutPut(
					tableEntity);
			predictionTableOutPut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.PredictTable, locale)) ;
			output.addChildOutPut(predictionTableOutPut);

			String wordTopicSchema = pldaOutPut.getDocTopicOutSchema();
			String wordTopicTable = pldaOutPut.getDocTopicOutTable();
			table = getResultTableSampleRow(analyzerOutPut, wordTopicSchema,
					wordTopicTable);
			tableEntity = new TableEntity();
			generateTableEntity(table, tableEntity);
		 
			DataTableVisualizationOutPut wordTopicTableOutPut = new DataTableVisualizationOutPut(
					tableEntity);
			output.addChildOutPut(wordTopicTableOutPut);
			wordTopicTableOutPut.setName(VisualLanguagePack.getMessage(VisualLanguagePack.PLDADocTopicOutputTable, locale)) ;
 

			output.setName(analyzerOutPut.getAnalyticNode().getName());

			return output;
		} catch (AnalysisException e) {
			itsLogger.error(e.getMessage(),e);
			return null;
		}

	}

}
