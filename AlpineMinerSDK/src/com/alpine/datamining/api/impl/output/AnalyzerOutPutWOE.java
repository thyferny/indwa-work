/**
* ClassName AnalyzerOutPutWOE.java
*
* Version information: 1.00
*
* Data: 25 Oct 2011
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
**/


package com.alpine.datamining.api.impl.output;

import java.sql.Connection;

import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.operator.woe.AnalysisWOETable;
import com.alpine.datamining.utility.DatabaseConnection;

/**
 * @author Shawn
 *
 */
public class AnalyzerOutPutWOE extends AnalyzerOutPutTableObject {
	
	private static final long serialVersionUID = -1994887333876307324L;
	
	

	AnalysisWOETable resultList;
	DataSet dataset;
	 
	public DataSet getDataset() {
		return dataset;
	}
	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
	}
	private AnalyzerOutPutTableObject WOETables;


	public AnalyzerOutPutTableObject getWOETables() {
		return WOETables;
	}



	public void setWOETables(AnalyzerOutPutTableObject wOETables) {
		WOETables = wOETables;
	}



	public AnalysisWOETable getResultList() {
		return resultList;
	}



	public void setResultList(AnalysisWOETable resultList) {
		this.resultList = resultList;
	}
	
	public String toString()
	{
		return resultList.toString();
	}
	
	public Connection getConnection(){
		 
		DataSet dataSet =  getDataset();
		DatabaseConnection databaseConnection = ((DBTable) dataSet.getDBTable()).getDatabaseConnection();
 
		return databaseConnection.getConnection();
	} 

}
