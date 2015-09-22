/**
 * ClassName  OperatSystemAdapter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.util.List;


/**
 * @author John Zhao
 *
 */
public interface OperaterSystemAdapter {

	public String getExecuteFileContent(String targetFlowFilePathName,String targetDirectory, boolean visual, boolean isHadoop, String libJarSourceDir);
	public List<String> getEngineJarFileNames();
	public List<String> getChartingJarFileNames();
	public List<String> getHadoopJarFileNames(String hadoopVersion);
	public String getExecuteFileName(String sourceFileName); 
	

}
