/**
 * ClassName VisualizationModelText.java
 *
 * Version information: 1.00
 *
 * Data: 2011-7-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.output.visual;

import java.util.List;

import com.alpine.miner.workflow.output.AbstractVisualizationModel;


public class VisualizationModelTableGrouped extends AbstractVisualizationModel {
	 
	private List<List<VisualizationModel>> models;
	public List<List<VisualizationModel>> getModels() {
		return models;
	}

	public void setModels(List<List<VisualizationModel>> models) {
		this.models = models;
	}

	public List<String> getTableHeader() {
		return tableHeader;
	}

	public void setTableHeader(List<String> tableHeader) {
		this.tableHeader = tableHeader;
	}

	private List<String> tableHeader;

	public VisualizationModelTableGrouped(String title,List<String> tableHeader, List<List<VisualizationModel>> models) {
		super(TYPE_TABLE_GROUPED,title);
		//keep the order...
		this.tableHeader=tableHeader;
		this.models=models;
	 
	 
	}
 
}
