/**
 * ClassName :MailConfigManager.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.interfaces;

import com.alpine.miner.impls.mail.MailConfigMgrImpl;
import com.alpine.miner.impls.mail.MailConfiguration;

/**
 * @author linan,zhaoyong
 *
 */
 
public interface MailConfigManager {
	public static MailConfigManager INSTANCE= new MailConfigMgrImpl();

	MailConfiguration readConfig();
	
	void saveConfig(MailConfiguration config) throws Exception;
}
