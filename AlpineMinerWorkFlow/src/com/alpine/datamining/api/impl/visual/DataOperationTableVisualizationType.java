/**
 * ClassName DataOperationTableVisualizationType.java
 *
 * Version information:1.00
 *
 * Date:Jun 2, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.AbstractDBTableOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.view.ui.dataset.TableEntity;
import org.apache.log4j.Logger;

/**
 * @author Richie Lo
 *Z
 */
public class DataOperationTableVisualizationType extends TableVisualizationType {
    private static final Logger logger =Logger.getLogger(DataOperationTableVisualizationType.class);

    public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		
		DataTable dt = new DataTable();
		TableEntity tableEntity = new TableEntity();
		if(analyzerOutPut instanceof AnalyzerOutPutTableObject){
			AnalyzerOutPutTableObject outPut = (AnalyzerOutPutTableObject)analyzerOutPut;
			try {
				dt=getResultTableSampleRow(analyzerOutPut,outPut.getSchemaName(),outPut.getTableName());
				outPut.setDataTable(dt);
				outPut.setColumns(dt.getColumns());
			} catch (AnalysisException e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			};
		} else {
			return null;
		}
		
		generateTableEntity(dt, tableEntity);
		
		DataTableVisualizationOutPut vOutput = new DataTableVisualizationOutPut(tableEntity);
		vOutput.setName(analyzerOutPut.getAnalyticNode().getName());
		vOutput.setTableName(dt.getTableName());
		vOutput.fillDBTableInfo((AbstractDBTableOutPut)analyzerOutPut);
		return vOutput;
	}
}
