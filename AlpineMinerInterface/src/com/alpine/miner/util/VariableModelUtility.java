/**
 * ClassName VariableModelUtility.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-10
 *
 * COPYRIGHT   2011  Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.util;

import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.utility.file.StringUtil;

public class VariableModelUtility {

	public static String getReplaceValue(VariableModel variableModel,
			String paramValue) {
		if(variableModel!=null){
			if(StringUtil.isEmpty(paramValue)||!paramValue.contains(VariableModel.VARIABLE_PREFIX)){
				return paramValue;
			}
			return com.alpine.utility.common.VariableModelUtility.getReplaceValue(variableModel.getVariableMap(), paramValue);
		}
		return paramValue;
	}	
}
