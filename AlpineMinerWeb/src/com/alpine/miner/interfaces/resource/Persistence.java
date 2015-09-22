/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * Persistence.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 30, 2011
 */
 
package com.alpine.miner.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.FlowResultInfo;
import com.alpine.miner.impls.web.resource.ModelInfo;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
 

/**
 * @author sam_zang
 *
 */
public interface Persistence {
	public static final String ENCODING ="UTF-8";
	public static final Persistence INSTANCE = new FilePersistence();
	public UserInfo loadUserInfo(String user);
	public void storeUserInfo(UserInfo info);
	public void deleteUserInfo(UserInfo info) throws Exception;
	
	public List<DbConnectionInfo> loadDbConnectionInfo(ResourceType type, String user);
	public void storeDbConnectionInfo(DbConnectionInfo info);
	public void deleteDbConnectionInfo(DbConnectionInfo info) throws Exception;
	
	public List<FlowInfo> loadFlowInfo(String category);
	public void deleteFlowInfo(FlowInfo info) throws Exception;
	public List<String> getFlowCategories(String parent);
	OperatorWorkFlow readWorkFlow(FlowInfo info, Locale locale) throws OperationFailedException;
	
	public boolean hasBeenUpdated(ResourceInfo info);
	public void storeFlowInfoAndData(FlowInfo flowInfo) throws IOException;
    public void storeHistoricalFlowInfoAndData(FlowInfo info, String path)throws IOException;
	public String generateResourceKey(ResourceInfo info);

	public void updateUserInfo(UserInfo info);
	public void createUserInfo(UserInfo info);
	public void deleteGroupInfo(GroupInfo info) throws Exception;
	public void updateGroupInfo(GroupInfo info);
	public void createGroupInfo(GroupInfo info);
	public List<GroupInfo> getGroupInfoList();
	public List<UserInfo> getUserInfoList();
	public void loadXMLFlowData(FlowInfo src) throws IOException;
	
	public List<ModelInfo> getModelInfoList(String path, String flowName,
			String modelName,Locale locale) throws Exception ;
	public boolean deleteModel(String parentPath, ModelInfo modelInfo) throws Exception;
	public boolean createEngineModel(String parentPath,ModelInfo modelInfo, EngineModel engineModel) throws Exception;
	public EngineModel getEngineModel(String parentPath, ModelInfo modelInfo) throws Exception;
	public Long getLastModifiedTime(ResourceInfo info);
	public List<JDBCDriverInfo> getJDBCDriverInfos(   );
	public boolean createJDBCDriverInfo(JDBCDriverInfo info);
	public void updatePreference(PreferenceInfo info) throws Exception;
	public Collection<PreferenceInfo> getPreferences()  throws Exception;
	Collection<PreferenceInfo> getPreferencesDefaultValue()  ;
	void updateProfileReader(PreferenceInfo info);

//	public void updateFlowInfoCancel(FlowInfo flowInfo) throws Exception;
//	public void updateFlowInfoFinish(FlowInfo flowInfo) throws Exception;	
 	public void saveFlow(FlowInfo flowInfo, OperatorWorkFlow flow) throws Exception;
	
	// new API for ...
	public void saveFlowResultInfo(FlowResultInfo resultInfo, String resultJSON) throws IOException;
	public FlowResultInfo getFlowResultInfo(String user, String flowName, String uuid);
	public String getFlowResultJsonStrInfo(String user, String flowName, String uuid) throws Exception;
	public List<FlowResultInfo> getFlowResultInfos(String user);
	//return name of merged by category name adn flow name 
	public String getFlowFullName(FlowInfo flow);
	boolean deleteFlowResultInfo(String user, String flowName, String uuid);
	/**
	 * @param info
	 * @return
	 */
	public String getVersionFolderPath(FlowInfo info);
	/**
	 * @param folderName
	 * @return
	 */
	List<FlowInfo> getFlowInfosFromFolder(String folderName);
	/**
	 * @param path
	 * @param info
	 * @return
	 */
	public void storeFlowInfo(String path, FlowInfo info);
	/**
	 * @param flowInfo
	 * @param props
	 */
	public void getResourceFromProperties(ResourceInfo info, Properties props);
	 
	OperatorWorkFlow getFlowData4Path(String filePath, FlowInfo info, Locale locale) throws OperationFailedException;
	public Properties readProperties(File file);

	/**
	 * store the configuration of hadoop.
	 * @param config
	 */
	public void storeHadoopConnectionInfo(HadoopConnectionInfo config);
	
	public void deleteHadoopConnectionInfo(HadoopConnectionInfo config) throws Exception;
	
}
