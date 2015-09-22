/**
 * ClassName AbstractFlowMigrator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-26
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.reader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.OperatorParameterImpl;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;
import com.alpine.utility.xml.XmlDocManager;


public abstract class AbstractFlowMigrator implements FlowMigrator {
    private static final Logger itsLogger=Logger.getLogger(AbstractFlowMigrator.class);

    protected static final String connString=com.alpine.utility.db.Resources.FieldSeparator;
	
	protected void setSimpleParametersValue(Operator operator,
			List<OperatorParameter> operatorParameters, String paraName,
			String paraValue,HashMap<String,OperatorParameter> paraMap) {
		OperatorParameter operatorParameter= new OperatorParameterImpl(operator, paraName);
		operatorParameter.setValue(paraValue);
		operatorParameter.setOperator(operator);
		paraMap.put(paraName, operatorParameter);
	}

	protected void setParameters(Operator operator,
			List<OperatorParameter> operatorParameters,
			HashMap<String, OperatorParameter> paraMap) {
		List<String> paraNamesList=operator.getParameterNames();	
		if(paraNamesList==null){
			itsLogger.warn("Parameter list empty.Return.");
			return;
		}
		Iterator<String> iter=paraNamesList.iterator();
		while(iter.hasNext()){
			String paraName=iter.next();
			OperatorParameter parameter=paraMap.get(paraName);
			if(parameter!=null){
				operatorParameters.add(parameter);
			}else{
				OperatorParameter operatorParameter=new OperatorParameterImpl(operator, paraName);
				operatorParameter.setValue(null);
				operatorParameter.setOperator(operator) ;
				operatorParameters.add(operatorParameter);
			}
		}
	}
	protected void createSimpleElements(Document xmlDoc, Element operatorElement,
			Object value,String paraName){
		createSimpleElements(xmlDoc,operatorElement,value,paraName,null);
	}
	protected void createSimpleElements(Document xmlDoc, Element operatorElement,
			Object value,String paraName, String username) {
		String str=(String)value;
		createParameterElement(xmlDoc, operatorElement,
				paraName,str,username);
	}
	protected void createParameterElement(Document xmlDoc,
			Element operator_element, String key,String value){
		createParameterElement(xmlDoc,operator_element,key,value,null);
	}
	protected void createParameterElement(Document xmlDoc,
			Element operator_element, String key,String value, String username) {
		Element parameter_element = xmlDoc.createElement("Parameter");
		parameter_element.setAttribute("key", key);
		boolean addSuffixToOutput=Boolean.valueOf(ProfileReader.getInstance().getParameter(ProfileUtility.UI_ADD_PREFIX));
		itsLogger.info("AbstractFlowMigrator.createParameterElement:="+addSuffixToOutput);
		if(addSuffixToOutput&&XmlDocManager.OUTPUT_TABLE.equals(key)&&!StringUtil.isEmpty(username)){
			String newTable=StringHandler.addPrefix(value, username);
			parameter_element.setAttribute("value",newTable);
		}else if(addSuffixToOutput&&XmlDocManager.SELECTED_OUTPUT_TABLE.equals(key)&&!StringUtil.isEmpty(username)){
			String[] temp=value.split("\\.");
			String newTable=temp[0]+"."+StringHandler.addPrefix(temp[1], username);
			parameter_element.setAttribute("value",newTable);
		}else{	
			parameter_element.setAttribute("value",value);
		}

		operator_element.appendChild(parameter_element);
	}
	
	protected void saveSimpleParameters(Operator operator, Document xmlDoc,
			Element element, String username) {
		List<OperatorParameter> parameterList=operator.getOperatorParameterList();
		if(parameterList==null)return;
		Iterator<OperatorParameter> iter_para=parameterList.iterator();
		while(iter_para.hasNext()){
			OperatorParameter parameter=iter_para.next();
			String paraName=parameter.getName();
			Object value=parameter.getValue();
			if(value instanceof String){
				createSimpleElements(xmlDoc, element, value,paraName,username);
			}
		}
	}
	
	protected String connectString(List<String> stringList,String connSymbol){
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<stringList.size();i++){
			sb.append(stringList.get(i));
			if(i!=stringList.size()-1){
				sb.append(connSymbol);
			}
		}
		return sb.toString();
	}
}
