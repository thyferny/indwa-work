/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * CustomProvider.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security.impl;

import com.alpine.miner.security.AuthenticationProvider;
import org.apache.log4j.Logger;

/**
 * @author sam_zang
 *
 */
public class CustomProvider {
    private static Logger itsLogger = Logger.getLogger(CustomProvider.class);
    private static AuthenticationProvider instance = null;

	/**
	 * @return a singleton instance of Local Provider
	 */
	public static synchronized AuthenticationProvider getInstance() {
		if (instance != null) {
			return instance;
		}
		SecurityConfiguration cfg = ProviderFactory.loadConfiguration();
		instance = initializeInstance(cfg);
		return instance;
	}

	/**
	 * @param cfg 
	 * @return
	 */
	private static AuthenticationProvider initializeInstance(SecurityConfiguration cfg) {
		// get the custom configuration and create instance
		// using reflection.
		
		String clsName = cfg.getCustomConfiguration().getClassName();
		try {
			Class cls = Class.forName(clsName);
			Object obj = cls.newInstance();
			if (obj instanceof AuthenticationProvider) {
				return (AuthenticationProvider) obj;
			}
			else {
				return null;
			}
		} catch (ClassNotFoundException e) {
			itsLogger.error(e.getMessage(),e);
		} catch (InstantiationException e) {
			itsLogger.error(e.getMessage(),e);
		} catch (IllegalAccessException e) {
			itsLogger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @param cfg
	 * @return
	 */
	public static AuthenticationProvider getTestInstance(
			SecurityConfiguration cfg) {
		AuthenticationProvider obj = initializeInstance(cfg);
		return obj;
	}
}
