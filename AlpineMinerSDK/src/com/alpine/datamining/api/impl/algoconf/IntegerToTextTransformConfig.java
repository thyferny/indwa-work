package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;
import java.util.List;

public class IntegerToTextTransformConfig extends DataOperationConfig {

	private String columnNames = null;
	private String modifyOriginTable=null;



	private static final List<String> parameterNames = new ArrayList<String>();
	

	public static final String PARAMETER_MODIFY_ORIGIN_TABLE = "modifyOriginTable";
	static{
		parameterNames.add(PARAMETER_COLUMN_NAMES);
		parameterNames.add(ConstOutputType);
		parameterNames.add(PARAMETER_MODIFY_ORIGIN_TABLE); 
		parameterNames.add(ConstOutputSchema);
		parameterNames.add(ConstOutputTable);
		parameterNames.add(ConstDropIfExist);
		parameterNames.add(ConstOutputTableStorageParameters);

	}
	
	public String getModifyOriginTable() {
		return modifyOriginTable;
	}

	public void setModifyOriginTable(String modifyOriginTable) {
		this.modifyOriginTable = modifyOriginTable;
	}

	public String getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

	public IntegerToTextTransformConfig(String columnNames) {
		super();
		this.columnNames = columnNames;
	}
	
	public IntegerToTextTransformConfig(){
		super();
		setParameterNames(parameterNames);
	}
	
}
