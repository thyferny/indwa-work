/**
 * ClassName SingleSelectParameterHelper.java
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

public class SingleSelectParameterHelper extends AbstractOperatorParameterHelper{

	private List<String> avaliableValues; 

	public SingleSelectParameterHelper(){
		super.setParameterDataType(ParameterDataType.STRING);
	}
	
	public SingleSelectParameterHelper(List<String> avaliableValues){
		this();
		this.avaliableValues=avaliableValues;
	}

	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType, Locale locale) throws Exception {
		return getAvaliableValues(  parameter,  userName,   dbType) ;
	}
	
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName ,ResourceType dbType) throws Exception {
		return this.avaliableValues;
	}
	
	@Override
	public String getInputType(String paramName) {
		return ParameterInputType.SINGLE_SELECT;
	}

}
