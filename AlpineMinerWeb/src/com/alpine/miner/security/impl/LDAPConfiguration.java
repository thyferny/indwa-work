/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * LDAPConfiguration.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 11, 2012
 */
package com.alpine.miner.security.impl;

/**
 * @author sam_zang
 *
 */
public class LDAPConfiguration {
	private static final String DEFAULT_PORT = "389";
	public static String V3 = "LDAP v3";
	public static String V2 = "LDAP v2";
	
	// The following constants are all defined by Microsoft.
	// They can not be changed.
	private static final String AD_ATTR_LOGIN = "sAMAccountName";
	private static final String AD_ATTR_FIRST_NAME = "givenName";
	private static final String AD_ATTR_LAST_NAME = "SN";
	private static final String AD_ATTR_EMAIL = "mail";
	private static final String AD_ATTR_DESCRIPTION = "description";
	private static final String AD_ATTR_PREFIX = "CN=Users,";
	private static final String AD_ATTR_GROUP = "CN";
	private static final String AD_ATTR_ROLE = "physicalDeliveryOfficeName";
	private static final String AD_ATTR_CHORUS_KEY = "personalTitle";
	
	private String host;
	private String port;
	private String ldapVersion;
	private String principal;
	private String credential;
	private String level;
	private String domain;
	
	private String groupDN;
	private String groupNameAddr;
	private String userDN;
	private String userNameAddr;
	private String userPassowrdAddr;
	private String userEmailAddr;
	private String userNotifyAddr;
	private String userFirstNameAddr;
	private String userLastNameAddr;
	private String userDescriptionAddr;
	private String roleAddr;
	private String chorusKey;
	
	LDAPConfiguration() {
		this.ldapVersion = V3;
		this.port = DEFAULT_PORT;
		
		this.host = "localhost";
		this.principal = "cn=manager,dc=maxcrc,dc=com";
		this.credential = "secret";
		this.level = "simple";
		this.groupDN = "ou=groups,dc=example,dc=com";
		this.groupNameAddr = "cn";
		this.userDN = "ou=users,dc=example,dc=com";
		this.userNameAddr = "uid";
		this.userPassowrdAddr = "userPassword";
		this.userEmailAddr = "mail";
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * @return the ldapVersion
	 */
	public String getLdapVersion() {
		return ldapVersion;
	}
	/**
	 * @param ldapVersion the ldapVersion to set
	 */
	public void setLdapVersion(String ldapVersion) {
		this.ldapVersion = ldapVersion;
	}
	/**
	 * @return the principal
	 */
	public String getPrincipal() {
		return principal;
	}
	/**
	 * @param principal the principal to set
	 */
	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	/**
	 * @return the credential
	 */
	public String getCredential() {
		return credential;
	}
	/**
	 * @param credential the credential to set
	 */
	public void setCredential(String credential) {
		this.credential = credential;
	}
	/**
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * @return the groupDN
	 */
	public String getGroupDN() {
		return groupDN;
	}
	/**
	 * @param groupDN the groupDN to set
	 */
	public void setGroupDN(String groupDN) {
		this.groupDN = groupDN;
	}
	
	/**
	 * @return the groupNameAddr
	 */
	public String getGroupNameAddr() {
		return groupNameAddr;
	}
	/**
	 * @param groupNameAddr the groupNameAddr to set
	 */
	public void setGroupNameAddr(String groupNameAddr) {
		this.groupNameAddr = groupNameAddr;
	}
	/**
	 * @return the userDN
	 */
	public String getUserDN() {
		return userDN;
	}
	/**
	 * @param userDN the userDN to set
	 */
	public void setUserDN(String userDN) {
		this.userDN = userDN;
	}
	/**
	 * @return the userNameAddr
	 */
	public String getUserNameAddr() {
		return userNameAddr;
	}
	/**
	 * @param userNameAddr the userNameAddr to set
	 */
	public void setUserNameAddr(String userNameAddr) {
		this.userNameAddr = userNameAddr;
	}
	/**
	 * @return the userPassowrdAddr
	 */
	public String getUserPassowrdAddr() {
		return userPassowrdAddr;
	}
	/**
	 * @param userPassowrdAddr the userPassowrdAddr to set
	 */
	public void setUserPassowrdAddr(String userPassowrdAddr) {
		this.userPassowrdAddr = userPassowrdAddr;
	}
	/**
	 * @return the userEmailAddr
	 */
	public String getUserEmailAddr() {
		return userEmailAddr;
	}
	/**
	 * @param userEmailAddr the userEmailAddr to set
	 */
	public void setUserEmailAddr(String userEmailAddr) {
		this.userEmailAddr = userEmailAddr;
	}

	/**
	 * @return the userNotifyAddr
	 */
	public String getUserNotifyAddr() {
		return userNotifyAddr;
	}

	/**
	 * @param userNotifyAddr the userNotifyAddr to set
	 */
	public void setUserNotifyAddr(String userNotifyAddr) {
		this.userNotifyAddr = userNotifyAddr;
	}

	/**
	 * @return the userFirstNameAddr
	 */
	public String getUserFirstNameAddr() {
		return userFirstNameAddr;
	}

	/**
	 * @param userFirstNameAddr the userFirstNameAddr to set
	 */
	public void setUserFirstNameAddr(String userFirstNameAddr) {
		this.userFirstNameAddr = userFirstNameAddr;
	}

	/**
	 * @return the userLastNameAddr
	 */
	public String getUserLastNameAddr() {
		return userLastNameAddr;
	}

	/**
	 * @param userLastNameAddr the userLastNameAddr to set
	 */
	public void setUserLastNameAddr(String userLastNameAddr) {
		this.userLastNameAddr = userLastNameAddr;
	}

	/**
	 * @return the userDescriptionAddr
	 */
	public String getUserDescriptionAddr() {
		return userDescriptionAddr;
	}

	/**
	 * @param userDescriptionAddr the userDescriptionAddr to set
	 */
	public void setUserDescriptionAddr(String userDescriptionAddr) {
		this.userDescriptionAddr = userDescriptionAddr;
	}

	/**
	 * @return
	 */
	public String getUrl() {
		String url;
		url = "ldap://";
		url += this.host;
		url += ":";
		url += this.port;
		
		return url;
	}

	/**
	 * @return
	 */
	public String getDomain() {
		return this.domain;
	}

	/**
	 * @param string
	 */
	public void setDomain(String domain) {
		this.domain = domain;		
	}

	/**
	 * @return
	 */
	public void setActiveDirectoryDefaults() {

		if (this.getDomain() != null) {
			this.groupDN = AD_ATTR_PREFIX + toDC(this.getDomain());
			this.userDN = AD_ATTR_PREFIX + toDC(this.getDomain());
			if (this.principal != null && this.principal.contains("@") == false) {
				this.principal = this.principal + "@" + this.domain;
			}
		}
		this.groupNameAddr = AD_ATTR_GROUP;
		this.userNameAddr = AD_ATTR_LOGIN;
		this.userFirstNameAddr = AD_ATTR_FIRST_NAME;
		this.userLastNameAddr = AD_ATTR_LAST_NAME;
		this.userDescriptionAddr = AD_ATTR_DESCRIPTION;
		this.userEmailAddr = AD_ATTR_EMAIL;
		this.userNotifyAddr = AD_ATTR_EMAIL;
		this.roleAddr = AD_ATTR_ROLE;
		this.chorusKey = AD_ATTR_CHORUS_KEY;
	}

	private String toDC(String domainName) {
		StringBuilder buf = new StringBuilder();
		for (String token : domainName.split("\\.")) {
			if (token.length() == 0) {
				continue;
			}
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append("DC=").append(token);
		}
		return buf.toString();
	}

	public String getRoleAddr() {
		return roleAddr;
	}

	public void setRoleAddr(String roleAddr) {
		this.roleAddr = roleAddr;
	}

	public String getChorusKey() {
		return chorusKey;
	}

	public void setChorusKey(String chorusKey) {
		this.chorusKey = chorusKey;
	}

}
