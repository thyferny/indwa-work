/**
 * ClassName PCATableVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-14
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.AbstractDBTableOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutPCA;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.miner.view.ui.dataset.TableEntity;
import org.apache.log4j.Logger;

public class PCATableVisualizationType extends DataOperationTableVisualizationType {
    private static final Logger itsLogger =Logger.getLogger(PCATableVisualizationType.class);
    public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {

        AnalyzerOutPutTableObject result = null;
		if(analyzerOutPut instanceof AnalyzerOutPutPCA){
			AnalyzerOutPutPCA outPut = (AnalyzerOutPutPCA)analyzerOutPut;
			result=outPut.getPCAResultTables();
			result.setAnalyticNode(analyzerOutPut.getAnalyticNode());
		}
		
		DataTable dt = new DataTable();
		TableEntity tableEntity = new TableEntity();
		try {
			dt = getResultTableSampleRow(analyzerOutPut,
					result.getSchemaName(), result.getTableName());
			result.setDataTable(dt);
			result.setColumns(dt.getColumns());
		} catch (AnalysisException e) {
			itsLogger.error(e.getMessage(),e);
			return null;
		}
		;

		generateTableEntity(dt, tableEntity);

		DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(
				tableEntity);
		output.setName(analyzerOutPut.getAnalyticNode().getName());
		output.setTableName(dt.getTableName());
		output.fillDBTableInfo((AbstractDBTableOutPut)result);
		
		output.setName(VisualLanguagePack.getMessage(VisualLanguagePack.PCA_OUTPUTTABLE,locale));

		return output;
	}
}
