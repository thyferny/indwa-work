/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * PermissionFilter.java
 */
package com.alpine.miner.impls.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.controller.ErrorDTO;
import com.alpine.miner.impls.controller.ProtocolUtil;
import com.alpine.miner.security.permission.Permission;

/**
 * @author Gary
 * Aug 31, 2012
 */
public class PermissionFilter implements Filter {

	private static final HashMap<String, Permission> PERMISSION_MAPPING = new HashMap<String, Permission>();
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse rep, FilterChain nextFilter) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String requestURI = request.getRequestURI();
		String reqeustMethod = request.getParameter("method");
		Permission permission = getPerission(requestURI, reqeustMethod);
		if(validatePermission(request, permission)){
			nextFilter.doFilter(req, rep);
		}else{
			HttpServletResponse response = (HttpServletResponse) rep;
			String message = ResourceBundle.getBundle("app", request.getLocale()).getString("permission_message_none_permission");
			ProtocolUtil.sendResponse(response, new ErrorDTO(ErrorDTO.UNKNOW_ERROR, message));
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

	private boolean validatePermission(HttpServletRequest request, Permission permission){
		Set<Permission> userBlacklist = (Set<Permission>) request.getSession().getAttribute(Resources.SESSION_PERMISSION);
		if(userBlacklist == null){
			return true;//not login yet.
		}
		return !userBlacklist.contains(permission);
	}
	
	private Permission getPerission(String uri, String requestMethod){
		String key = uri + "?" + requestMethod;
		Permission permission = PERMISSION_MAPPING.get(key);
		if(permission == null){
			synchronized(PERMISSION_MAPPING){
				permission = PERMISSION_MAPPING.get(key);
				if(permission == null){
					permission = Permission.getPermission(uri, requestMethod);
					PERMISSION_MAPPING.put(key, permission);
				}
			}
		}
		return permission;
	}
}
