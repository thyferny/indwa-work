/**
 * ClassName PredictTableVisualizationType.java
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
import com.alpine.datamining.api.impl.AbstractDBTableOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.view.ui.dataset.TableEntity;
import org.apache.log4j.Logger;

public class PredictTableVisualizationType extends TableVisualizationType {
    private static final Logger logger =Logger.getLogger(PredictTableVisualizationType.class);
    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		TableEntity tableEntity = new TableEntity();
		if(analyzerOutPut instanceof AnalyzerOutPutDataBaseUpdate){
			AnalyzerOutPutDataBaseUpdate outPut = (AnalyzerOutPutDataBaseUpdate)analyzerOutPut;
			
			DataTable dt=null;
			try {
				dt = getResultTableSampleRow(analyzerOutPut,outPut.getSchemaName(),outPut.getTableName());
			} catch (AnalysisException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
			
			generateTableEntity(dt, tableEntity);
			
			DataTableVisualizationOutPut output = new DataTableVisualizationOutPut(tableEntity);
			output.setName(analyzerOutPut.getAnalyticNode().getName());
		 
			output.fillDBTableInfo((AbstractDBTableOutPut)analyzerOutPut);
			
			return output;
		}
			
		return null;
	}
}
