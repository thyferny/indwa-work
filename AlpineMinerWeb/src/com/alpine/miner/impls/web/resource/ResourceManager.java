/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ResourceManager.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */

package com.alpine.miner.impls.web.resource;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.alpine.miner.ifc.DBResourceManagerIfc;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.controller.ProtocolUtil;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;

/**
 * ResourceManager class manages the life cycle of users, groups, flows, DB
 * connections and CSV import configuration.
 * 
 * Users and groups are handled here. Note: they do not have full life cycle
 * yet. It will be added when administration support is added.
 * 
 * Flows, DB connections and CSV import are managed by ResourceFlowManager,
 * ResourceDBManager and ResourceCSVManager, respectively.
 * 
 * publishFlow() public API is implemented here. Since the data is passed in by
 * caller. It does not touch persistence. However, handlePublishFlowRequest() is
 * delegated to ResourceFlowManager.
 * 
 * Each of the resource managers is holding an interface of persistence. All
 * persistent work is done there. Currently the only implementation is
 * FilePersistence. If database persistence is needed that is the only class
 * need to be replaced.
 * 
 * @author sam_zang
 * 
 */
public class ResourceManager   {
	private static ResourceManager instance = new ResourceManager();

	/**
	 * @return a singleton instance of ResourceManager
	 */
	public static ResourceManager getInstance() {
		return instance;
	}

	/**
	 * private constructor to enforce singleton pattern.
	 */
	private ResourceManager() {
		// initialize private fields
		flowMgr = ResourceFlowManager.getInstance();
		dbMgr = WebDBResourceManager.getInstance();
		persistence = new FilePersistence();


		classIconTable = new HashMap<String, String[]>();
		for (String[] item : Resources.OPERATOR_CLASSNAME_ICONS) {
			classIconTable.put(item[0], item);
		}
	}

	private static final String PUBLISH_URI = "/AlpineMinerWeb/main/flow.do?method=publishFlow";

	private ResourceFlowManager flowMgr;
	private DBResourceManagerIfc dbMgr;
	private Persistence persistence;



	// This is a read only table. No lock is required.
	private HashMap<String, String[]> classIconTable;

	private boolean preferenceInited = false; 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getClassIcons(java.lang.String)
	 */
	public String[] getClassIcons(String className) {
		return classIconTable.get(className);
	}

 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getFlowList(java.lang.String)
	 */
	public List<FlowInfo> getFlowList(String category) {
		return flowMgr.getFlowList(category);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getFlowData(com.alpine.miner.impls.resource.FlowInfo)
	 */
	public OperatorWorkFlow getFlowData(FlowInfo flowInfo, Locale locale) throws OperationFailedException {
		return flowMgr.getFlowData(flowInfo,locale);
	}

	public OperatorWorkFlow getFlowData4Version(FlowInfo flowInfo, String version, Locale locale) throws OperationFailedException {
		return flowMgr.getFlowData4Version(flowInfo,version,locale);
	}
	
 
 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#updateFlow(com.alpine.miner.impls.resource.FlowInfo, com.alpine.miner.workflow.operator.OperatorWorkFlow)
	 */
	public void updateFlow(FlowInfo flowInfo, OperatorWorkFlow flow)
			throws Exception {
		flowMgr.updateFlow(flowInfo, flow);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#deleteFlow(com.alpine.miner.impls.resource.FlowInfo)
	 */
	public void updateFlowFinish(FlowInfo flowInfo, OperatorWorkFlow flow) throws Exception
			 {
		flowMgr.updateFlowFinish(flowInfo, flow);
	}
	
	/**
	 * @param flowInfo
	 */
	public void updateFlowCancel(FlowInfo flowInfo)  throws Exception
			 {
		flowMgr.updateFlowCancel(flowInfo);
	}

	/**
	 * @param flowInfo
	 * @throws Exception 
	 */
	public void deleteFlow(FlowInfo flowInfo) throws Exception {
		flowMgr.deleteFlow(flowInfo);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#publishFlow(java.lang.String, com.alpine.miner.impls.resource.ResourceInfo.ResourceType, java.lang.String, java.lang.String[], java.lang.String, java.lang.String)
	 */
	public void publishFlow(String host, ResourceType type, String owner,
			String[] categoryList, String flowName, String flowXMLString,String comments)
			throws HttpException, IOException, OperationFailedException {
		// TODO:make sure the eclipse version work
		if(comments!=null){
			comments=URLEncoder.encode(comments);
		}
		
		String uri = "http://" + host + PUBLISH_URI+"&comments="+comments;
		 
		FlowInfo info = new FlowInfo();
		info.setId(flowName);
		info.setCreateUser(owner);
		info.setCreateTime(System.currentTimeMillis());
		info.setModifiedUser(owner);
		info.setModifiedTime(System.currentTimeMillis());
		info.setResourceType(type);
	 
 
		info.setXmlString(flowXMLString);
		if (type.equals(ResourceType.Group)) {
			info.setGroupName(owner);
		}

		String data = ProtocolUtil.toJson(info);

		RequestEntity entity = new StringRequestEntity(data, "text/plain", null);
		PostMethod post = new PostMethod(uri);
		post.addRequestHeader("X-test-header", "test-header-value");
		post.addRequestHeader("Accept", "text/plain");
		post.addRequestHeader("Content-Type", "text/plain");
		post.setRequestEntity(entity);
	
		HttpClient httpclient = new HttpClient();

		int result=0;
		try {
			result = httpclient.executeMethod(post);
		}catch(Exception e1){
			e1.printStackTrace();
		} finally {
			post.releaseConnection();
		}

		if (result != 200) {
			throw new OperationFailedException("publish flow failed. Error: "
					+ result);
		}
	}


 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#saveFlowXMLData(com.alpine.miner.impls.resource.FlowInfo)
	 */
	public void saveFlowXMLData(FlowInfo flowInfo) throws  Exception {
		flowMgr.saveFlowXMLData(flowInfo);
	}
 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getDBConnectionList(java.lang.String)
	 */
	public List<DbConnectionInfo> getDBConnectionList(String userLogin) {
		return dbMgr.getDBConnectionList(userLogin);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getDBConnection(java.lang.String, java.lang.String, com.alpine.miner.impls.resource.ResourceInfo.ResourceType)
	/**
	 * @param userLogin
	 * @param type 
	 * @return
	 * @throws Exception 
	 */
	public DbConnectionInfo  getDBConnection(String userLogin,String connName, ResourceType type) throws Exception {
		return dbMgr.getDBConnection(  userLogin,  connName,   type);
	}
	

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#createDBConnection(com.alpine.miner.impls.resource.DbConnectionInfo)
	 */
	public void createDBConnection(DbConnectionInfo dbInfo)
			throws OperationFailedException {
		dbMgr.createDBConnection(dbInfo);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#updateDBConnection(com.alpine.miner.impls.resource.DbConnectionInfo)
	 */
	public void updateDBConnection(DbConnectionInfo dbInfo)
			throws DataOutOfSyncException {
		dbMgr.updateDBConnection(dbInfo);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#deleteDBConnection(com.alpine.miner.impls.resource.DbConnectionInfo)
	 */
	public void deleteDBConnection(DbConnectionInfo dbInfo) throws Exception {
		dbMgr.deleteDBConnection(dbInfo);
	}

 
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#generateResourceKey(com.alpine.miner.impls.resource.ResourceInfo)
	 */
	public String generateResourceKey(ResourceInfo info) {
		return persistence.generateResourceKey(info);
	}

    /* (non-Javadoc)
      * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#moveFlow(com.alpine.miner.impls.resource.FlowInfo, com.alpine.miner.impls.resource.FlowInfo)
      */
    //this is used for renameFlow
    public boolean moveFlow(FlowInfo src, FlowInfo dest) throws Exception {
        return flowMgr.moveFlow(src, dest);
    }

    /* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#copyFlow(com.alpine.miner.impls.resource.FlowInfo, com.alpine.miner.impls.resource.FlowInfo)
	 */
	//this is used for addFlow,duplicateFlow and shareFlow
	public boolean copyFlow(FlowInfo src, FlowInfo dest) throws Exception {
		return flowMgr.copyFlow(src, dest);
	}
 

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getDBConnectionsByPath(java.lang.String)
	 */
	public List<DbConnectionInfo> getDBConnectionsByPath(  String path) {
		return dbMgr.getDBConnectionListByPath(path);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#hasBeenUpdated(com.alpine.miner.impls.resource.ResourceInfo)
	 */
	public boolean hasBeenUpdated(ResourceInfo info) {
		return this.persistence.hasBeenUpdated(info);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getJDBCDriverInfos()
	 */
	public List<JDBCDriverInfo> getJDBCDriverInfos(){
		return this.dbMgr.getJDBCDriverInfos( );
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#createJDBCDriverInfos(com.alpine.miner.impls.resource.JDBCDriverInfo)
	 */
	public void createJDBCDriverInfos(JDBCDriverInfo info){
		  this.dbMgr.createJDBCDriverInfos(info);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#initJDBCDriverInfo()
	 */
	public void initJDBCDriverInfo() {
		 this.dbMgr.initJDBCDriverInfo( );
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#updatePreference(com.alpine.miner.impls.resource.PreferenceInfo)
	 */
	public void updatePreference(PreferenceInfo info) throws Exception {
		persistence.updatePreference(  info); 
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getPreferences()
	 */
	public Collection<PreferenceInfo> getPreferences() throws Exception {
		return persistence.getPreferences(   ); 
	}
	
	public String getPreferenceProp(String group,String key) throws Exception{
		Collection<PreferenceInfo> preferenceInfoList = null;
		if(group == null || key == null){
			throw new NullPointerException("arguments cannot be null.");
		}
		try {
			preferenceInfoList = getPreferences();
		} catch (Exception e) {
			throw e;
		}
		for(PreferenceInfo item : preferenceInfoList){
			if(group.equals(item.getId())){
				Properties props = item.getPreferenceItems();
				return props.getProperty(key);
			}
		}
		throw new NoSuchFieldException("cannot found result with group: " + group + " and key: " + key);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getPreferencesDefaultValue(java.lang.String)
	 */
	public PreferenceInfo getPreferencesDefaultValue( String type ) throws Exception {
		Collection<PreferenceInfo> defaultValus = persistence.getPreferencesDefaultValue(   ); 
		for (Iterator iterator = defaultValus.iterator(); iterator.hasNext();) {
			PreferenceInfo preferenceInfo = (PreferenceInfo) iterator.next();
			if(preferenceInfo.getId().equals(type)) {
				return preferenceInfo;
			}
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#getPreferencesDefaultValue(java.lang.String)
	 */
	public Collection<PreferenceInfo> getPreferencesDefaultValue(   )   {
		Collection<PreferenceInfo> defaultValus = persistence.getPreferencesDefaultValue(   ); 
		 
		return defaultValus;
	}

	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#updateProfileReader(com.alpine.miner.impls.resource.PreferenceInfo)
	 */
	public void updateProfileReader(PreferenceInfo info) {
		persistence.updateProfileReader(info) ;
		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#isPreferenceInited()
	 */
	public boolean isPreferenceInited() { 
		
		return preferenceInited;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.resource.ResoueceManagerIfc#SetPreferenceInited(boolean)
	 */
	public void SetPreferenceInited(boolean preferenceInited){
		this.preferenceInited=preferenceInited;
	}
	
	public void saveFlowResultInfo(FlowResultInfo resultInfo,String resultJSON) throws IOException{
		persistence.saveFlowResultInfo(  resultInfo,  resultJSON) ;
	}
	
	public FlowResultInfo getFlowResultInfo(String user,String flowName,String uuid){
		return  persistence.getFlowResultInfo( user,  flowName,  uuid) ;
	}
	public List<FlowResultInfo> getFlowResultInfos(String user ){
		return  persistence.getFlowResultInfos( user ) ;
	}
	
	public String getFlowResultJsonStrInfo(String user,String flowName,String uuid) throws Exception {
		return persistence.getFlowResultJsonStrInfo(  user,  flowName,  uuid) ;
	}

	 
	public boolean deleteFlowResultInfo(String user, String flowName,
			String uuid) {
		return persistence.deleteFlowResultInfo(  user,  flowName,  uuid) ;
	}
 

	/**
	 * @return
	 */
	public List<FlowInfo> getFlowHistorys(FlowInfo info) {
 
		return persistence.getFlowInfosFromFolder( persistence.getVersionFolderPath(info)) ;
	}

	/**
	 * @param info
	 * @return
	 */
	public String getVersionFolderPath(FlowInfo info) {
 
		return   persistence.getVersionFolderPath(info)  ;
	}

	/**
	 * @param oldFlowInfoPath
	 * @param info
	 */
	public void storeFlowInfo(String path, FlowInfo info) {
		    persistence.storeFlowInfo(path,info)  ;
		
	}

	/**
	 * @param info
	 * @param newVersion
	 * @return
	 */
	public String getFlowVersionPath(FlowInfo info, int version) {
		String path = getVersionFolderPath(info);
		path= path +File.separator+info.getId()+FilePersistence.FLOW_SUFFIX_VERSION + version;
		return path;
	}

	/**
	 * @param flowInfo
	 * @param props
	 */
	public void getResourceFromProperties(FlowInfo flowInfo, Properties props) {
		persistence.getResourceFromProperties(flowInfo,props);
		
	}

	/**
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public String readFile(String fileName) throws IOException {
		
		return ((FilePersistence)persistence).readFile(fileName);
	}

 
}
