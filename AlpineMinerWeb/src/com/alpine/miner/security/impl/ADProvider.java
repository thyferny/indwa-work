/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ADProvider.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Feb 21, 2012
 */
package com.alpine.miner.security.impl;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import com.alpine.miner.security.AuthenticationProvider;
import org.apache.log4j.Logger;

/**
 * @author sam_zang
 *
 */
public class ADProvider extends LDAPProvider {
    private static Logger itsLogger = Logger.getLogger(ADProvider.class);
    private static final String LOGIN_FILTER = "(&(objectclass=person)(sAmAccountName=";
	private static final String LOGIN_FILTER_END = "))";
	private static final String DOMAIN_SEPARATOR = "@";

	private static LDAPProvider instance = new ADProvider();

	/**
	 * @return a singleton instance of AD Provider
	 */
	public static AuthenticationProvider getInstance() {
		return instance;
	}
	
	protected ADProvider() {
		this(ProviderFactory.loadConfiguration());
	}	
	
	/**
	 * @param cfg
	 */
	public ADProvider(SecurityConfiguration cfg) {
		this.lcfg = cfg.getADConfiguration();
		this.ctx = getContext(this.lcfg);
	}

	public static void reset(SecurityConfiguration cfg) {
		instance = new ADProvider(cfg);
	}

	protected DirContext getContext() {
//		if (ctx != null) {
//			return ctx;
//		}

		if(lcfg == null){
			SecurityConfiguration cfg = ProviderFactory.loadConfiguration();
			lcfg = cfg.getADConfiguration();
		}
		ctx = getContext(lcfg);
		if(ctx == null){
			throw new RuntimeException("Cannot connect Active Directory Server.");
		}
		return ctx;
	}
	
	public static AuthenticationProvider getTestInstance(
			SecurityConfiguration cfg) {
		ADProvider auth = new ADProvider(cfg);
		if (auth.ctx == null) {
			return null;
		}
		return auth;
	}
	
	/**
	 * @param user
	 * @return
	 */
	protected String getPrincipal(String user) {
		if (user != null && user.contains(DOMAIN_SEPARATOR)) {
			return user;
		}
		
		return user + DOMAIN_SEPARATOR + lcfg.getDomain();
	}
	

	
	protected String getUserDN(String login) {
		String dn = "";
		String[] ratts = {lcfg.getUserNameAddr()};
		String filter = LOGIN_FILTER + login + LOGIN_FILTER_END;
		NamingEnumeration result = null;
		
		try {
			result = search(lcfg.getUserDN(), filter, ratts);
			while (result.hasMore()) {
			    SearchResult sr = (SearchResult)result.next();
			    dn = sr.getNameInNamespace();
			    break;
			}
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
		}

		return dn;
	}

	@Override
	protected String getLoginName(SearchResult sr) {
		Attributes attrs = sr.getAttributes();
		try {
			return attrs.get(this.lcfg.getUserNameAddr()).get().toString();
		} catch (NamingException e) {
			itsLogger.error(e.getMessage(),e);
			return "";
		}
	}
}
