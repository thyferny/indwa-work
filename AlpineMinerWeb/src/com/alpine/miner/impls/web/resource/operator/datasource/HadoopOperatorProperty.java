/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * HadoopOperatorProperty.java
 */
package com.alpine.miner.impls.web.resource.operator.datasource;

/**
 * @author Gary
 * Jul 9, 2012
 */
public class HadoopOperatorProperty {
	private static final String OPERATOR_CLASS = "HadoopFileOperator";

	private String 	connectionName,
					filePath,
					fileName,
					operatorClass = OPERATOR_CLASS;//because gson convert json base on field. So sick.
	private boolean isDir;

	public HadoopOperatorProperty() {
		
	}
	
	public HadoopOperatorProperty(String connName, String filePath, String fileName, boolean isDir){
		this.connectionName = connName;
		this.filePath = filePath;
		this.fileName = fileName;
		this.isDir = isDir;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isDir() {
		return isDir;
	}

	public String getConnectionName() {
		return connectionName;
	}

}
