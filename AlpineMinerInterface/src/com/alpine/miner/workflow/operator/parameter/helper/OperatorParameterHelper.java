/**
 * ClassName OperatorParameterHelper.java
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

import org.w3c.dom.Element;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhaoyong
 *
 */
public interface OperatorParameterHelper extends OperatorParameterValidator {
	 	
	public  abstract String getInputType(String parameterName);
	
	
	public abstract String getParameterDataType(String parameterName);
	
	//user name is the username logged in in the current context (web or rcp)
	public abstract List<String> getAvaliableValues(OperatorParameter parameter,String userName,ResourceType dbType) throws Exception;
	public abstract List<String> getAvaliableValues(OperatorParameter parameter,String userName,ResourceType dbType,Locale locale) throws Exception;

	
	public abstract Element toXMLElement(OperatorParameter parameter);
	public abstract OperatorParameter fromXMLElement(Element element);
	
	//will return the i18n label for the UI in server side
	public abstract String getParameterLabel(String parameterName);

	public String getParameterLabel(String parameterName, Locale locale);

}
