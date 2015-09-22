/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterAssociation.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutDataBaseUpdate;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataTypeConverter;

public class VisualAdapterAssociation extends DBUpdateOutPutVisualAdapter implements OutPutVisualAdapter {
	 
	public static final VisualAdapterAssociation INSTANCE = new VisualAdapterAssociation();
 	
	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
	 
		DataTable dataTable = new DataTable();

		if(analyzerOutPut instanceof AnalyzerOutPutDataBaseUpdate){
			fillDataTable(dataTable,   (AnalyzerOutPutDataBaseUpdate)analyzerOutPut);  
		 
		}
		 
		VisualizationModelDataTable visualModel= new VisualizationModelDataTable(analyzerOutPut.getDataAnalyzer().getName(),
				dataTable);
		return visualModel;
	}
 
	protected void fillDataTables(DataTable dataTable, ResultSet rs,
			int fetchSize, ResultSetMetaData rsmd,
			AnalyticOutPut outPut) throws SQLException {
		DataSet dataSet = ((AnalyzerOutPutDataBaseUpdate)outPut).getDataset();
		List<DataRow> rows = new ArrayList<DataRow> ();	
		int count = rsmd.getColumnCount();
		while(rs.next()&&fetchSize > 0){
			fetchSize--;
			String[] items = new String[count];
			for(int i=0;i<count;i++){
				if (DataTypeConverter.isDoubleType(rsmd.getColumnType(i+1))) {
					items[i] = AlpineUtil.dealNullValue(rs,i+1);
				} else {
					if(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName().equals(DataSourceInfoOracle.dBType) && (i == 0 || i == 1)){
						String[] result=(String[])rs.getArray(i+1).getArray();
 						items[i] = createItem(result);
					}else{
						items[i] = rs.getString(i+1);
					}
				}
			}
			DataRow row =new DataRow();
			row.setData(items);
			rows.add(row );
		}
		dataTable.setRows(rows );
	}
 
	private String createItem(String[] result) {
		String item = "{";
		for(int j = 0; j < result.length; j++){
			if (j != 0){
				item += ",";
			}
			item += result[j];
		}
		item += "}";
		return item;
	}
 	 
}
