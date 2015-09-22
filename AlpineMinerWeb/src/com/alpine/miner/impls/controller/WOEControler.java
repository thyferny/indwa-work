/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * WOEControler
 * Jan 6, 2012
 */
package com.alpine.miner.impls.controller;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.miner.impls.Resources;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.woe.IWOEDataService;
import com.alpine.miner.impls.woe.WOECalculateParam;
import com.alpine.miner.impls.woe.WoeCalculateElement;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

/**
 * @author Gary
 *
 */
@Controller
@RequestMapping("/main/flow/woeOperator.do")
public class WOEControler extends AbstractControler {

	private IWOEDataService service = IWOEDataService.INSTANCE;
	
	/**
	 * @throws Exception
	 */
	public WOEControler() throws Exception {
		super();
	}

	@RequestMapping(params = "method=autoCalculate", method = RequestMethod.POST)
	public void autoCalculate(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception{
		WOECalculateParam param = ProtocolUtil.getRequest(request, WOECalculateParam.class);
		OperatorWorkFlow flowInfo = getFlowInfo(param.getFlowInfo(), request.getLocale());
		UserInfo user = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
		UIOperatorModel woeOperatorModel = null;
		List<WoeCalculateElement> woeInfoList;
		for(UIOperatorModel operatorModel : flowInfo.getChildList()){
			if(operatorModel.getUUID().equals(param.getOperatorUUID())){
				woeOperatorModel = operatorModel;
				break;
			}
		}
		
		if(woeOperatorModel == null){
			return;
		}
		List<OperatorParameter> parameters = woeOperatorModel.getOperator().getOperatorParameterList();
		for(OperatorParameter parame : parameters){
			if("dependentColumn".equals(parame.getName())){
				parame.setValue(param.getDependentColumn());
			}else if("goodValue".equals(parame.getName())){
				parame.setValue(param.getGoodValue());
			}else if("columnNames".equals(parame.getName())){
				parame.setValue(param.getColumnNames());
			}
		}
		try {
			woeInfoList = service.autoCalculate(woeOperatorModel.getOperator(), param.getCalculateElements(), user.getLogin(), param.getFlowInfo().getResourceType());
			ProtocolUtil.sendResponse(response, woeInfoList);
		} catch (AnalysisException e) {
			generateErrorDTO(response, e, request.getLocale()); 
		}
	}

	@RequestMapping(params = "method=calculate", method = RequestMethod.POST)
	public void calculate(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception{
		WOECalculateParam param = ProtocolUtil.getRequest(request, WOECalculateParam.class);
		OperatorWorkFlow flowInfo = getFlowInfo(param.getFlowInfo(), request.getLocale());
		UserInfo user = (UserInfo) request.getSession().getAttribute(Resources.SESSION_USER);
		UIOperatorModel woeOperatorModel = null;
		WoeCalculateElement woeElement;
		for(UIOperatorModel operatorModel : flowInfo.getChildList()){
			if(operatorModel.getUUID().equals(param.getOperatorUUID())){
				woeOperatorModel = operatorModel;
				break;
			}
		}
		if(woeOperatorModel == null){
			return;
		}
		List<OperatorParameter> parameters = woeOperatorModel.getOperator().getOperatorParameterList();
		for(OperatorParameter parame : parameters){
			if("dependentColumn".equals(parame.getName())){
				parame.setValue(param.getDependentColumn());
			}else if("goodValue".equals(parame.getName())){
				parame.setValue(param.getGoodValue());
			}else if("columnNames".equals(parame.getName())){
				parame.setValue(param.getColumnNames());
			}
		}
		try {
			woeElement = service.calculate(woeOperatorModel.getOperator(), param.getCalculateElements(), user.getLogin(), param.getFlowInfo().getResourceType());
			ProtocolUtil.sendResponse(response, woeElement);
		} catch (AnalysisException e) {
			generateErrorDTO(response, e, request.getLocale()); 
		}
	}

	private OperatorWorkFlow getFlowInfo(FlowInfo info, Locale locale) throws Exception{
		OperatorWorkFlow flow = null;
		try {
			flow = rmgr.getFlowData(info,locale);
		} catch (Exception e) {
			throw e;
		}
		return flow;
	}
}
