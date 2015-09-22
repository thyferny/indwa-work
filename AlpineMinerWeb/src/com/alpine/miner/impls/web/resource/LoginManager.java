/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * LoginManager.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Dec 26, 2011
 */
package com.alpine.miner.impls.web.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.miner.security.rolemgr.impl.FixedRoleManageServiceImpl;

/**
 * @author sam_zang
 *
 */
public class LoginManager {
	public enum Status {
		OK,
		UserNotFound,
		BadPassword,
		NotLoggedIn,
		AlreadyLoggedIn,
		BrowserHasLoggedByOtherUser,
		LoginOverridden
	}

	public static final String ADMIN_USER = "admin";
	
	private static Map<String, HttpSession> userCache = Collections.synchronizedMap(new HashMap<String, HttpSession>());
	
	private static Set<HttpSession> sessionSet = Collections.synchronizedSet(new HashSet<HttpSession>());
	
	public static Status authenticate(UserInfo user, HttpSession currentSession) {
		AuthenticationProvider auth = ProviderFactory.getAuthenticator(user.getLogin());
	
		Status st = Status.OK;
		UserInfo u = auth.getUserInfoByName(user.getLogin());
		if (u == null) {
			st = Status.UserNotFound;
		} else if (auth.authenticate(user.getLogin(), user.getPassword()) == false) {
			st = Status.BadPassword;
		} else if (findUser(u.getLogin())) {
			st = Status.AlreadyLoggedIn;
		} else if(currentSession != null && sessionSet.contains(currentSession)){
			st = Status.BrowserHasLoggedByOtherUser;
		}
		return st;
	}
	
	public static boolean findUser(String name) {
		return userCache.get(name) != null;		
	}
	
	public static List<LoginInfo> getLoginInfoList(){
		List<LoginInfo> loginInfoList = new ArrayList<LoginInfo>();
		synchronized(userCache){
			for(Entry<String, HttpSession> element : userCache.entrySet()){
				loginInfoList.add(new LoginInfo(element.getKey(), new Date(Long.parseLong((String) element.getValue().getAttribute(Resources.SESSION_TIME_STAMP)))));
			}
		}
		return loginInfoList;
	}

	public static void addUser(String name, HttpSession value) {
//		HttpSession session = userCache.get(name);
//		if(session != null && session != value){
//			session.invalidate();// invalidate session if overrided.
//		}
		synchronized(userCache){
			userCache.put(name, value);
		}
		sessionSet.add(value);
	}
	
	public static void removeUser(String name) {
		HttpSession session = userCache.get(name);
		sessionSet.remove(session);
		session.invalidate();
		synchronized(userCache){
			userCache.remove(name);
		}
	}
	
	/**
	 * call this function when user replace old login user to current user.(in same browser)
	 * or session expired by free time.
	 * @param session
	 * @return userName or null if argument was not found.
	 */
	public static String removeUser(HttpSession session){
		String userName = null;
		synchronized(userCache){
			for(Entry<String, HttpSession> element : userCache.entrySet()){
				if(element.getValue() == session){
					userName = element.getKey();
					userCache.remove(element.getKey());
					break;
				}
			}
		}
		sessionSet.remove(session);
		return userName;
	}
	
	public static Status validete(String name, HttpSession value) {
		HttpSession session = userCache.get(name);
		if (session == null) {
			return Status.NotLoggedIn;
		}
		String ts1 = (String) session.getAttribute(Resources.SESSION_TIME_STAMP);
		String ts2 = (String) value.getAttribute(Resources.SESSION_TIME_STAMP);
		if (ts1.equals(ts2) == false) {
			return Status.LoginOverridden;
		}
		
		return Status.OK;
	}
	
	public static void reset() {
		HttpSession value = userCache.get(ADMIN_USER);
		userCache.clear();
		sessionSet.clear();
		if (value != null) {
			addUser(ADMIN_USER, value);
			sessionSet.add(value);
		}
	}
	
	public static int getTotalNum(){
		return userCache.size();
	}
	
	public static int getTotalModelerNum(){
		int count = 0;
		synchronized(userCache){
			for(HttpSession session : userCache.values()){
				UserInfo userInfo = (UserInfo) session.getAttribute(Resources.SESSION_USER);
				if(userInfo != null && userInfo.getRoleSet() != null){
					if(Arrays.binarySearch(userInfo.getRoleSet(), FixedRoleManageServiceImpl.MODELER_ROLE_IDENTIFIER) >= 0){
						count++;
					}
				}
			}
		}
		return count;
	}
	
	public static class LoginInfo{
		private String loginName;
		
		private Date loginTime;
		
		/**
		 * 
		 */
		public LoginInfo(String loginName, Date loginTime) {
			this.loginName = loginName;
			this.loginTime = loginTime;
		}

		public String getLoginName() {
			return loginName;
		}

		public Date getLoginTime() {
			return loginTime;
		}
	}
}
