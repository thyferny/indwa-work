/**
 * ClassName  WindowsSystemAdapter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import com.alpine.datamining.workflow.AlpineAnalyticEngine;
import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 *
 */
public class WindowsSystemAdapter extends AbstractSystemAdapter {

	private static final String FILE_SUFFIX = ".bat";
	private static final String CLASS_PATH_SEP = ";";
	
	@Override
	public String 	getClassPathSeparator(){
		return CLASS_PATH_SEP;
	}
	/* (non-Javadoc)
	 * @see com.alpine.datamining.util.OperatSystemAdapter#getExecuteFileName(java.lang.String)
	 */
	@Override
	public String getExecuteFileName(String sourceFileName) {
		
		return sourceFileName+FILE_SUFFIX; 
	}

	/* (non-Javadoc)
	 * @see com.alpine.datamining.util.OperatSystemAdapter#getExecuteFileContent(java.lang.String)
	 */
	@Override
	public String getExecuteFileContent(String flowName,String targetDirectory,boolean isVisual,boolean isHadoop,String libJarSourceDir) {
		StringBuffer sb = createExportClasspathCommand(libJarSourceDir);
	
		String vizual = "Yes" ;
		if(isVisual == false){
			vizual = "No" ;
		}
		String miner_classpath=sb.toString();
		String command="java -Xms512m -Xmx1024m -classpath "+miner_classpath+" " +
				AlpineAnalyticEngine.class.getCanonicalName() + " \""
				+flowName+"\" \""+targetDirectory+"\" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_User+"=")+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_Visual+"=" +vizual)+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_NewThread+"=No")+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_ModeSaveFolder+"=\"\"")+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_HadoopLocalMode+"=false");

		return command; 
	} 



	@Override
	public StringBuffer createExportClasspathCommand(String libDir) {
		StringBuffer sb=new StringBuffer();
		appendJars(sb,libDir);
		return sb;
	}

	protected String getPlatformSpecificChartingJars(  ){
		return "org.eclipse.swt.win32.win32.x86_3.5.2.v3557f.jar";
	}

 
}
