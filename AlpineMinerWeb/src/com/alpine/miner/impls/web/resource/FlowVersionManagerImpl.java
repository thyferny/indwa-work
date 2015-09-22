/**
 * ClassName :FlowVersionManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-10-13
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.interfaces.FlowVersionManager;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 * 
 */
public class FlowVersionManagerImpl implements FlowVersionManager {
	ResourceManager rmgr = ResourceManager.getInstance();

	public FlowVersionManagerImpl() {

	}

	public FlowInfo relaodFlowInfo(FlowInfo flowInfo) throws IOException,
			FileNotFoundException {
 
		
		Properties props = FilePersistence.INSTANCE.readProperties(new File(rmgr
				.generateResourceKey(flowInfo) + FilePersistence.INF));
		// initialize info fields with property values
		flowInfo = new FlowInfo();
		rmgr.getResourceFromProperties(flowInfo, props);
		rmgr.generateResourceKey(flowInfo);
		return flowInfo;
	}

	public List<FlowInfo> getFlowVersionInfos(FlowInfo info) {
		List<FlowInfo> versions = new ArrayList<FlowInfo>();
		// current version
		versions.add(info);
		if (info.getVersion() == null || info.getVersion().trim().length() == 0) {
			info.setVersion(FlowInfo.INIT_VERSION);
		}

		if (Integer.parseInt(info.getVersion()) > 1) { // history version

			List<FlowInfo> hisotrys = rmgr.getFlowHistorys(info);
			if (hisotrys != null) {
				versions.addAll(hisotrys);
			}
		}
		return versions;
	}

    public void copyHistoryToNewName(FlowInfo src, FlowInfo dest) throws Exception
    {
       List<FlowInfo> versions = rmgr.getFlowHistorys(src);
        for (FlowInfo info : versions) {
            FlowInfo newFlowInfo = relaodFlowInfo4Version(info, info.getVersion());
            FilePersistence.INSTANCE.loadXMLFlowData(newFlowInfo);
            newFlowInfo.setId(dest.getId());
            String newPath = rmgr.getFlowVersionPath(dest,Integer.parseInt(newFlowInfo.getVersion()));

            FilePersistence.INSTANCE.storeHistoricalFlowInfoAndData(newFlowInfo,newPath);
      }
    }

	public void copyFlowToHistory(FlowInfo flowInfo) throws IOException {
		// 1 copy to version folder + subfolder...
		String oldPath = rmgr.generateResourceKey(flowInfo);
		String oldFlowInfoPath = oldPath + FilePersistence.INF;
		String oldFlowFilePath = oldPath + FilePersistence.AFM;
		if(StringUtil.isEmpty(flowInfo.getVersion())){ 
			flowInfo.setVersion(FlowInfo.INIT_VERSION) ;
		}
		String newPath = rmgr.getFlowVersionPath(flowInfo,
				Integer.parseInt(flowInfo.getVersion()));
		String newFlowInfoPath = newPath + FilePersistence.INF;
		String newFlowFilePath = newPath + FilePersistence.AFM;

		FileUtils
				.copyFile(new File(oldFlowInfoPath), new File(newFlowInfoPath));
		FileUtils
				.copyFile(new File(oldFlowFilePath), new File(newFlowFilePath));
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.interfaces.FlowVersionManager#hasHistoryVersin(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean hasHistoryVersion(String flowName, String user) {
		
		String versionDir = FilePersistence.FLOWPREFIX + ResourceType.Personal+File.separator +user+File.separator
		+flowName+FilePersistence.FOLDER_SUFFIX_VERSION;
		
		return hasFlowInFolder(versionDir);
	}

	private boolean hasFlowInFolder(String versionDir) {
		File folder = new File(versionDir);
		boolean has= false;
		if(folder.exists()==true){
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f= files[i];
				if(f.getName().endsWith(FilePersistence.INF)){
					String path = f.getAbsolutePath(); 
					String flowFileName =path.substring(0,path.length()-4)+FilePersistence.AFM;
					File flowFile = new File(flowFileName);
					if(flowFile.exists()==true){
						has=true;
						break;
					}
				}
			}
		}
		
		return has;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.interfaces.FlowVersionManager#getLatestFlowVersion(java.lang.String, java.lang.String)
	 */
	@Override
	public String getLatestFlowVersion(String flowName, String user) {
		String versionDir = FilePersistence.FLOWPREFIX + ResourceType.Personal+File.separator +user+File.separator
		+flowName+FilePersistence.FOLDER_SUFFIX_VERSION;
		
	 
		return getMaxFlowVersionin(versionDir); 
	}

	private String getMaxFlowVersionin(String  versionDir) { 
		File historyFolder = new File(versionDir);
		int maxVersion=1;
		if(historyFolder.exists()==true){
			File[] files = historyFolder.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f= files[i];
				if(f.getName().endsWith(FilePersistence.INF)){
					String path = f.getAbsolutePath(); 
					String flowFileName =path.substring(0,path.length()-4)+FilePersistence.AFM;
					File flowFile = new File(flowFileName);
					if(flowFile.exists()==true){
						String version = f.getName().substring(f.getName().lastIndexOf("_")+1,f.getName().length()-4);
						if(version!=null&&version.trim().length()>0){
							int intV=Integer.parseInt(version);
							if(maxVersion<intV){
								maxVersion= intV;
							}
						}
					}
				}
			}
		}
		
		return String.valueOf(maxVersion);
	}

	@Override
	public String getLatestFlowVersion(FlowInfo flowInfo  ) {
		String versionDir=FilePersistence.INSTANCE.getVersionFolderPath(flowInfo);
		return getMaxFlowVersionin(versionDir); 
		
	 
	}

	@Override
	public boolean hasHistoryVersion(FlowInfo flowInfo ) {
		String versionDir=FilePersistence.INSTANCE.getVersionFolderPath(flowInfo);
		
		return hasFlowInFolder(versionDir); 
	 
	}

	@Override
	public FlowInfo relaodFlowInfo4Version(FlowInfo info, String version) throws Exception {
		
		Properties props = new Properties();

		  String path = ResourceManager.getInstance().getFlowVersionPath(info, Integer.parseInt(version));
		  String fileName = path+ FilePersistence.INF; 
		FileInputStream is = new FileInputStream(fileName);
		props.load(is);
		is.close();
		// initialize info fields with property values
		FlowInfo flowInfo = new FlowInfo(); 
		rmgr.getResourceFromProperties(flowInfo, props);
		rmgr.generateResourceKey(flowInfo);
		return flowInfo;
	}

	@Override
	public FlowInfo replaceWithVersion(FlowInfo flowInfo,Locale locale) throws  Exception  {

		// copy a history
		
		// reload the current with the version 
		String fromversion =flowInfo.getVersion();
		FlowInfo currentFlowInfo = relaodFlowInfo(flowInfo);
		String currnetVersion = currentFlowInfo.getVersion();
		if(fromversion.endsWith(currnetVersion)){
			throw new Exception(ErrorNLS.getMessage(ErrorNLS.Can_Not_Replace_Itself,locale));
		}
		copyFlowToHistory(currentFlowInfo);
		
		//copy the flow from history
		
		String oldPath = rmgr.getFlowVersionPath(flowInfo,
				Integer.parseInt(fromversion));
		String oldFlowFilePath = oldPath + FilePersistence.AFM;
		
		String newPath = rmgr.generateResourceKey(flowInfo);
		String  newFlowFilePath= newPath + FilePersistence.AFM;
	  
		FileUtils
				.copyFile(new File(oldFlowFilePath), new File(newFlowFilePath));
		
		//make sure return the new version 		
		int newVersion = Integer.parseInt(currnetVersion) + 1 ;
		currentFlowInfo.setVersion(String.valueOf(newVersion)) ;
		currentFlowInfo.setModifiedTime(System.currentTimeMillis()) ;
		currentFlowInfo.setComments("Replaced from version '" +fromversion+	"'") ;
		//use this method to update the cache...
		ResourceFlowManager.getInstance().saveFlowInfoData(currentFlowInfo) ;
		
		return currentFlowInfo;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.interfaces.FlowVersionManager#deleteFlowhistory(com.alpine.miner.impls.web.resource.FlowInfo)
	 */
	@Override
	public void deleteFlowhistory(FlowInfo flowInfo) throws IOException {
		String dir = rmgr.getVersionFolderPath(flowInfo) ;
		FileUtils.deleteDirectory(new File(dir)) ;
		
		
	}

}
