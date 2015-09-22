package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class TableScatterConfig extends AbstractAnalyticConfig {

	private String dependentColumn;
	private String referenceColumn;
	private String referenceType;
	private String categoryColumn;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	private static final String PARAMETER_Y_COLUMN = "columnY";
	private static final String PARAMETER_X_COLUMN = "columnX";
	private static final String PARAMETER_C_COLUMN = "categoryColumn";
	static{ 
		parameters.add(PARAMETER_Y_COLUMN);
		parameters.add(PARAMETER_X_COLUMN);
		parameters.add(PARAMETER_C_COLUMN);
	}
	
	public TableScatterConfig(String depColumn,String refColumn,String refCategoryColumn,String refType){
		setColumnY(depColumn);
		setColumnX(refColumn);
		setCategoryColumn(refCategoryColumn);
		setReferenceType(refType);
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.ScatterImageVisualizationType");
		setParameterNames(parameters);
	}
	
	public TableScatterConfig(){
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.ScatterImageVisualizationType");
		setParameterNames(parameters);
	}
	
	public String getColumnY() {
		return dependentColumn;
	}
	public void setColumnY(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}
	public String getColumnX() {
		return referenceColumn;
	}
	public void setColumnX(String referenceColumn) {
		this.referenceColumn = referenceColumn;
	}
	public String getReferenceType() {
		return referenceType;
	}
	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}
	public String getCategoryColumn() {
		return categoryColumn;
	}

	public void setCategoryColumn(String categoryColumn) {
		this.categoryColumn = categoryColumn;
	}
}
