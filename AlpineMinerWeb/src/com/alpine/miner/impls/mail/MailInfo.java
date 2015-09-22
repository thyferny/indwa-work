/**
 * ClassName :MailConfiguration.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.LogFactory;


/**
 * @author linan,zhaoyong
 *
 */
 
public class MailInfo {
 
	private List<Address> receivers = new ArrayList<Address>();
	
	private String 	subject,
					content;
	private Date sendTime;
	
 
	
	public MailInfo(List<Address> receivers, String subject, String content,
			Date sendTime) {
 
		this.receivers = receivers;
		this.subject = subject;
		this.content = content;
		this.sendTime = sendTime;
	}

	public MailInfo() {
		this.sendTime = new Date();
	}

	/**
	 * @return the receivers
	 */
	public Address[] getReceivers() {
		return receivers.toArray(new Address[receivers.size()]);
	}
	
	public void addReceiver(String receiver) {
		try {
			this.receivers.add(new InternetAddress(receiver));
		} catch (AddressException e) {
			LogFactory.getLog(this.getClass()).warn(e);
		}
	}
	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the sendTime
	 */
	public Date getSendTime() {
		return sendTime;
	}
	/**
	 * @param sendTime the sendTime to set
	 */
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	 			
}
