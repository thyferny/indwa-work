/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * SecurityConfiguration.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 11, 2012
 */
package com.alpine.miner.security.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sam_zang
 *
 */
public class SecurityConfiguration {
	private static final String AUTHENTICATION_CHOICE = "choice";
	
	private static final String LDAP_HOST = "host";
	private static final String LDAP_PORT = "port";
	private static final String LDAP_PRINCIPAL = "principal";
	private static final String LDAP_PASSWORD = "password";
	private static final String LDAP_VERSION = "version";
	private static final String LDAP_LEVEL = "level";
	private static final String LDAP_GROUP_DN = "groupDN";
	private static final String LDAP_GROUP_ADDR = "groupNameAddr";
	private static final String LDAP_USER_DN = "userDN";
	private static final String LDAP_USER_ADDR = "userNameAddr";
	private static final String LDAP_USER_PADDWORD = "userPassowrdAddr";
	private static final String LDAP_USER_EMAIL = "userEmailAddr";
	private static final String LDAP_USER_Notify = "userNotifyAddr";
	private static final String LDAP_USER_FirstName = "userFirstNameAddr";
	private static final String LDAP_USER_LastName = "userLastNameAddr";
	private static final String LDAP_USER_Description = "userDescriptionAddr";
	private static final String LDAP_USER_ROLE_SET = "roleAddr";
	private static final String LDAP_USER_CHORUS_KEY = "chorusKey";
	
	private static final String CUSTOM_JAR = "custom_jer";
	private static final String CUSTOM_CLASS = "custom_class";

	private static final String AD_HOST = "ad_host";
	private static final String AD_PORT = "ad_port";
	private static final String AD_PRINCIPAL = "ad_principal";
	private static final String AD_PASSWORD = "ad_password";
	private static final String AD_DOMAIN = "ad_domain";


	private String[] choice_list;

	private LDAPConfiguration ldapCfg;
	private LDAPConfiguration adCfg;
	private CustomConfiguration customCfg;
	
	public enum ProviderType {
		LocalProvider,
		LDAPProvider,
		ADProvider,
		CustomProvider
	};
	
	public SecurityConfiguration() {
		this.current_choice = ProviderType.LocalProvider;
		this.ldapCfg = new LDAPConfiguration();
		this.adCfg = new LDAPConfiguration();
		this.customCfg = new CustomConfiguration();
		this.choice_list = new String[4];
		this.choice_list[0] = ProviderType.LocalProvider.name();
		this.choice_list[1] = ProviderType.LDAPProvider.name();
		this.choice_list[2] = ProviderType.ADProvider.name();
		this.choice_list[3] = ProviderType.CustomProvider.name();
	}
	
	public SecurityConfiguration(Map<String,String> props) {
		this.current_choice = ProviderType.LocalProvider;
		this.ldapCfg = new LDAPConfiguration();
		this.adCfg = new LDAPConfiguration();	
		this.customCfg = new CustomConfiguration();
		
		// the following is for java script.
		this.choice_list = new String[4];
		this.choice_list[0] = ProviderType.LocalProvider.name();
		this.choice_list[1] = ProviderType.LDAPProvider.name();
		this.choice_list[2] = ProviderType.ADProvider.name();
		this.choice_list[3] = ProviderType.CustomProvider.name();
		
		if(props == null || props.size() == 0){
			return;
		}
		
		ProviderType c = ProviderType.valueOf(props.get(AUTHENTICATION_CHOICE));
		this.setCurrent_choice(c);
		
		this.ldapCfg.setGroupDN(props.get(LDAP_GROUP_DN));
		this.ldapCfg.setGroupNameAddr(props.get(LDAP_GROUP_ADDR));
		this.ldapCfg.setHost(props.get(LDAP_HOST));
		this.ldapCfg.setCredential(props.get(LDAP_PASSWORD));
		this.ldapCfg.setPort(props.get(LDAP_PORT));
		this.ldapCfg.setPrincipal(props.get(LDAP_PRINCIPAL));
		this.ldapCfg.setLdapVersion(props.get(LDAP_VERSION));
		this.ldapCfg.setLevel(props.get(LDAP_LEVEL));
		this.ldapCfg.setUserDN(props.get(LDAP_USER_DN));
		this.ldapCfg.setUserEmailAddr(props.get(LDAP_USER_EMAIL));
		this.ldapCfg.setUserFirstNameAddr(props.get(LDAP_USER_FirstName));
		this.ldapCfg.setUserLastNameAddr(props.get(LDAP_USER_LastName));
		this.ldapCfg.setUserNotifyAddr(props.get(LDAP_USER_Notify));
		this.ldapCfg.setUserDescriptionAddr(props.get(LDAP_USER_Description));
		this.ldapCfg.setUserNameAddr(props.get(LDAP_USER_ADDR));
		this.ldapCfg.setUserPassowrdAddr(props.get(LDAP_USER_PADDWORD));
		this.ldapCfg.setRoleAddr(props.get(LDAP_USER_ROLE_SET));
		this.ldapCfg.setChorusKey(props.get(LDAP_USER_CHORUS_KEY));
		
		this.customCfg.setClassName(props.get(CUSTOM_CLASS));
		this.customCfg.setJarFile(props.get(CUSTOM_JAR));
		
		this.adCfg.setHost(props.get(AD_HOST));
		this.adCfg.setCredential(props.get(AD_PASSWORD));
		this.adCfg.setPort(props.get(AD_PORT));
		this.adCfg.setPrincipal(props.get(AD_PRINCIPAL));
		this.adCfg.setDomain(props.get(AD_DOMAIN));
		
	}
	
	public Map<String, String> getProperties() {
		Map<String, String> props = new HashMap<String, String>();

		props.put(LDAP_GROUP_DN, this.ldapCfg.getGroupDN());
		props.put(LDAP_GROUP_ADDR, this.ldapCfg.getGroupNameAddr());
		props.put(LDAP_HOST, this.ldapCfg.getHost());
		props.put(LDAP_PASSWORD, this.ldapCfg.getCredential());
		props.put(LDAP_PORT, this.ldapCfg.getPort());
		props.put(LDAP_PRINCIPAL, this.ldapCfg.getPrincipal());
		props.put(LDAP_VERSION, this.ldapCfg.getLdapVersion());
		props.put(LDAP_LEVEL, this.ldapCfg.getLevel());
		props.put(LDAP_USER_DN, this.ldapCfg.getUserDN());
		props.put(LDAP_USER_Description, this.ldapCfg.getUserDescriptionAddr());
		props.put(LDAP_USER_Notify, this.ldapCfg.getUserNotifyAddr());
		props.put(LDAP_USER_FirstName, this.ldapCfg.getUserFirstNameAddr());
		props.put(LDAP_USER_LastName, this.ldapCfg.getUserLastNameAddr());
		props.put(LDAP_USER_EMAIL, this.ldapCfg.getUserEmailAddr());
		props.put(LDAP_USER_ADDR, this.ldapCfg.getUserNameAddr());
		props.put(LDAP_USER_PADDWORD, this.ldapCfg.getUserPassowrdAddr());
		props.put(LDAP_USER_ROLE_SET, this.ldapCfg.getRoleAddr());
		props.put(LDAP_USER_CHORUS_KEY, this.ldapCfg.getChorusKey());
		
		props.put(AD_HOST, this.adCfg.getHost());
		props.put(AD_PORT, this.adCfg.getPort());
		props.put(AD_PRINCIPAL, this.adCfg.getPrincipal());
		props.put(AD_PASSWORD, this.adCfg.getCredential());
		props.put(AD_DOMAIN, this.adCfg.getDomain());
		
		props.put(CUSTOM_CLASS, this.customCfg.getClassName());
		props.put(CUSTOM_JAR, this.customCfg.getJarFile());
		
		props.put(AUTHENTICATION_CHOICE, this.current_choice.name());
		
		return props;
	}

	public Map<String, String> updateProperties(Map<String, String> props,
			SecurityConfiguration cfg) {

		props.put(AUTHENTICATION_CHOICE, cfg.getCurrent_choice().name());

		switch (cfg.getCurrent_choice()) {
		case LDAPProvider:
			props.put(LDAP_GROUP_DN, cfg.getLDAPConfiguration().getGroupDN());
			props.put(LDAP_GROUP_ADDR, cfg.getLDAPConfiguration().getGroupNameAddr());
			props.put(LDAP_HOST, cfg.getLDAPConfiguration().getHost());
			props.put(LDAP_PASSWORD, cfg.getLDAPConfiguration().getCredential());
			props.put(LDAP_PORT, cfg.getLDAPConfiguration().getPort());
			props.put(LDAP_PRINCIPAL, cfg.getLDAPConfiguration().getPrincipal());
			props.put(LDAP_VERSION, cfg.getLDAPConfiguration().getLdapVersion());
			props.put(LDAP_LEVEL, cfg.getLDAPConfiguration().getLevel());
			props.put(LDAP_USER_DN, cfg.getLDAPConfiguration().getUserDN());
			props.put(LDAP_USER_EMAIL, cfg.getLDAPConfiguration().getUserEmailAddr());
			props.put(LDAP_USER_LastName, cfg.getLDAPConfiguration().getUserLastNameAddr());
			props.put(LDAP_USER_Notify, cfg.getLDAPConfiguration().getUserNotifyAddr());
			props.put(LDAP_USER_Description, cfg.getLDAPConfiguration().getUserDescriptionAddr());
			props.put(LDAP_USER_FirstName, cfg.getLDAPConfiguration().getUserFirstNameAddr());
			props.put(LDAP_USER_ADDR, cfg.getLDAPConfiguration().getUserNameAddr());
			props.put(LDAP_USER_PADDWORD, cfg.getLDAPConfiguration().getUserPassowrdAddr());
			props.put(LDAP_USER_ROLE_SET, cfg.getLDAPConfiguration().getRoleAddr());
			props.put(LDAP_USER_CHORUS_KEY, cfg.getLDAPConfiguration().getChorusKey());
			break;
		case CustomProvider:
			props.put(CUSTOM_CLASS, cfg.getCustomConfiguration().getClassName());
			props.put(CUSTOM_JAR, cfg.getCustomConfiguration().getJarFile());
			break;
		case ADProvider:
			props.put(AD_HOST, cfg.getADConfiguration().getHost());
			props.put(AD_PORT, cfg.getADConfiguration().getPort());
			props.put(AD_PRINCIPAL, cfg.getADConfiguration().getPrincipal());
			props.put(AD_PASSWORD, cfg.getADConfiguration().getCredential());
			props.put(AD_DOMAIN, cfg.getADConfiguration().getDomain());
			break;
		}

		return props;
	}

	private ProviderType current_choice;
	/**
	 * @return the current_choice
	 */
	public ProviderType getCurrent_choice() {
		return current_choice;
	}

	/**
	 * @param current_choice the current_choice to set
	 */
	public void setCurrent_choice(ProviderType current_choice) {
		this.current_choice = current_choice;
	}

	/**
	 * @return
	 */
	public CustomConfiguration getCustomConfiguration() {
		return this.customCfg;
	}

	/**
	 * @return
	 */
	public LDAPConfiguration getLDAPConfiguration() {
		return this.ldapCfg;
	}
	
	/**
	 * @return
	 */
	public LDAPConfiguration getADConfiguration() {
		this.adCfg.setActiveDirectoryDefaults();
		return this.adCfg;
	}
}
