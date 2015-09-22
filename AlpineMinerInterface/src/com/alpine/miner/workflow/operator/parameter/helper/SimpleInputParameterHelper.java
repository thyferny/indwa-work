/**
 * ClassName SimpleInputParameterHelper.java
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
public class SimpleInputParameterHelper extends AbstractOperatorParameterHelper{
 
	public SimpleInputParameterHelper(){
		//default value
		super.setParameterDataType(ParameterDataType.STRING);
	}
	public SimpleInputParameterHelper(String dataType){
		super.setParameterDataType(dataType) ;
	}
	
	@Override
	public String getInputType(String parameterName) {
		return ParameterInputType.SIMPLE_INPUT;
	}

}
