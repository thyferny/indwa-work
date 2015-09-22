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

import java.sql.Connection;
import java.util.List;

import com.alpine.datamining.api.impl.AbstractDBTableOutPut;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.utility.DatabaseConnection;

/**
 * @author John Zhao
 * tell the user what happens 
 */
public class AnalyzerOutPutDataBaseUpdate extends AbstractDBTableOutPut  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7944293624847876970L;
	
	List<String> updatedColumns;
	
	String updatedSummary;
	 
	DataSet dataset;
 
	public DataSet getDataset() {
		return dataset;
	}
	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
	}
	 
	String updateType;//add column, update column, delete comlumn
	 
	public String getUpdateType() {
		return updateType;
	}
	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}
	 

	public Connection getConnection(){
 
		DataSet dataSet =  getDataset();
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
 
		return databaseConnection.getConnection();
	} 
 
	public List<String> getUpdatedColumns() {
		return updatedColumns;
	}
	public void setUpdatedColumns(List<String> updatedColumns) {
		this.updatedColumns = updatedColumns;
	}
	public String getUpdatedSummary() {
		return updatedSummary;
	}
	public void setUpdatedSummary(String updatedSummary) {
		this.updatedSummary = updatedSummary;
	}

}
