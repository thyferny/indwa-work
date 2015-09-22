package com.alpine.utility.hadoop.pig;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConstants;
import org.apache.log4j.Logger;
/***
 * 
 * @author John Zhao
 *
 */
@Deprecated
//this is never used now
public class PigServerClassLoaderFactory {
 
	//version -> classLoader
	private static HashMap<String,ClassLoader> classLoaderMap = new HashMap<String,ClassLoader>();
	private static String jarFileDir = // "file://" +
	AlpineUtil.getJarFileDir();
    private static final Logger itsLogger = Logger.getLogger(PigServerClassLoaderFactory.class);

	public static ClassLoader getClassLoader(String hadoopVersion) throws Exception{
		if(classLoaderMap.get(hadoopVersion)==null){
			ClassLoader classLoader = createHadoopClassLoader(hadoopVersion); 
			classLoaderMap.put(hadoopVersion, classLoader);
		}
		
		return classLoaderMap.get(hadoopVersion) ;
	}

	private static ClassLoader createHadoopClassLoader(
			String hadoopVersion) throws Exception {
		itsLogger.info("jarFileDir= " +jarFileDir);
		
		List<URL> urls= new ArrayList<URL>();
		if(StringUtil.isEmpty(hadoopVersion)){
			throw new Exception("Hadoop version is null");
		}else if (hadoopVersion.equalsIgnoreCase(HadoopConstants.VERSION_APACHE_HADOOP_0_20_2)){
			urls.add(  new File(jarFileDir+ HadoopConstants.JAR_APACHE_HADOOP_0_20_2 ).toURI().toURL() );
			urls.add( new File(jarFileDir+ HadoopConstants.JAR_APACHE_PIG_0_10_0).toURI().toURL()) ;//pig-0.10.0-withouthadoop.jar
		 
		}
//		else if (hadoopVersion.equalsIgnoreCase(HadoopConstants.VERSION_0_20_2_CDH3_U4)){
//			urls.add(  new URL(jarFileDir+ HadoopConstants.JAR_CDH3_U4_PIG_0_8_1 )) ;// 
//			urls.add(  new URL(jarFileDir+ "guava-r09-jarjar.jar"));
//		}
		else{
			throw new Exception("Hadoop version not supported :" +hadoopVersion);
		}
 
		urls.add( new File(jarFileDir+ "commons-configuration-1.6.jar").toURI().toURL());
		urls.add( 	 new File(jarFileDir+ "commons-lang-2.4.jar").toURI().toURL());
		urls.add( 	 new File(jarFileDir+ "commons-cli-1.2.jar").toURI().toURL());
		urls.add( 	 new File(jarFileDir+ "AlpineUtility.jar").toURI().toURL());
		urls.add( 	 new File(jarFileDir+ "log4j-1.2.8.jar").toURI().toURL());
		urls.add( 	 new File(jarFileDir+ "commons-httpclient-3.0.1.jar").toURI().toURL());
		urls.add( 	new File(jarFileDir+ "commons-logging.jar").toURI().toURL());
		 
		URL[] urlArray = new URL[urls.size()] ;
		for (int i = 0; i<urls.size();i++) {
			urlArray[i] = urls.get(i) ;
		}
		//		ClassLoader loader =  new HadoopClassLoader(jarFileDir, hadoopJarFile);
		ClassLoader loader = new HadoopClassLoader(urlArray); 
				//new URLClassLoader(urls,ClassLoader.getSystemClassLoader());
		return loader;
		 
	}

	
}
