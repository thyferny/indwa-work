package com.alpine.datamining.api.impl.output;

import java.util.List;
import java.util.Map;

import com.alpine.datamining.api.impl.db.table.ScatterMatrixColumnPairs;



public class AnalyzerOutPutScatterMatrix extends AnalyzerOutPutTableObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4064262860755880511L;


	private Map<ScatterMatrixColumnPairs,DataTable> dataTableMap;
	private List<Double> corrList;
	private String[] columnNames;

	public Map<ScatterMatrixColumnPairs, DataTable> getDataTableMap() {
		return dataTableMap;
	}


	public void setDataTableMap(
			Map<ScatterMatrixColumnPairs, DataTable> dataTableMap) {
		this.dataTableMap = dataTableMap;
	}

	public List<Double> getCorrList() {
		return corrList;
	}


	public void setCorrList(List<Double> corrList) {
		this.corrList = corrList;
	}


	public String[] getColumnNames() {
		return columnNames;
	}


	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	
}
