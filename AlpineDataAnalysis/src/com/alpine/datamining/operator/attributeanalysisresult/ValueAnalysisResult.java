/**
 * ClassName ValueAnalysisResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.attributeanalysisresult;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.OutputObject;

/**
 * @author Eason
 * 
 */
public class ValueAnalysisResult extends OutputObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tableName = "";
	private List<ColumnValueAnalysisResult> valueAnalysisResult = null;
	private DataSet dataSet;

	

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public ValueAnalysisResult() {
		valueAnalysisResult = new ArrayList<ColumnValueAnalysisResult>();
	}

	public ValueAnalysisResult(String tableName) {
		this.tableName = tableName;
		valueAnalysisResult = new ArrayList<ColumnValueAnalysisResult>();
	}

	public String getTalbeName() {
		return tableName;
	}

	public ColumnValueAnalysisResult getColumnValueAnalysisResult(int i) {
		return valueAnalysisResult.get(i);
	}

	public List<ColumnValueAnalysisResult> getValueAnalysisResult() {
		return valueAnalysisResult;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setValueAnalysisResult(
			List<ColumnValueAnalysisResult> valueAnalysisResult) {
		this.valueAnalysisResult = valueAnalysisResult;
	}

	public void addColumnValueAnalysisResult(
			ColumnValueAnalysisResult columnValueAnalysisResult) {
		valueAnalysisResult.add(columnValueAnalysisResult);
	}

//	public String toString() {
//		String ret = "columnName columnType count uniqueValueCount nullCount emptyCount zeroCount positiveValueCount negativeValueCount"
//				+ CommonUtility.getLineSeparator();
//		for (int i = 0; i < valueAnalysisResult.size(); i++) {
//			ret += valueAnalysisResult.get(i).toString();
//		}
//		return ret;
//
//	}

}
