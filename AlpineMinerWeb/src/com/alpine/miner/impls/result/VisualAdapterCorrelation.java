/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterCorrelation.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutObject;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.TableColumnMetaInfo;

public class VisualAdapterCorrelation extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterCorrelation INSTANCE = new VisualAdapterCorrelation();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
		Object obj = ((AnalyzerOutPutObject)analyzerOutPut).getOutPutObject();
		DataTable dataTable = new DataTable();
		
		String[] columns = new String[0];
		List<TableColumnMetaInfo> tableColumns= new ArrayList<TableColumnMetaInfo>(); 
		if(obj instanceof Object[]){
			if(((Object[])obj)[0] instanceof String[]){
				columns = (String[])((Object[])obj)[0];
				String[] showColumns = new String[columns.length+1];
				for(int i=0;i<showColumns.length;i++){
					
					if(i==0){
						tableColumns.add(new TableColumnMetaInfo(COLUMN_NAME,"")); 
			 
					}else{
						tableColumns.add(new TableColumnMetaInfo(columns[i-1],DBUtil.TYPE_NUMBER));
					 
					}
				}
			
			}
			
			dataTable.setColumns(tableColumns);
			List<DataRow> rows = new ArrayList<DataRow> ();			
			if(((Object[])obj)[1] instanceof HashMap){
				HashMap<String,Double> ht = (HashMap<String, Double>) ((Object[])obj)[1];
					for(int j=0;j<columns.length;j++){
						String[] item = new String[columns.length+1];
						item[0] = columns[j];
						for(int i=0;i<columns.length;i++){
							Double dd = ht.get(columns[i]+"/"+columns[j]);
							item[i+1] =String.valueOf(dd==null?"":dd);
							if(item[i+1].equals("")){
								dd = ht.get(columns[j]+"/"+columns[i]);
								item[i+1] =String.valueOf(dd==null?"":dd);
							}
							
							if(!item[i+1].equals("")){
								item[i+1] = AlpineMath.powExpression(Double.parseDouble(item[i+1]));
							}
						}
						DataRow row =new DataRow();
						row.setData(item);
						rows.add(row );
					}
			}
			

			dataTable.setRows(rows );
		}
		
		 
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(analyzerOutPut.getDataAnalyzer().getName(),
				dataTable);
		return visualModel;
	}
	 
 	 
}
