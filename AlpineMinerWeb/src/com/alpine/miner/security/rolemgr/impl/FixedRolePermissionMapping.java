/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FixedRolePermissionMapping.java
 */
package com.alpine.miner.security.rolemgr.impl;

import com.alpine.miner.security.permission.Permission;

/**
 * @author Gary
 * Aug 29, 2012
 */
public class FixedRolePermissionMapping {

	static final Permission[] ADMIN_PERMISSION_SET = {
		Permission.IMPORT_JDBC_DRIVER,
		//Permission.DELETE_JDBC_DRIVER,
		Permission.EDIT_DATASOURCE_TO_PUBLIC,
		Permission.UPDATE_PREFERENCE,
		Permission.SECURITY_LOAD,
		Permission.SECURITY_UPDATE,
		Permission.USER_MANAGEMENT_EDIT,
		Permission.GROUP_MANAGEMENT_EDIT,
		Permission.MAIL_SERVER_CONFIG_QUERY,
		Permission.MAIL_SERVER_CONFIG_UPDATE,
		Permission.MAIL_SERVER_CONFIG_TESTING,
		Permission.UDF_MANAGEMENT_QUERY,
		Permission.UDF_MANAGEMENT_EDIT,
		Permission.SESSION_MANAGEMENT_QUERY,
		Permission.SESSION_MANAGEMENT_KILL,
		Permission.DELETE_FLOW_FROM_PUBLIC,
        Permission.UPDATE_SYSTEM,
        Permission.CHORUS_ADMIN
	};
	
	static final Permission[] MODELER_PERMISSION_SET = {
		Permission.CREATE_FLOW,
		Permission.OPERATOR_EDIT,
		Permission.CONNECT_EDIT,
		Permission.UDF_MANAGEMENT_QUERY,
	};
}
