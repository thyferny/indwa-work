/**
 * ClassName :MailSenderImpl.java
 *
 * Version information: 3.0
 *
 * Data: 2011-8-31
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.mail;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.LogFactory;

import com.alpine.miner.interfaces.MailConfigManager;
import com.alpine.miner.interfaces.MailSender;
import org.apache.log4j.Logger;


/**
 * @author linan
 *http://javamail.kenai.com/nonav/javadocs/com/sun/mail/smtp/package-summary.html
 */
 
public class MailSenderImpl implements MailSender{
    private static Logger itsLogger = Logger.getLogger(MailSenderImpl.class);
    private MailConfiguration config = null;
	public MailConfiguration getConfig() {
		return config;
	}
	
	public MailSenderImpl(){
		config= MailConfigManager.INSTANCE.readConfig();
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.interfaces.MailSender#send(com.alpine.miner.impls.mail.MailInfo)
	 */
	@Override
	public void send(MailInfo info) throws SendMailException {
		send(config,info);
	}

	public void send(final MailConfiguration config, MailInfo info) throws SendMailException{
	
		boolean configValidate = true;
		if(config.getFromMail() == null || "".equals(config.getFromMail())){
			itsLogger.warn("From mail address is null in Mail Configuration.");
			configValidate = false;
		}
		if(config.getHost() == null || "".equals(config.getHost())){
			itsLogger.warn("Host is null in Mail Configuration.");
			configValidate = false;
		}
		if(config.getPassword() == null || "".equals(config.getPassword())){
			itsLogger.warn("Password of Mail account is null in Mail Configuration.");
			configValidate = false;
		}
		if(config.getPort() == null || "".equals(config.getPort())){
			itsLogger.warn("Port of Mail Server is null in Mail Configuration.");
			configValidate = false;
		}
		if(config.getUserName() == null || "".equals(config.getUserName())){
			itsLogger.warn("Login Id is null in Mail Configuration.");
			configValidate = false;
		}
		if(info.getReceivers().length < 1){
			itsLogger.warn("found any Receiver.");
			configValidate = false;
		}
		if(!configValidate){
			return;
		}
		
		Properties props = new Properties();
	    Authenticator auth = new Authenticator(){
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(config.getUserName(), config.getPassword());
			}
	    	
	    };
	    props.put("mail.smtp.host", config.getHost());
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.port", config.getPort());
	    if(config.isUseSSL()){
	    	//if use SSL protocol then enable ssl and use SSL protocol
	    	//if use TLS protocol then only need enable starttls, nothing anymore.
	    	if(MailConfiguration.PROTOCOL_SSL.equals(config.getSslProtocols())){
	        	props.put("mail.smtp.ssl.enable", config.isUseSSL());
//	    		props.put("mail.smtp.ssl.protocols", config.getSslProtocols());
	    	}else{
	            props.put("mail.smtp.starttls.enable","true");
	    	}
	    }
//    	props.put("mail.debug", "true");
	    Session session = Session.getInstance(props, auth);
	    
	    Message message = new MimeMessage(session);
	    try {
			message.setSubject(info.getSubject());
		    message.setText(info.getContent());
		    message.setSentDate(info.getSendTime());
		    Address sendAddress = new InternetAddress(config.getFromMail(),config.getSender());
		    message.setFrom(sendAddress);
		    message.addRecipients(Message.RecipientType.TO, info.getReceivers());
		    Transport.send(message); 
		} catch (Exception e) {
			throw new SendMailException(e);
		}
	    LogFactory.getLog(MailSender.class).info("the mail of Subject is " + info.getSubject() + " was success.");
	}
	
//	private static void from163(){
//		MailInfo info = new MailInfo();
//		info.setContent("Hello, this mail is testing for make sure java mail is working.");
//		
//		info.setSubject("the auto send Testing Mail");
//		info.addReceiver("gli@alpinedatalabs.com");
//		try {
//			MailSenderImpl sender= new MailSenderImpl(); 
//			MailConfiguration mailconfig = new MailConfiguration();
//			mailconfig.setFromMail("javamailtesting@163.com");
//			mailconfig.setHost("smtp.163.com");
//			mailconfig.setUserName("javamailtesting");
//			mailconfig.setPassword("passw0rd");
//			mailconfig.setSender("Gary");
//			mailconfig.setPort("25");
//			sender .send(mailconfig, info);
//		} catch (SendMailException e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	public void setConfig(MailConfiguration conf) {
		this.config=conf;
		
	}
}
