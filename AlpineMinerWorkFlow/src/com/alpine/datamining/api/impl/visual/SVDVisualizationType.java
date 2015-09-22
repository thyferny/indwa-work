/**
 * ClassName SVDVisualizationType.java
 *
 * Version information:1.00
 *
 * Date:Jun 9, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.visual;

import java.util.List;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.VisualizationType;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.output.SVDAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.SVDLanczosAnalyzerOutPutTrainModel;
import com.alpine.datamining.api.visual.AbstractVisualizationType;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;

/**
 * @author Eason
 *
 */
public class SVDVisualizationType  extends AbstractVisualizationType implements
VisualizationType{
    private static final Logger itsLogger =Logger.getLogger(SVDVisualizationType.class);

    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		if(analyzerOutPut instanceof SVDAnalyzerOutPutTrainModel){
			SVDAnalyzerOutPutTrainModel analyzerOutput = ((SVDAnalyzerOutPutTrainModel)analyzerOutPut);
			CompositeVisualizationOutPut composite = new CompositeVisualizationOutPut();
			
			try {
				DataTable uDataTable = getResultTableSampleRow(analyzerOutput,analyzerOutput.getUmatrixTable().getSchemaName(),analyzerOutput.getUmatrixTable().getTableName());
				analyzerOutput.getUmatrixTable().setDataTable(uDataTable);
				analyzerOutput.getUmatrixTable().setColumns(uDataTable.getColumns());
				DataTable vDataTable = getResultTableSampleRow(analyzerOutput,analyzerOutput.getVmatrixTable().getSchemaName(),analyzerOutput.getVmatrixTable().getTableName());
				analyzerOutput.getVmatrixTable().setDataTable(vDataTable);
				analyzerOutput.getVmatrixTable().setColumns(vDataTable.getColumns());
			} catch (AnalysisException e) {
				itsLogger.error(e.getMessage(),e);
				return null;
			}
			tableVisualization(composite, analyzerOutput.getUmatrixTable());
			tableVisualization(composite, analyzerOutput.getVmatrixTable());

			return composite;
		} else if(analyzerOutPut instanceof SVDLanczosAnalyzerOutPutTrainModel){
			SVDLanczosAnalyzerOutPutTrainModel analyzerOutput =(SVDLanczosAnalyzerOutPutTrainModel)analyzerOutPut;
			CompositeVisualizationOutPut composite = new CompositeVisualizationOutPut();
			
			try {
				DataTable uDataTable = getResultTableSampleRow(analyzerOutput,analyzerOutput.getUmatrixTable().getSchemaName(),analyzerOutput.getUmatrixTable().getTableName());
				analyzerOutput.getUmatrixTable().setDataTable(uDataTable);
				analyzerOutput.getUmatrixTable().setColumns(uDataTable.getColumns());
				DataTable vDataTable = getResultTableSampleRow(analyzerOutput,analyzerOutput.getVmatrixTable().getSchemaName(),analyzerOutput.getVmatrixTable().getTableName());
				analyzerOutput.getVmatrixTable().setDataTable(vDataTable);
				analyzerOutput.getVmatrixTable().setColumns(vDataTable.getColumns());
				DataTable sDataTable = getResultTableSampleRow(analyzerOutput,analyzerOutput.getSingularValueTable().getSchemaName(),analyzerOutput.getSingularValueTable().getTableName());
				analyzerOutput.getSingularValueTable().setDataTable(sDataTable);
				analyzerOutput.getSingularValueTable().setColumns(sDataTable.getColumns());
			} catch (AnalysisException e) {
				itsLogger.error(e.getMessage(),e);
				return null;
			}
			
			tableVisualization(composite, analyzerOutput.getUmatrixTable());
			tableVisualization(composite, analyzerOutput.getVmatrixTable());
			tableVisualization(composite, analyzerOutput.getSingularValueTable());
			return composite;
		}else{
			return null;
		}
		

	}

	private void tableVisualization(CompositeVisualizationOutPut composite,
			AnalyzerOutPutTableObject tableObj) {
		DataTable dt = tableObj.getDataTable();
		TableEntity tableEntity = new TableEntity();
		
		List<TableColumnMetaInfo> columnList = dt.getColumns();
		String[] columns = new String[columnList.size()];
		for(int i=0;i<columns.length;i++){
			columns[i] = columnList.get(i).getColumnName();
			tableEntity.addSortColumn(columns[i], columnList.get(i).getColumnsType());
		}
		tableEntity.setColumn(columns);
		
		List<DataRow> drList = dt.getRows();
		for(DataRow dr:drList){
			tableEntity.addItem(dr.getData());
		}
		DataTableVisualizationOutPut vOutput = new DataTableVisualizationOutPut(tableEntity);
		vOutput.setName(dt.getTableName());
		composite.addChildOutPut(vOutput); 
		
		vOutput.fillDBTableInfo(tableObj);
	}
}
