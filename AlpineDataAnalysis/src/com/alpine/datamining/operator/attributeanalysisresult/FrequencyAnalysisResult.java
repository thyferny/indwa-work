
package com.alpine.datamining.operator.attributeanalysisresult;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.Tools;


public class FrequencyAnalysisResult extends OutputObject {
	
	private static final long serialVersionUID = 1L;
	private String tableName = null;
	private List<ValueFrequencyAnalysisResult> frequencyAnalysisResult = null;

	
	public FrequencyAnalysisResult(String tableName) {
		this.tableName = tableName;
		frequencyAnalysisResult = new ArrayList<ValueFrequencyAnalysisResult>();
	}

	
	public String getTableName() {
		return tableName;
	}

	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	
	public List<ValueFrequencyAnalysisResult> getFrequencyAnalysisResult() {
		return frequencyAnalysisResult;
	}

	
	public void setFrequencyAnalysisResult(
			List<ValueFrequencyAnalysisResult> frequencyAnalysisResult) {
		this.frequencyAnalysisResult = frequencyAnalysisResult;
	}

	public void addValueFrequencyAnalysisResult(
			ValueFrequencyAnalysisResult valueFrequencyAnalysisResult) {
		frequencyAnalysisResult.add(valueFrequencyAnalysisResult);
	}

	public ValueFrequencyAnalysisResult getValueFrequencyAnalysisResult(int i) {
		return frequencyAnalysisResult.get(i);
	}

	// @Override
	// public String getExtension() {
	// return null;
	// }
	//
	// @Override
	// public String getFileDescription() {
	// return null;
	// }

	public String toString() {
		StringBuilder sb_result=new StringBuilder("columnName columnValue count percentage");
		sb_result.append(Tools.getLineSeparator());
		for (int i = 0; i < frequencyAnalysisResult.size(); i++) {
			sb_result.append(frequencyAnalysisResult.get(i).toString());
		}
		return sb_result.toString();
	}

}
