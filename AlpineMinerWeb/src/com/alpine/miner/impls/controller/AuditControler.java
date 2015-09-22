/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * AuditControler
 * Nov 24, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.audit.AuditManager;
import com.alpine.miner.impls.web.resource.LoginManager;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gary
 *
 */
@Controller
@RequestMapping("/main/auditManager.do")
public class AuditControler extends AbstractControler {
    private static Logger itsLogger = Logger.getLogger(AuditControler.class);
    private AuditManager manager = AuditManager.INSTANCE;
	/**
	 * @throws Exception
	 */
	public AuditControler() throws Exception {
		super();
	}

	@RequestMapping(params = "method=loadCategories", method = RequestMethod.GET)
	public void loadCategories(HttpServletRequest request,String category, HttpServletResponse response, ModelMap model) throws Exception{
		try{
			String userName = getUserName(request);
			List<AuditCategory> categories = new ArrayList<AuditCategory>();
			if(LoginManager.ADMIN_USER.equals(userName)){
				
				//make sure admin is in it
				categories.add(new AuditCategory(LoginManager.ADMIN_USER,LoginManager.ADMIN_USER));
				try{
					for(UserInfo user : ProviderFactory.getAuthenticator("").getUserInfoList()){
						if(user.getLogin()!=null&&user.getLogin().equals(LoginManager.ADMIN_USER)==false){
							categories.add(new AuditCategory(user.getLogin(),user.getLogin()));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					itsLogger.error(e.getMessage(),e);
				}
				
			}else{
				UserInfo user = getUserInfo(request);
				categories.add(new AuditCategory(user.getLogin(),user.getLogin()));
			}
			ProtocolUtil.sendResponse(response, categories);
		}catch(Exception e){
			generateErrorDTO(response, e, request.getLocale());
		}
	}

	@RequestMapping(params = "method=loadAudits", method = RequestMethod.GET)
	public void loadAudits(HttpServletRequest request,String category, HttpServletResponse response, ModelMap model) throws Exception{
		try{
			ProtocolUtil.sendResponse(response, manager.getUserAudits(category));
		}catch(Exception e){
			generateErrorDTO(response, e, request.getLocale());
		}
	}

	@RequestMapping(params = "method=removeAudit", method = RequestMethod.GET)
	public void removeAudit(HttpServletRequest request,String category, HttpServletResponse response, ModelMap model) throws IOException{
		//modify by Will
		try {
			manager.deleteUserAudits(category, null);
			ProtocolUtil.sendResponse(response, "{result:\"success\"}");
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale());
		}
	}
	
	public static class AuditCategory{
		private String 	label,
						value;
		private AuditCategory(String label,String value){
			this.label = label;
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public String getValue() {
			return value;
		}
	}
}
