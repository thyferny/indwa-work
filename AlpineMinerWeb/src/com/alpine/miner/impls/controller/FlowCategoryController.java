/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowCategoryController
 * Feb 27, 2012
 */
package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.categorymanager.IFlowCategoryPersistence;
import com.alpine.miner.impls.categorymanager.exception.FlowCategoryException;
import com.alpine.miner.impls.categorymanager.model.FlowBasisInfo;
import com.alpine.miner.impls.categorymanager.model.FlowCategory;
import com.alpine.miner.impls.categorymanager.model.FlowCategoryForm;
import com.alpine.miner.impls.categorymanager.model.FlowDisplayModel;
import com.alpine.miner.security.UserInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Gary
 *
 */
@Controller
@RequestMapping("/main/flowCategory.do")
public class FlowCategoryController extends AbstractControler {

	private static final int ERROR_CODE = -100;
	
	private IFlowCategoryPersistence persistence = IFlowCategoryPersistence.INSTANCE;
	
	/**
	 * @throws Exception
	 */
	public FlowCategoryController() throws Exception {
		super();
	}

	@RequestMapping(params = "method=getFlowByCategoryFromUser", method = RequestMethod.GET)
	public void getFlowByCategoryFromUser(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		UserInfo user = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
		FlowCategory rootCategory = null;
		try {
			rootCategory = persistence.buildRootCategory(user.getLogin());
			ProtocolUtil.sendResponse(response, rootCategory);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params = "method=saveFlowCategory", method = RequestMethod.POST)
	public void saveFlowCategory(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		FlowCategory category = ProtocolUtil.getRequest(request, FlowCategory.class);
		try {
			persistence.createCategory(category);
			returnSuccess(response);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params = "method=renameFlowCategory", method = RequestMethod.POST)
	public void renameFlowCategory(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		FlowCategory category = ProtocolUtil.getRequest(request, FlowCategory.class);
		try {
			persistence.updateCategory(category);
			
			ProtocolUtil.sendResponse(response, category);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params = "method=getRootCategoryFromUser", method = RequestMethod.GET)
	public void getRootCategoryFromUser(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		UserInfo user = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
		try {
			List<FlowCategory> categories = persistence.getChildrenCategory(user.getLogin());
			ProtocolUtil.sendResponse(response, categories);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params = "method=removeFlowCategory", method = RequestMethod.POST)
	public void removeFlowCategory(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		FlowCategory category = ProtocolUtil.getRequest(request, FlowCategory.class);
		try {
			persistence.removeCategory(category);
			returnSuccess(response);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params = "method=moveFlowIntoCategory", method = RequestMethod.POST)
	public void moveFlowIntoCategory(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		FlowCategoryForm form = ProtocolUtil.getRequest(request, FlowCategoryForm.class);
		try {
			String userName = getUserName(request);
			Map<String, FlowDisplayModel> successList = persistence.moveFlow(form.getCategoryInfo(), form.getOperateFlowInfoArray());
 
			ProtocolUtil.sendResponse(response, successList);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
	}

	@RequestMapping(params = "method=getAllFlowInfo", method = RequestMethod.GET)
	public void getAllFlowInfo(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException{
		UserInfo user = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
		List<FlowBasisInfo> result;
		try {
			result = persistence.getAllFlowInfo(user.getLogin());
			ProtocolUtil.sendResponse(response, result);
		} catch (FlowCategoryException e) {
			request.getLocale();
			String msg = ResourceBundle.getBundle("app", request.getLocale()).getString(e.getMessage());
			ProtocolUtil.sendResponse(response,new ErrorDTO(ERROR_CODE, msg));
		}
		
	}
}
