/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * UserInfo.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Jun 22, 2011
 */

package com.alpine.miner.security;


/**
 * @author sam_zang
 * 
 */
public class UserInfo {
	public static final String LOGIN = "login";
	public static final String EMAIL = "email";
	public static final String PASSWORD = "password";
	public static final String NOTIFICATION = "notification";
	public static final String GROUPS = "groups";
	public static final String DESC = "description";
	public static final String FIRSTNAME = "firstName";
	public static final String LASTNAME = "lastName";
	public static final String ROLE_SET = "roleSet";
	public static final String CHORUS_KEY = "chorusKey";
	
	public UserInfo() {
		login = "";
		password = "";
		description = "";
		setNotification(true);
		firstName = "";
		lastName = "";
		
	}

	public UserInfo(String login, String desc) {
		this.login = login;
		this.description = desc;
		this.setNotification(true);
	}
	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the groups
	 */
	public String[] getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(String[] groups) {
		this.groups = groups;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param notification the notification to set
	 */
	public void setNotification(boolean notification) {
		this.notification = notification;
	}

	/**
	 * @return the notification
	 */
	public boolean getNotification() {
		return notification;
	}
	
	public String[] getRoleSet() {
		return roleSet;
	}
	
	public void setRoleSet(String[] roleSet) {
		this.roleSet = roleSet;
	}

	public String getChorusKey() {
		return chorusKey;
	}

	public void setChorusKey(String chorusKey) {
		this.chorusKey = chorusKey;
	}

	private String login;
	private String email = "";
	private String password;
	private boolean notification;
	private String firstName;
	private String lastName;
	private String[] groups;
	private String description;
	// for password reset.
	private String newPassword;
	private String[] roleSet;
	private String chorusKey;
}
