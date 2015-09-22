/**
 * ClassName FilterConfig.java
 *
 * Version information:1.00
 *
 * Date:Jun 1, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.datamining.api.impl.algoconf;

import java.util.ArrayList;

/**
 * @author Richie Lo
 *
 */
public class FilterConfig extends DataOperationConfig {

	private String whereClause;
	
	private static final String ConstWhereClause = "whereClause";

	private final static ArrayList<String> parameters=new  ArrayList<String>();
	static{
		parameters.add(ConstWhereClause);
		parameters.add(ConstOutputType);
		parameters.add(ConstDropIfExist);
		parameters.add(ConstOutputSchema);
		parameters.add(ConstOutputTable);
		parameters.add(ConstOutputTableStorageParameters);

		
	}

	public FilterConfig(String outputType, 
			String outputSchema, String outputTable, String dropIfExist) {
		
		super(outputType,outputSchema,outputTable,dropIfExist);
		setParameterNames(parameters);
		 	}
	
	public FilterConfig( ) {
		super();
		setParameterNames(parameters);
	}
	
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}
	
	public String getWhereClause() {
		return whereClause;
	}


}
