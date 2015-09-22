/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * IFlowVariableService
 * Apr 10, 2012
 */
package com.alpine.miner.impls.flowvariables;

import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.flowvariables.impl.FlowVariableServiceImpl;
import com.alpine.miner.impls.flowvariables.model.FlowVariable;
import com.alpine.miner.impls.web.resource.FlowInfo;

/**
 * Flow variable service
 * @author Gary
 *
 */
public interface IFlowVariableService {
	
	IFlowVariableService INSTANCE = new FlowVariableServiceImpl();

	/**
	 * get variables from flowInfo
	 * @param flowInfo
	 * @param locale
	 * @return
	 * @throws FlowVariableException
	 */
	List<FlowVariable> getVariableWebModelList(FlowInfo flowInfo, Locale locale) throws FlowVariableException;
	
	/**
	 * save flow variables into flowInfo
	 * @param flowVariable
	 * @param flowInfo
	 * @param locale
	 * @throws FlowVariableException
	 */
	void storeFlowVariableToFlow(FlowVariable[] flowVariable, FlowInfo flowInfo, Locale locale) throws FlowVariableException;
}
