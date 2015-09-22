/**
 * ClassName :MailConfigMgrImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.mail;

import java.util.Map;

import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.MailConfigManager;
import com.alpine.miner.interfaces.MailSender;
import com.alpine.miner.utils.PropertiesEditor;
import org.apache.log4j.Logger;

/**
 * @author linan,zhaoyong
 *
 */
public class MailConfigMgrImpl implements MailConfigManager {
    private static Logger itsLogger = Logger.getLogger(MailConfigMgrImpl.class);

    private static final String CONFIG_PATH = FilePersistence.Preference_PREFIX+"mailConfiguration.properties";
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.mail.config.MailConfigMgr#readConfig()
	 */
	@Override
	public MailConfiguration readConfig() {
		Map conf = null;
		try {
			conf = PropertiesEditor.readProp(CONFIG_PATH);
		} catch ( Exception e) {
			//ignore, because we have the default value
			itsLogger.error(e.getMessage(),e) ;
		}
		return new MailConfiguration(conf);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.impls.mail.config.MailConfigMgr#saveConfig(com.alpine.miner.impls.mail.config.bean.MailConfiguration)
	 */
	@Override
	public void saveConfig(MailConfiguration config) throws Exception {
		PropertiesEditor.storeProp(config.returnProps(), CONFIG_PATH);
		//sync send server config
		((MailSenderImpl)MailSender.instance).setConfig(config);
	}

	public static void main(String[] args) {
		MailConfigMgrImpl handler = new MailConfigMgrImpl();
		MailConfiguration result = handler.readConfig();
		System.out.println(result);
		result.setHost("a");
		result.setFromMail("b");
		result.setSender("c");
		result.setUserName("d");
		result.setPassword("e");
		try {
			handler.saveConfig(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
