/**
 * ClassName  ExecutableFlowExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alpine.datamining.api.impl.algoconf.CustomziedConfig;
import com.alpine.utility.db.DataSourceInfoDB2;
import com.alpine.utility.db.DataSourceInfoNZ;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.FileUtility;
import com.alpine.utility.hadoop.HadoopConnection;

/**
 * @author John Zhao
 *
 */
public class ExecutableFlowExporter {
    private static final Logger itsLogger=Logger.getLogger(ExecutableFlowExporter.class);


    /**
	 * @param jarFileRootDir
	 * @param systemAdapter 
	 * @param extraLibRootDir 
	 * @param tempDir
	 */
	private static void copyJarFiles(String jarFileRootDir, String targetLibDir,boolean isVisual,boolean isHadoop, 
			OperaterSystemAdapter systemAdapter, String chartingLibRootDir,String hadoopVersion) {
		
		if(isVisual == true){
			copyJars(chartingLibRootDir,targetLibDir , systemAdapter.getChartingJarFileNames(),false);	
		}
		if(isHadoop == true){  
			copyJars(jarFileRootDir, targetLibDir, systemAdapter.getHadoopJarFileNames(hadoopVersion),false);	
		}
		
		copyJars(jarFileRootDir, targetLibDir, systemAdapter.getEngineJarFileNames(),false);	
 
	}

	/**
	 * @param jarFileRootDir
	 * @param targetDir
	 * @param jarFiles
	 * @param overwrite
	 */
	private static void copyJars(String jarFileRootDir, String targetDir,
			List<String> jarFiles, boolean overwrite) {
		try {
			for (Iterator iterator = jarFiles.iterator(); iterator.hasNext();) {
				String jarFile = (String) iterator.next();
				String jarFilePath=jarFileRootDir+File.separator+jarFile;
			
				FileUtility.copy(new File(jarFilePath), targetDir,overwrite);
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 
//	//return the script name
//	public static String exportExecutableFlowWithLib(String sourceFlowFile,String targetDirectory,
//			OperatorWorkFlow ow, String jarFileRootDir,
//			String extraLibRootDir, String licenseFileFullPath) throws Exception{
//		File sourceFile=new File(sourceFlowFile);
//		
//		
//		//save flow
//		XMLWorkFlowSaver saver=new XMLWorkFlowSaver();
//		saver.doSave(targetDirectory+File.separator+sourceFile.getName(), ow, System.getProperty("user.name"), false);
//	 
//		String exeFilePathName = exportExecutableFlow(targetDirectory,
//				jarFileRootDir, extraLibRootDir, sourceFile,
//				OperaterSystemAdapterFactory.getAdatper(),true,true,
//				licenseFileFullPath);
//		
//		return exeFilePathName;
//	}

 

	public static boolean exportExecutableFlow(String targetDirectory,
			String jarFileRootDir, String extraLibRootDir, File sourceFile,
			OperaterSystemAdapter systemAdapter ,boolean isVisual,boolean isHadoop,
			String licenseFileFullPath, String customId)
			throws IOException {
		String hadoopVersion = HadoopConnection.CURRENT_HADOOP_VERSION;
		String sourceFileName =sourceFile.getName();
		sourceFileName=sourceFileName.substring(0,sourceFileName.lastIndexOf("."));
		String exeFileName=systemAdapter.getExecuteFileName(sourceFileName);
		
	
		
		//create the lib file
		File libDir= new File(targetDirectory+File.separator+"lib");
		if(libDir.exists()==false){
			libDir.mkdir();
		}
		String targetLibDir = libDir.getAbsolutePath() ;
		//copy the jar files
		copyCustomizedObject(targetDirectory,CustomziedConfig.getObjectPath());
		
		copyJarFiles(jarFileRootDir,targetLibDir,  isVisual,  isHadoop ,systemAdapter,extraLibRootDir,hadoopVersion);

		copyOracleJdbc(libDir.getAbsolutePath());
		copyDB2Jdbc(libDir.getAbsolutePath());
		copyNZJdbc(libDir.getAbsolutePath());
		
		copyLicenseFile(licenseFileFullPath,targetLibDir);
		storeCustomId(customId, targetLibDir);
		
		String exeFileContent=systemAdapter.getExecuteFileContent("./"+sourceFile.getName(),"./",isVisual, 
				isHadoop,targetDirectory+File.separator+"lib");//newFilePathName,targetDirectory);

		String exeFilePathName=targetDirectory+File.separator+exeFileName;
		//generate the execute bat file
		FileUtility.writeFile(exeFilePathName,exeFileContent);
		
		return true;
	}
	 
 
 
	private static void copyNZJdbc(String targetDir) {
		File nzJdbc=new File(DataSourceInfoNZ.getJdbcFullFilePath());
		if(nzJdbc.exists()){
			try {
				FileUtility.copy(nzJdbc, targetDir ,false);
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}



	private static void copyDB2Jdbc(String targetDir) {
		File db2Jdbc=new File(DataSourceInfoDB2.getJdbcFullFilePath());
		if(db2Jdbc.exists()){
			try {
				FileUtility.copy(db2Jdbc, targetDir,false);
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}



	private static void copyCustomizedObject(String targetDirectory,
			String modelPath) {
 
		
		File modelFile=new File(modelPath);
		String[] fileList=modelFile.list();
		if(fileList==null)return;
		
		targetDirectory=targetDirectory+File.separator+"configuration"+File.separator
		+"CustomizedOperator"+Resources.minerEdition;
		File targetFile=new File(targetDirectory);
		if(!targetFile.exists()){
			targetFile.mkdirs();
		}
		for(int i=0;i<fileList.length;i++){
			String jarFilePath=modelPath+File.separator+fileList[i];
			try {
				File objFile=new File(targetDirectory+File.separator+fileList[i]);
				if(objFile.exists()){
					continue;
				}
				FileUtility.copy(new File(jarFilePath), targetDirectory,false);
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
			}
		}
	}



	private static void copyOracleJdbc(String targetDir) {
		File orclJdbc=new File(DataSourceInfoOracle.getJdbcFullFilePath());
		if(orclJdbc.exists()){
			try {
				FileUtility.copy(orclJdbc, targetDir,false);
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}

 

	/**
	 * @param jarFileRootDir
	 * @param absolutePath
	 */
	private static void copyLicenseFile(String licenseFileFullPath,
			String targetDir) {
		File licenseFile= new File(licenseFileFullPath);
		if(licenseFile.exists()==false){
			itsLogger.error("copyLicenseFile:file doesn't exists!");
		}else{
			try {
				FileUtility.copy(licenseFile, targetDir);
			} catch (Exception e) {
				e.printStackTrace();
				itsLogger.error("copyLicenseFile:"+e.getMessage());
			}
		}
		
	}
	
	private static void storeCustomId(String customId, String targetDir){
		Map<String, String> storage = new HashMap<String, String>();
		File dir = new File(targetDir);
		if(!dir.exists()){
			dir.mkdirs();
		}
		storage.put("customId", customId);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(targetDir + File.separator + "executable_configuration"));
			oos.writeObject(storage);
		} catch (Exception e) {
			itsLogger.error("store custom id:"+e.getMessage());
		}finally{
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					
				}
			}
		}
	}
}
