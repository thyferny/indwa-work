/**
 * ClassName DataAnlyticOutPut.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.output;

import com.alpine.datamining.api.impl.AbstractDBTableOutPut;

/**
 * @author John Zhao
 * means ourput is a table data
 */
public class AnalyzerOutPutTableObject extends AbstractDBTableOutPut   {

	//here move 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -562140496689475146L;
	DataTable dataTable;

	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}


	
}
