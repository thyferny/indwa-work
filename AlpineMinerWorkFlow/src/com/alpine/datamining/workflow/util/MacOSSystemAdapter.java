/**
 * ClassName  LinuxSystemAdapter.java
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
public class MacOSSystemAdapter extends AbstractUnixSystemAdapter {


	@Override
	protected String getPlatformSpecificChartingJars() {
		return "org.eclipse.swt.cocoa.macosx.x86_64_3.7.2.v3740f.jar";
	}
 
	//-XstartOnFirstThread is very important in MAC!!!
	@Override
	public String getExecuteFileContent(String flowName,
			String targetDirectory ,boolean isVisual,boolean isHadoop,String libJarSourceDir) {
		StringBuffer sb = createExportClasspathCommand(libJarSourceDir);
		String vizual = "Yes" ;
		if(isVisual == false){
			vizual = "No" ;
		}
		String command=sb.toString();
		command=command +"java -XstartOnFirstThread -Xms128m -Xmx512m -classpath $miner_classpath " +AlpineAnalyticEngine.class.getCanonicalName()+
				" \"" +flowName+"\" \""+targetDirectory+"\" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_User+"=")+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_Visual+"="+vizual)+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_NewThread+"=No")+" "+    
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_ModeSaveFolder+"=")+" "+
				StringHandler.doubleQ(AlpineAnalyticEngine.ARG_HadoopLocalMode+"=false")
				+"\n";
		return command; 
	}
}
