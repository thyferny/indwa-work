/**
 * ClassName CustomizeParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import com.alpine.miner.workflow.operator.parameter.ParameterDataType;
import com.alpine.miner.workflow.operator.parameter.ParameterInputType;

/**
 * @author zhaoyong
 *
 */
public class CustomizeParameterHelper extends AbstractOperatorParameterHelper{
 
	public static final OperatorParameterHelper INSTANCE = new CustomizeParameterHelper(); 


	public CustomizeParameterHelper(){
		
	}
 
	
	@Override
	public String getInputType(String parameterName) {
		return ParameterInputType.CUTOMIZE;
	}

	@Override
	public String   getParameterDataType(String parameterName) {
		return ParameterDataType.STRING;
	}

}
