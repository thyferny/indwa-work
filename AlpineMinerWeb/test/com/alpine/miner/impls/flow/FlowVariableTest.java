/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * FlowVariableTest
 * May 15, 2012
 */
package com.alpine.miner.impls.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;

import com.alpine.miner.impls.flowvariables.FlowVariableException;
import com.alpine.miner.impls.flowvariables.IFlowVariableService;
import com.alpine.miner.impls.flowvariables.model.FlowVariable;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import org.junit.Test;

/**
 * @author Gary
 *
 */
public class FlowVariableTest extends AbstractFlowTest {

	private FlowInfo testFlow = ResourceManager.getInstance().getFlowList("Public").get(0);

    @Test
	public void testLoadVariable() throws FlowVariableException{
		List<FlowVariable> flowVars;
		try {
			flowVars = IFlowVariableService.INSTANCE.getVariableWebModelList(testFlow, Locale.getDefault());
		} catch (FlowVariableException e) {
			throw e;
		}
		Assert.assertTrue(flowVars.size() > 0);
	}

    @Test
	public void testStoreVariables() throws FlowVariableException{
		List<FlowVariable> orignalVars;
		try {
			orignalVars = IFlowVariableService.INSTANCE.getVariableWebModelList(testFlow, Locale.getDefault());
		} catch (FlowVariableException e) {
			throw e;
		}
		IFlowVariableService.INSTANCE.storeFlowVariableToFlow(orignalVars.toArray(new FlowVariable[orignalVars.size()]), testFlow, Locale.getDefault());
	}
}
