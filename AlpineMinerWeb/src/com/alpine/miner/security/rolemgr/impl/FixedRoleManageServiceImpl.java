/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FixedRoleManageServiceImpl.java
 */
package com.alpine.miner.security.rolemgr.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alpine.miner.security.permission.Permission;
import com.alpine.miner.security.rolemgr.IRoleManageService;
import com.alpine.miner.security.rolemgr.Role;

/**
 * Only support fixed Role info with Admin and Modeler
 * @author Gary
 * Aug 28, 2012
 */
public class FixedRoleManageServiceImpl implements IRoleManageService {
    private static final Logger itsLogger=Logger.getLogger(FixedRoleManageServiceImpl.class);

    private static Map<String, Role> fixedRoleCache = new HashMap<String, Role>();
	
	public static final String 	ADMIN_ROLE_IDENTIFIER = "admin",
								MODELER_ROLE_IDENTIFIER = "modeler";
	
	private static final String STORE_FILE = getStoreFilePath();
	
	
	/* (non-Javadoc)
	 * @see com.alpine.miner.security.rolemgr.IRoleManageService#addRole(com.alpine.miner.security.rolemgr.Role)
	 */
	@Override
	public void addRole(Role role) {
		throw new UnsupportedOperationException("addRole");
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.rolemgr.IRoleManageService#updateRole(com.alpine.miner.security.rolemgr.Role)
	 */
	@Override
	public void updateRole(Role role) {
		fixedRoleCache.get(role.getIdentifier()).setRoleName(role.getRoleName());
		storeRoles();
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.rolemgr.IRoleManageService#removeRole(java.lang.String[])
	 */
	@Override
	public void removeRole(String... identifiers) {
		throw new UnsupportedOperationException("removeRole");
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.rolemgr.IRoleManageService#queryRole(com.alpine.miner.security.rolemgr.Role)
	 */
	@Override
	public List<Role> queryRole(Role condition) {
		return new ArrayList<Role>(fixedRoleCache.values());
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.rolemgr.IRoleManageService#loadRole(java.lang.String)
	 */
	@Override
	public Role loadRole(String identifier) {
		return fixedRoleCache.get(identifier);
	}

	/* (non-Javadoc)
	 * @see com.alpine.miner.security.rolemgr.IRoleManageService#getPermissionSet(java.lang.String[])
	 */
	@Override
	public Set<Permission> getBlacklist(String... roleIdentifier) {
		Set<Permission> permissionSet = new HashSet<Permission>();
		Set<Permission> allPermissionSet = Permission.getAllPermission();
		if(roleIdentifier != null){
			for(String roleId : roleIdentifier){
				Role role = loadRole(roleId);
				if(role != null){
					permissionSet.addAll(role.getPermissions());
				}
			}
		}
		allPermissionSet.removeAll(permissionSet);
		return allPermissionSet;
	}

	private static void loadRoles(){
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(STORE_FILE)));
			fixedRoleCache = (Map<String, Role>) ois.readObject();
		} catch (Exception e) {
			itsLogger.warn(e.getMessage());
			fixedRoleCache = new HashMap<String, Role>();
			storeRoles();
		}finally{
			if(ois != null){
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
					itsLogger.warn(e.getMessage(), e);
				}
			}
		}
	}
	
	private static void storeRoles(){
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(STORE_FILE)));
			oos.writeObject(fixedRoleCache);
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.warn(e.getMessage(), e);
		}finally{
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
				itsLogger.warn(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * To initialize Roles informations
	 */
	public static void init(){
		loadRoles();
		if(fixedRoleCache.size() == 0){
			initRoles();
		}
		fillPermissionSet(fixedRoleCache.get(ADMIN_ROLE_IDENTIFIER), FixedRolePermissionMapping.ADMIN_PERMISSION_SET);
		fillPermissionSet(fixedRoleCache.get(MODELER_ROLE_IDENTIFIER), FixedRolePermissionMapping.MODELER_PERMISSION_SET);
	}
	
	private static String getStoreFilePath(){
		try {
			return FixedRoleManageServiceImpl.class.getResource("").toURI().getPath() + "FixedRoles";
		} catch (URISyntaxException e) {
			return FixedRoleManageServiceImpl.class.getResource("").getPath() + "FixedRoles";
		}
	}
	
	private static void initRoles(){
		Role adminRole = new Role();
		Role modelerRole = new Role();
		fixedRoleCache.put(ADMIN_ROLE_IDENTIFIER, adminRole);
		fixedRoleCache.put(MODELER_ROLE_IDENTIFIER, modelerRole);
		adminRole.setIdentifier(ADMIN_ROLE_IDENTIFIER);
		adminRole.setRoleName("Admin");
		modelerRole.setIdentifier(MODELER_ROLE_IDENTIFIER);
		modelerRole.setRoleName("Modeler");
	}
	
	private static void fillPermissionSet(Role role, Permission[] permissionArray){
		HashSet<Permission> hs = new HashSet<Permission>(Arrays.asList(permissionArray));
		role.setPermissions(hs);
	}
}