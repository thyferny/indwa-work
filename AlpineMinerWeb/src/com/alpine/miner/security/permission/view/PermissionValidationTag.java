/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * PermissionValidationTag.java
 */
package com.alpine.miner.security.permission.view;

import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.security.permission.Permission;


/**
 * @author Gary
 * Aug 31, 2012
 */
public class PermissionValidationTag extends BodyTagSupport{
	
	private static final long serialVersionUID = 1L;
	private Permission[] permission;
	
	@Override
	public int doStartTag() throws JspException {
		Set<Permission> blacklist = (Set<Permission>) pageContext.getSession().getAttribute(Resources.SESSION_PERMISSION);
		boolean isAllow = false;
		for(Permission item : permission){
			isAllow |= !blacklist.contains(item);
		}
		return isAllow ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}

	public void setPermission(String permissionStrs) {
		String[] permissionStrArray = permissionStrs.split(",");
		permission = new Permission[permissionStrArray.length];
		for(int i = 0;i < permissionStrArray.length;i++){
			String permissionStr = permissionStrArray[i].replaceAll("\\W", "");
			permission[i] = Permission.valueOf(permissionStr);
		}
	}
}
