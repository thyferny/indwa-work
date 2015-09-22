/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AuthenticationProvider.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security;

import java.util.List;

/**
 * @author sam_zang
 * 
 */
public interface AuthenticationProvider {

	public boolean authenticate(String user, String password);
		
	public List<UserInfo> getUserInfoList();
	public List<GroupInfo> getGroupInfoList();

	public List<UserInfo> getUserInfoByGroup(String group);
	public UserInfo getUserInfoByName(String user);
	public String[] getUserGroups(String user);
	public String[] getUserRoleSet(String user);

	public void createGroup(GroupInfo info) throws OperationNotAllowedException;
	public void updateGroup(GroupInfo info) throws OperationNotAllowedException;
	public void deleteGroup(GroupInfo info) throws OperationNotAllowedException;

	public void createUser(UserInfo info) throws OperationNotAllowedException;
	public void updateUser(UserInfo info) throws OperationNotAllowedException;
	public void deleteUser(UserInfo info) throws OperationNotAllowedException;

}
