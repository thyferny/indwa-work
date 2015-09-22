/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * DBConnectionController.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Aug 07, 2011
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.resource.DataOutOfSyncException;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Controller
@RequestMapping("/main/preference.do")
public class PreferenceController extends AbstractControler  {
	
	public PreferenceController() throws Exception{
		super();
	}
 
	@RequestMapping(params = "method=getPreferences", method = RequestMethod.GET)
	public String getPreferences(  
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		String resultStr = null;
		try{
 
			Collection<PreferenceInfo> preferenceList = rmgr.getPreferences( );
			resultStr = ProtocolUtil.toJson(preferenceList); 
			ProtocolUtil.sendResponse(response, resultStr);
		}
		catch(Exception e){ 
			generateErrorDTO(response, e, request.getLocale()) ;	
		}

		return null;
	}
 
	
	@RequestMapping(params = "method=updatePreference", method = RequestMethod.POST)
	public void updatePreference(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {

		PreferenceInfo info = ProtocolUtil.getRequest(request, PreferenceInfo.class);
		
		rmgr.updateProfileReader(info);
		
		if (info != null) {
			try {
 
				rmgr.updatePreference(info);
			} catch (Exception e) {
				generateErrorDTO(response, e, request.getLocale()) ;	
			}
			
		}
		 
		returnSuccess(response) ;
	}
	


	//type= ui /alg/db
	@RequestMapping(params = "method=getPreferencesDefaultValue", method = RequestMethod.GET)
	public void getPreferencesDefaultValue(String type,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {

		try {
			PreferenceInfo defaultInfo = rmgr.getPreferencesDefaultValue(type);
			String resultStr = ProtocolUtil.toJson(defaultInfo);
			ProtocolUtil.sendResponse(response, resultStr);
		} catch (Exception e) {
			generateErrorDTO(response, e, request.getLocale()) ;
			 
		}
 
	}

	@RequestMapping(params = "method=getPrefix", method = RequestMethod.GET)
	public void getPrefix(HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, DataOutOfSyncException {
		try{
			String isAddPrefix = ProfileReader.getInstance(false).getParameter(ProfileUtility.UI_ADD_PREFIX);
			ProtocolUtil.sendResponse(response, isAddPrefix);
		}
		catch(Exception e){ 
			generateErrorDTO(response, e, request.getLocale()) ;	
		}
	}
}

