
package com.alpine.datamining.operator.attributeanalysisresult;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.Tools;


public class HistogramAnalysisResult extends OutputObject {

	private static final long serialVersionUID = 1L;
	private List<BinHistogramAnalysisResult> result = null;
	private String tableName = null;

	public HistogramAnalysisResult() {
		result = new ArrayList<BinHistogramAnalysisResult>();
	}

	
	public HistogramAnalysisResult(String tableName) {
		this.tableName = tableName;
		result = new ArrayList<BinHistogramAnalysisResult>();
	}

	
	public List<BinHistogramAnalysisResult> getResult() {
		return result;
	}

	
	public void setResult(List<BinHistogramAnalysisResult> result) {
		this.result = result;
	}

	
	public String getTableName() {
		return tableName;
	}

	
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
