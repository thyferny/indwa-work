/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * Role.java
 */
package com.alpine.miner.security.rolemgr;

import java.util.Set;

import com.alpine.miner.security.permission.Permission;

/**
 * @author Gary
 * Aug 28, 2012
 */
public class Role {

	private Set<Permission> permissions;
	
	private String identifier;
	
	private String roleName;

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
