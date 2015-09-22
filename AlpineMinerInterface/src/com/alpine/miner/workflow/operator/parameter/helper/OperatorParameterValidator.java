/**
 * ClassName OperatorParameterValidator.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhaoyong
 *
 */
public interface OperatorParameterValidator {
	
	public boolean doValidate(OperatorParameter parameter) ;

}
