/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * Permissions.java
 */
package com.alpine.miner.security.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gary
 * Aug 28, 2012
 */
public enum Permission {

	//data source management
	IMPORT_JDBC_DRIVER("/main/manager.do", "uploadDBDriver"),
//	DELETE_JDBC_DRIVER("/main/dbconnection.do", "deleteJdbcDriverByName"),
	EDIT_DATASOURCE_TO_PUBLIC("", ""),
	//preference setting
	UPDATE_PREFERENCE("/main/preference.do", "updatePreference"),
	//security management
	SECURITY_LOAD("/main/admin.do", "loadSecurityConfig"),
	SECURITY_UPDATE("/main/admin.do", "updateSecurityConfig"),
	//user & group management
	USER_MANAGEMENT_EDIT("/main/admin.do", "createUser", "deleteUser"),
	GROUP_MANAGEMENT_EDIT("/main/admin.do", "createGroup", "updateGroup", "deleteGroup"),
	//Mail server configuration
	MAIL_SERVER_CONFIG_QUERY("/main/admin.do", "getMailServerConfig"),
	MAIL_SERVER_CONFIG_UPDATE("/main/admin.do", "updateMailServerConfig"),
	MAIL_SERVER_CONFIG_TESTING("/main/admin.do", "testMailConfig"),
	//UDF XML upload
	UDF_MANAGEMENT_QUERY("/main/udf.do", "getUDFModels"),
	UDF_MANAGEMENT_EDIT("/main/udf.do", "uploadUDFModels", "deleteUDFModel"),
	//Session Management
	SESSION_MANAGEMENT_QUERY("/main/admin.do", "getLoginInfoList"),
	SESSION_MANAGEMENT_KILL("/main/admin.do", "removeLoginInfo"),
	//Public flow Management
	DELETE_FLOW_FROM_PUBLIC("", ""),
	//flow edit
	CREATE_FLOW("/main/flow/import_flow.do", "newFlow"),
	OPERATOR_EDIT("/main/operatorManagement.do", "addOperator", "removeOperator", "copyOperators"),
	CONNECT_EDIT("/main/linkManagement.do", "connectOperator", "reconnectOperator", "deleteLink", "batchDeleteLink"),
    //System update
	UPDATE_SYSTEM("/main/systemUpdate.do","getUpdateFileInfos","getCurrentRunFlowInfo","execuSystemUpdate","haveNewUpdateVersion"),
    //Chorus admin
    CHORUS_ADMIN("/main/chorus.do", "getChorusConfig", "updateChorusConfig"),

	
	NONE_VALIDATE("NONE", "NONE");// this Enumeration would transparent out of the class. 
	
	
	private String 	requestURL;
	private String[] requestMethods;
	private Permission(String requestURL, String... requestMethod){
		this.requestURL = requestURL;
		this.requestMethods = requestMethod;
	}
	
	public boolean match(String requestURL, String requestMethod){
		if(this.requestURL == "NONE"){
			return true;
		}
		if(this.requestURL != "" && requestURL.indexOf(this.requestURL) != -1){
			boolean isMatch = false;
			for(int i = 0;i < requestMethods.length;i++){
				isMatch |= requestMethod.indexOf(requestMethods[i]) != -1;
			}
			return isMatch;
		}else{
			return false;
		}
	}
	
	public static Permission getPermission(String requestURL, String requestMethod){
		for(Permission permission : Permission.values()){
			if(permission.match(requestURL, requestMethod)){
				return permission;
			}
		}
		return NONE_VALIDATE;
	}
	
	public static Set<Permission> getAllPermission(){
		Set<Permission> allPermissions = new HashSet<Permission>(Arrays.asList(Permission.values()));
		allPermissions.remove(NONE_VALIDATE);
		return allPermissions;
	}
}
