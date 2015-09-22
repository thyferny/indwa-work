/**
 * ClassName MutilpleSelectParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterDataType;
import com.alpine.miner.workflow.operator.parameter.ParameterInputType;

/**
 * @author zhaoyong
 *
 */
public class MutilpleSelectParameterHelper extends AbstractOperatorParameterHelper{

	private List<String> avaliableValues; 

	public MutilpleSelectParameterHelper(){
		super.setParameterDataType(ParameterDataType.STRING);
	}
	
	public MutilpleSelectParameterHelper(List<String> avaliableValues){
		this.avaliableValues=avaliableValues;
	}
	
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType resourceType,Locale locale) throws Exception {
		return getAvaliableValues(  parameter,  userName,  resourceType);
	}

	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName,ResourceType dbResourceType) throws Exception {
		return this.avaliableValues;
	}
	
	@Override
	public String getInputType(String parameterName) {
		return ParameterInputType.MULTI_SELECT;
	}

}
