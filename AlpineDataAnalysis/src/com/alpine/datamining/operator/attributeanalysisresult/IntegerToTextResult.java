/**
 * ClassName IntegerToTextResult.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-24
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.attributeanalysisresult;

import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.OutputObject;

public class IntegerToTextResult extends OutputObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8601072662651736395L;
	
	String newTableName=null;
	
	DataSet dataSet=null;

	public DataSet getDataset() {
		return dataSet;
	}

	public void setDataset(DataSet dataset) {
		this.dataSet = dataset;
	}

	public IntegerToTextResult(String newTableName) {
		this.newTableName = newTableName;
	}

	public String getNewTableName() {
		return newTableName;
	}

	public void setNewTableName(String newTableName) {
		this.newTableName = newTableName;
	}

}
