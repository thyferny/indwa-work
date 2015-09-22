/**
 * 

* ClassName AnalyzerOutPutDataBaseUpdatePLDA.java
*
* Version information: 1.00
*
* Data: Mar 14, 2012
*
* COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.


 */
package com.alpine.datamining.api.impl.output;

/**
 * @author Shawn
 *
 */
public class AnalyzerOutPutDataBaseUpdatePLDA extends AnalyzerOutPutDataBaseUpdate{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3498014709772254713L;

	private  String docTopicOutSchema;
	private  String docTopicOutTable;

	public  String getDocTopicOutTable() {
		return docTopicOutTable;
	}

	public  void setDocTopicOutTable(String docTopicOutTable) {
		this.docTopicOutTable = docTopicOutTable;
	}

	public String getDocTopicOutSchema() {
		return docTopicOutSchema;
	}

	public void setDocTopicOutSchema(String docTopicOutSchema) {
		this.docTopicOutSchema = docTopicOutSchema;
	}
	
}
