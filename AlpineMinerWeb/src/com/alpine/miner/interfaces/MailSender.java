/**
 * ClassName :MailSender.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.interfaces;

import com.alpine.miner.impls.mail.MailConfiguration;
import com.alpine.miner.impls.mail.MailInfo;
import com.alpine.miner.impls.mail.MailSenderImpl;
import com.alpine.miner.impls.mail.SendMailException;


/**
 * @author linan,zhaoyong
 *
 */
 
public interface MailSender{
	public MailSender instance = new MailSenderImpl();

	public void send(final MailInfo info)throws SendMailException;
	
	public void send(MailConfiguration config, MailInfo info)throws SendMailException;
		 
}
