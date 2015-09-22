package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class TableUnivariateConfig extends AbstractAnalyticConfig {
	
	public static final String PARAMETER_REFERENCE_COLUMN = "referenceColumn";
	public static final String PARAMETER_ANALYSIS_COLUMN = "analysisColumn";

	private String referenceColumn;
	private List<String> analysisColumn;
	private List<String> allSelectedColumn;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{ 
		parameters.add(PARAMETER_REFERENCE_COLUMN);
		parameters.add(PARAMETER_ANALYSIS_COLUMN);
	}
	
	public TableUnivariateConfig(){
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.UnivariateImageVisualizationType");
		setParameterNames(parameters);
	}
	
	public TableUnivariateConfig(String referenceColumn,List<String> analysisColumn,List<String> allSelectedColumn){
		setReferenceColumn(referenceColumn);
		setAnalysisColumn(analysisColumn);
		setAllSelectedColumn(allSelectedColumn);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.UnivariateImageVisualizationType");
		setParameterNames(parameters);
	}
	
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
