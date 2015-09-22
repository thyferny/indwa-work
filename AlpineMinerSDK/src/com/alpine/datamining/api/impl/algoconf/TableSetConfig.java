package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

import com.alpine.datamining.api.impl.db.attribute.model.tableset.AnalysisTableSetModel;

public class TableSetConfig extends DataOperationConfig {
	
	private AnalysisTableSetModel tableSetModel;
	
	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);
	}
	
	
	public TableSetConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
	}
	
	public TableSetConfig() {
		super();
		setParameterNames(parameters);
	}
	public AnalysisTableSetModel getTableSetModel() {
		return tableSetModel;
	}
	public void setTableSetModel(AnalysisTableSetModel tableSetModel) {
		this.tableSetModel = tableSetModel;
	}

	
}
