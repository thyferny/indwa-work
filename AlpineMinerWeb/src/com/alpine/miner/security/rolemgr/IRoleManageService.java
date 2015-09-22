/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * IRoleManageService.java
 */
package com.alpine.miner.security.rolemgr;

import java.util.List;
import java.util.Set;

import com.alpine.miner.security.permission.Permission;
import com.alpine.miner.security.rolemgr.impl.FixedRoleManageServiceImpl;

/**
 * declare some functions to maintain Role Information
 * @author Gary
 * Aug 28, 2012
 */
public interface IRoleManageService {
	
	IRoleManageService INSTANCE = new FixedRoleManageServiceImpl();

	/**
	 * create new Role
	 * @param role
	 */
	void addRole(Role role);
	
	/**
	 * update role information
	 * @param role
	 */
	void updateRole(Role role);
	
	/**
	 * delete role information by identifier
	 * @param identifiers
	 */
	void removeRole(String... identifiers);
	
	/**
	 * query roles information by a condition
	 * @param condition
	 * @return
	 */
	List<Role> queryRole(Role condition);
	
	/**
	 * load role information by identifier
	 * @param identifier
	 * @return
	 */
	Role loadRole(String identifier);
	
	/**
	 * merge all of permissions of giving roles and return backlist
	 * @param roleIdentifier
	 * @return
	 */
	Set<Permission> getBlacklist(String... roleIdentifier);
}
