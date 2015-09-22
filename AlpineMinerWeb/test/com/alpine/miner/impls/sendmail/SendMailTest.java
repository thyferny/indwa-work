/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * SendMailTest
 * May 16, 2012
 */
package com.alpine.miner.impls.sendmail;

import org.junit.Test;

import com.alpine.miner.impls.mail.MailConfiguration;
import com.alpine.miner.impls.mail.MailInfo;
import com.alpine.miner.impls.mail.SendMailException;
import com.alpine.miner.interfaces.MailSender;

/**
 * @author Gary
 *
 */
public class SendMailTest {

	@Test
	public void testSendMail(){
		MailInfo info = new MailInfo();
		info.setSubject("Unit test for send Mail");
		info.setContent("This is a testing mail for unit test from Alpine data labs");
		info.addReceiver("gli@alpinedatalabs.com");
		try {
			MailSender.instance.send(buildConfig(), info);
		} catch (SendMailException e) {
			e.printStackTrace();
		}
	}
	
	private MailConfiguration buildConfig(){
		MailConfiguration config = new MailConfiguration();
		config.setHost("smtp.163.com");
		config.setPort("25");
		config.setSslProtocols("TLS");
		config.setUseSSL(false);
		config.setFromMail("javamailtesting@163.com");
		config.setUserName("javamailtesting");
		config.setPassword("passw0rd");
		config.setSender("Alpine data Labs System");
		return config;
	}
}
