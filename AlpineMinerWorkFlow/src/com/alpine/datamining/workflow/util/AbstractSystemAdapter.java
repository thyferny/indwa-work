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
import java.util.ArrayList;
import java.util.List;

import com.alpine.utility.hadoop.HadoopConstants;

/**
 * @author John Zhao
 *
 */
public abstract class AbstractSystemAdapter implements OperaterSystemAdapter {
	
 
 
	public abstract String 	getClassPathSeparator();
	public abstract String getExecuteFileName(String sourceFileName) ;
	public abstract StringBuffer createExportClasspathCommand(String libDir);

	protected void appendJars(StringBuffer sb, String libDir) {
		File libFolder = new File(libDir);
		File[] files = libFolder.listFiles() ;
		int index = 0;
		if(files!=null){
			for (File file : files) {
				if(file.isFile()==true&&file.getName().endsWith(".jar")){
					String jarName = file.getName().substring(file.getName().lastIndexOf(File.separator)+1,
							file.getName().length()) ;
					if(index ==0){
						sb.append("./lib/" + jarName);
					}else{
						sb.append(getClassPathSeparator()+"./lib/"+jarName);	
					}
					
					index = index+1;
				}
			}
		}
	
	}
	
	@Override
	public List<String> getEngineJarFileNames() {
		List<String> list= new ArrayList<String>();
		list.add("commons-codec-1.3.jar");	
		list.add("log4j-1.2.17.jar");
		list.add("slf4j-api-1.6.1.jar");
		list.add("jcommon-1.0.16.jar");
		list.add("commons-logging-1.1.1.jar");
		
		list.add("commons-configuration-1.6.jar");
		list.add("commons-httpclient-3.0.1.jar");
		list.add("commons-lang-2.4.jar");
		list.add("commons-cli-1.2.jar");
		
		list.add("postgresql-8.3-604.jdbc4.jar");
		list.add("opencsv-2.3.jar");
		
		list.add("AlpineDataAnalysis.jar");
		list.add("AlpineMinerSDK.jar");
		list.add("AlpineMinerInterface.jar");
		list.add("AlpineMinerWorkFlow.jar");
		list.add("AlpineUtility.jar");
		
		//belowing is for remote log collector
		list.add("httpasyncclient-4.0-beta3.jar");
		list.add("httpasyncclient-cache-4.0-beta3.jar");
		list.add("httpclient-4.2.1.jar");
		list.add("httpclient-cache-4.2.1.jar");
		list.add("httpcore-4.2.2.jar");
		list.add("httpcore-nio-4.2.2.jar");
		list.add("json-lib-2.2.3-jdk13.jar");
		list.add("ezmorph-1.0.3.jar");
		list.add("commons-collections-3.2.jar");
		list.add("commons-beanutils-1.6.jar");
		list.add("commons-digester.jar");
		list.add("commons-httpclient-3.0.1.jar");
		list.add("commons-validator.jar");
		return list;
 
	}
	 
	protected abstract String getPlatformSpecificChartingJars(  ) ;

	protected void appendAlpineJarfiles(StringBuffer sb) {

	 
		
	}
	@Override
	public List<String> getChartingJarFileNames() {
		List<String> list= new ArrayList<String>();
		
		list.add("jfreechart-1.0.13.jar");
		list.add("jfreechart-1.0.13-experimental.jar");
		list.add("jfreechart-1.0.13-swt.jar");
		list.add("org.eclipse.draw2d_3.5.2.v20091126-1908.jar");
		list.add("org.eclipse.swt_3.5.2.v3557f.jar");
		list.add("swtgraphics2d.jar");

		list.add(getPlatformSpecificChartingJars());
		return list;
	}


 
	 
 
	@Override
	public List<String> getHadoopJarFileNames(String hadoopVersion) {
		List<String> list= new ArrayList<String>();
//		list.add("pig-0.10.0-withouthadoop.jar");
		list.add("AlpinePig.jar");
		list.add("AlpineHadoopAnalytics.jar");

//		list.add("hadoop-0.20.2-core.jar");
		list.add("commons-io-2.4.jar");
		list.add("datafu-0.0.6-SNAPSHOT.jar");
		list.add("commons-math-2.2.jar");
		list.add("gson-1.7.1.jar");
		
		list.addAll(HadoopConstants.JarFileMap.get(hadoopVersion));     
		
		return list;	
	}


 
}
