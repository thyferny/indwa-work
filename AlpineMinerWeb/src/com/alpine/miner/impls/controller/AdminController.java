/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AdminController.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 23, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.license.validator.illuminator.LicenseInfomation;
import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.audit.ActionType;
import com.alpine.miner.impls.audit.AuditItem;
import com.alpine.miner.impls.audit.AuditManager;
import com.alpine.miner.impls.flowHistory.FlowHistoryInfo;
import com.alpine.miner.impls.flowHistory.FlowHistoryServiceFactory;
import com.alpine.miner.impls.license.LicenseManager;
import com.alpine.miner.impls.license.LicenseUpdateInfo;
import com.alpine.miner.impls.license.WebLicenseChecker;
import com.alpine.miner.impls.license.WebLicenseChecker.CheckResult;
import com.alpine.miner.impls.mail.MailConfiguration;
import com.alpine.miner.impls.mail.MailInfo;
import com.alpine.miner.impls.mail.SendMailException;
import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.web.resource.*;
import com.alpine.miner.impls.web.resource.LoginManager.Status;
import com.alpine.miner.interfaces.MailConfigManager;
import com.alpine.miner.interfaces.MailSender;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.eula.EULAManager;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.miner.security.impl.SecurityConfiguration;
import com.alpine.miner.security.permission.Permission;
import com.alpine.miner.security.rolemgr.IRoleManageService;
import com.alpine.miner.security.rolemgr.impl.FixedRoleManageServiceImpl;
import com.alpine.miner.workflow.runner.FlowRunningHelper;
import com.alpine.utility.log.LogPoster;
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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@Controller
@RequestMapping("/main/admin.do")
public class AdminController extends AbstractControler  {

	public AdminController() throws Exception {
		super();	 
	}
	
	@RequestMapping(params = "method=testConn", method = RequestMethod.POST)
	public String testConn(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
		// for Miner test connect.
		UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
		if (info != null) {
			AuthenticationProvider auth = ProviderFactory.getAuthenticator(info.getLogin());
			UserInfo u = auth.getUserInfoByName(info.getLogin());
			String msg = "";
			Status status = LoginManager.authenticate(info, null); 
			if (status==Status.BadPassword) {
					msg = Resources.BAD_PASSWORD;
			} else if(status==Status.UserNotFound){
					msg = Resources.NO_USER;
			}else {
					//login OK...
				request.getSession().setAttribute(Resources.SESSION_USER, u);
			}
		 
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(msg));
		}else{
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(Resources.NO_USER));
		}
		
		return null;
	}
	
	@RequestMapping(params = "method=getGroupsForUser", method = RequestMethod.GET)
	public void getGroupsForUser(
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
 
		try {
			String user = getUserName(request);
			AuthenticationProvider auth = ProviderFactory.getAuthenticator("");
			String[] userGroup = auth.getUserGroups(user); 
			ProtocolUtil.sendResponse(response, userGroup);
		}
		catch (IOException e) {
			generateErrorDTO(response, e, request.getLocale()) ;				 
		}
	}
	
	@RequestMapping(params = "method=getGroups", method = RequestMethod.GET)
	public void getGroups(
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
 
		try {
			String user = getUserName(request);
//			if (LoginManager.ADMIN_USER.equals(user) == false) {
//				ProtocolUtil.sendResponse(response, new ErrorDTO());
//				return  ;
//			}
			AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
			ProtocolUtil.sendResponse(response, auth.getGroupInfoList());
		}
		catch ( Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(e.getMessage()));
			return  ;
		}			 
	}
	
	@RequestMapping(params = "method=getGroupsNoLogin", method = RequestMethod.GET)
	public String getGroupsNoLogin(
			String user,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		
		if (user == null || user.length() == 0) {
			ProtocolUtil.sendResponse(response, new String[0]);
			return null;
		}
		AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
		String[] groups;
		if (user.equals("admin")) {
			  List<String> groupList = getGroups4Admin();
			
			  if(groupList!=null){
				  groups=groupList.toArray( new  String[groupList.size()] );
			  }else{
				  groups = new String[0];
			  }
			 
		} else{
			groups = auth.getUserGroups(user);
		}
		ProtocolUtil.sendResponse(response,groups);
		return null;
	}

	@RequestMapping(params = "method=acceptAgreement", method = RequestMethod.GET)
	public void acceptAgreement(String userName, HttpServletRequest request, HttpServletResponse response) throws IOException{
		EULAManager.getInstance().saveAcceptStatus(userName);
		UserInfo userInfo = ProviderFactory.getAuthenticator(userName).getUserInfoByName(userName);
		try {
			storageUserInfo(request, userInfo);
			ProtocolUtil.sendResponse(response, true);
		} catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
		}
	}
	
	@RequestMapping(params = "method=login", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {

		UserInfo u = ProtocolUtil.getRequest(request, UserInfo.class);
		if (u != null) {
			AuthenticationProvider auth = ProviderFactory.getAuthenticator(u.getLogin());
			Status logStatus;
			
			try{
				logStatus = LoginManager.authenticate(u, request.getSession());
			}catch(Exception e){
				ProtocolUtil.sendResponse(response,new ErrorDTO(ErrorDTO.UNKNOW_ERROR, e.getMessage()));
				return;
			}
			switch (logStatus) {
			case UserNotFound:
				ProtocolUtil.sendResponse(response, new ErrorDTO(1));
				return;
				
			case BadPassword:
				ProtocolUtil.sendResponse(response, new ErrorDTO(2));
				return;	
				
			case AlreadyLoggedIn:
				ProtocolUtil.sendResponse(response, new ErrorDTO(3));
				return;	
			case BrowserHasLoggedByOtherUser:
				ProtocolUtil.sendResponse(response, new ErrorDTO(4));
				return;	
			case OK:
				u = auth.getUserInfoByName(u.getLogin());
				int modelerCount = LoginManager.getTotalModelerNum();
				if(u.getRoleSet() != null && Arrays.binarySearch(u.getRoleSet(), FixedRoleManageServiceImpl.MODELER_ROLE_IDENTIFIER) >= 0){
					modelerCount++;
				}
				CheckResult licenseCheck = WebLicenseChecker.checkLicense(LoginManager.getTotalNum(), modelerCount);
				if(licenseCheck != CheckResult.PASSED){
					ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ErrorNLS.getMessage("LOGIN_LICENSE_" + licenseCheck.name(), request.getLocale(), new String[]{licenseCheck.getMsg()})));
					return;
				}

				if(!EULAManager.getInstance().isUserAccepted(u.getLogin())){
					// need read and accept the EULA
					ProtocolUtil.sendResponse(response, new ErrorDTO(5));
					return;
				}
				storageUserInfo(request, u);
				break;
			}

			ProtocolUtil.sendResponse(response, new ErrorDTO(0));
		}
	}
	
	private void storageUserInfo(HttpServletRequest request, UserInfo userInfo) throws Exception{
		String ts = "" + System.currentTimeMillis();
		Set<Permission> blacklist = IRoleManageService.INSTANCE.getBlacklist(userInfo.getRoleSet());
		request.getSession().setAttribute(Resources.SESSION_USER, userInfo);
		request.getSession().setAttribute(Resources.SESSION_PERMISSION, blacklist);
		request.getSession().setAttribute(Resources.SESSION_TIME_STAMP, ts);
		request.getSession().setAttribute(Resources.AUTH_TYPE, 
				ProviderFactory.loadConfiguration().getCurrent_choice().name());
		LoginManager.addUser(userInfo.getLogin(), request.getSession());
		AuditManager.INSTANCE.appendUserAuditItem(userInfo.getLogin(),
				new AuditItem(userInfo.getLogin(), ActionType.LOGIN, ""));
		
		ResourceFlowManager.instance.syncSampleFlow(userInfo.getLogin());
	}

	@RequestMapping(params = "method=logout", method = RequestMethod.POST)
	public void logout(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {

		String user = (String) request.getHeader(Resources.SESSION_USER);
		if (user != null) {
			LoginManager.removeUser(user);
			releaseUserResource(user);
			ProtocolUtil.sendResponse(response, new ErrorDTO(0));
		}
 
	}

	private void releaseUserResource(String user) {
		Map<String, Map<String, RowInfo>> resultMap = FlowRunningHelper.RESULT_MAP;
		Map<String, Map<String, WebWorkFlowRunner>> runnerMap = FlowRunningHelper.RUNNER_MAP;
		Map<String, WebWorkFlowStepRunner> stepRunnerMap = FlowRunningHelper.STEP_RUNNER_MAP;

		//make sure remove the cahche...
		synchronized(resultMap){
			if(resultMap.containsKey(user)){
				resultMap.remove(user) ;
			}
		}
		synchronized(runnerMap){
			if(runnerMap.containsKey(user)){
				Map<String, WebWorkFlowRunner> runners = runnerMap.remove(user) ;
				if(runners!=null){
					runners.clear();
					 
				}
			}
		}
		
		synchronized(stepRunnerMap){
			if(stepRunnerMap.containsKey(user)){
				WebWorkFlowStepRunner stepRunner = stepRunnerMap.remove(user);
				if(stepRunner!=null){
					stepRunner.dispose();
				}
			}
		}
		
		Collection<FlowHistoryInfo> history = FlowHistoryServiceFactory.getService(user).getFlowHistory();
		for (Iterator<FlowHistoryInfo> iterator = history.iterator(); iterator.hasNext();) {
			FlowHistoryInfo flowHistoryInfo = iterator.next();
			String flowKey = flowHistoryInfo.getKey();
			ResourceFlowManager.instance.forceClearCache(flowKey );
		}
	}
	
	@RequestMapping(params = "method=forceLogin", method = RequestMethod.POST)
	public void forceLogin(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {

		UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
		if (info != null) {
			AuthenticationProvider auth = ProviderFactory.getAuthenticator(info.getLogin());
			UserInfo u = auth.getUserInfoByName(info.getLogin());
			switch (LoginManager.authenticate(info, request.getSession())) {
			case UserNotFound:
				ProtocolUtil.sendResponse(response, new ErrorDTO(1));
				return;
				
			case BadPassword:
				ProtocolUtil.sendResponse(response, new ErrorDTO(2));
				return;	
				
			case AlreadyLoggedIn:
			case BrowserHasLoggedByOtherUser:
				//remove old user info from usercache
				LoginManager.removeUser(request.getSession());
			case OK:
				if(!EULAManager.getInstance().isUserAccepted(u.getLogin())){
					// need read and accept the EULA
					ProtocolUtil.sendResponse(response, new ErrorDTO(5));
					return;
				}
				String ts = "" + System.currentTimeMillis();
				u = auth.getUserInfoByName(u.getLogin());
				Set<Permission> blacklist = IRoleManageService.INSTANCE.getBlacklist(u.getRoleSet());
				request.getSession().setAttribute(Resources.SESSION_USER, u);
				request.getSession().setAttribute(Resources.SESSION_PERMISSION, blacklist);
				request.getSession().setAttribute(Resources.SESSION_TIME_STAMP, ts);
				request.getSession().setAttribute(Resources.AUTH_TYPE, 
						ProviderFactory.loadConfiguration().getCurrent_choice().name());
				LoginManager.addUser(u.getLogin(), request.getSession());
				AuditManager.INSTANCE.appendUserAuditItem(u.getLogin(),
						new AuditItem(u.getLogin(), ActionType.LOGIN, ""));
				break;
			}

			ProtocolUtil.sendResponse(response, new ErrorDTO(0));
		}
	}

	@RequestMapping(params = "method=getUserInfoByLoginName", method = RequestMethod.GET)
	public void getUserInfoByLoginName(String loginName, HttpServletRequest request, HttpServletResponse response) throws IOException{
		AuthenticationProvider auth = ProviderFactory.getAuthenticator(loginName);
		UserInfo userInfo = auth.getUserInfoByName(loginName);
		ProtocolUtil.sendResponse(response, userInfo);
	}
	
	@RequestMapping(params = "method=getUserInfo", method = RequestMethod.POST)
	public void getUserInfo(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
 
		UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
		if (info != null) {
			AuthenticationProvider auth = ProviderFactory.getAuthenticator("");
			List<UserInfo> list = auth.getUserInfoList();

			UserInfo user = null;
			for (UserInfo u : list ) {
				if (info.getEmail().equals(u.getEmail())) {
					user = u;
					break;
				}
			}
			if (user == null) {
				auth = ProviderFactory.getAuthenticator(LoginManager.ADMIN_USER);
				list = auth.getUserInfoList();
				for (UserInfo u : list ) {
					if (info.getEmail().equals(u.getEmail())) {
						user = u;
						break;
					}
				}
			}
			String msg = "";
			if (user == null) {
				msg = Resources.NO_USER;
			} 
			else {
				// email the user info.
				MailInfo mail = new MailInfo();
				String subject = "Alpine Illuminator account information";
				String content = "Login: " + user.getLogin();
				content += "\n";

				if (ProviderFactory.loadConfiguration().getCurrent_choice() 
						== SecurityConfiguration.ProviderType.LocalProvider) {
					content += "Password: " + user.getPassword();
				}
				else {
					content += "Please contact your administrator to get a new password";
				}
				
				mail.setSubject(subject);
				mail.setContent(content);
				mail.setSendTime(new Date());
				mail.addReceiver(user.getEmail());
				try {
					MailSender.instance.send(mail);
				} catch (SendMailException e) {
					msg = "send mail failed. please correct email address.";
				}
			}
			ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(msg));
		}		
	}
	
	
	@RequestMapping(params = "method=resetPassword", method = RequestMethod.POST)
	public void resetPassword(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
 
		try {
			UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
			if (info != null) {
				AuthenticationProvider auth = ProviderFactory.getAuthenticator(info.getLogin());
				UserInfo u = auth.getUserInfoByName(info.getLogin());
				String msg = "";
				if (u == null) {
					msg = Resources.NO_USER;
				} else if (u.getPassword().equals(info.getPassword()) == false) {
					msg = Resources.BAD_PASSWORD;
				} else {
					u.setPassword(info.getNewPassword());
					auth.updateUser(u);
				}
				ProtocolUtil.sendResponse(response, ProtocolUtil.toJson(msg));
			}
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}
	}
	
	@RequestMapping(params = "method=createGroup", method = RequestMethod.POST)
	public void createGroup(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
	 
		GroupInfo info = ProtocolUtil.getRequest(request, GroupInfo.class);
		try {
			if (info != null) {
				String user = getUserName(request);
				AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
				List<GroupInfo> list = auth.getGroupInfoList();
				for (GroupInfo i : list) {
					if (i.getId().equals(info.getId())) {
						 
						ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR,ErrorNLS.getMessage(ErrorNLS.Group_already_exist, request.getLocale())));
						return;
					}
				}
				if (isNull(info.getDescription())) {
					info.setDescription("");
				}
				auth.createGroup(info);
				ProtocolUtil.sendResponse(response, auth.getGroupInfoList());
			}
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
			return;
		}

	}
	
	@RequestMapping(params = "method=updateGroup", method = RequestMethod.POST)
	public void updateGroup(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
 
		GroupInfo info = ProtocolUtil.getRequest(request, GroupInfo.class);
	
		try{
			if (info != null) {
				boolean found = false;
				String user = getUserName(request);
				AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
				List<GroupInfo> list = auth.getGroupInfoList();
				for (GroupInfo i : list) {
					if (i.getId().equals(info.getId())) {
						found = true;
						break;
					}
				}
				if (found == false) {
					
					ProtocolUtil.sendResponse(response,new ErrorDTO(ErrorDTO.UNKNOW_ERROR,ErrorNLS.getMessage(ErrorNLS.Group_not_found, request.getLocale())));
					return  ;
				}
				if(isNull(info.getDescription())){
					info.setDescription("");
				}
				auth.updateGroup(info);
				ProtocolUtil.sendResponse(response, auth.getGroupInfoList());
			}
		}
		catch ( Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;
		 
		}
	}
	
	@RequestMapping(params = "method=deleteGroup", method = RequestMethod.POST)
	public void deleteGroup(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		
		GroupInfo info = ProtocolUtil.getRequest(request, GroupInfo.class);
		try{
		if (info != null) {
			String user = getUserName(request);
			AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
			boolean found = false;
			List<GroupInfo> list = auth.getGroupInfoList();
			for (GroupInfo i : list) {
				if (i.getId().equals(info.getId())) {
					found = true;
					break;
				}
			}
			if (found == false) {
				ProtocolUtil.sendResponse(response, auth.getGroupInfoList());
				return  ;
			}
			List<UserInfo> ulist = auth.getUserInfoList();
			for (UserInfo u : ulist) {
				//avoid the null point
				if(u.getGroups()!=null){
				for (String g : u.getGroups()) {
					if (info.getId().equals(g)) {
						ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR,ErrorNLS.getMessage(ErrorNLS.Group_used_by_user, request.getLocale())));
			           
						return  ;
					}
				}
				}
			}
			auth.deleteGroup(info);
			ProtocolUtil.sendResponse(response, auth.getGroupInfoList());
		}
		
		}
		catch ( Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;
	 
		}
	}
	
	
	// for User
	@RequestMapping(params = "method=getUsers", method = RequestMethod.GET)
	public void getUsers(
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		try {
			String user = getUserName(request);
			AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
			ProtocolUtil.sendResponse(response, auth.getUserInfoList());
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}
	
	@RequestMapping(params = "method=createUser", method = RequestMethod.POST)
	public void createUser(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {

		try {
			UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
			if (info != null) {
				String user = getUserName(request);
				AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
				List<UserInfo> list = auth.getUserInfoList();
				//-----------check license-----------
				CheckResult licenseCheck = WebLicenseChecker.checkLicense(list.size(), LoginManager.getTotalModelerNum());
				if(licenseCheck != CheckResult.PASSED){
					ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ErrorNLS.getMessage("LOGIN_LICENSE_" + licenseCheck.name(), request.getLocale(), new String[]{licenseCheck.getMsg()})));
					return;
				}
				//-----------check license-----------
				
				for (UserInfo i : list) {
					if (i.getLogin().equals(info.getLogin())) {
						ProtocolUtil.sendResponse(response,new ErrorDTO(ErrorDTO.UNKNOW_ERROR,ErrorNLS.getMessage(ErrorNLS.User_already_exist, request.getLocale())) );
						
						return;
					}
				}
				if (isNull(info.getDescription())) {
					info.setDescription("");
				}
				auth.createUser(info);
                LogPoster.getInstance().createAndAddEvent(LogPoster.User_Create,info.getRoleSet(),user);
				ProtocolUtil.sendResponse(response, auth.getUserInfoList());
			}
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}
	
	@RequestMapping(params = "method=updateUser", method = RequestMethod.POST)
	public void updateUser(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
		try{
				UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
				if (info != null) {
					String user = getUserName(request);
					AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
					boolean found = false;
					List<UserInfo> list = auth.getUserInfoList();
					for (UserInfo i : list) {
						if (i.getLogin().equals(info.getLogin())) {
							found = true;
							break;
						}
					}
					if (found == false) {
						 
						ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR,ErrorNLS.getMessage(ErrorNLS.User_not_exists, request.getLocale())));
						return  ;
					}
					if(isNull(info.getDescription())){
						info.setDescription("");
					}
					auth.updateUser(info);
					ProtocolUtil.sendResponse(response, auth.getUserInfoList());
				}
		}
		catch ( Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;
		
		}
	}
	
	@RequestMapping(params = "method=deleteUser", method = RequestMethod.POST)
	public void deleteUser(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		
		UserInfo info = ProtocolUtil.getRequest(request, UserInfo.class);
		try {
			if (info != null) {
				String user = getUserName(request);
				AuthenticationProvider auth = ProviderFactory.getAuthenticator(user);
				boolean found = false;
				List<UserInfo> list = auth.getUserInfoList();
				for (UserInfo i : list) {
					if (i.getLogin().equals(info.getLogin())) {
						found = true;
						break;
					}
				}
				if (found == true) {
					auth.deleteUser(info);
				}

				ProtocolUtil.sendResponse(response, auth.getUserInfoList());
			}

		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());

		}
	}
	
	@RequestMapping(params = "method=getMailServerConfig", method = RequestMethod.GET)
	public void getMailServerConfig(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
	 
		try {
			MailConfiguration config = MailConfigManager.INSTANCE.readConfig();
			ProtocolUtil.sendResponse(response, config);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
		 
	}


	@RequestMapping(params = "method=updateMailServerConfig", method = RequestMethod.POST)
	public String updateMailServerConfig(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
 
		MailConfiguration config = ProtocolUtil.getRequest(request, MailConfiguration.class);
		try {
			MailConfigManager.INSTANCE.saveConfig(config);
//			EventNotifier.resetMailConfiguration(config);	update config to send service moved to MailConfigManager
			returnSuccess(response) ;
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
		
		return null;
	}

	@RequestMapping(params = "method=testMailConfig", method = RequestMethod.POST)
	public void testMailConfig(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException{
		TestMailConfiguration testConfig = ProtocolUtil.getRequest(request, TestMailConfiguration.class);
		try {
			MailSender.instance.send(testConfig, buildMailContent(testConfig));
			returnSuccess(response) ;
		} catch (SendMailException e) {
			String msg = e.getMessage().substring(e.getMessage().indexOf("Exception:") + "Exception:".length());
			msg = msg.substring(0, msg.length() - 1);
			generateErrorDTO(response, msg, request.getLocale()) ;	
		}
	}
	
	@RequestMapping(params = "method=loadSecurityConfig", method = RequestMethod.GET)
	public void loadSecurityConfig(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException{
		try {
			SecurityConfiguration cfg = ProviderFactory.loadConfiguration();
			ProtocolUtil.sendResponse(response, cfg);
		}
		catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(e.getMessage()));
			return;
		}			 
	}
	
	@RequestMapping(params = "method=updateSecurityConfig", method = RequestMethod.POST)
	public void updateSecurityConfig(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException{
		SecurityConfiguration cfg = ProtocolUtil.getRequest(request, SecurityConfiguration.class);
		try {
			if (cfg != null) {
				ProviderFactory.saveConfiguration(cfg);
				request.getSession().setAttribute(Resources.AUTH_TYPE, 
						ProviderFactory.loadConfiguration().getCurrent_choice().name());
				ProtocolUtil.sendResponse(response, cfg);
			}
		} catch (Exception e) {
			String msg = e.getMessage().substring(e.getMessage().indexOf("Exception:") + "Exception:".length());
			msg = msg.substring(0, msg.length() - 1);
			generateErrorDTO(response, msg, request.getLocale()) ;	
		}
	}
	
	@RequestMapping(params = "method=testSecurityConfig", method = RequestMethod.POST)
	public void testSecurityConfig(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException{
		SecurityConfiguration cfg = ProtocolUtil.getRequest(request, SecurityConfiguration.class);
		try {
			if (cfg != null) {
				SecurityTestDTO ret = ProviderFactory.testConfiguration(cfg,request.getLocale());
				ProtocolUtil.sendResponse(response, ret);							
			}
		} catch (Exception e) {
			int splitIdx = e.getMessage().indexOf("Exception:");
			String msg;
			if(splitIdx != -1){
				msg = e.getMessage().substring(e.getMessage().indexOf("Exception:") + "Exception:".length());
				msg = msg.substring(0, msg.length() - 1);
			}else{
				msg = e.getMessage();
			}
			generateErrorDTO(response, msg, request.getLocale()) ;	
		}
	}
	
	@RequestMapping(params = "method=updateLicense", method = RequestMethod.POST)
	public void updateLicense(HttpServletRequest request, HttpServletResponse response) throws IOException{
		LicenseUpdateInfo updateInfo = ProtocolUtil.getRequest(request, LicenseUpdateInfo.class);
		UserInfo user = new UserInfo();
		user.setLogin("admin");
		user.setPassword(updateInfo.getAdminPwd());
		Status status = LoginManager.authenticate(user, null);
		if(status == Status.OK || status == Status.AlreadyLoggedIn){
			try {
				LicenseManager.storeLicense(updateInfo.getLicense());
				returnSuccess(response);
			} catch (IOException e) {
				generateErrorDTO(response, e.getMessage(), request.getLocale()) ;	
			}
		}else{
			generateErrorDTO(response, ErrorNLS.getMessage("UPDATE_LICENSE_ERROR_PWD", request.getLocale()), request.getLocale()) ;	
		}
	}
	
	@RequestMapping(params = "method=uploadCustomJar", method = RequestMethod.POST)
	public void uploadCustomJar(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException{
		String webAppPath = getServletContext().getRealPath("/"); 
		String libPath = webAppPath + File.separator + "WEB-INF"
			+ File.separator + "lib";
		MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
		Iterator<String> it = req.getFileNames();
		try {
			if (it.hasNext() == false) {
				generateErrorDTO(response, "No file.", request.getLocale());
				return;
			}

			while (it.hasNext()) {
				String fn = it.next();

				if (fn == null || fn.trim().length() == 0) {
					continue;
				}
				List<MultipartFile> fileList = req.getFiles(fn);
				for (MultipartFile f : fileList) {
					String path = saveUploadedFile(f, libPath);
					//add this jar file into the current classpath
					addJar2ClassPath(path);
				}
			}

			ProtocolUtil.sendResponse(response, true);
		} catch (Exception e) {
			String msg = e.getMessage().substring(
					e.getMessage().indexOf("Exception:")
							+ "Exception:".length());
			msg = msg.substring(0, msg.length() - 1);
			generateErrorDTO(response, msg, request.getLocale());
		}
	}

	@RequestMapping(params = "method=getLicenseInfo", method = RequestMethod.GET)
	public void getLicenseInfo(HttpServletRequest request, HttpServletResponse response) throws IOException{
		try {
			LicenseInfomation licenseInfo = LicenseManager.getLicenseInfo();
            String UUID = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_LOG,PreferenceInfo.KEY_LOG_CUST_ID);
			ProtocolUtil.sendResponse(response, new LicenseInfomationUI(licenseInfo.getProductID(), licenseInfo.getExpireDate(), licenseInfo.getLimitUserCount(), licenseInfo.getLimitModelerCount(),UUID));
		} catch (Exception e) {
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, ErrorNLS.getMessage("LOGIN_LICENSE_" + e.getMessage(), request.getLocale())));
		}
		
	}

	@RequestMapping(params = "method=getLoginInfoList", method = RequestMethod.GET)
	public void getLoginInfoList(HttpServletRequest request, HttpServletResponse response) throws IOException{
		ProtocolUtil.sendResponse(response, LoginManager.getLoginInfoList());
	}

	@RequestMapping(params = "method=removeLoginInfo", method = RequestMethod.POST)
	public void removeLoginInfo(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String[] loginNames = ProtocolUtil.getRequest(request, String[].class);
		for(String loginName : loginNames){
			LoginManager.removeUser(loginName);
			releaseUserResource(loginName);
		}
		
		
		returnSuccess(response);
	}
	
	/**
	 * @param path
	 */
	private void addJar2ClassPath(String path) {
		URLClassLoader classLoader = (URLClassLoader)this.getClass().getClassLoader();
		try {
			 Method[] methods = classLoader.getClass().getDeclaredMethods();
			 for (int i = 0; i < methods.length; i++) {
				if(methods[i].getName().equals("addURL")){
					Method addURLMethod =methods[i]; 
					addURLMethod.setAccessible(true) ;
					URL jarURL = new URL("file:///"+path); 
					addURLMethod.invoke(classLoader, jarURL);
					return ;
				}
			}
		
		} catch ( Exception e) {
			 
			e.printStackTrace();
		} 
		
		
	}

	/**
	 * @param f
	 * @return
	 * @throws IOException 
	 */
	private String saveUploadedFile(MultipartFile f, String path) throws IOException {
		String fileName = f.getOriginalFilename();
		String filePath = path + File.separator + fileName;
		InputStream in = f.getInputStream();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
		} finally {
			if (out != null) {
				out.close();
				out.flush();
			}
		}
		return filePath;
	}


	private MailInfo buildMailContent(TestMailConfiguration config){
		MailInfo info = new MailInfo();
		ResourceBundle rb = ResourceBundle.getBundle("app");
		info.setSubject(rb.getString("TEST_MAIL_SUBJECT"));
		info.setContent(rb.getString("TEST_MAIL_CONTENT"));
		info.addReceiver(config.getReceiver());
		return info;
	}
	
	private boolean isNull(String arg){
		return arg == null || "".equals(arg) || "null".equalsIgnoreCase(arg);
	}
	
	public static class TestMailConfiguration extends MailConfiguration{
		private String receiver;

		public String getReceiver() {
			return receiver;
		}

		public void setReceiver(String receiver) {
			this.receiver = receiver;
		}
	}
	
	public static class LicenseInfomationUI{
		private String productID, expireDate, limitUserCount, limitModelerCount, customerUUID;

		public LicenseInfomationUI(String productID, String expireDate, String limitUserCount, String limitModelerCount,String customerUUID){
			this.productID = productID;
			this.expireDate = expireDate;
			this.limitUserCount = limitUserCount;
			this.limitModelerCount = limitModelerCount;
            this.customerUUID = customerUUID;
		}
		
		public String getProductID() {
			return productID;
		}

		public String getExpireDate() {
			return expireDate;
		}

		public String getLimitUserCount() {
			return limitUserCount;
		}

		public String getLimitModelerCount() {
			return limitModelerCount;
		}

        public String getCustomerUUID() {
            return customerUUID;
        }

	}
	 
}

