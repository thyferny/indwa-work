/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ProviderFactory.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security.impl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.alpine.miner.error.ErrorNLS;
import com.alpine.miner.impls.controller.SecurityTestDTO;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.LoginManager;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.utils.PropertiesEditor;
import org.apache.log4j.Logger;

/**
 * @author sam_zang
 * 
 */
public class ProviderFactory {
    private static Logger itsLogger = Logger.getLogger(ProviderFactory.class);
    private static final String CONFIG_PATH = FilePersistence.Preference_PREFIX
			+ "security.properties";
	private static SecurityConfiguration cached_cfg = null;

	public static synchronized AuthenticationProvider getAuthenticator(String user) {
		AuthenticationProvider auth = LocalProvider.getInstance();
		
		//
		// admin user will use local provider to login.
		// else, when provider changes, admin user
		// will no longer be able to login to change
		// the provider back.
		//
		if (LoginManager.ADMIN_USER.equals(user)) {
			return auth;
		}
		if (cached_cfg == null) {
			cached_cfg = readConfig();
		}
		
		switch (cached_cfg.getCurrent_choice()) {
		case LDAPProvider:
			auth = LDAPProvider.getInstance();
			break;
		case ADProvider:
			auth = ADProvider.getInstance();
			break;
		case CustomProvider:
			auth = CustomProvider.getInstance();
			break;
		}
		if (auth == null) {
			auth = LocalProvider.getInstance();
		}
		return auth;
	}

	/**
	 * @return
	 */
	public static synchronized SecurityConfiguration loadConfiguration() {
		if (cached_cfg == null) {
			cached_cfg = readConfig();
		}
		return cached_cfg;
	}

	/**
	 * @param cfg
	 */
	public static synchronized void saveConfiguration(SecurityConfiguration cfg) {
		try {
			if (cfg.getCurrent_choice() != cached_cfg.getCurrent_choice()) {
				// provider type has changed.
				// reset providers
				LoginManager.reset();
			}
			saveConfig(cfg);
			switch(cfg.getCurrent_choice()){
			case LDAPProvider: 
				LDAPProvider.reset(cfg);
				break;
			case ADProvider:
				ADProvider.reset(cfg);
			}
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		}
	}

	/**
	 * @param cfg
	 * @param locale 
	 */
	public static synchronized SecurityTestDTO testConfiguration(SecurityConfiguration cfg, Locale locale) {
		AuthenticationProvider auth = null;
		SecurityTestDTO ret = new SecurityTestDTO();
		switch (cfg.getCurrent_choice()) {
		case LDAPProvider:
			auth = LDAPProvider.getTestInstance(cfg);
			if (auth == null) {
				ret.setConnection(false);
				ret.setMessage(ErrorNLS.getMessage("test_LDAP_authentication_fail", locale));
			}
			break;
		case ADProvider:
			auth = ADProvider.getTestInstance(cfg);
			if (auth == null) {
				ret.setConnection(false);
				ret.setMessage(ErrorNLS.getMessage("test_LDAP_authentication_fail", locale));
			}
			break;
		case CustomProvider:
			auth = CustomProvider.getTestInstance(cfg);
			if (auth == null) {
				ret.setConnection(false);
				ret.setMessage(ErrorNLS.getMessage("test_custom_authentication_fail", locale));
			}
			break;
		case LocalProvider:
			ret.setConnection(true);
			ret.setMessage(ErrorNLS.getMessage("test_local_authentication", locale));
			return ret;
		}
		
		if (auth != null) {
			// test both connection and also provider
			// will get useful data.
			List users = auth.getUserInfoList();
			List groups = auth.getGroupInfoList();
			ret.setUserCount(users.size());
			ret.setGroupCount(groups.size());
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private static SecurityConfiguration readConfig() {
		Map<String, String> conf = null;
		try {
			conf = PropertiesEditor.readProp(CONFIG_PATH);
		} catch (Exception e) {
			// ignore, use the default value
			itsLogger.error(e.getMessage(),e);
		}
		return new SecurityConfiguration(conf);
	}

	private static void saveConfig(SecurityConfiguration config) throws Exception {
		Map<String, String> props = cached_cfg.getProperties();
		props = cached_cfg.updateProperties(props, config);
		PropertiesEditor.storeProp(props, CONFIG_PATH);
		cached_cfg = new SecurityConfiguration(props);
	}

}
