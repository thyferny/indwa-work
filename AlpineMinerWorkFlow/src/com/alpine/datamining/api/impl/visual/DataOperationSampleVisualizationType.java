/**
 * ClassName DataOperationSampleVisualizationType.java
 *
 * Version information:1.00
 *
 * Date:Jun 9, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.visual;

import java.util.List;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.VisualizationType;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutSampling;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.output.hadoop.HadoopMultiAnalyticFileOutPut;
import com.alpine.datamining.api.visual.AbstractVisualizationType;
import com.alpine.datamining.api.visual.CompositeVisualizationOutPut;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.TableColumnMetaInfo;
import org.apache.log4j.Logger;

/**
 * @author Richie Lo
 *
 */
public class DataOperationSampleVisualizationType  extends AbstractVisualizationType implements
VisualizationType{
    private static final Logger itsLogger =Logger.getLogger(DataOperationSampleVisualizationType.class);

    @Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		AnalyzerOutPutSampling analyzerOutput = null;
		if(analyzerOutPut instanceof AnalyzerOutPutSampling){
			analyzerOutput = ((AnalyzerOutPutSampling)analyzerOutPut);
		} else {
			return null;
		}
		
		CompositeVisualizationOutPut composite = new CompositeVisualizationOutPut();
		if(analyzerOutput.isHadoopSampling()){
			return genHadoopVisulization(analyzerOutput);
		}
		List<AnalyzerOutPutTableObject> tableList = analyzerOutput.getSampleTables();
		for(AnalyzerOutPutTableObject tableObj:tableList){
			try {
				DataTable dataTable = getResultTableSampleRow(analyzerOutPut,tableObj.getSchemaName(),tableObj.getTableName());
				tableObj.setDataTable(dataTable);
				tableObj.setColumns(dataTable.getColumns());
			} catch (AnalysisException e) {
				itsLogger.error(e.getMessage(),e);
				return null;
			}
		}
		
		for (AnalyzerOutPutTableObject tableObj:tableList) {
			
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

		return composite;
	}

	private VisualizationOutPut genHadoopVisulization(AnalyzerOutPutSampling analyzerOutput) {
		List<AnalyzerOutPutTableObject> tableList = analyzerOutput.getSampleTables();
		CompositeVisualizationOutPut composite = new CompositeVisualizationOutPut();
		
		for (AnalyzerOutPutTableObject tableObj:tableList) {
			
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
			vOutput.setName(dt.getSchemaName());
			vOutput.fillDBTableInfo(tableObj);
			composite.addChildOutPut(vOutput); 
		}
		
		return composite;
	}



}
