package com.alpine.datamining.api.impl.algoconf;

import com.alpine.datamining.api.impl.AbstractAnalyticConfig;

public class DataOperationConfig extends AbstractAnalyticConfig {
	
	public static final String ConstOutputSchema = "outputSchema";
	public static final String ConstOutputTable = "outputTable";
	public static final String ConstDropIfExist = "dropIfExist";
	public static final String ConstOutputType = "outputType";
	
	
	private String outputType;
	private String outputSchema;
	private String outputTable;
	private String dropIfExist;
	public DataOperationConfig() {
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DataOperationTableVisualizationType");
	}
	public DataOperationConfig(String outputType, String outputSchema,
			String outputTable, String dropIfExist) {
		this.outputType = outputType;
		this.outputSchema = outputSchema;
		this.outputTable = outputTable;
		this.dropIfExist = dropIfExist;
		setVisualizationTypeClass("com.alpine.datamining.api.impl.visual.DataOperationTableVisualizationType");
	}
	public String getOutputType() {
		return outputType;
	}
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}
	public String getOutputSchema() {
		return outputSchema;
	}
	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}
	public String getOutputTable() {
		return outputTable;
	}
	public void setOutputTable(String outputTable) {
		this.outputTable = outputTable;
	}
	public String getDropIfExist() {
		return dropIfExist;
	}
	public void setDropIfExist(String dropIfExist) {
		this.dropIfExist = dropIfExist;
	}
	
}
