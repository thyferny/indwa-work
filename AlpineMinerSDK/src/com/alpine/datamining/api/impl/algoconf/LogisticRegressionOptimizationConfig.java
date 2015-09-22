package com.alpine.datamining.api.impl.algoconf;

public class LogisticRegressionOptimizationConfig extends LogisticRegressionConfigGeneral {

	public static final String CONFIDENCE = "confidence";

	
	private String confidence = null;

	public LogisticRegressionOptimizationConfig(
			String columnames,	
			String dependentColumn){
			super();
			setColumnNames(columnames);
			setDependentColumn(dependentColumn);
		 	}

	public String getConfidence() {
		return confidence;
	}


	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}
	
}
