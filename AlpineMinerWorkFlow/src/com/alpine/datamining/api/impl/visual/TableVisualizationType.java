/**
 * ClassName ImageVisualizationGenerator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.List;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.VisualizationType;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.visual.AbstractVisualizationType;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.TableColumnMetaInfo;

/**
 * @author John Zhao
 *
 */
public class TableVisualizationType extends AbstractVisualizationType implements
VisualizationType {

	/* (non-Javadoc)
	 * @see com.alpine.datamining.api.VisualizationType#generateOutPut(com.alpine.datamining.api.AnalyticOutPut)
	 */
	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected void generateTableEntity(DataTable dt, TableEntity tableEntity) {
		List<TableColumnMetaInfo> columnList = dt.getColumns();
		String[] columns = new String[columnList.size()];
		for(int i=0;i<columns.length;i++){
			columns[i] = columnList.get(i).getColumnName();
			tableEntity.addSortColumn(columns[i],columnList.get(i).getColumnsType());
		}
		tableEntity.setColumn(columns);
		
		
		List<DataRow> drList = dt.getRows();
		for(DataRow dr:drList){
			tableEntity.addItem(dr.getData());
		}
		tableEntity.setTableName(dt.getTableName()) ;
	}

}
