/**
 * ClassName :TempFileManagerImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-29
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alpine.miner.impls.taskmanager.Task;
import com.alpine.miner.impls.taskmanager.TaskManagerStore;
import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.taskmanager.executor.ExecutorFactory;
import com.alpine.miner.impls.taskmanager.impl.scheduler.SchedulerTrigger;
import com.alpine.miner.impls.taskmanager.rule.IntervalRule;
import com.alpine.miner.impls.taskmanager.rule.TriggerRule;
import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.utils.SysConfigManager;
import com.alpine.utility.file.FileUtility;
import org.apache.log4j.Logger;

/**
 * @author zhaoyong
 *
 */
public class TempFileManagerImpl implements TempFileManager ,Serializable{
    private static Logger itsLogger = Logger.getLogger(TempFileManagerImpl.class);


    /**
	 * 
	 */
	private static final long serialVersionUID = -6729769670533105552L;
	
	private String root = null;  

	//avoid the null;
	private String flowFolder= System.getProperty("java.io.tmpdir");
	private String modelFolder=  System.getProperty("java.io.tmpdir");
	private String reportFolder= System.getProperty("java.io.tmpdir");

	private long liveTime =SysConfigManager.DEFAULT_LIVE_TIME; 

	private long scanFrequency = SysConfigManager.DEFAUlT_SCAN_FREQUENCY;
	


	public TempFileManagerImpl(){
		
	}
	
	@Override
	public String getTempFolder4Flow() {
	 
		return flowFolder;
	}

	@Override
	public String getTempFolder4Model() {
	 
		return modelFolder;
	}

	@Override
	public String getTempFolder4Report() {
		 
		return reportFolder;
	}
	//this is for test use
	@Override
	public void init(String tempFolder,long liveTime, long scanFrequency) throws Exception {
		this.liveTime =liveTime;
		this.scanFrequency=scanFrequency;
		init(tempFolder);
	}
	
 
	private void init(String tempFolder) throws Exception{
		this.root = tempFolder;
		File folder = new File (this.root) ;
		if(folder.exists()==false){
			folder.mkdir();
		}
		
		flowFolder = this.initTempFolder(TYPE_FLOW);
		modelFolder = this.initTempFolder(TYPE_MODEL);
		reportFolder =this.initTempFolder(TYPE_REPORT);
		
		
//		String configFile = FilePersistence.Preference_PREFIX+SysConfigManager.configFileName;
//		File file = new File(configFile) ;
		 	
		 
		Task tempFileScanner = new TempFileScannerTask();			//init thr schedulrt 0 means always ...
		TriggerRule triggerRule = new IntervalRule(Calendar.getInstance().getTime(), 0, scanFrequency) ;
		
		TaskTrigger trigger = new SchedulerTrigger("sys_temp_group","sys_temp_triger",tempFileScanner, triggerRule, true);  
		//because the config may changed, need not remove, append will overwrite it 
		//TaskManagerStore.SCHEDULER.getInstance().removeTrigger(trigger) ;
		ExecutorFactory.getExecutor().run(trigger);
//		TaskManagerStore.SCHEDULER.getInstance().appendTrigger(trigger);
		
	}

	private String initTempFolder(String folderName) {
		if(this.root.endsWith(File.separator)==false) {
			this.root  =this.root +File.separator;
		}
		String tempFolder = this.root  + folderName;
		File folder = new File (tempFolder) ;
		if(folder.exists()==false){
			folder.mkdir();
		}
		return tempFolder;
	}

	// will be called by scheduler...
	@Override
	public void scanAndClear() {
		scanAndClearFolder(new File(flowFolder));
		scanAndClearFolder(new File(modelFolder));
		scanAndClearFolder(new File(reportFolder));
		
		
	}

	private void scanAndClearFolder(File folder) { 
		if(folder.isDirectory()){
			File[] files = folder.listFiles();
			if(files!=null){
				for(int i = 0;i<files.length;i++){
					if(files[i].isDirectory()){
						scanAndClearFolder(files[i]) ;
					}else{
						long age = liveTime+files[i].lastModified();
						//expired ... 
						if(age < System.currentTimeMillis()){
							String name = files[i].getName();
							if(files[i].delete()==true){
								itsLogger.info("Temp file deleted:"+name);
							}
						}
					}
				}
			}
		}
		
	}
  

}
