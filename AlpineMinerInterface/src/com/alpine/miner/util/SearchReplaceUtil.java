/**
 * ClassName SearchReplaceManager.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorWorkFlow;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.reader.AbstractReaderParameters;
import com.alpine.miner.workflow.reader.XMLFileReaderParameters;
import com.alpine.miner.workflow.reader.XMLWorkFlowReader;
import com.alpine.miner.workflow.saver.XMLWorkFlowSaver;
import org.apache.log4j.Logger;

public class SearchReplaceUtil {
    private static final Logger itsLogger=Logger.getLogger(SearchReplaceUtil.class);
    public static final String[] searchParameterNames = new String[] {
		LanguagePack.getMessage(OperatorParameter.NAME_dBConnectionName, Locale.getDefault()) ,
		LanguagePack.getMessage(OperatorParameter.NAME_schemaName,Locale.getDefault()) ,
				LanguagePack.getMessage(OperatorParameter.NAME_tableName,Locale.getDefault()) ,
						LanguagePack.getMessage(OperatorParameter.NAME_outputSchema, Locale.getDefault()) 
			};
	
	public static final String[] realParameterNames = new String[] {
			OperatorParameter.NAME_dBConnectionName,
			OperatorParameter.NAME_schemaName,
			OperatorParameter.NAME_tableName,
		 	OperatorParameter.NAME_outputSchema 
		};
	
	static XMLWorkFlowReader reader = new XMLWorkFlowReader();
	static XMLWorkFlowSaver workFlowSaver = new XMLWorkFlowSaver(); 

	public static List<ParameterSearchItem> searchInOperators(String paramterName,
			String parameterValue,   String flowName, List<UIOperatorModel> oModelList, boolean ignoreCase) {
		List<ParameterSearchItem> result = new ArrayList<ParameterSearchItem>();
		if(oModelList!=null){
			for(int i = 0 ; i <oModelList.size();i++){
				Operator op = oModelList.get(i).getOperator();
				List<String> paramNames = op.getParameterNames();
				if(paramNames==null){
					continue;
				}
				for(int j = 0 ;j<paramNames.size();j++){
					String paramName = paramNames.get(j) ;
					if(paramName!=null&&paramName.equals(paramterName)) {
						if(true==matchSearch(paramterName, parameterValue, op,ignoreCase)){
							result.add(new ParameterSearchItem(flowName, oModelList.get(i).getId(),paramName,ParameterUtility.getParameterByName(op, paramterName).getValue().toString()));
							break;
						}
						
					}
					
				}
			}
		}
		return result;
	}

	private static boolean matchSearch(String paramterName, String parameterValue,
		Operator op,boolean ignoreCase) {
		if(parameterValue==null){
			return false;
		}
		if(parameterValue.trim().equals("*")){
		 return true;
		}else{
			OperatorParameter parameter = ParameterUtility.getParameterByName(op, paramterName);
			if(parameter.getValue()==null){
				return false;
			}
			else if(ignoreCase==false&&parameterValue.trim().equals(parameter.getValue())){
				return true;	 
			}else if (ignoreCase==true&&parameterValue.trim().equalsIgnoreCase(parameter.getValue().toString())){
				return true;	 
			}
			
		}
		return false;
	} 

	

	public static int replaceParameterValue(List<UIOperatorModel> oModelList,
			HashMap<String, String> opParamMap, String parameterValue) {
		int number = 0;
		
		if(oModelList!=null&&opParamMap!=null&&parameterValue!=null){
			for(int i = 0 ; i <oModelList.size();i++){
				if(opParamMap.keySet().contains(oModelList.get(i).getId())){
					Operator op = oModelList.get(i).getOperator();
					String paramName = opParamMap.get(oModelList.get(i).getId());
					OperatorParameter parameter = ParameterUtility.getParameterByName(op, paramName);
					parameter.setValue(parameterValue)  ;
					number=number + 1;
				}
				 
			}
		}
		return number;
	}

	public static int replaceParameterValue(String filePath,
			HashMap<String, String> opParamMap, String parameterValue) throws Exception {
		OperatorWorkFlow workflow = readWorkFlow(filePath);
		int operatorNumber =	replaceParameterValue(workflow.getChildList(), opParamMap, parameterValue);
	
		workFlowSaver.doSave(filePath, workflow, false) ;
		return operatorNumber;
	}
	public static List<ParameterSearchItem> searchInFlow(String paramterName,
			String parameterValue, String flowName, String filePath, boolean ignoreCase)   {
		OperatorWorkFlow workflow = null;
		try {
			workflow = readWorkFlow(filePath);
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e) ;
			e.printStackTrace();
			return new ArrayList<ParameterSearchItem> ();
		}
 
		return  searchInOperators(paramterName, parameterValue, flowName, workflow.getChildList(),ignoreCase) ;
	}

	private static OperatorWorkFlow readWorkFlow(String filePath)
			throws Exception {
		
		AbstractReaderParameters para  = new XMLFileReaderParameters(filePath, 
				System.getProperty("user.name"), ResourceType.Personal) ;  
		OperatorWorkFlow workflow = reader.doRead(para, Locale.getDefault());
		return workflow;
	}

	/**
	 * @param paramName
	 * @return
	 */
	public static String getRealParameterName(String paramName) {
		List paramList = Arrays.asList(searchParameterNames);
		int realIndex = paramList.indexOf(paramName);
		if(realIndex>=0&&realIndex<realParameterNames.length){
			return realParameterNames[realIndex];
		}else{
			return paramName; //this time ,paramName is real name
		}
	}
 
}
