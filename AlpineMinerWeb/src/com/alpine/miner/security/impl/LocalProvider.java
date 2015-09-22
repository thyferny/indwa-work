/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * LocalProvider.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 9, 2012
 */
package com.alpine.miner.security.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.GroupInfo;
import com.alpine.miner.security.OperationNotAllowedException;
import com.alpine.miner.security.UserInfo;
import org.apache.log4j.Logger;

/**
 * @author sam_zang
 *
 */
public class LocalProvider implements AuthenticationProvider {
    private static Logger itsLogger = Logger.getLogger(LocalProvider.class);
    private static AuthenticationProvider instance = new LocalProvider();

	/**
	 * @return a singleton instance of Local Provider
	 */
	public static AuthenticationProvider getInstance() {
		return instance;
	}

	LocalProvider() {
		persistence = new FilePersistence();
		userTable = new HashMap<String, UserInfo>();
		groupTable = new HashMap<String, GroupInfo>();
		rwl = new ReentrantReadWriteLock();
	}
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticate(String user, String password) {
		UserInfo u = getUserInfoByName(user);
		if (u.getPassword()!=null&&u.getPassword().equals(password)) {
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getAllUserInfoList()
	 */
	@Override
	public List<UserInfo> getUserInfoList() {
		return persistence.getUserInfoList();
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getAllGroupInfoList()
	 */
	@Override
	public List<GroupInfo> getGroupInfoList() {
		return persistence.getGroupInfoList();
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserInfoByGroup(java.lang.String)
	 */
	@Override
	public List<UserInfo> getUserInfoByGroup(String group) {
		List<UserInfo> list = new LinkedList<UserInfo>();
		if (group == null) {
			return list;
		}
		for (UserInfo u : getUserInfoList()) {
			if (isInGroup(u, group)) {
				list.add(u);
			}
		}
		return list;
	}

	/**
	 * @param u
	 * @param group
	 * @return
	 */
	private boolean isInGroup(UserInfo u, String group) {
		for (String g : u.getGroups()) {
			if (group.equals(g)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#createGroupInfo(com.alpine.miner.security.GroupInfo)
	 */
	@Override
	public void createGroup(GroupInfo info)
			throws OperationNotAllowedException {
		rwl.writeLock().lock();
		try {
			persistence.createGroupInfo(info);
			groupTable.clear();
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#updateGroupInfo(com.alpine.miner.security.GroupInfo)
	 */
	@Override
	public void updateGroup(GroupInfo info)
			throws OperationNotAllowedException {
		rwl.writeLock().lock();
		try {
			persistence.updateGroupInfo(info);
			groupTable.clear();
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#deleteGroupInfo(com.alpine.miner.security.GroupInfo)
	 */
	@Override
	public void deleteGroup(GroupInfo info)
			throws OperationNotAllowedException {
		rwl.writeLock().lock();
		try {
			persistence.deleteGroupInfo(info);
			groupTable.clear();
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#createUserInfo(com.alpine.miner.security.UserInfo)
	 */
	@Override
	public void createUser(UserInfo info)
			throws OperationNotAllowedException {
		rwl.writeLock().lock();
		try {
			persistence.createUserInfo(info);
			userTable.clear();
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#updateUserInfo(com.alpine.miner.security.UserInfo)
	 */
	@Override
	public void updateUser(UserInfo info)
			throws OperationNotAllowedException {
		rwl.writeLock().lock();
		try {
			persistence.updateUserInfo(info);
			userTable.clear();
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#deleteUserInfo(com.alpine.miner.security.UserInfo)
	 */
	@Override
	public void deleteUser(UserInfo info)
			throws OperationNotAllowedException {
		rwl.writeLock().lock();
		try {
			persistence.deleteUserInfo(info);
			userTable.clear();
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
			throw new OperationNotAllowedException(e);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserInfoByName(java.lang.String)
	 */
	@Override
	public UserInfo getUserInfoByName(String user) {
		UserInfo info = null;
		rwl.readLock().lock();
		try {
			info = userTable.get(user);
			if (info != null) {
				return info;
			}
		} finally {
			rwl.readLock().unlock();
		}

		// If it gets here then User Info is not in the cache
		rwl.writeLock().lock();
		try {
			info = userTable.get(user);
			if (info == null) {
				// load User Info from persistence
				info = persistence.loadUserInfo(user);
				if (info != null) {
					userTable.put(user, info);
				}
			}
		} finally {
			rwl.writeLock().unlock();
		}

		return info;
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserGroups(java.lang.String)
	 */
	@Override
	public String[] getUserGroups(String user) {
		UserInfo info = getUserInfoByName(user);
		if (info != null && info.getGroups() != null) {
			return info.getGroups();
		}

		return new String[0];
	}
	
	// user info table and read/write lock.
	private HashMap<String, UserInfo> userTable;
	private HashMap<String, GroupInfo> groupTable;
	private ReentrantReadWriteLock rwl;
	private Persistence persistence;

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.AuthenticationProvider#getUserRoleSet(java.lang.String)
	 */
	@Override
	public String[] getUserRoleSet(String user) {
		UserInfo info = getUserInfoByName(user);
		return info == null ? null : info.getRoleSet();
	}
}
