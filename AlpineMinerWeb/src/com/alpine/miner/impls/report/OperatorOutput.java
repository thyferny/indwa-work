/**
 * ClassName :OperatorReportOutput.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report;

import com.alpine.miner.workflow.output.visual.VisualizationModel;

/**
 * @author zhaoyong
 *
 */
public class OperatorOutput {
	/**
	 * @param name
	 * @param description
	 */
	public OperatorOutput(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}
	public static final int TYPE_TEXT = VisualizationModel.TYPE_TEXT;
	public static final int TYPE_DATA_TABLE = VisualizationModel.TYPE_DATATABLE;
	public static final int TYPE_CHART = VisualizationModel.TYPE_CHART;
	public static final int TYPE_COMPOSITE = VisualizationModel.TYPE_COMPOSITE;
	//special for cluster...
	public static final int TYPE_TABLED_GROUP = VisualizationModel.TYPE_TABLE_GROUPED;
	//
	public static final int VISUAL_TYPE_SCATT_MATRIX = 28;

	/**
	 * have to list all possiable field for gson use, need to be a simple java bean*/
	String name;
	String description;
	String text;
	String svg;
	String[] svg_legend;
	String[] svg_legend_labels;

	public String[] getSvg_legend_labels() {
		return svg_legend_labels;
	}
	public void setSvg_legend_labels(String[] svgLegendLabels) {
		svg_legend_labels = svgLegendLabels;
	}
	//each line is the data row,
	//the first line is the table header(column name)
	String[][] tableData;
	String tableName;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	String tableType;//Table:View
	//could for composite and the tabled group
	//if is group, each one is composit (a row...)
	OperatorOutput[] outPuts;
	//this is for culster profile table
	//if has tableGroupHeader, the data is in tableData, 
	//each cell is a gfx surface.,first cell is a text 
	String[] tableGroupHeader;
	String[] tableGroupColumnType;//text,svg
	public String[] getTableGroupColumnType() {
		return tableGroupColumnType;
	}
	public void setTableGroupColumnType(String[] tableGroupColumnType) {
		this.tableGroupColumnType = tableGroupColumnType;
	}
	//add tableGroupCellType add by Will
	String[][] tableGroupCellType;//text,svg

	public String[][] getTableGroupCellType() {
		return tableGroupCellType;
	}
	public void setTableGroupCellType(String[][] tableGroupCellType) {
		this.tableGroupCellType = tableGroupCellType;
	}
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getType() {
		return type;
	}
	int type =-1; 
	public void setType(int type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getSvg() {
		return svg;
	}
	public void setSvg(String svg) {
		this.svg = svg;
	}
	public String[][] getTableData() {
		return tableData;
	}
	public void setTableData(String[][] tableData) {
		this.tableData = tableData;
	}
	public OperatorOutput[] getOutPuts() {
		return outPuts;
	}
	public void setOutPuts(OperatorOutput[] outPuts) {
		this.outPuts = outPuts;
	}
 
	public String[] getTableGroupHeader() {
		return tableGroupHeader;
	}
	public void setTableGroupHeader(String[] tableGroupHeader) {
		this.tableGroupHeader = tableGroupHeader;
	}

	public String[] getSvg_legend() {
		return svg_legend;
	}
	public void setSvg_legend(String svgLegend[]) {
		svg_legend = svgLegend;
	}


}
