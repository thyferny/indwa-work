/**
 * ClassName  DataBaseInfo.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-13
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;
/**
 * @author John Zhao
 *
 */
public class DataBaseInfo {
	private String system;
	private String url;
	private String userName;
	private String password;
	private String useSSL;//"false"
	

	 
	public String getUseSSL() {
		return useSSL;
	}
	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
	}
	/**
	 * @param system
	 * @param url
	 * @param userName
	 * @param password
	 * @param tableName
	 */
	public DataBaseInfo(String system, String url, String userName, String password,String useSSL) {
		this.system = system;
		this.url = url;
		this.userName = userName;
		this.password = password;
		 this.useSSL = useSSL;
 
	}
	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}
	/**
	 * @param system the system to set
	 */
	public void setSystem(String system) {
		this.system = system;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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
}
