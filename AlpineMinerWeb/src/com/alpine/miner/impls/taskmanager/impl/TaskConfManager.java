/**
 * 
 */
package com.alpine.miner.impls.taskmanager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.LogFactory;

import com.alpine.miner.impls.taskmanager.TaskTrigger;
import com.alpine.miner.impls.web.resource.FilePersistence;

/**
 * @author Gary
 * read and write scheduler configurations
 */
public class TaskConfManager {

	private static final File CONFIGURATION_ROOT;
	
	static{
		CONFIGURATION_ROOT = new File(FilePersistence.initRoot() + "scheduler" + File.separator);
		if(!CONFIGURATION_ROOT.exists()){
			CONFIGURATION_ROOT.mkdirs();
		}
	}
	
	public static boolean exists(String group, String triggerName){
		File directory = new File(CONFIGURATION_ROOT.getPath() + File.separator + group);
		if(!directory.exists()){
			return false;
		}
		boolean exists = false;
		for(String taskPersistenceName : directory.list()){
			if(taskPersistenceName.equalsIgnoreCase(triggerName.trim())){
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	public static void readConf(TriggerReader handler){
		readConf(CONFIGURATION_ROOT, handler);
	}
	
	public static void readConfByGroup(String group, TriggerReader handler){
		File groupFolder = new File(CONFIGURATION_ROOT.getPath() + File.separator + group);
		if(!groupFolder.exists()){
			return;
		}
		readConf(groupFolder, handler);
	}
	
	private static void readConf(File folder, TriggerReader handler){
		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				readConf(file,handler);
			}else{
				ObjectInputStream ois = null;
				try {
					ois = new ObjectInputStream(new FileInputStream(file));
					handler.read((TaskTrigger)ois.readObject());
				} catch (Exception e) {
					LogFactory.getLog(TaskConfManager.class).error("read config of task is failed.", e);
				}finally{
					if(ois != null){
						try {
							ois.close();
						} catch (IOException e) {
							LogFactory.getLog(TaskConfManager.class).error(e);
						}
					}
				}
			}
		}
	}
	
	public static void writeConf(TaskTrigger trigger){
		File directory = new File(CONFIGURATION_ROOT.getPath() + File.separator + trigger.getGroup());
		if(!directory.exists()){
			directory.mkdir();
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(directory.getPath() + File.separator + trigger.getName().trim()));
			oos.writeObject(trigger);
		} catch (Exception e) {
			LogFactory.getLog(TaskConfManager.class).error("save config of task is failed.", e);
		}finally{
			try {
				if(oos != null){
					oos.close();
				}
			} catch (IOException e) {
				LogFactory.getLog(TaskConfManager.class).error(e);
			}
		}
	}
	
	public static void removeConf(TaskTrigger trigger){
		File taskFile = new File(CONFIGURATION_ROOT.getPath() + File.separator + trigger.getGroup() + File.separator + trigger.getName());
		boolean isDelete = taskFile.delete();
		if(!isDelete){
			throw new RuntimeException("Cannot delete file: " + taskFile.getParent());
		}
//		FlowFileStore.removeFlowFile(trigger.getTask().getParams().get(FlowTaskRunerImpl.FLOW_FILE_NAME));
	}
	
	public static interface TriggerReader{
		/**
		 * read file hook method
		 * @param conf
		 */
		void read(TaskTrigger trigger);
	}
}
