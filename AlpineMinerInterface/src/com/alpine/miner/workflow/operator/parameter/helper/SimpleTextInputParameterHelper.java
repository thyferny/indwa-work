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
 * This is used for sql execution operator...
 * @author zhaoyong
 *
 */
public class SimpleTextInputParameterHelper extends AbstractOperatorParameterHelper{
 
	public SimpleTextInputParameterHelper(){
		//default value
		super.setParameterDataType(ParameterDataType.STRING);
	}
 	/**Tell the user this is a text input.
 	 * */
	@Override
	public String getInputType(String parameterName) {
		return ParameterInputType.TEXT;
	}

}
