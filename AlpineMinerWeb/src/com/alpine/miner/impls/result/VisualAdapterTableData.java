/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * TableDataOutPutJSONAdapter.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.Locale;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTableObject;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;

public class VisualAdapterTableData extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterTableData INSTANCE = new VisualAdapterTableData();

	   
	/**
	 * This json is for the client js dojo grid use
	 * 
	 * "dataTable":
	 * {
	{"columns":["a1","a2","a3","a4","id"],
	"items":[
	{a1:xxx,a2:yyy,a3:vvv,a4:zzz,id:000},
	{a1:xxx,a2:yyy,a3:vvv,a4:zzz,id:000},
	{a1:xxx,a2:yyy,a3:vvv,a4:zzz,id:000}
	]
	}
	The json utility class will transfer the model to json
	 * @throws AnalysisException 
	 * */
	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws  Exception {
		AnalyzerOutPutTableObject outputTable = (AnalyzerOutPutTableObject)outPut;

		DataTable dataTable =tableVType.getResultTableSampleRow(outPut, outputTable.getSchemaName(),outputTable.getTableName()) ;
		outputTable.setColumns(dataTable.getColumns()) ;
	 
		String dbType = outPut.getDataAnalyzer().getAnalyticSource().getDataSourceType(); 
		DBUtil.reSetColumnType(dbType, dataTable) ;
	 
		
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(outPut.getDataAnalyzer().getName(),
				dataTable);
		
		return visualModel;
	}
 
 	 
}
