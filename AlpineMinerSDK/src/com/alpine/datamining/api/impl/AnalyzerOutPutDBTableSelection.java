/**
 * ClassName  AnalyzerOutPutDBTableDef.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl;

/**
 * @author John Zhao
 * 
 */
public class AnalyzerOutPutDBTableSelection extends AbstractDBTableOutPut {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4015937739294467450L;
	private String dbConenctionName;
	
	public String getDbConenctionName() {
		return dbConenctionName;
	}

	public void setDbConenctionName(String dbConenctionName) {
		this.dbConenctionName = dbConenctionName;
	}
}
