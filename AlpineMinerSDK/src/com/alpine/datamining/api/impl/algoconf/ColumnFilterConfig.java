/**
 * ClassName ColumnFilterConfig.java
 *
 * Version information:1.00
 *
 * Date:May 13, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

/**
 * @author Eason
 *
 */
public class ColumnFilterConfig extends DataOperationConfig {

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(PARAMETER_COLUMN_NAMES);
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);

	}

	public ColumnFilterConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
		 	}
	
	public ColumnFilterConfig( ) {
		super();
		setParameterNames(parameters);
	}
}
