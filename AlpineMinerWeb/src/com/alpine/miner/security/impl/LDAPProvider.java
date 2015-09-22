/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * LocalProvider.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.alpine.miner.impls.web.resource.LoginManager;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.OperationNotAllowedException;
import com.alpine.miner.security.UserInfo;
import com.alpine.utility.file.StringUtil;

import org.apache.log4j.Logger;

/**
 * @author sam_zang
 *
 */
public class LDAPProvider implements AuthenticationProvider {
    private static Logger itsLogger = Logger.getLogger(LDAPProvider.class);
    private static final String CN = "cn";
	private static final String SN = "sn";
//	private static final String USER_FILTER = "(objectclass=person)";

	private static final String MEMBER = "member";
	private static final String UNI_MEMBER = "uniqueMember";
//	private static final String GROUP_FILTER = 
//		"(|(objectclass=groupOfNames)(objectclass=groupOfUniqueNames)(objectclass=posixGroup)(objectclass=group))";
	private static final String DEFAULT_ATTR = "objectClass";
	private static final String DEFAULT_AUTH_METHOD = "simple";
	private static final String DEFAULT_AUTH_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String FALSE = "false";
	private static final String NO = "no";
	private static final String DOMAIN_SEPARATOR = "@";

	private static LDAPProvider instance = new LDAPProvider();

	/**
	 * @return a singleton instance of Local Provider
	 */
	public static AuthenticationProvider getInstance() {
		return instance;
	}

	protected LDAPProvider() {
		this(ProviderFactory.loadConfiguration());
	}	
	
	protected LDAPProvider(SecurityConfiguration cfg) {
		this.ctx = getContext(cfg.getLDAPConfiguration());
		this.lcfg = cfg.getLDAPConfiguration();
	}

	public static void reset(SecurityConfiguration cfg) {
		instance = new LDAPProvider(cfg);
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#authenticate
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticate(String user, String password) {
		String dn = getPrincipal(user);
		Hashtable<String, String> env = new Hashtable<String, String>(11);
		env.put(Context.SECURITY_AUTHENTICATION, lcfg.getLevel());
		env.put(Context.SECURITY_PRINCIPAL, dn); // User
		env.put(Context.SECURITY_CREDENTIALS, password); // Password
		env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_AUTH_FACTORY);
		env.put(Context.PROVIDER_URL, lcfg.getUrl());

		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			return true;
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			return false;
		}		
	}

	/**
	 * @param user
	 * @return
	 */
	protected String getPrincipal(String user) {
		return getUserDN(user);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserInfoList()
	 */
	@Override
	public List<UserInfo> getUserInfoList() {
		String[] ratts = {lcfg.getUserNameAddr(), MEMBER, UNI_MEMBER};
		NamingEnumeration result = null;		
		List<UserInfo> userList = new LinkedList<UserInfo>();
		
		try {
			result = search(lcfg.getUserDN(), lcfg.getUserNameAddr() + "=*", ratts);
			while (result.hasMore()) {
			    SearchResult sr = (SearchResult)result.next();
			    String name = getLoginName(sr);	
			    UserInfo u = getUserInfoByName(name);
			    if (u != null) {
			    	userList.add(u);
			    }
			}
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			// will get empty list
		}
		return userList;
	}
	
	protected String getLoginName(SearchResult sr){
		return getSimpleName(sr.getName());
	}


	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getGroupInfoList()
	 */
	@Override
	public List<GroupInfo> getGroupInfoList() {
		String[] ratts = {lcfg.getGroupNameAddr(), MEMBER, UNI_MEMBER};
		NamingEnumeration result = null;		
		List<GroupInfo> groupList = new LinkedList<GroupInfo>();
		
		try {
			result = search(lcfg.getGroupDN(), lcfg.getGroupNameAddr() + "=*", ratts);
			while (result.hasMore()) {
			    SearchResult sr = (SearchResult)result.next();
			    String group = getSimpleName(sr.getName());
			    groupList.add(new GroupInfo(group));
			}
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			// will get empty list
		}
		return groupList;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserInfoByGroup
	 * (java.lang.String)
	 */
	@Override
	public List<UserInfo> getUserInfoByGroup(String group) {
		String dn = getGroupDN(group);
		List<UserInfo> userList = new LinkedList<UserInfo>();

		String[] ratts = new String[] { 
				lcfg.getGroupNameAddr(),
				MEMBER, 
				UNI_MEMBER };

		try {
			Attributes atts = read(dn, ratts);
			if (atts != null) {
				for (Enumeration e = atts.getAll(); e.hasMoreElements();) {
					BasicAttribute item = (BasicAttribute) e.nextElement();
					if (item != null) {
						if (item.getID().equalsIgnoreCase(MEMBER) == false &&
								item.getID().equalsIgnoreCase(UNI_MEMBER) == false) {
							continue;
						}
						for (Enumeration m = item.getAll(); m.hasMoreElements();) {
							String name = getSimpleName(m.nextElement().toString());
							UserInfo u = getUserInfoByName(name);
							if (u != null) {
								userList.add(u);
							}
						}
					}
				}
			}
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			// will get empty list
		}
		return userList;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserInfoByName
	 * (java.lang.String)
	 */
	@Override
	public UserInfo getUserInfoByName(String user) {
		getContext();
		String dn = getUserDN(user);
		
		String[] ratts = new String[] {
				lcfg.getUserNameAddr(),
				lcfg.getUserFirstNameAddr(),
				lcfg.getUserLastNameAddr(),
				lcfg.getUserNotifyAddr(),
				lcfg.getUserDescriptionAddr(),
				lcfg.getUserPassowrdAddr(), 
				lcfg.getUserEmailAddr(),
				lcfg.getRoleAddr(),
				lcfg.getChorusKey()
		};

		UserInfo info = null;
		try {
			Attributes atts = read(dn, ratts);
			if (atts == null) {
				return null;
			}
			Attribute a = atts.get(lcfg.getUserNameAddr());
			if (a == null || user.equals((String) a.get()) == false) {
				return null;
			}
			info = new UserInfo();
			info.setLogin(user);
			info.setNotification(false);
			a = atts.get(lcfg.getUserNameAddr());
			if (a != null) {
				info.setLogin((String) a.get());
			}
			a = atts.get(lcfg.getUserEmailAddr());
			if (a != null) {
				info.setEmail((String) a.get());
			}
			a = atts.get(lcfg.getUserFirstNameAddr());
			if (a != null) {
				info.setFirstName((String) a.get());
			}
			a = atts.get(lcfg.getUserLastNameAddr());
			if (a != null) {
				info.setLastName((String) a.get());
			}
			a = atts.get(lcfg.getUserDescriptionAddr());
			if (a != null) {
				info.setDescription((String) a.get());
			}
			a = atts.get(lcfg.getUserNotifyAddr());
			if (a != null) {
				info.setNotification(isNotify((String) a.get()));
			}
			a = atts.get(lcfg.getRoleAddr());
			if(a != null){
				info.setRoleSet(a.get().toString().split(","));
			}
			a = atts.get(lcfg.getChorusKey());
			if(a != null){
				info.setChorusKey((String) a.get());
			}
			
			String[] groups = getUserGroups(user);
			if(groups!=null){
				info.setGroups(groups) ;
			}
			return info;
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			return null;
		}
	}

	/**
	 * @param value
	 * @return
	 */
	protected boolean isNotify(String value) {
		if (value == null || value.length() == 0 
				|| value.equalsIgnoreCase(NO) || value.equalsIgnoreCase(FALSE)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserGroups
	 * (java.lang.String)
	 * 
	 * ldapsearch --hostname localhost --port 389 
	 * --bindDN "cn=Directory Manager" \
	 * --bindPassword password --baseDN dc=example,dc=com 
	 * "(uid=user)" isMemberOf	 
	 */
	@Override
	public String[] getUserGroups(String user) {
		if (user != null && user.equals(LoginManager.ADMIN_USER)) {
			return getAllGroups();
		}
		String[] ratts = {lcfg.getGroupNameAddr(), MEMBER, UNI_MEMBER};
		NamingEnumeration result = null;		
		List<String> groupList = new LinkedList<String>();
		
		try {
			result = search(lcfg.getGroupDN(), lcfg.getGroupNameAddr() + "=*", ratts);
			while (result.hasMore()) {
			    SearchResult sr = (SearchResult)result.next();
			    String group = getSimpleName(sr.getName());
			   
			    Attribute att = sr.getAttributes().get(MEMBER);
				if (att != null && userInGroup(att, user)) {
					groupList.add(group);
				}
				else {
					att = sr.getAttributes().get(UNI_MEMBER);
					if (att != null && userInGroup(att, user)) {
						groupList.add(group);
					}
				}
			}
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			return new String[0];
		}
		return groupList.toArray(new String[groupList.size()]);
	}

	/**
	 * @return
	 */
	protected String[] getAllGroups() {
		List<GroupInfo> list = getGroupInfoList();
		String[] ret = new String[list.size()];
		int idx = 0;
		for (GroupInfo g : list) {
			ret[idx++] = g.getId();
		}
		return ret;
	}

	protected boolean userInGroup(Attribute att, String user) throws NamingException {
		String user_name = getSimpleName(getUserDN(user));
		if (att != null) {
			for (Enumeration e = att.getAll(); e.hasMoreElements();) {
				String item = (String) e.nextElement();
				if (item != null) {
					String name = getSimpleName(item);
					if (name.equals(user_name)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#createGroupInfo(com.alpine.miner.security.GroupInfo)
	 */
	@Override
	public void createGroup(GroupInfo info)
			throws OperationNotAllowedException {
		Attributes atts = groupInfo2Attr(info);
		try {
			addEntry(getGroupDN(info), atts);
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		}		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#updateGroupInfo(com.alpine.miner.security.GroupInfo)
	 */
	@Override
	public void updateGroup(GroupInfo info)
			throws OperationNotAllowedException {
		Attributes atts = groupInfo2Attr(info);
		try {
			updateEntry(getGroupDN(info), atts);
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		}	
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#deleteGroupInfo(com.alpine.miner.security.GroupInfo)
	 */
	@Override
	public void deleteGroup(GroupInfo info)
			throws OperationNotAllowedException {
		try {
			deleteEntry(getGroupDN(info));
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		}	
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#createUserInfo(com.alpine.miner.security.UserInfo)
	 */
	@Override
	public void createUser(UserInfo info)
			throws OperationNotAllowedException {
		Attributes atts = userInfo2Attr(info);
		try {
			addEntry(getUserDN(info), atts);
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		}		
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#updateUserInfo(com.alpine.miner.security.UserInfo)
	 */
	@Override
	public void updateUser(UserInfo info)
			throws OperationNotAllowedException {
		Attributes atts = userInfo2AttrUpdate(info);
		try {
			updateEntry(getUserDN(info), atts);
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		}	
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#deleteUserInfo(com.alpine.miner.security.UserInfo)
	 */
	@Override
	public void deleteUser(UserInfo info)
			throws OperationNotAllowedException {
		try {
			deleteEntry(getUserDN(info));
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		}	
	}
	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserRoleSet(java.lang.String)
	 */
	@Override
	public String[] getUserRoleSet(String user) {
		UserInfo userInfo = getUserInfoByName(user);
		return userInfo == null ? null : userInfo.getRoleSet();
	}

	/**
	 * @param info
	 * @return
	 */
	protected String getUserDN(UserInfo info) {
		return getUserDN(info.getLogin());
	}

	protected String getUserDN(String login) {
		String dn = lcfg.getUserNameAddr() + "="
			+ login + "," + lcfg.getUserDN();
		return dn;
	}
	
	protected Attributes userInfo2AttrUpdate(UserInfo info) {
		String dn = getUserDN(info);
		
		String[] ratts = new String[] {
				lcfg.getUserNameAddr(),
				lcfg.getUserFirstNameAddr(),
				lcfg.getUserLastNameAddr(),
				lcfg.getUserNotifyAddr(),
				lcfg.getUserDescriptionAddr(),
				lcfg.getUserPassowrdAddr(), 
				lcfg.getUserEmailAddr(), 
				lcfg.getRoleAddr(),
				lcfg.getChorusKey()
		};

		Attributes atts = new BasicAttributes();
		try {
			Attributes list = read(dn, ratts);
			if (list == null) {
				return atts;
			}

			if (list.get(lcfg.getUserNameAddr()) != null) {
				atts.put(lcfg.getUserNameAddr(), info.getLogin());
			}
			if (list.get(lcfg.getUserPassowrdAddr()) != null) {
				atts.put(lcfg.getUserPassowrdAddr(), info.getPassword());
			}
			if (list.get(lcfg.getUserEmailAddr()) != null) {
				atts.put(lcfg.getUserEmailAddr(), info.getEmail());
			}
			if (list.get(lcfg.getUserFirstNameAddr()) != null) {
				atts.put(lcfg.getUserFirstNameAddr(), info.getFirstName());
			}
			if (list.get(lcfg.getUserLastNameAddr()) != null) {
				atts.put(lcfg.getUserLastNameAddr(), info.getLastName());
			}
			if (list.get(lcfg.getUserDescriptionAddr()) != null) {
				atts.put(lcfg.getUserDescriptionAddr(), info.getDescription());
			}
			if (list.get(lcfg.getUserNotifyAddr()) != null) {
				atts.put(lcfg.getUserNotifyAddr(), info.getNotification());
			}
			if(list.get(lcfg.getUserEmailAddr()) != null){
				atts.put(lcfg.getUserEmailAddr(), info.getEmail());
			}
			if(list.get(lcfg.getChorusKey()) != null && !StringUtil.isEmpty(info.getChorusKey())){
				atts.put(lcfg.getChorusKey(), info.getChorusKey());
			}

		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
		}
		return atts;
	}
	
	protected Attributes userInfo2Attr(UserInfo info) {

		Attributes atts = new BasicAttributes();
		if (CN.equalsIgnoreCase(lcfg.getUserNameAddr())) {
			atts.put(CN, info.getLogin());
		} 
		else {
			atts.put(CN, info.getFirstName());
			atts.put(lcfg.getUserNameAddr(), info.getLogin());
		}
		
		atts.put(SN, info.getLastName());
		atts.put(lcfg.getUserPassowrdAddr(), info.getPassword());
		atts.put(lcfg.getUserEmailAddr(), info.getEmail());
		if(!StringUtil.isEmpty(info.getChorusKey())){
			atts.put(lcfg.getChorusKey(), info.getChorusKey());
		}
		
		return atts;
	}
	
	/**
	 * @param info
	 * @return
	 */
	protected String getGroupDN(GroupInfo info) {
		return getGroupDN(info.getId());
	}

	protected String getGroupDN(String group) {
		String dn = lcfg.getUserNameAddr() + "="
			+ group + "," + lcfg.getGroupDN();
		return dn;
	}
	
	protected Attributes groupInfo2Attr(GroupInfo info) {
		Attributes atts = new BasicAttributes();
		atts.put(lcfg.getGroupNameAddr(), info.getId());
		
		return atts;
	}
	
	/**
	 * @param cfg
	 * @return
	 */
	public static AuthenticationProvider getTestInstance(
			SecurityConfiguration cfg) {
		LDAPProvider auth = new LDAPProvider(cfg);
		if (auth.ctx == null) {
			return null;
		}
		return auth;
	}

	protected DirContext getContext() {

//		if (ctx != null) {
//			return ctx;
//		}
		if(lcfg == null){
			SecurityConfiguration cfg = ProviderFactory.loadConfiguration();
			lcfg = cfg.getLDAPConfiguration();
		}
		ctx = getContext(lcfg);
		if(ctx == null){
			throw new RuntimeException("Cannot connect LDAP Server.");
		}
		return ctx;
	}
	
	protected DirContext getContext(LDAPConfiguration lcfg) {

		Hashtable<String, String> env = new Hashtable<String, String>(11);
		env.put(Context.SECURITY_AUTHENTICATION, DEFAULT_AUTH_METHOD);
		env.put(Context.SECURITY_PRINCIPAL, lcfg.getPrincipal());
		env.put(Context.SECURITY_CREDENTIALS, lcfg.getCredential());
		env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_AUTH_FACTORY);				
		env.put(Context.PROVIDER_URL, lcfg.getUrl());

		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
		}

		return ctx;
	}

	private void addEntry(String dn, Attributes atts) throws NamingException {
		getContext().createSubcontext(dn, atts);
	}
	 
	private void deleteEntry(String dn) throws NamingException {
		getContext().destroySubcontext(dn);
	}
    
	private void updateEntry(String dn, Attributes atts) throws NamingException {
		getContext().modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, atts);
	}
	
	private Attributes read(String dn, String[] retAtts)
			throws NamingException {
		return getContext().getAttributes(dn, retAtts);
	}
	
	protected NamingEnumeration search(String dn,
			String filter,
			String[] retAtts)
			throws NamingException {
		NamingEnumeration result = null;

		if (retAtts == null || retAtts.length == 0) {
			retAtts = new String[] {DEFAULT_ATTR};
		}

		/* specify search constraints to search subtree */
		SearchControls constraints = new SearchControls();

		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		constraints.setCountLimit(0);
		constraints.setTimeLimit(0);
		constraints.setReturningAttributes(retAtts);
		result = getContext().search(dn, filter, constraints);

		return result;
	}
	
	// get the user or group name out of 
	// "cn=Group,OU=Distribution Lists,DC=DOMAIN,DC=com"
	protected String getSimpleName(String cnName) {
		if (cnName != null && cnName.toUpperCase().startsWith("CN=")) {
			cnName = cnName.substring(3);
		}
		if (cnName != null && cnName.toUpperCase().startsWith("UID=")) {
			cnName = cnName.substring(4);
		}
		int position = cnName.indexOf(',');
		if (position == -1) {
			return cnName;
		} else {
			return cnName.substring(0, position);
		}
	}
	
	protected DirContext ctx;
	protected LDAPConfiguration lcfg;
	private SearchControls existOpt;
	{
        existOpt = new SearchControls();
        existOpt.setSearchScope(SearchControls.OBJECT_SCOPE);
        existOpt.setCountLimit(0);
        existOpt.setTimeLimit(0);
        existOpt.setReturningAttributes(new String[]{"1.1"});
    }
}
