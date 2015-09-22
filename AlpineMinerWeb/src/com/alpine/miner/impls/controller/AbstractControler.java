/**
 * ClassName :AbstractControler.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedException;
import com.alpine.datamining.api.resources.AnalysisError;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.*;
import com.alpine.miner.utils.SysConfigManager;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author zhaoyong
 * 
 */
public abstract class AbstractControler extends MultiActionController {
    private static Logger absLogger = Logger.getLogger(AbstractControler.class);

    private static final String USER_IS_OFFLINE = "User is offline.";
	ResourceManager rmgr = ResourceManager.getInstance();
 	
	
	public AbstractControler () throws Exception{
		super();

	}
	protected void generateErrorDTO(HttpServletResponse response, Exception e,
			Locale locale) throws IOException {
		e.printStackTrace();
		absLogger.error(e.getMessage(), e) ;
		
		if(e instanceof CustomizedException){
			String key = "CustomizedException_"+((CustomizedException) e).getError_id();
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ErrorNLS.getMessage(key, locale)));
		}else if(e.getMessage()!=null&&e.getMessage().equals(USER_IS_OFFLINE)){
			ProtocolUtil.sendResponse(response, new ErrorDTO());
		}
		else if(e instanceof FileNotFoundException){
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ErrorNLS.getMessage(ErrorNLS.FILE_NOT_EXISTS, locale)));
		}else{
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR,buildErrorString(e, locale)));
		}
		
	}

	/**
	 * @param response
	 * @param msg
	 * @param locale
	 */
	protected void generateErrorDTO(HttpServletResponse response, String msg,
			Locale locale) throws IOException{
		ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR,msg));

		
	}
	
	protected String getUserName(HttpServletRequest request) throws IOException {
		UserInfo user = getUserInfo(request);
		return user.getLogin();
	}
	
	protected UserInfo getUserInfo(HttpServletRequest request) throws IOException{
		UserInfo user = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
		if (user == null) {
			throw new IOException(USER_IS_OFFLINE);
		}
		return user;
	}
	
	protected String buildErrorString(Exception e,Locale locale) {
		
		String msg = e.getMessage();
		if(e instanceof AnalysisError){
			AnalysisError error = (AnalysisError)e;
			String errorName = error.getName();
			 
			Object[] argument= error.getArguments();
			try{
				msg=ErrorNLS.getMessage(errorName,locale,argument);
			}catch(Exception ex){
				//nothing to do, will use the native message
			}
			
			 if(msg!=null){
				msg=msg.replace('\"',' ') ;
				//	StringHandler.r.removeMiddleDoubleQ(msg) ; 
			}
			 
		}else{
		
			if(msg!=null){
				msg=msg.replace('\"',' ') ;
			//	StringHandler.r.removeMiddleDoubleQ(msg) ; 
			}
		}
		if(StringUtil.isEmpty(msg)){
			ErrorNLS.getMessage(ErrorNLS.Message_Unknow_Error, locale);
		}
		return msg;
	}

  
	
	public boolean checkUser(String userName, HttpServletRequest request, HttpServletResponse response) {
		try {
			String suser = getUserName(request);
			if (suser == null || suser.equals(userName) == false) {
				ProtocolUtil.sendResponse(response, new ErrorDTO());
				return false;
			}
		}
		catch (IOException e) {
			try {
				ProtocolUtil.sendResponse(response, new ErrorDTO());
			} catch (IOException e1) {
				// no user. return
			}
			return false;
		}
		return true;
	}

	
	ResourceType getResourceType(String type){
		if(type==null){
			return ResourceType.Personal;
		}
		if(type.equals(ResourceType.Public.toString())){
			return ResourceType.Public; 
		}else if(type.equals(ResourceType.Group.toString())){
			return ResourceType.Group;
		}else{
			return ResourceType.Personal;
		}
	}
	

	
	/**
	 * @param flowInfo
	 * @return
	 */
	protected boolean flowExists(FlowInfo flowInfo) {
		String filePath = rmgr.generateResourceKey(flowInfo) + FilePersistence.INF;
		File file = new File(filePath) ;
		return file.exists();
	}
	

	
	protected void saveData2File(String path, String data) throws IOException {
		int idx = path.lastIndexOf(File.separatorChar);
		if (idx > 0) {
			String dirs = path.substring(0, idx);

			File folder = new File(dirs);
			if (folder.exists() == false) {
				folder.mkdirs();
			}
		}

		File f = new File(path);
		if (f.exists() == false) {
			f.createNewFile();
		}
		// System.out.println("Create file at: " + f.getAbsolutePath());
		
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(path),Persistence.ENCODING);
			out.write(data);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * @param response
	 * @throws IOException 
	 */
	protected void returnSuccess(HttpServletResponse response) throws IOException {
		ProtocolUtil.sendResponse(response,"{\"message\":\"success\"}");
		
	}


	protected String getUTF8StringFromImport(String nativeString)
			throws UnsupportedEncodingException {
		String xmlString = "";
		String serverEncoding =SysConfigManager.INSTANCE.getServerEncoding();
		if (nativeString.indexOf("encoding=\"GB2312\"") > 0) {
			
			xmlString = new String(nativeString.getBytes(serverEncoding ), "GB2312");
			xmlString = xmlString.replace("encoding=\"GB2312\"",
					"encoding=\""+Persistence.ENCODING+"\"");
		} else {
			xmlString = new String(nativeString.getBytes(serverEncoding), Persistence.ENCODING);
		}
		return xmlString;
	}
	
	protected String getUTFParamvalue(String value, 
			HttpServletRequest request) throws UnsupportedEncodingException {
		String serverEncoding =SysConfigManager.INSTANCE.getServerEncoding();
 		value =new String (value.getBytes(serverEncoding),Persistence.ENCODING);
		return value;
	}
	
	protected List<String> getGroups4Admin() {
		SecurityConfiguration cfg = ProviderFactory.loadConfiguration();
		List<String> groupList = new ArrayList<String>();
 
		// String[] tempGroups = null;
		switch (cfg.getCurrent_choice()) {
		
			case LocalProvider:
				addGroupNameList(groupList, LocalProvider.getInstance()
						.getGroupInfoList());
				break;
			case LDAPProvider:
				addGroupNameList(groupList, LDAPProvider.getInstance()
						.getGroupInfoList());
				break;
			case ADProvider:
				addGroupNameList(groupList, ADProvider.getInstance().getGroupInfoList());
				break;
			case CustomProvider:
				addGroupNameList(groupList, CustomProvider.getInstance()
						.getGroupInfoList());
				break;
		}
		return groupList;
	}
	
	/**
	 * acquire groupName
	 * */
	private  void addGroupNameList(List<String> groupList,List<GroupInfo> groupInfoList) {
		if(null!=groupInfoList){
			for (Iterator<GroupInfo> iterator = groupInfoList.iterator(); iterator
					.hasNext();) {
				GroupInfo groupInfo = (GroupInfo) iterator.next();
				groupList.add( groupInfo.getId());
				
			}
				
				
		}	
	}
}

