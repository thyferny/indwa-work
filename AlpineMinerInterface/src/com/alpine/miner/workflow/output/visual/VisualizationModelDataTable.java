/**
 * ClassName VisualizationModelDataTable.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.workflow.output.AbstractVisualizationModel;

public class VisualizationModelDataTable extends AbstractVisualizationModel {
	private DataTable dataTable;

	public VisualizationModelDataTable(String title,DataTable dataTable) {
		super(TYPE_DATATABLE,title);
		setDataTable(dataTable);
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}
	
	
}
