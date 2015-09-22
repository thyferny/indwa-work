/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * FilePersistence.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 24, 2011
 */

package com.alpine.miner.impls.web.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import org.apache.log4j.Level;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.datasourcemgr.model.hadoop.HadoopConnectionInfo;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.OperationFailedException;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.interfaces.FlowVersionManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;
import com.alpine.util.AlpineUtil;
import com.alpine.utility.db.DbConnection;
import com.alpine.utility.file.FileUtility;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopConnection;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.AlpineMath;
import com.alpine.utility.tools.ProfileReader;
import org.apache.log4j.LogManager;

/**
 * @author sam_zang
 * 
 */
public class FilePersistence implements Persistence {
    private static Logger itsLogger = Logger.getLogger(FilePersistence.class);
    public static final String ALPINE_DATA_REPOSITORY = "ALPINE_DATA_REPOSITORY";
	private static final String LISTSEP = ",";
	public static final String SEP = ":";
	public static final String AFM = ".afm";
	public static final String FOLDER_SUFFIX_VERSION = "_version";
	public static final String FLOW_SUFFIX_VERSION = "_v_";
	
	//alpine model info
	public static final String SUFFIX_AM = ".am";
	public static final String SUFFIX_FLOW_RESULT = ".fr";
	public static final String INF = ".inf";
	private static final String JAR = ".jar";
	
	public static final String ROOT = initRoot();
	private static final String USRPREFIX = ROOT + "user" + File.separator;
	private static final String GROUPPREFIX = ROOT + "group" + File.separator;
	public static final String FLOWPREFIX = ROOT + "flow" + File.separator;
	public static final String FLOW_VERSION_PREFIX = ROOT + "flow_version" + File.separator;
    public static final String DBPREFIX = ROOT + "dbconn" + File.separator;
	public static final String HADOOP_PREFIX = ROOT + "hadoop_connection" + File.separator;
	public static final String JDBC_DRIVER_PREFIX = ROOT + "jdbc_driver" + File.separator;
	public static final String FLOW_RESULT_PREFIX = ROOT + "result" + File.separator;
	public static final String File_Preference = "AlpineMinerUI.prefs";
	public static final String Preference_PREFIX = ROOT + "preferene" + File.separator;
    public static final String Chorus_PREFIX = ROOT + "chorusPublished" + File.separator;
	//alpine model info
	public static final String MODELPRFIX =  ROOT + "model" + File.separator;
	//udf model info
	public static final String UDFPRFIX =  ROOT + "udf" + File.separator;
	 
	
	enum OpenMode {
		Writing, Reading, Create
	};

 

	/**
	 * @param info
	 * @throws Exception 
	 */
	public void deleteDbConnectionInfo(DbConnectionInfo info) throws Exception {
		String fileName = generateResourceKey(info) + INF;
		deleteResource(fileName);
	}
	
	public void deleteHadoopConnectionInfo(HadoopConnectionInfo config) throws Exception{
		String fileName = generateResourceKey(config) + HadoopConnection.file_suffix;
		deleteResource(fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#deleteFlowInfo(com.alpine
	 * .miner.impls.resource.FlowInfo)
	 */
	@Override
	public void deleteFlowInfo(FlowInfo info) throws Exception {
		String fileName = generateResourceKey(info);
		deleteResource(fileName + AFM);
		deleteResource(fileName + INF);
	}

	/**
	 * @param fileName
	 * @throws Exception 
	 */
	private void deleteResource(String fileName) throws Exception {
		File file = openFile(fileName, OpenMode.Writing);
		if (file.isFile() == true) {
			boolean result = file.delete();
			if(result ==false){
				throw new Exception("*Unable to delete :"+fileName);
			}
		}
	}
	
	/**
	 * @param jarFileName
	 * @throws Exception 
	 */
	public void deleteJDBCJar(String jarFileName) throws Exception{
		String fileFullName =JDBC_DRIVER_PREFIX+"Public"+File.separator+jarFileName; 
		deleteResource(fileFullName + JAR);
		deleteResource(fileFullName + INF);
	}


	/**
	 * @param info
	 * @throws Exception 
	 */
	public void deleteUserInfo(UserInfo info) throws Exception {
		String fileName = USRPREFIX + info.getLogin().trim() + INF;
		deleteResource(fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#getFlowCategories(java
	 * .lang.String)
	 */
	@Override
	public List<String> getFlowCategories(String parent) {
		String folderName = FLOWPREFIX
				+ parent.trim().replaceFirst(SEP, File.separator);
		List<String> list = new LinkedList<String>();

		File folder = openFile(folderName, OpenMode.Reading);
		if (folder.exists() == false) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isDirectory() == true) {
				list.add(f.getName());
			}
		}

		return list;
	}
   

	public List<ModelInfo> getModelInfoList(String user, String flowName,
			String modelName, Locale locale) throws Exception {
		String refinePath = ResourceType.Personal + File.separator + user;
//		String algName=ModelUtility.getModelAlgName(refinePath,   flowName,		  modelName,locale);
		// hardcode Personal to String of file persistence. in order to fix error on replace File.separator 
		String folderName = MODELPRFIX + refinePath + File.separator;

		File folder = openFile(folderName, OpenMode.Reading);
		List<ModelInfo> list = new ArrayList<ModelInfo>();
		if (folder.exists() == false ) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isDirectory() == false && f.getName().endsWith(INF)) {
				//only the model of same type can work...
				ModelInfo modelInfo = loadModelInfo(f);
				//an empty model and be replaced with any one...
//				if(algName==null){
					list.add(modelInfo);
//				}
//				else if(modelInfo.getAlgorithmName()!=null&&modelInfo.getAlgorithmName().equals(algName)){
//					list.add(modelInfo);
//				}
			}
		}
		
		Comparator< ModelInfo> comparator = new Comparator < ModelInfo> (){

			@Override
			public int compare(ModelInfo o1, ModelInfo o2) {
				return o2.getModifiedTime().compareTo(o1.getModifiedTime()); 
			}
			
		}; 
		Collections.sort(list, comparator) ;
		return list;

	}


	private ModelInfo loadModelInfo(File f) {
		ModelInfo modelInfo = new ModelInfo();
		Properties props = new Properties();
		InputStreamReader is = null;
		try {
			  is = new InputStreamReader(new FileInputStream(f),Persistence.ENCODING);
			props.load(is);
			
		} catch ( Exception e) {
			e.printStackTrace();
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}  
		getResourceFromProperties(modelInfo, props);
		modelInfo.setAlgorithmName(props.getProperty("algorithmName"));
		modelInfo.setModelName(props.getProperty("modelName"));
		modelInfo.setFlowName(props.getProperty("flowName"));
		return modelInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#getFlowData(com.alpine
	 * .miner.impls.resource.FlowInfo)
	 */
	@Override
	public OperatorWorkFlow readWorkFlow(FlowInfo info,Locale locale) 
		throws OperationFailedException {
		OperatorWorkFlow flow = null;
		XMLWorkFlowReader reader = new XMLWorkFlowReader();
		XMLFileReaderParameters params = null;
		 
		try {
//			String fileName = info.getTmpPath();
//			if (fileName != null && fileName.length() > 0) {
//				File f = new File(fileName);
//				if (f.exists() == true) {
//					params = new XMLFileReaderParameters(fileName,
//							info.getModifiedUser(), info.getResourceType());
//
//					flow = reader.doRead(params,locale);
//					return flow;
//				}				
//			}

			// first time.
			String fileName = generateResourceKey(info) + AFM;
			File folder = openFile(fileName, OpenMode.Reading);
			if (folder.exists() == false) {
				return null;
			}
			String tempDir = System.getProperty("java.io.tmpdir");
			if (info.getResourceType() == ResourceType.Personal) {
				
				String tmpPath = tempDir + File.separator
						+ UUID.randomUUID().toString() + info.getId() + AFM;
				// System.out.println("Copy flow to: " + tmpPath);
				WebWorkFlowRunner.copyFile(fileName, info.getResourceType(), info.getModifiedUser(), tmpPath, locale,false);
//				copyFile(fileName, tmpPath);
		 
				storeFlowInfo(generateResourceKey(info) + INF, info);
			}
			params = new XMLFileReaderParameters(fileName, info.getModifiedUser(), info.getResourceType());
			flow = reader.doRead(params,locale);
		} catch ( Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			e.printStackTrace();
			throw new OperationFailedException(ErrorNLS.getMessage(ErrorNLS.CAN_NOT_OPEN_FILE, locale));
		}  

		return flow;
	}

	/**
	 * @param info
	 * @param props
	 */
	public void getResourceFromProperties(ResourceInfo info, Properties props) {
		info.setId(props.getProperty(ResourceInfo.ID));
		info.setCreateUser(props.getProperty(ResourceInfo.CREATE_USER));
		info.setModifiedUser(props.getProperty(ResourceInfo.MODIFIED_USER));
		info.setGroupName(props.getProperty(ResourceInfo.GROUP_NAME));

		String createTime = props.getProperty(ResourceInfo.CREATE_TIME);
		String modifiedTime = props.getProperty(ResourceInfo.MODIFIED_TIME);
		String type = props.getProperty(ResourceInfo.TYPE);
		String comments = props.getProperty(ResourceInfo.COMMENTS);
		info.setComments(comments) ;
		String version = props.getProperty(ResourceInfo.VERSION);
		if (version != null && version.trim().length() > 0) {
			info.setVersion(version);
		}
		if(createTime!=null){
			info.setCreateTime(Long.valueOf(createTime));
		}
		if(modifiedTime!=null){
			info.setModifiedTime(Long.valueOf(modifiedTime));
		}
		info.setResourceType(ResourceType.valueOf(type));

		String categories = props.getProperty(ResourceInfo.CATEGORIES);
		if (categories != null && categories.length() > 0) {
			String[] list = categories.split(LISTSEP);
			info.setCategories(list);
		}
	}
 

	/**
	 * @param user
	 * @return
	 */
	public List<DbConnectionInfo> loadDbConnectionInfo(ResourceType type,
			String user) {
		String folderName = DBPREFIX + type.name();
		if (!type.equals(ResourceType.Public)) {
			folderName = folderName + File.separator + user.trim();
		}

		List<DbConnectionInfo> list = new LinkedList<DbConnectionInfo>();

		File folder = openFile(folderName, OpenMode.Reading);
		if (folder.exists() == false) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isFile() == true&&f.getName().endsWith(FilePersistence.INF)) { 

				Properties props = readProperties(f);
				if (props == null) {
					continue;
				}

				// initialize info fields with property values
				DbConnectionInfo info = new DbConnectionInfo();
				getResourceFromProperties(info, props);
				generateResourceKey(info);

				DbConnection dbc = new DbConnection(props);
				info.setConnection(dbc);
				list.add(info);
			}
		}

		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#loadFlowInfo(com.alpine
	 * .miner.impls.resource.ResourceInfo.ResourceType, java.lang.String)
	 */
	@Override
	public List<FlowInfo> loadFlowInfo(String category) {
		String folderName = FLOWPREFIX
				+ category.trim().replaceFirst(SEP, File.separator);
		return getFlowInfosFromFolder(folderName);
	}
	@Override
	public List<FlowInfo> getFlowInfosFromFolder(String folderName) {
		List<FlowInfo> list = new LinkedList<FlowInfo>();

		File folder = openFile(folderName, OpenMode.Reading);
		if (folder.exists() == false) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isFile() == true && f.getName().endsWith(INF)) {

				Properties props = readProperties(f);
				if (props == null) {
					continue;
				}

				// initialize info fields with property values
				FlowInfo info = new FlowInfo();
				getResourceFromProperties(info, props);
				generateResourceKey(info);
				list.add(info);

			}
		}
//    MINERWEB-374 		flow should be sorted in "Add flow"
		Comparator< FlowInfo> comparator = new Comparator< FlowInfo>(){

			@Override
			public int compare(FlowInfo o1, FlowInfo o2) {
				return o1.getId().compareTo(o2.getId());
	
			}
			
		}; 
		Collections.sort(list, comparator) ;
		return list;
	}

	/**
	 * @param user
	 * @return
	 */
	public UserInfo loadUserInfo(String user) {
		if (user == null || user.length() == 0) {
			return null;
		}
		String fileName = USRPREFIX + user.trim() + INF;
		Properties props = readProperties(fileName);
		return loadUserInfo(props);
	}

	private UserInfo loadUserInfo(Properties props) {
		if (props == null) {
			return null;
		}
		UserInfo info = new UserInfo();

		// initialize info fields with property values
		info.setLogin(props.getProperty(UserInfo.LOGIN));
		info.setEmail(props.getProperty(UserInfo.EMAIL));
		String passWord = (String)props.getProperty(UserInfo.PASSWORD); 
		passWord = (String)StringUtil.stringToObject(passWord) ;
		info.setPassword(passWord);
		info.setDescription(props.getProperty(UserInfo.DESC));
		info.setFirstName(props.getProperty(UserInfo.FIRSTNAME));
		info.setLastName(props.getProperty(UserInfo.LASTNAME));
		Boolean notify = Boolean.parseBoolean(props.getProperty(UserInfo.NOTIFICATION));
		info.setNotification(notify);
		String groups = props.getProperty(UserInfo.GROUPS);
		if (groups != null && groups.length() > 0) {
			String[] groupList = groups.split(LISTSEP);
			info.setGroups(groupList);
		}
		String roleSet = props.getProperty(UserInfo.ROLE_SET);
		if(roleSet != null && roleSet.length() > 0){
			info.setRoleSet(roleSet.split(LISTSEP));
		}
		
		String chorusKey = props.getProperty(UserInfo.CHORUS_KEY);
		info.setChorusKey(chorusKey);
		return info;
	}

	/**
	 * @param file
	 * @return
	 */
	public Properties readProperties(File file) {

		Properties props = new Properties();
		InputStreamReader  iStream = null; 
 
			
			try {
				iStream = new InputStreamReader(new FileInputStream(file),Persistence.ENCODING);
			props.load(iStream);
			return props;
		} catch (IOException e) {
			itsLogger.error(e.getMessage(),e);
		} finally {
			if (iStream != null) {
				try {
					iStream.close();
				} catch (IOException e) {
				}
				// ignore close error, we are done here.
			}
		}

		return null;
	}

	/**
	 * @param fileName
	 * @return
	 */
	private Properties readProperties(String fileName) {
		File file = openFile(fileName, OpenMode.Reading);
		if (file.exists()) {
			return readProperties(file);
		}
		return null;
	}

	/**
	 * @param file
	 * @param props
	 */
	private void saveProperties(File file, Properties props) {
		OutputStreamWriter oStream = null; 
		try {
			oStream = new OutputStreamWriter(new FileOutputStream(file),Persistence.ENCODING);
	
			props.store(oStream, "");
			oStream.close();

		} catch (IOException e) {
			itsLogger.error(e.getMessage(),e);
		} finally {
			if (oStream != null) {
				try {
					oStream.close();
				} catch (IOException e) {
				}
				// ignore close error.
			}
		}
	}

	/**
	 * @param fileName
	 * @param props
	 */
	private void saveProperties(String fileName, Properties props) {
		File file = openFile(fileName, OpenMode.Writing);

		saveProperties(file, props);
	}

	/**
	 * @param conn
	 * @param props
	 */
	private void setDBProperties(DbConnection conn, Properties props) {
		String passwordText = conn.getPassword();
		if (passwordText == null) {
			passwordText = "";
		}
		String password = AlpineUtil.objectToString(passwordText);
		String engineCombo = conn.getDbType();

		props.put(DbConnection.PASSWORD_TEXT, password);
		props.put(DbConnection.USER_NAME_TEXT, conn.getDbuser());
		props.put(DbConnection.DB_NAME_TEXT, conn.getDbname());
		props.put(DbConnection.PORT_TEXT, "" + conn.getPort());
		props.put(DbConnection.HOST_TEXT, conn.getHostname());
		props.put(DbConnection.ENGINE_COMBO, engineCombo);
		props.put(DbConnection.CONN_NAME_TEXT, conn.getConnName());
		props.put(DbConnection.JDBC_DRIVER_COMBO, conn.getJdbcDriverFileName() == null ? "" : conn.getJdbcDriverFileName());
		props.put(DbConnection.USE_SSL, conn.getUseSSL() == null ? "" : conn.getUseSSL());
	}

	/**
	 * @param info
	 * @param props
	 */
	private void setPropertiesFromResource(ResourceInfo info, Properties props) {
		props.put(ResourceInfo.ID, info.getId());
		props.put(ResourceInfo.CREATE_USER, info.getCreateUser());
		props.put(ResourceInfo.CREATE_TIME, "" + info.getCreateTime());
		props.put(ResourceInfo.MODIFIED_USER, info.getModifiedUser());
		props.put(ResourceInfo.MODIFIED_TIME, "" + info.getModifiedTime());
		if(info.getGroupName()!=null){
			props.put(ResourceInfo.GROUP_NAME, info.getGroupName());
		} 
		props.put(ResourceInfo.TYPE, info.getResourceType().toString());
		if(info.getVersion()!=null){
			props.put(ResourceInfo.VERSION, info.getVersion());
		}
		if(info.getComments()!=null){
			props.put(ResourceInfo.COMMENTS, info.getComments());
		}
		String categoryList = "";
		if (info.getCategories() != null) {
			for (String category : info.getCategories()) {
				categoryList += category + LISTSEP;
			}
		}
		props.setProperty(ResourceInfo.CATEGORIES, categoryList);
	}
 

	/**
	 * @param info
	 */
	public void storeDbConnectionInfo(DbConnectionInfo info) {
 
		String fileName = generateResourceKey(info) + INF;
 
		DbConnection conn = info.getConnection();
		Properties props = new Properties();

		setDBProperties(conn, props);
		setPropertiesFromResource(info, props);
		saveProperties(fileName, props);
	}
	
	public void storeHadoopConnectionInfo(HadoopConnectionInfo config){
		String fileName = generateResourceKey(config) + HadoopConnection.file_suffix;
		Properties props = new Properties();
		setHadoopProperties(config.getConnection(), props);
		setPropertiesFromResource(config, props);
		saveProperties(fileName, props);
	}
	
	private void setHadoopProperties(HadoopConnection connInfo, Properties props){
		props.setProperty(HadoopConnection.KEY_CONNNAME, connInfo.getConnName());
		props.setProperty(HadoopConnection.KEY_HDFS_HOSTNAME, connInfo.getHdfsHostName());
		props.setProperty(HadoopConnection.KEY_HDFS_PORT, Integer.toString(connInfo.getHdfsPort()));
		props.setProperty(HadoopConnection.KEY_JOB_HOSTNAME, String.valueOf(connInfo.getJobHostName()));
		props.setProperty(HadoopConnection.KEY_JOB_PORT, String.valueOf(connInfo.getJobPort()));
		props.setProperty(HadoopConnection.KEY_USERNAME, connInfo.getUserName());
		props.setProperty(HadoopConnection.KEY_VERSION, connInfo.getVersion());
		
		props.setProperty(HadoopConnection.KEY_SECURITY_MODE, connInfo.getSecurityMode());
		if(connInfo.getHdfsPrincipal()!=null){
			props.setProperty(HadoopConnection.KEY_HDFS_PRINCIPAL, connInfo.getHdfsPrincipal());
		}
		if(connInfo.getHdfsKeyTab()!=null){
			props.setProperty(HadoopConnection.KEY_HDFS_KEYTAB, connInfo.getHdfsKeyTab());
		}
		if(connInfo.getMapredPrincipal()!=null){
			props.setProperty(HadoopConnection.KEY_MAPRED_PRINCIPAL, connInfo.getMapredPrincipal());

		}
		if(connInfo.getMapredKeyTab()!=null){
			props.setProperty(HadoopConnection.KEY_MAPRED_KEYTAB, connInfo.getMapredKeyTab());
		}
		
		
		props.setProperty("Hadoop_" + HadoopConnection.KEY_GROUPNAME, connInfo.getGroupName());
	}

	/**
	 * @param info
	 */
	public void storeUserInfo(UserInfo info) {
		String fileName = USRPREFIX + info.getLogin().trim() + INF;
		Properties props = readProperties(fileName);
		if(props == null){
			props = new Properties();
		}
		props.setProperty(UserInfo.LOGIN, info.getLogin());
		//MINERWEB-256	 	Error when reset User password
		if(info.getEmail()!=null){
			props.setProperty(UserInfo.EMAIL, info.getEmail());	
		}
		String passWord = info.getPassword();
		passWord = StringUtil.objectToString(passWord) ;
		props.setProperty(UserInfo.PASSWORD, passWord);
		
		props.setProperty(UserInfo.DESC, info.getDescription());
		props.setProperty(UserInfo.FIRSTNAME, info.getFirstName());
		props.setProperty(UserInfo.LASTNAME, info.getLastName());
		Boolean notify = info.getNotification();
		props.setProperty(UserInfo.NOTIFICATION, notify.toString());
		if (info.getGroups() != null) {
			String groupList = "";
			for (String group : info.getGroups()) {
				groupList += group + LISTSEP;
			}
			props.setProperty(UserInfo.GROUPS, groupList);
		}
		if(info.getRoleSet() != null){
			String roleSet = "";
			for (String role : info.getRoleSet()) {
                roleSet += role + LISTSEP;
            }
			props.setProperty(UserInfo.ROLE_SET, roleSet);
		}
		
		if(info.getChorusKey() != null){
			props.setProperty(UserInfo.CHORUS_KEY, info.getChorusKey());
		}
		saveProperties(fileName, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#flowHasBeenUpdated(com
	 * .alpine.miner.impls.resource.FlowInfo)
	 */
	@Override
	public boolean hasBeenUpdated(ResourceInfo info) {
		Long timestamp = getLastModifiedTime(info);
		return timestamp > info.getModifiedTime();
	}

	public Long getLastModifiedTime(ResourceInfo info) {
		
		String fileName = generateResourceKey(info) + INF;
		Properties props = readProperties(fileName);
		if (props == null) {
			return (long) 0;
		}

		ResourceInfo local = new FlowInfo();
		getResourceFromProperties(local, props);
		return local.getModifiedTime();
	}

	public String generateResourceKey(ResourceInfo info) {
		String prefix;
		String key;
		if (info instanceof FlowInfo) {
			prefix = FLOWPREFIX;
		} else if (info instanceof DbConnectionInfo) {
			prefix = DBPREFIX;
		}  
		else if(info instanceof HadoopConnectionInfo){
			prefix = HADOOP_PREFIX;
		}
		else {
			return null;
		}
		ResourceType type = info.getResourceType();
		key = prefix + type.name() + File.separator;
		String[] categoryList = info.getCategories();
		if (type.equals(ResourceType.Group)) {
			String name = info.getGroupName();
			if (name == null || name.length() == 0) {
				name = info.getModifiedUser();
			}
			key += name + File.separator;
		}
		else if (type.equals(ResourceType.Personal) && (categoryList == null || categoryList.length == 0)) {
			key += info.getModifiedUser() + File.separator;//category already include user folder
		}
		if (categoryList != null && categoryList.length > 0) {
			for (String category : categoryList) {
				if (category.length() > 0) {
					key += category + File.separator;
				}
			}
		}
		key += info.getId();
		info.setKey(key);

		return key;
	}

	private File openFile(String fileName, OpenMode mode) {

		int idx = fileName.lastIndexOf(File.separatorChar);
		if (idx > 0) {
			String dirs = fileName.substring(0, idx);

			File folder = new File(dirs);
			if (folder.exists() == false) {
				folder.mkdirs();
			}
		}

		File f = new File(fileName);
		if (f.exists() == true) {
			return f;
		}
		if (mode == OpenMode.Create) {
			try {
				f.createNewFile();
			} catch (IOException e) {
			}
		}

		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#storeFlowInfo(com.alpine
	 * .miner.impls.resource.FlowInfo)
	 */
	@Override
	public void storeFlowInfoAndData(FlowInfo info) throws IOException {

		String fileName;

		// saving the info part.
		fileName = generateResourceKey(info) + INF;
		storeFlowInfo(fileName, info);

		// saving data part.
		fileName = generateResourceKey(info) + AFM;
		
		writeFile(fileName, info.getXmlString());
 
		
	//	XMLWorkFlowReader
		
	//	XMLWorkFlowSaver
		
	}

    /*
      * (non-Javadoc)
      *
      * @see
      * com.alpine.miner.interfaces.resource.Persistence#storeHistoricalFlowInfoAndData(com.alpine
      * .miner.impls.resource.FlowInfo, java.lang.String)
      */
    @Override
    public void storeHistoricalFlowInfoAndData(FlowInfo info, String path)throws IOException {
         System.out.println("saving to: " + path);
        String fileName;

        // saving the info part.
        fileName = path + INF;
        Properties props = new Properties();
        setPropertiesFromResource(info, props);

        saveProperties(fileName, props);

        // saving data part.
        fileName = path + AFM;
        writeFile(fileName, info.getXmlString());
    }

    public void storeFlowInfo(String fileName, FlowInfo info) {
		fileName = generateResourceKey(info) + INF;
		Properties props = new Properties();
		setPropertiesFromResource(info, props);
	 
		saveProperties(fileName, props);
	}
	

	public static String initRoot() {
		String root = System.getenv(ALPINE_DATA_REPOSITORY);
		if (root != null) {
			root += File.separator;
			return root;
		}

		root = System.getProperty("user.home");
		if (root != null) {
			root += File.separator + ALPINE_DATA_REPOSITORY + File.separator;
		} else {
			root = ALPINE_DATA_REPOSITORY + File.separator;
		}


		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#createGroupInfo(com.
	 * alpine.miner.impls.resource.GroupInfo)
	 */
	@Override
	public void createGroupInfo(GroupInfo info) {
		storeGroupInfo(info);
	}

	/**
	 * @param info
	 */
	private void storeGroupInfo(GroupInfo info) {
		String fileName = GROUPPREFIX + info.getId().trim() + INF;

		Properties props = new Properties();
		props.setProperty(GroupInfo.ID, info.getId());
		props.setProperty(GroupInfo.DESC, info.getDescription());
		saveProperties(fileName, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#createUserInfo(com.alpine
	 * .miner.impls.resource.UserInfo)
	 */
	@Override
	public void createUserInfo(UserInfo info) {
		storeUserInfo(info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#deleteGroupInfo(com.
	 * alpine.miner.impls.resource.GroupInfo)
	 */
	@Override
	public void deleteGroupInfo(GroupInfo info) throws Exception {
		String fileName = GROUPPREFIX + info.getId().trim() + INF;
		deleteResource(fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.miner.interfaces.resource.Persistence#getGroupInfoList()
	 */
	@Override
	public List<GroupInfo> getGroupInfoList() {
		String folderName = GROUPPREFIX;
		List<GroupInfo> list = new LinkedList<GroupInfo>();

		File folder = openFile(folderName, OpenMode.Reading);
		if (folder.exists() == false) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isFile() == true && f.getName().endsWith(INF)) {
				Properties props = readProperties(f);
				if (props == null) {
					continue;
				}

				list.add(loadGroupInfo(props));
			}
		}

		return list;
	}

	/**
	 * @param props
	 * @return
	 */
	private GroupInfo loadGroupInfo(Properties props) {
		if (props == null) {
			return null;
		}
		GroupInfo info = new GroupInfo();

		// initialize info fields with property values
		info.setId(props.getProperty(GroupInfo.ID));
		info.setDescription(props.getProperty(GroupInfo.DESC));

		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alpine.miner.interfaces.resource.Persistence#getUserInfoList()
	 */
	@Override
	public List<UserInfo> getUserInfoList() {
		String folderName = USRPREFIX;
		List<UserInfo> list = new LinkedList<UserInfo>();

		File folder = openFile(folderName, OpenMode.Reading);
		if (folder.exists() == false) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			if (f.isFile() == true && f.getName().endsWith(INF)) {
				Properties props = readProperties(f);
				if (props == null) {
					continue;
				}

				list.add(loadUserInfo(props));
			}
		}

		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#updateGroupInfo(com.
	 * alpine.miner.impls.resource.GroupInfo)
	 */
	@Override
	public void updateGroupInfo(GroupInfo info) {
		storeGroupInfo(info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#updateUserInfo(com.alpine
	 * .miner.impls.resource.UserInfo)
	 */
	@Override
	public void updateUserInfo(UserInfo info) {
		storeUserInfo(info);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.interfaces.resource.Persistence#loadXMLFlowData(com.alpine.miner.impls.resource.FlowInfo)
	 */
	@Override
	public void loadXMLFlowData(FlowInfo info) throws IOException {
		String fileName = generateResourceKey(info) + AFM;
		if(false==StringUtil.isEmpty(info.getVersion())&&FlowVersionManager.INSTANCE.hasHistoryVersion(info)){
			int flowVersion =Integer.valueOf(info.getVersion()) ;
			//version
			String maxHisotryVersion = FlowVersionManager.INSTANCE.getLatestFlowVersion(info);
			if(StringUtil.isEmpty(maxHisotryVersion)==false
					&&flowVersion<=Integer.valueOf(maxHisotryVersion)){
				  String path = ResourceManager.getInstance().getFlowVersionPath(info, flowVersion);
				    fileName = path+ Resources.AFM;
 
			}
		}
	 	
		
		info.setXmlString(readFile(fileName));
	}
	
	public String readFile(String fileName) throws IOException {
		StringBuilder data = new StringBuilder();
		String NL = System.getProperty("line.separator");
		 
		Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(fileName),Persistence.ENCODING));
		try {
			while (scanner.hasNextLine()) {
				data.append(scanner.nextLine() + NL);
			}
		} finally {
			scanner.close();
		}

	 
		 
		return data.toString();
	}
	
	private void writeFile(String fileName, String data) throws IOException {
		File file = openFile(fileName, OpenMode.Create);
		assert (file.exists() == true);

		itsLogger.info("save data to: " + file.getAbsolutePath());
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(fileName),Persistence.ENCODING);

			out.write(data);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void copyFile(String src, String dest) throws IOException {
		writeFile(dest, readFile(src));
	}
	
	//always create the private one, can be shared to public...
	public boolean createEngineModel(String parentPath, ModelInfo modelInfo,
			EngineModel engineModel) throws Exception {
		String modelFileName = getModelFileName(ResourceType.Personal + File.separator + parentPath, modelInfo);
		String infoFileName = getModelINFFileName(ResourceType.Personal + File.separator + parentPath, modelInfo);
		
		Properties props = new Properties();
		setPropertiesFromResource(modelInfo, props);
		props.setProperty(ModelInfo.ALGORITHM_NAME,
				modelInfo.getAlgorithmName());
		props.setProperty(ModelInfo.MODEL_NAME, modelInfo.getModelName());
		props.setProperty(ModelInfo.FLOW_NAME, modelInfo.getFlowName());

		saveProperties(infoFileName, props);

		File modelFile = new File(modelFileName);

		modelFile.createNewFile();
		FileOutputStream outStream = new FileOutputStream(modelFile);
		String modelStr = AlpineUtil.objectToString(engineModel);
		byte[] bytes = modelStr.getBytes();
		outStream.write(bytes);
		outStream.close();

		return true;
	}

	private String getModelFileName(String parentPath, ModelInfo modelInfo) {
		String folderName = MODELPRFIX + parentPath.trim();

		String modelFileName = folderName + File.separator
		+ modelInfo.getFlowName() + "_"
		+ modelInfo.getModelName()+ "_" + modelInfo.getId()+  SUFFIX_AM;
		return modelFileName;
	}
	
	private String getModelINFFileName(String parentPath, ModelInfo modelInfo) {
		String folderName = MODELPRFIX + parentPath.trim()	;

		String modelFileName = folderName + File.separator
		+ modelInfo.getFlowName() + "_"
		+ modelInfo.getModelName()+ "_" + modelInfo.getId()+  INF;
		return modelFileName;
	}

	public boolean deleteModel(String parentPath, ModelInfo modelInfo) throws Exception {
		String modelFileName = getModelFileName(ResourceType.Personal + File.separator + parentPath, modelInfo);
		String infoFileName = getModelINFFileName(ResourceType.Personal + File.separator + parentPath, modelInfo);
		deleteResource(modelFileName);
		deleteResource(infoFileName);

		return true;
	}

	public EngineModel getEngineModel(String parentPath, ModelInfo modelInfo) throws Exception
			 {
		String modelFileName = getModelFileName(
				ResourceType.Personal + File.separator + parentPath, modelInfo);
		File file = new File(modelFileName);
		if (file.exists()) {

			
			  StringBuffer result = FileUtility.readFiletoString(file);

 
			EngineModel model = (EngineModel) AlpineUtil.stringToObject(result.toString() );
			return model;

		}else{
			return null;
		}
	}
	
	public List<JDBCDriverInfo> getJDBCDriverInfos(   ){
		String folderName = JDBC_DRIVER_PREFIX + "Public";

		List<JDBCDriverInfo> list = new LinkedList<JDBCDriverInfo>();

		File folder = openFile(folderName, OpenMode.Reading);
		if (folder.exists() == false) {
			return list;
		}

		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			//only need the inf file
			if (f.isFile() == true&&f.getName().endsWith(INF)) {

				Properties props = readProperties(f);
				if (props == null) {
					continue;
				}

				// initialize info fields with property values
				JDBCDriverInfo info = new JDBCDriverInfo();
				getResourceFromProperties(info, props); 
				info.setDriverName(props.getProperty(JDBCDriverInfo.Driver_NAME)) ;
				generateResourceKey(info);
 
				list.add(info);
			}
		}

		return list;
	}
	public boolean createJDBCDriverInfo(JDBCDriverInfo info){
		String folderName = JDBC_DRIVER_PREFIX + "Public";
		String driverName = info.getDriverName().substring(0,info.getDriverName().lastIndexOf(".")); 
		String infoFileName = folderName+File.separator+driverName+INF;
		
		Properties props = new Properties();
		setPropertiesFromResource(info, props);
		props.setProperty(JDBCDriverInfo.Driver_NAME,
				info.getDriverName());

		saveProperties(infoFileName, props);

 
		return true;
	}

	@Override
	public void updatePreference(PreferenceInfo info) throws Exception {
		String fileName = Preference_PREFIX + File_Preference;
		Properties props= new Properties(); 
		FileInputStream is = new FileInputStream(fileName);
		props.load(is) ;
		is.close();
		String id = info.getId();
		Properties items = info.getPreferenceItems();
	 
		if(PreferenceInfo.GROUP_ALG.equals(id)){
			props.put(PreferenceInfo.GROUP_ALG + "." + PreferenceInfo.KEY_DISTINCT_VALUE_COUNT, items.getProperty(PreferenceInfo.KEY_DISTINCT_VALUE_COUNT));
			props.put(PreferenceInfo.GROUP_ALG + "." + PreferenceInfo.KEY_VA_DISTINCT_VALUE_COUNT, items.getProperty(PreferenceInfo.KEY_VA_DISTINCT_VALUE_COUNT));
			props.put(PreferenceInfo.GROUP_ALG + "." + PreferenceInfo.KEY_DECIMAL_PRECISION, items.getProperty(PreferenceInfo.KEY_DECIMAL_PRECISION));
		}else if(PreferenceInfo.GROUP_DB.equals(id)){
			props.put(PreferenceInfo.GROUP_DB + "." + PreferenceInfo.KEY_CONNECTION_TIMEOUT, items.getProperty(PreferenceInfo.KEY_CONNECTION_TIMEOUT));
			props.put(PreferenceInfo.GROUP_UI + "." + PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX, items.getProperty(PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX));
			props.put(PreferenceInfo.GROUP_ALG + "." + PreferenceInfo.KEY_LOCAL_HD_RUNNER_THRESHOLD, items.getProperty(PreferenceInfo.KEY_LOCAL_HD_RUNNER_THRESHOLD));
		}else if(PreferenceInfo.GROUP_SYS.equals(id)){
			props.put(PreferenceInfo.GROUP_SYS + "." + PreferenceInfo.KEY_DEBUG_LEVEL, items.getProperty(PreferenceInfo.KEY_DEBUG_LEVEL));
		}else if(PreferenceInfo.GROUP_UI.equals(id)){
			props.put(PreferenceInfo.GROUP_UI + "." + PreferenceInfo.KEY_MAX_TABLE_LINES, items.getProperty(PreferenceInfo.KEY_MAX_TABLE_LINES));
			props.put(PreferenceInfo.GROUP_UI + "." + PreferenceInfo.KEY_MAX_SCATTER_POINTS, items.getProperty(PreferenceInfo.KEY_MAX_SCATTER_POINTS));
			props.put(PreferenceInfo.GROUP_UI + "." + PreferenceInfo.KEY_MAX_TIMESERIES_POINTS, items.getProperty(PreferenceInfo.KEY_MAX_TIMESERIES_POINTS));
			props.put(PreferenceInfo.GROUP_UI + "." + PreferenceInfo.MAX_CLUSTER_POINTS, items.getProperty(PreferenceInfo.MAX_CLUSTER_POINTS));
		} else if (PreferenceInfo.GROUP_LOG.equals(id))
        {
            props.put(PreferenceInfo.GROUP_LOG + "." + PreferenceInfo.KEY_LOG_CUST_ID, items.getProperty(PreferenceInfo.KEY_LOG_CUST_ID));
            props.put(PreferenceInfo.GROUP_LOG + "." + PreferenceInfo.KEY_LOG_OPT_OUT, items.getProperty(PreferenceInfo.KEY_LOG_OPT_OUT));
        }
		props.store(new FileOutputStream(fileName), "") ; 
	}

	@Override
	public Collection<PreferenceInfo> getPreferences() throws Exception {
		String fileName = Preference_PREFIX + File_Preference;
		File configFile= new File(fileName);
		if(configFile.exists()==false){
			Properties props = new Properties ();
			Collection<PreferenceInfo> defaultValues = getPreferencesDefaultValue();
			for (Iterator iterator = defaultValues.iterator(); iterator.hasNext();) {
				PreferenceInfo  info = (PreferenceInfo) iterator.next();
				String id=info.getId() ;
				Properties items = info.getPreferenceItems();
				for (Iterator it = items.keySet().iterator(); it.hasNext();) {
					String  key = (String) it.next();
					String value = items.getProperty(key) ;
					props.setProperty(id+"." +key, value);
				}
			}
			File path= new File( ROOT + "preferene");
			if(path.exists()==false){
				path.mkdir();
			}
			configFile.createNewFile();
			props.store(new FileOutputStream(fileName), "") ; 
			return defaultValues;
		}
		Properties props= new Properties(); 
		FileInputStream is = new FileInputStream(fileName);
		props.load(is) ;
		is.close();
		HashMap<String,PreferenceInfo> prefMap= new LinkedHashMap<String,PreferenceInfo>();
	 
	 
		for (Iterator iterator = props.keySet().iterator(); iterator.hasNext();) {
			String  key = (String) iterator.next();
	 
			String prefID = key.substring(0,key.indexOf("."));
			String propkey = key.substring(key.indexOf(".")+1,key.length()); 

			PreferenceInfo pInfo= prefMap.get(prefID) ;
			if(PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX.equals(propkey)
					|| PreferenceInfo.KEY_LOCAL_HD_RUNNER_THRESHOLD.equals(propkey)){
				prefID = PreferenceInfo.GROUP_DB;
				pInfo = prefMap.get(PreferenceInfo.GROUP_DB);
			}
			if(pInfo ==null){
				
				pInfo=new PreferenceInfo() ;
				pInfo.setId(prefID);
	 
				pInfo.setPreferenceItems(new Properties()) ;
				prefMap.put(prefID,pInfo);
			}
			pInfo.setProperty(propkey,(String)props.get(key)) ;
		}

        //there are two new properties that need to be added
        if (!props.containsKey(PreferenceInfo.GROUP_LOG + "." + PreferenceInfo.KEY_LOG_CUST_ID) || !props.containsKey(PreferenceInfo.GROUP_LOG + "." + PreferenceInfo.KEY_LOG_OPT_OUT))
        {
            PreferenceInfo pInfo = prefMap.get(PreferenceInfo.GROUP_LOG);
            if(pInfo ==null){
                pInfo=new PreferenceInfo() ;
                pInfo.setId(PreferenceInfo.GROUP_LOG);

                pInfo.setPreferenceItems(new Properties()) ;
                prefMap.put(PreferenceInfo.GROUP_LOG,pInfo);
            }

            if (!props.containsKey(PreferenceInfo.KEY_LOG_CUST_ID))
            {
            UUID uuid = UUID.randomUUID();
            pInfo.setProperty(PreferenceInfo.KEY_LOG_CUST_ID, uuid.toString());
                props.setProperty(PreferenceInfo.GROUP_LOG+"." +PreferenceInfo.KEY_LOG_CUST_ID, uuid.toString());
            }
            if (!props.containsKey(PreferenceInfo.KEY_LOG_OPT_OUT))
            {
                pInfo.setProperty(PreferenceInfo.KEY_LOG_OPT_OUT, "false");
                props.setProperty(PreferenceInfo.GROUP_LOG+"." +PreferenceInfo.KEY_LOG_OPT_OUT, "false");
            }
            props.store(new FileOutputStream(fileName), "") ;
        }

        return prefMap.values();
		
	}
	
	@Override
	public Collection<PreferenceInfo> getPreferencesDefaultValue()   {
		List<PreferenceInfo> infos= new ArrayList<PreferenceInfo>();
		Properties algProps = new Properties(); 
		PreferenceInfo algPrefs = new PreferenceInfo(PreferenceInfo.GROUP_ALG,algProps);
		algPrefs.setProperty(PreferenceInfo.KEY_DISTINCT_VALUE_COUNT, "100000");
		algPrefs.setProperty(PreferenceInfo.KEY_VA_DISTINCT_VALUE_COUNT, "1000");
		algPrefs.setProperty(PreferenceInfo.KEY_DECIMAL_PRECISION, "4");
		infos.add(algPrefs);
		
		Properties sysProps  = new Properties(); 
		sysProps.setProperty(PreferenceInfo.KEY_DEBUG_LEVEL, "Info");
		PreferenceInfo sysPrefs = new PreferenceInfo(PreferenceInfo.GROUP_SYS, sysProps   ); 
		infos.add(sysPrefs);
		
		Properties dbProps  = new Properties(); 
		dbProps.setProperty(PreferenceInfo.KEY_CONNECTION_TIMEOUT, "5");
		PreferenceInfo dbPrefs = new PreferenceInfo(PreferenceInfo.GROUP_DB, dbProps   ); 
		dbPrefs.setProperty(PreferenceInfo.KEY_LOCAL_HD_RUNNER_THRESHOLD, ProfileUtility.D_HD_LOCAL_RUNNER_THRESHOLD);
		dbPrefs.setProperty(PreferenceInfo.KEY_ADD_OUTPUTTABLE_PREFIX, "false");
		infos.add(dbPrefs);
		
		Properties uiProps  = new Properties();  
		//uiProps.setProperty(PreferenceInfo.KEY_LINES_PER_PAGE, "100");
		// this will affect the performance, default 200 in the eclipse
		uiProps.setProperty(PreferenceInfo.KEY_MAX_TABLE_LINES, "100");
		uiProps.setProperty(PreferenceInfo.KEY_MAX_SCATTER_POINTS , "60");
		uiProps.setProperty(PreferenceInfo.KEY_MAX_TIMESERIES_POINTS , "30" );
		uiProps.setProperty(PreferenceInfo.MAX_CLUSTER_POINTS , "60" );
		PreferenceInfo uiPrefs = new PreferenceInfo(PreferenceInfo.GROUP_UI ,uiProps); 
		infos.add(uiPrefs);
	
        Properties logProps = new Properties();
        logProps.setProperty(PreferenceInfo.KEY_LOG_CUST_ID,UUID.randomUUID().toString());
        logProps.setProperty(PreferenceInfo.KEY_LOG_OPT_OUT, "false");
        PreferenceInfo logPrefs = new PreferenceInfo(PreferenceInfo.GROUP_LOG ,uiProps);
        infos.add(logPrefs);

        return infos ;
		
	}
	
	@Override
	public void updateProfileReader(PreferenceInfo info) {
		Properties props = ProfileReader.getInstance(false).getProperties();
		String id = info.getId();
		Properties items = info.getPreferenceItems();
		if (id.equals("alg")) {
			props.setProperty("alg_para1",
					items.getProperty("distinct_value_count"));
			props.setProperty("alg_para2",
					items.getProperty("decimal_precision"));
			props.setProperty("alg_para3",
					items.getProperty("va_distinct_value_count"));
		} else if (id.equals("ui")) {
			//fix bug :An error occurred in result tab, the message is "For input string: 1,000"( please view the screenshot.)
			props.setProperty("ui_para1", items.getProperty("max_table_lines").replace(",", ""));
			props.setProperty("ui_para2", items.getProperty("max_table_lines").replace(",", ""));
		} else if (id.equals("db")) {
			props.setProperty("db_para1",
					items.getProperty("connection_timeout"));
			if(items.getProperty(ProfileUtility.LOCAL_HD_RUNNER_THRESHOLD)!=null){
				props.setProperty(ProfileUtility.LOCAL_HD_RUNNER_THRESHOLD, items.getProperty(ProfileUtility.LOCAL_HD_RUNNER_THRESHOLD));
			} 
			props.setProperty("ui_para3",
					items.getProperty("add_outputtable_prefix"));
		} else if (id.equals("sys")) {
			String debug_level = items.getProperty("debug_level");
			if (debug_level.equalsIgnoreCase("Info")) {
                LogManager.getRootLogger().setLevel(Level.INFO);
                itsLogger.warn("Setting level to INFO");
                //LogService.getInstance().setLogLevel(Level.INFO);
			} else if (debug_level.equalsIgnoreCase("Debug")) {
                LogManager.getRootLogger().setLevel(Level.DEBUG);
                itsLogger.warn("Setting level to DEBUG");
                //LogService.getInstance().setLogLevel(Level.DEBUG);
			}
		}
		if (id.equals("alg")) {
			String precision = info.getPreferenceItems().getProperty(
					"decimal_precision");
			AlpineMath.setDecimalPrecision(Integer.parseInt(precision));
		}
	}

//	/* (non-Javadoc)
//	 * @see com.alpine.miner.interfaces.resource.Persistence#updateFlowInfoCancel(com.alpine.miner.impls.resource.FlowInfo)
//	 */
//	@Override
//	public void updateFlowInfoCancel(FlowInfo flowInfo) throws Exception {
//        String tmpPath = flowInfo.getTmpPath();
//        String version4Cacnel = flowInfo.getVersion();
//        FlowInfo newFlowInfo = FlowVersionManager.INSTANCE.relaodFlowInfo(flowInfo);
//        String version4newFlowInfo = newFlowInfo.getVersion();
//        //cancle the current. avoid cancel the published new one MINERWEB-715
//        if(version4Cacnel.equals(version4newFlowInfo)==true){
//            // saving the info part.
//            String fileName = generateResourceKey(flowInfo) + INF;
//            storeFlowInfo(fileName, flowInfo);
//
//            if (tmpPath == null || tmpPath.length() == 0) {
//                return;
//            }
//        }
//
//        XMLFileReaderParameters params = new XMLFileReaderParameters(tmpPath, flowInfo.getModifiedUser(), flowInfo.getResourceType());
//        OperatorWorkFlow flow;
//        try{
//            flow = new XMLWorkFlowReader().doRead(params,Locale.ENGLISH);
//        }catch(Exception e){
//            // if here means cannot found UDF custom file, so continue delete temporary file
//            if("1076".equals(e.getMessage())){
//                deleteResource(tmpPath);
//                return;
//            }
//            throw e;
//        }
//        List<UIOperatorModel> uiModels = flow.getChildList();
//        for (UIOperatorModel uiOperatorModel : uiModels) {
//            if(uiOperatorModel.getOperator() instanceof SubFlowOperator){
//                String subFlowName = (String) ParameterUtility.getParameterByName(uiOperatorModel.getOperator(), OperatorParameter.NAME_subflowPath).getValue();
//                if(subFlowName == null){
//                	continue;
//                }
//                String subFlowPath = tmpPath.substring(0, tmpPath.lastIndexOf(File.separator) + 1) + subFlowName+Resources.AFM;
//                deleteResource(subFlowPath);
//            }
//        }
//        deleteResource(tmpPath);
//	}

//	/* (non-Javadoc)
//	 * @see com.alpine.miner.interfaces.resource.Persistence#updateFlowInfoFinish(com.alpine.miner.impls.resource.FlowInfo)
//	 */
//	@Override
//	public void updateFlowInfoFinish(FlowInfo flowInfo) throws Exception {
//		String fileName;
//		String tmpPath = flowInfo.getTmpPath();
//		
//		// saving the info part.
//		fileName = generateResourceKey(flowInfo) + INF;
//		flowInfo.setTmpPath("");
//		storeFlowInfo(fileName, flowInfo);
//
//		fileName = generateResourceKey(flowInfo) + AFM;		
//		if (tmpPath == null || tmpPath.length() == 0 ||
//				tmpPath.equals(fileName)) {
//			return;
//		}
//	 
//			copyFile(tmpPath, fileName);
//
//			XMLFileReaderParameters params = new XMLFileReaderParameters(tmpPath, flowInfo.getModifiedUser(), flowInfo.getResourceType());
//			OperatorWorkFlow flow = new XMLWorkFlowReader().doRead(params,Locale.ENGLISH);
//			List<UIOperatorModel> uiModels = flow.getChildList();
//			for (UIOperatorModel uiOperatorModel : uiModels) {
//				if(uiOperatorModel.getOperator() instanceof SubFlowOperator){
//					String subFlowName = ParameterUtility.getParameterByName(uiOperatorModel.getOperator(), OperatorParameter.NAME_subflowPath).getValue().toString() ;
//					String subFlowPath = tmpPath.substring(0, tmpPath.lastIndexOf(File.separator) + 1) + subFlowName+Resources.AFM;
//					deleteResource(subFlowPath);
//				} 
//			}
//			deleteResource(tmpPath);
//		
//	}
	
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * com.alpine.miner.interfaces.resource.Persistence#storeFlowInfo(com.alpine
//	 * .miner.impls.resource.FlowInfo,
//	 * com.alpine.miner.workflow.runoperator.OperatorWorkFlow)
//	 */
//	@Override
//	public void updateFlowInfoIncrement(FlowInfo info, OperatorWorkFlow data) throws Exception {
//		String fileName;
//
//		fileName = info.getTmpPath();
//		if (fileName == null || fileName.length() == 0) {
//			fileName = generateResourceKey(info) + INF;
//			storeFlowInfo(fileName, info);
//			fileName = generateResourceKey(info) + AFM;
//		}
//		
//		File file = openFile(fileName, OpenMode.Create);
//		assert (file.exists() == true);
//		
//		XMLWorkFlowSaver saver = new XMLWorkFlowSaver();
//		try {
//			saver.doSave(fileName, data,false);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//	}

	//SUFFIX_FLOW_RESULT = ".fr";
	//FLOW_RESULT_PREFIX
	//root/personal/user/flowname/uuid.fr
	@Override
	public void saveFlowResultInfo(FlowResultInfo resultInfo, String resultJSON) throws IOException {
		//1 save result info
		String uuid=resultInfo.getId();
		String infoFileName=uuid+ INF;
		String jsonFileName=uuid+ SUFFIX_FLOW_RESULT;
		
		String filePath=FLOW_RESULT_PREFIX+"Personal"+File.separator+resultInfo.getCreateUser()
						+File.separator+resultInfo.getFlowName() + File.separator;
		 
		Properties props = new Properties();
		props.setProperty(FlowResultInfo.START_TIME, String.valueOf(resultInfo.getStartTime()));
		props.setProperty(FlowResultInfo.END_TIME, String.valueOf(resultInfo.getEndTime()));
		props.setProperty(FlowResultInfo.FLOW_NAME, resultInfo.getFlowName());
		props.setProperty(FlowResultInfo.RUN_TYPE, resultInfo.getRunType());
		props.setProperty(FlowResultInfo.FLOW_FULL_NAME, resultInfo.getFlowFullName());
		
		setPropertiesFromResource(resultInfo, props);
		saveProperties(filePath+infoFileName, props);
		
		//2 save result json
		writeFile(filePath+jsonFileName , resultJSON);
	 	itsLogger.info("FlowResultInfo saved:" + filePath+jsonFileName)	 ;
	}

	@Override
	public FlowResultInfo getFlowResultInfo(String user, String flowName,
			String uuid) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean deleteFlowResultInfo(String user, String flowName,
			String uuid) {
		String jsonFileName = uuid + SUFFIX_FLOW_RESULT;
		String infoFileName = uuid + INF;
		String filePath = FLOW_RESULT_PREFIX + ResourceType.Personal + File.separator
				+ user + File.separator + flowName + File.separator;
		File jsonFile = new File(filePath + jsonFileName);
		File infoFile = new File(filePath + infoFileName);
		jsonFile.delete();
		infoFile.delete();
		itsLogger.info("FlowResultInfo deleted:" + filePath+jsonFileName)	 ;
		itsLogger.info("FlowResultInfo deleted:" + filePath+infoFileName)	 ;
		return true;
	}

	@Override
	public String getFlowResultJsonStrInfo(String user, String flowName,
			String uuid) throws  Exception {
	 
 
		String jsonFileName=uuid+ SUFFIX_FLOW_RESULT;
 		String filePath=FLOW_RESULT_PREFIX+ResourceType.Personal+File.separator+user
						+File.separator 
 		//chinese flow name will cause error...
						 +flowName+ File.separator;
 		filePath = filePath+jsonFileName; 
 		File file= new File(filePath);
 		 FileInputStream stream=new FileInputStream(file);
 		
 		
 		BufferedReader br = new BufferedReader(new InputStreamReader(stream,Persistence.ENCODING));
 		StringBuffer buffer= new StringBuffer();
 		String data = null;
 		while((data = br.readLine())!=null){
             buffer.append(data);
 		}
 		 return buffer.toString();
	}

	@Override
	public List<FlowResultInfo> getFlowResultInfos(String user) {
		List<FlowResultInfo> infos = new ArrayList<FlowResultInfo>();  
 		String filePath=FLOW_RESULT_PREFIX+ResourceType.Personal+File.separator+user +File.separator;
 		File dir= new File(filePath) ;
 		File[] files = dir.listFiles();
 		if(files!=null){
	 		for(int i =0 ;i<files.length;i++){
	 			//flow name is a dir
	 			if(files[i].isDirectory()==true){
	 			fillFlowInfo(files[i],infos);
	 			}
	 		}
 		}
 		
		return infos;
 	}

	private void fillFlowInfo(File file, List<FlowResultInfo> infos) {
		String[] files = file.list(new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
 
				return name.endsWith(INF);
			}}) ;
		
			for(int i =0 ;i<files.length;i++){
				String infoFileName = files[i];
				FlowResultInfo info= loadFlowResultInfo( file.getAbsolutePath()+File.separator+infoFileName) ;
				if(info!=null){
					infos.add(info);
				}
			}
	}

	private FlowResultInfo loadFlowResultInfo(String infoFileName) {
		FlowResultInfo info = new FlowResultInfo();
		Properties props = readProperties(infoFileName);
		getResourceFromProperties(info, props);
		info.setStartTime(Long.parseLong(props.getProperty(FlowResultInfo.START_TIME))) ;
		info.setFlowName(props.getProperty(FlowResultInfo.FLOW_NAME)) ;
		info.setEndTime(Long.parseLong(props.getProperty(FlowResultInfo.END_TIME))) ;
		info.setRunType(props.getProperty(FlowResultInfo.RUN_TYPE)) ;
		info.setFlowFullName(props.getProperty(FlowResultInfo.FLOW_FULL_NAME));
		
		return info;
	}

	public String getVersionFolderPath(FlowInfo info){

		String key;
		ResourceType type = info.getResourceType();
		key = FLOW_VERSION_PREFIX + type.name() + File.separator;
		String[] categoryList = info.getCategories();
		if (type.equals(ResourceType.Group)) {
			String name = info.getGroupName();
			if (name == null || name.length() == 0) {
				name = info.getModifiedUser();
			}
			key += name + File.separator;
		}
		else if (type.equals(ResourceType.Personal) && (categoryList == null || categoryList.length == 0)) {
			key += info.getModifiedUser() + File.separator;//category already include user folder
		}
		if (categoryList != null && categoryList.length > 0) {
			for (String category : categoryList) {
				if (category.length() > 0) {
					key += category + File.separator;
				}
			}
		}
		key += info.getId();
		
		String flowPath = key;
		String versionFolderPath = flowPath + FOLDER_SUFFIX_VERSION;
		return versionFolderPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.miner.interfaces.resource.Persistence#getFlowData(com.alpine
	 * .miner.impls.resource.FlowInfo)
	 */
	@Override
	public OperatorWorkFlow getFlowData4Path(String filePath, FlowInfo info, Locale locale) 
		throws OperationFailedException {
		OperatorWorkFlow flow = null;
		XMLWorkFlowReader reader = new XMLWorkFlowReader();
		XMLFileReaderParameters params = null;
		 
		try {
			if (filePath != null && filePath.length() > 0) {
				File f = new File(filePath);
				if (f.exists() == true) {
					params = new XMLFileReaderParameters(filePath,
							info.getModifiedUser(), info.getResourceType());

					flow = reader.doRead(params,locale);
					return flow;
				}				
			}


		} catch ( Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			e.printStackTrace();
			throw new OperationFailedException(e.getMessage());
		}  

		return flow;
	}
 
	/* (non-Javadoc)
	 * @see com.alpine.miner.interfaces.resource.Persistence#getFlowFullName(com.alpine.miner.impls.web.resource.FlowInfo)
	 */
	@Override
	public String getFlowFullName(FlowInfo flow) {
		if(flow.getCategories() != null && flow.getCategories().length > 0){
			return flow.getCategories()[0] + File.separator + flow.getId();
		}else{
			return flow.getModifiedUser() + File.separator + flow.getId();
		}
	}

	@Override
	public void saveFlow(FlowInfo flowInfo, OperatorWorkFlow flow)
			throws Exception {
 
	 String key  =generateResourceKey(flowInfo);
		String	fileName = key + INF;
			storeFlowInfo(fileName, flowInfo);
			fileName =key+ AFM;
	 
		
		File file = openFile(fileName, OpenMode.Create);
		assert (file.exists() == true);
		
		XMLWorkFlowSaver saver = new XMLWorkFlowSaver();
		try {
			saver.doSave(fileName, flow,false);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
 
}