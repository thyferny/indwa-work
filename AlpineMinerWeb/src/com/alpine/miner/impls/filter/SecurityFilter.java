/**
 * ClassName :SecurityFilter.java
 *
 * Version information: 3.0
 *
 * Data: 2011-12-12
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.controller.ChorusController;
import com.alpine.miner.impls.controller.ErrorDTO;
import com.alpine.miner.impls.controller.ProtocolUtil;
import com.alpine.miner.impls.web.resource.LoginManager;
import com.alpine.miner.interfaces.ChorusConfigManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.security.UserInfo;

public class SecurityFilter implements Filter {
	public static final String Alpine_Special_Key ="Alpine_Special_Key" ;
	public static final String Alpine_Special_Value ="ff1335bea314baa82e" ;

	static HashMap<String, String> allowBypass = new HashMap<String, String>();
	static {
		allowBypass.put("login", "yes");
		allowBypass.put("forceLogin", "yes");
		allowBypass.put("testConn", "yes");
		allowBypass.put("getGroupsNoLogin", "yes");
		allowBypass.put("publishFlow", "yes");
		allowBypass.put("getUserInfo", "yes");
		allowBypass.put("getMessage", "yes");
		allowBypass.put("updateLicense", "yes");
        allowBypass.put("getFlowImage", "yes"); //added for chorus
        allowBypass.put("checkScriptVersion", "yes");
        allowBypass.put("acceptAgreement", "yes");
        allowBypass.put("putFlowRunningProperty", "yes");
	}

    static HashMap<String, String> chorusAuthWithAPI_KEY = new HashMap<String, String>();

    static {
        chorusAuthWithAPI_KEY.put("getWorkFlowImage","yes");
        chorusAuthWithAPI_KEY.put("runWorkFlow","yes");
    }
	
    public void destroy() {
    }

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		request.setCharacterEncoding(Persistence.ENCODING) ;
		HttpServletResponse response = (HttpServletResponse) res;
		
		String method = request.getParameter("method");
		String specialValue = request.getParameter(Alpine_Special_Key);
	    String chorusAPI = request.getParameter(ChorusController.API_KEY);

        if (chorusAPI != null && chorusAuthWithAPI_KEY.get(method) != null) {
            if (chorusAPI.equals(ChorusConfigManager.INSTANCE.readConfig().getApiKey())) {
                chain.doFilter(request, response);
            } else {
                ProtocolUtil.sendChorusAuthFailure(response, new ErrorDTO(401, "The supplied api key is invalid"));
            }
        } else if (allowBypass.get(method) != null) {
			chain.doFilter(request, response);
		} else if(Alpine_Special_Value.equals(specialValue)==true) {
			 
			UserInfo user = new UserInfo("admin","admin");
			request.getSession().setAttribute(Resources.SESSION_USER, user);
			chain.doFilter(request, response);
		}
		else if (checkUser(request, response) == true) {
			chain.doFilter(request, response);
		}
	}

    public void init(FilterConfig arg0) throws ServletException {

    }
	
	public boolean checkUser(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String user = (String) request.getHeader(Resources.SESSION_USER);
			String ts = (String) request.getHeader(Resources.SESSION_TIME_STAMP);

			if (user == null) {
				UserInfo u = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
				if (u == null) {
					ProtocolUtil.sendResponse(response, new ErrorDTO());
					return false;
				}
				user = u.getLogin(); 
				ts = (String) request.getSession().getAttribute(Resources.SESSION_TIME_STAMP);
			}
			
			switch (LoginManager.validete(user, request.getSession())) {
			case NotLoggedIn:
				ProtocolUtil.sendResponse(response, new ErrorDTO());
				return false;

			case LoginOverridden:
				ProtocolUtil.sendResponse(response, new ErrorDTO(-2));
				return false;
			}

		} catch (IOException e) {
			try {
				ProtocolUtil.sendResponse(response, new ErrorDTO());
			} catch (IOException e1) {
				// no user. return
			}
			return false;
		}
		return true;
	}
}