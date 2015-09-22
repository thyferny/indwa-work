/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * DBConnectionController.java
 * 
 * Author zhaoyong
 * 
 * Version 2.0
 * 
 * Date Aug 07, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.controller.dbmeta.DBMetaDataDTO;
import com.alpine.miner.impls.datasourcemgr.WebDBResourceManager;
import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.resource.DbConnectionInfo;
import com.alpine.miner.impls.resource.JDBCDriverInfo;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.impls.web.resource.DBConnInfoTree;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.utility.db.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
@Deprecated
@RequestMapping("/main/dbconnection.do")
public class DBConnectionController extends AbstractControler  {
//    private static Logger itsLogger = Logger.getLogger(DBConnectionController.class);
//
    public DBConnectionController() throws Exception{
		super();
	}
//
//	@RequestMapping(params = "method=getJdbcDriverNames", method = RequestMethod.GET)
//	public void getJdbcDriverNames(HttpServletRequest request,
//			HttpServletResponse response, ModelMap model) throws IOException{
//		List<String> driverNames = new ArrayList<String>();
//		List<JDBCDriverInfo> jdbcDriverInfos= WebDBResourceManager.getInstance().getJDBCDriverInfos();
//		for (int i = 0; i < jdbcDriverInfos.size(); i++) {
//			JDBCDriverInfo driverInfo = jdbcDriverInfos.get(i);
//			driverNames.add(driverInfo.getDriverName());
//		}
//		ProtocolUtil.sendResponse(response, driverNames);
//	}
//
//	@RequestMapping(params = "method=deleteJdbcDriverByName", method = RequestMethod.GET)
//	public void deleteJdbcDriverByName(String driverFileName,HttpServletRequest request,
//			HttpServletResponse response, ModelMap model) throws IOException{
//	   //HttpSession session = request.getSession();
//	   //UserInfo u = (UserInfo) session.getAttribute(Resources.SESSION_USER);
//	   String username = getUserName(request);
//	   String msg = "";
//	   if(null!=username && username.equalsIgnoreCase("admin")){
//		   FilePersistence fp = new FilePersistence();
//			 try {
////				 if(this.isJDBCJarUsered(username, driverFileName)){
////					 ResourceBundle bundle = ResourceBundle.getBundle(
////								"app", request.getLocale());
////						msg = bundle.getString("DB_JDBC_JAR_ERROR_USING");
////						ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, msg));
////						return;
////				 }
//				fp.deleteJDBCJar(driverFileName.trim().replaceAll(".jar", ""));
//				returnSuccess(response);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				ResourceBundle bundle = ResourceBundle.getBundle(
//						"app", request.getLocale());
//				msg = bundle.getString("DB_JDBC_JAR_ERROR_FAIL");
//				ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, msg));
//
//			}
//	   }else{
//	      //do not delet jar
//		   ResourceBundle bundle = ResourceBundle.getBundle(
//					"app", request.getLocale());
//			msg = bundle.getString("DB_JDBC_JAR_ERROR_NORIGHT");
//			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, msg));
//
//	   }
//
//	}
//
//	public boolean isJDBCJarUsered(String username,String driverFileName){
//		AuthenticationProvider auth = ProviderFactory.getAuthenticator(username);
//		List<UserInfo> allUsers = auth.getUserInfoList();
//		for (Iterator iterator = allUsers.iterator(); iterator
//				.hasNext();) {
//			UserInfo userInfo = (UserInfo) iterator.next();
//			List<DbConnectionInfo> allCons = WebDBResourceManager.getInstance().getDBConnectionList(userInfo.getLogin());
//			for (Iterator iterator2 = allCons.iterator(); iterator2
//					.hasNext();) {
//				DbConnectionInfo dbConnectionInfo = (DbConnectionInfo) iterator2
//						.next();
//
//				if (null != dbConnectionInfo.getConnection()
//						&& null != dbConnectionInfo.getConnection()
//								.getJdbcDriverFileName()
//						&& driverFileName.trim().equals(
//								dbConnectionInfo.getConnection()
//										.getJdbcDriverFileName())) {
//
//					return true;
//				}
//			}
//		}
//
//
//
//		return false;
//	}
//
//
//
//	@RequestMapping(params = "method=getAllBDConnections", method = RequestMethod.GET)
//	public void getAllBDConnections(String user, HttpServletRequest request,
//			HttpServletResponse response, ModelMap model) throws IOException {
//
//		try {
//			HashMap<ResourceType, List> connMap = new HashMap<ResourceType, List>();
//
//			String path = ResourceType.Personal.name() + File.separator + user;
//			// will use the new API when ready
//			connMap.put(ResourceType.Personal, rmgr
//					.getDBConnectionsByPath(path));
//
//			path = ResourceType.Public.name();
//			// will use the new API when ready
//			connMap.put(ResourceType.Public, rmgr.getDBConnectionsByPath(path));
//
//
//
//
//            //when username is admin get groupName
//			if (user.equals("admin")) {
//				List<String> groupList = getGroups4Admin();
//
//				connMap.put(ResourceType.Group,
//						getDBConnections4Group((String[]) groupList
//								.toArray(new String[groupList.size()])));
//			} else {
//				String[] groups = ProviderFactory.getAuthenticator(user).getUserGroups(user);
//				connMap.put(ResourceType.Group, getDBConnections4Group(groups));
//			}
//
//			// A group get. build a tree.
//
//			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(connMap));
//		} catch (Exception e) {
//			generateErrorDTO(response, e, request.getLocale());
//
//		}
//
//	}
//
//
//	private List<DBConnInfoTree>getDBConnections4Group(String[] groups) {
//		List<DBConnInfoTree> dbConns = new LinkedList<DBConnInfoTree>();
//		if (groups != null&&groups.length>0) {
//
//			for (String g : groups) {
//				String path = ResourceType.Group.name() + File.separator + g;
//				List children = rmgr.getDBConnectionsByPath(path);//;rmgr.getFlowList(path);
//				if(children!=null&&children.size()>0){
//					DBConnInfoTree tree = new DBConnInfoTree(g);
//					tree.addChildren(children);
//					dbConns.add(tree);
//					dbConns.addAll(children);
//				}
//			}
//
//		}
//	return dbConns;
//	}
//
//
//
//	@Deprecated
//	@SuppressWarnings("unchecked")
//	@RequestMapping(params = "method=getBDConnections", method = RequestMethod.GET)
//	public void getDBConnections(String user, String type,
//			HttpServletRequest request, HttpServletResponse response,
//			ModelMap model) throws IOException {
//
//
//		String str = null;
//		try{
//
//			String path = type;
//
//			if (type.equals(ResourceType.Personal.name())) {
//				path = type + File.separator + user;
//				//will use the new API when ready
//				str = ProtocolUtil.toJson(rmgr.getDBConnectionsByPath(path));
//			}
//			else if (type.equals(ResourceType.Public.name())) {
//
//				str = ProtocolUtil.toJson(rmgr.getDBConnectionsByPath(type));
//			}
//			else {
//				// A group get. build a tree.
//				String[] groups = ProviderFactory.getAuthenticator(user).getUserGroups(user);
//				if (groups == null) {
//					str ="";
//				}else{
//					List<DBConnInfoTree> total = new LinkedList<DBConnInfoTree>();
//					for (String g : groups) {
//						path = type + File.separator + g;
//						List children = rmgr.getDBConnectionsByPath(path);//;rmgr.getFlowList(path);
//						if(children!=null&&children.size()>0){
//							DBConnInfoTree tree = new DBConnInfoTree(g);
//							tree.addChildren(children);
//							total.add(tree);
//							total.addAll(children);
//						}
//					}
//					str = ProtocolUtil.toJson(total);
//				}
//			}
//		}
//		catch(Exception e){
//			generateErrorDTO(response, e, request.getLocale()) ;
//			return;
//		}
//		ProtocolUtil.sendResponse(response, str);
//
//	}
//
//	@RequestMapping(params = "method=getBDConnection", method = RequestMethod.GET)
//	public void getBDConnection(  String connName, String resourceType,
//			HttpServletRequest request, HttpServletResponse response,
//			ModelMap model) throws IOException {
//
//		try{
//			String user =getUserName(request);
//
//			DbConnectionInfo result = null;
//			for (DbConnectionInfo info : rmgr.getDBConnectionList(user)) {
//				if (info.getId().equals(connName)&&info.getResourceType().equals(resourceType)) {
//					result = info;
//					break;
//				}
//			}
//			if (result != null) {
//				ProtocolUtil.sendResponse(response, result);
//			}
//		}
//		catch(Exception e){
//			generateErrorDTO(response, e, request.getLocale()) ;
//			return;
//		}
//	}
//
//	@RequestMapping(params = "method=updateDBConnction", method = RequestMethod.POST)
//	public void updateDBConnction(HttpServletRequest request, HttpServletResponse response,
//			ModelMap model) throws IOException, DataOutOfSyncException {
//		try{
//			DbConnectionInfo info = ProtocolUtil.getRequest(request, DbConnectionInfo.class);
//			info.setCreateUser(getUserName(request));
//			info.setModifiedUser(getUserName(request));
//			if (info != null) {
//				rmgr.updateDBConnection(info);
//			}
//			returnSuccess(response) ;
//		}
//		catch(Exception e){
//			generateErrorDTO(response, e, request.getLocale()) ;
//
//		}
//	}
//
//	@RequestMapping(params = "method=testDBConnction", method = RequestMethod.POST)
//	public String testDBConnction(HttpServletRequest request, HttpServletResponse response,
//			ModelMap model) throws IOException {
//		String errorMessage = "";
//
//		DbConnectionInfo info = ProtocolUtil.getRequest(request, DbConnectionInfo.class);
//		if (info == null) {
//			return null;
//		}
//		DbConnection dbc = info.getConnection();
//
//		// TODO: I should not have to do this. See issue MINER-938
//		//	The following code will be removed when above issue is fixed.
//		//
//		//   public DbConnection(java.lang.String dbType,
//		//		java.lang.String hostname, int port,
//		//		java.lang.String dbname,
//		//		java.lang.String dbuser,
//		//		java.lang.String password);
//		DbConnection dbcnew = new DbConnection(dbc.getDbType(),
//				dbc.getHostname(), dbc.getPort(),
//				dbc.getDbname(), dbc.getDbuser(), dbc.getPassword(), dbc.getJdbcDriverFileName(), dbc.getUseSSL());
//
//		IDataSourceInfo connInfo = DataSourceInfoFactory
//		.createConnectionInfo(dbcnew.getDbType());
//		connInfo.setLocale(request.getLocale());
//		try {
//			if(connInfo.checkDBConnection(dbcnew)){
//				errorMessage = "";
//				if(DBMetaDataUtil.connDeadList.contains(dbcnew.getUrl()+dbcnew.getDbuser())){
//					DBMetaDataUtil.connDeadList.remove(dbcnew.getUrl()+dbcnew.getDbuser());
//				}
//			}
//		} catch (Exception e) {
//			errorMessage = e.getMessage();
//			if(!DBMetaDataUtil.connDeadList.contains(dbcnew.getUrl()+dbcnew.getDbuser())){
//				DBMetaDataUtil.connDeadList.add(dbcnew.getUrl()+dbcnew.getDbuser());
//			}
//			itsLogger.error(e.getMessage(),e);
//		}
//
//		ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(errorMessage));
//
//		return null;
//	}
//
//
//	@RequestMapping(params = "method=deleteDBConnction", method = RequestMethod.POST)
//	public void removeDBConnction(HttpServletRequest request, HttpServletResponse response,
//			ModelMap model) throws IOException   {
//		try {
//			DbConnectionInfo[] infos = ProtocolUtil.getRequest(request, DbConnectionInfo[].class);
//			if (infos != null) {
//				for(int i=0;i<infos.length;i++){
//					infos[i].setModifiedUser(getUserName(request)) ;
//					rmgr.deleteDBConnection(infos[i]);
//				}
//			}
//		}
//		catch (Exception e) {
//			generateErrorDTO(response, e, request.getLocale()) ;
//		}
//		returnSuccess(response);
//	}
//
//
//	@RequestMapping(params = "method=createDBConnction", method = RequestMethod.POST)
//		public void createDBConnction(HttpServletRequest request,
//			HttpServletResponse response, ModelMap model) throws IOException {
//
//		try {
//			DbConnectionInfo info = ProtocolUtil.getRequest(request,
//					DbConnectionInfo.class);
//
//			info.setCreateUser(getUserName(request));
//			info.setModifiedUser(getUserName(request));
//
//			if (info != null) {
//
//				rmgr.createDBConnection(info);
//			}
//		} catch (Exception e) {
//			generateErrorDTO(response, e, request.getLocale()) ;
//			return  ;
//		}
//		returnSuccess(response) ;
//
//	}
	
	//where to put the driver?...

}

