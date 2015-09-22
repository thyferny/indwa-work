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

import java.util.HashMap;
import java.util.Map;

import com.alpine.miner.impls.resource.ResourceInfo;

/**
 * @author linan,zhaoyong
 *
 */
public class MailConfiguration extends ResourceInfo {
	public static final String 	PROTOCOL_SSL = "SSL",
								PROTOCOL_TLS = "TLS";
	
	private static final String HOST_FIELD = "host", 
								FROM_MAIL_FIELD = "fromMail",
								SENDER_FIELD = "sender",
								USER_NAME_FIELD = "userName",
								PASSWORD_FIELD = "password",
								PORT = "port",
								BY_SSL = "useSSL",
								PROTOCOLS = "sslProtocols";
							

	private String 	host ="",
					fromMail="",
					sender="",
					userName="",
					password="",
					port = "";
	private boolean useSSL;
	private String sslProtocols;

	public MailConfiguration(){
		
	}
	
	public MailConfiguration(Map<String,String> props){
		if(props == null){
			return;
		}
		this.setHost((String) props.get(HOST_FIELD));
		this.setFromMail((String) props.get(FROM_MAIL_FIELD));
		this.setSender((String) props.get(SENDER_FIELD));
		this.setUserName((String) props.get(USER_NAME_FIELD));
		this.setPassword((String) props.get(PASSWORD_FIELD));
		this.setPort(props.get(PORT));
		this.setUseSSL(Boolean.valueOf(props.get(BY_SSL)));
		this.setSslProtocols(props.get(PROTOCOLS));
	}
	
	public Map<String,String> returnProps(){
		 Map<String,String> props = new HashMap <String,String>();
		props.put(HOST_FIELD, this.getHost());
		props.put(FROM_MAIL_FIELD, this.getFromMail());
		props.put(SENDER_FIELD, this.getSender());
		props.put(USER_NAME_FIELD, this.getUserName());
		props.put(PASSWORD_FIELD, this.getPassword());
		props.put(PORT, this.getPort());
		props.put(BY_SSL, String.valueOf(this.isUseSSL()));
		props.put(PROTOCOLS, this.getSslProtocols());
		return props;
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the fromMail
	 */
	public String getFromMail() {
		return fromMail;
	}

	/**
	 * @param fromMail the fromMail to set
	 */
	public void setFromMail(String fromMail) {
		this.fromMail = fromMail;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public String getSslProtocols() {
		return sslProtocols;
	}

	public void setSslProtocols(String sslProtocols) {
		this.sslProtocols = sslProtocols;
	}
}
