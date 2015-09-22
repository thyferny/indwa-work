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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.db.variableOptimization.UnivariateVariableOutput;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterUnivariateVariable extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterUnivariateVariable INSTANCE = new VisualAdapterUnivariateVariable();

	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut outPut,Locale locale)
			throws RuntimeException {
		
		UnivariateVariableOutput o = (UnivariateVariableOutput)outPut;
		//Attribute P-value
		Map<String, Double> valueMap = o.getpValueMap();
		DataTable dataTable = new DataTable();
		List<TableColumnMetaInfo> columns= new ArrayList<TableColumnMetaInfo>();
		columns.add(new TableColumnMetaInfo(ATTRIBUTE, ""));
		columns.add(new TableColumnMetaInfo(P_VALUE, ""));
		dataTable.setColumns(columns);
		List<DataRow> rows=getRows(valueMap);
		dataTable.setRows(rows);
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(outPut.getDataAnalyzer().getName(),
				dataTable);
		return visualModel;
	}


	private List<DataRow> getRows(Map<String, Double> valueMap) {
		List<DataRow> rows= new ArrayList<DataRow>  ();
		for (Iterator<String> iterator = valueMap.keySet().iterator(); iterator.hasNext();) {
			String key =  iterator.next();
			DataRow row=new DataRow();
			row.setData(new String[]{key,valueMap.get(key).toString()});
			rows.add(row);
		}
		return rows;
	}
	 
}
