package com.alpine.datamining.api.impl.output;



public class AnalyzerOutPutScatter extends AnalyzerOutPutTableObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4064262860755880511L;
	private String dependentColumn;
	private String referenceColumn;
	private String categoryColumn;
	public String getCategoryColumn() {
		return categoryColumn;
	}

	public void setCategoryColumn(String categoryColumn) {
		this.categoryColumn = categoryColumn;
	}

	public String getDependentColumn() {
		return dependentColumn;
	}

	public void setDependentColumn(String dependentColumn) {
		this.dependentColumn = dependentColumn;
	}

	public String getReferenceColumn() {
		return referenceColumn;
	}

	public void setReferenceColumn(String referenceColumn) {
		this.referenceColumn = referenceColumn;
	}

	private String referenceColumnType;
	 

	public String getReferenceColumnType() {
		return referenceColumnType;
	}

	public void setReferenceColumnType(String referenceColumnType) {
		this.referenceColumnType = referenceColumnType;
	}
	
}
