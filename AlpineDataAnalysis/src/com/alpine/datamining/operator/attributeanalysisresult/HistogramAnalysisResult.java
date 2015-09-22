/**
 * ClassName HistogramAnalysisResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.attributeanalysisresult;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.Tools;

/**
 * @author Eason
 * 
 */
public class HistogramAnalysisResult extends OutputObject {

	private static final long serialVersionUID = 1L;
	private List<BinHistogramAnalysisResult> result = null;
	private String tableName = null;

	public HistogramAnalysisResult() {
		result = new ArrayList<BinHistogramAnalysisResult>();
	}

	/**
	 * @param tableName
	 */
	public HistogramAnalysisResult(String tableName) {
		this.tableName = tableName;
		result = new ArrayList<BinHistogramAnalysisResult>();
	}

	/**
	 * @return the result
	 */
	public List<BinHistogramAnalysisResult> getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<BinHistogramAnalysisResult> result) {
		this.result = result;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public BinHistogramAnalysisResult getBinResult(int i) {
		return result.get(i);
	}

	public void addBinResult(BinHistogramAnalysisResult binResult) {
		result.add(binResult);
	}

	public String toString() {
		StringBuilder sb_result=new StringBuilder("columnName bin begin end count percentage accumCount accumPercentage");
		sb_result.append(Tools.getLineSeparator());

		for (BinHistogramAnalysisResult binResult : result) {
			sb_result.append(binResult.toString());
		}
		return sb_result.toString();
	}

	public void addSetOfBinResult(
			List<BinHistogramAnalysisResult> binHistogramResult) {
		this.result.addAll(binHistogramResult);
		
	}

}
