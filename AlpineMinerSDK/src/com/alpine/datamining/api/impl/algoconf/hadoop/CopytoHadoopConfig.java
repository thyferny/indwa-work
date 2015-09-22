/**
 * ClassName  DBTableSelectorCongif.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-1
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.algoconf.hadoop;

import java.util.List;

import com.alpine.datamining.api.impl.db.DataBaseAnalyticSource;


/**
 * @author John Zhao
 *
 */
public class CopytoHadoopConfig extends HadoopFileSelectorConfig {

	public static final String OPTION_DROP = "Drop";
	public static final String OPTION_APPEND = "Append";
	public static final String OPTION_SKIP = "Skip";
	public static final String OPTION_ERROR = "Error";
	
	private String ifFileExists ="";
	//this is no use ...be cause we use the filename in superclass
	private String resultsLocation = "";
	public String getResultsLocation() {
		return resultsLocation;
	}

	public void setResultsLocation(String resultsLocation) {
		this.resultsLocation = resultsLocation;
	}

	public String getCopyToFileName() {
		return copyToFileName;
	}

	public void setCopyToFileName(String copyToFileName) {
		this.copyToFileName = copyToFileName;
	}

	private String  copyToFileName = "";

	public String getIfFileExists() {
		return ifFileExists;
	}

	public void setIfFileExists(String ifFileExists) {
		this.ifFileExists = ifFileExists;
	}

	public CopytoHadoopConfig(){
		super();
		List<String> parameters = getParameterNames();
		parameters.add("ifFileExists");
		setParameterNames(parameters);
	}
}
