/**
 * ClassName  AbstractSystemAdapter.java
 *
 * Version information: 1.00
 *
 * Data: Jun 28, 2012
 *
 * COPYRIGHT (C) 2012 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.io.File;

import com.alpine.datamining.workflow.AlpineAnalyticEngine;
import com.alpine.utility.tools.StringHandler;


/**
 * @author John Zhao
 *
 */
public abstract class AbstractUnixSystemAdapter extends  AbstractSystemAdapter{

	private static final String FILE_SUFFIX = ".sh";
	private static final String CLASS_PATH_SEP = ":";

	/* (non-Javadoc)
	 * @see com.alpine.datamining.util.OperatSystemAdapter#getExecuteFileName(java.lang.String)
	 */
	@Override
	public String getExecuteFileName(String sourceFileName) {
		return sourceFileName+FILE_SUFFIX;
	}

	
	@Override
	public String 	getClassPathSeparator(){
		return CLASS_PATH_SEP;
	}
	
	@Override
	public StringBuffer createExportClasspathCommand(String libDir) {
		StringBuffer sb=new StringBuffer();
		sb.append("export miner_classpath=");
		appendJars(sb,libDir);
		sb.append("\n");
		return sb;
	}

	
 
}
