/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AbstractOutPutJSONAdapter.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TableVisualizationType;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.utility.db.TableColumnMetaInfo;
import com.alpine.utility.tools.ProfileReader;

public abstract class AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	
	static TableVisualizationType tableVType= new TableVisualizationType();
 
	
	public int getTableMaxRows(){
		String maxRows=ProfileReader.getInstance(false).getProperties().getProperty(UI_PARA2);
		return Integer.parseInt(maxRows) ;
	}
	 
 
	protected void setColumns(DataTable te, String[] cols,String [] colTypes) {
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		for (int i = 0; i < cols.length; i++) {
			columns.add(new  TableColumnMetaInfo(cols[i],colTypes[i])) ;
		}
 
		te.setColumns(columns );
		
	}
	
	protected DataTable getDataTable4TimeSeries(TextTable table, String[] columnTypes) { 
		DataTable te = new DataTable();
		List<DataRow> rows = new ArrayList<DataRow>();
  
		
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				setColumns(te, table.getLines().get(i),columnTypes);
			 	
				 
			}else{
				String attribute = table.getLines().get(i)[0];
				table.getLines().get(i)[0] = BETA+L_BRAKE+attribute+R_BRAKE;
				DataRow row = new DataRow();
				row.setData((table.getLines().get(i)));
				rows.add(row);
			}
		} 
		te.setRows(rows);
		return te;
	}
	 

	//RGB each have 10 ,total 30
	protected String getRandomColor(int index) {
		return CONST_Colors[index%CONST_Colors.length];
	}

 

}
