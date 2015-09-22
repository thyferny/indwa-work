package com.alpine.datamining.api.impl.output;

import java.util.List;

public class AnalyzerOutPutUnivariate extends AnalyzerOutPutTableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3879940285787471651L;

	 
	private String referenceColumn;
	private List<String> allSelectedColumn;
	private List<String> analysisColumn;
	 
	public String getReferenceColumn() {
		return referenceColumn;
	}
	public void setReferenceColumn(String referenceColumn) {
		this.referenceColumn = referenceColumn;
	}
	public List<String> getAnalysisColumn() {
		return analysisColumn;
	}
	public void setAnalysisColumn(List<String> analysisColumn) {
		this.analysisColumn = analysisColumn;
	}
	public List<String> getAllSelectedColumn() {
		return allSelectedColumn;
	}
	public void setAllSelectedColumn(List<String> allSelectedColumn) {
		this.allSelectedColumn = allSelectedColumn;
	}

}
