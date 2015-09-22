/**
 * ClassName DBTableNameParamterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.tools.StringHandler;

/**
 * @author zhaoyong
 *
 */
public class RecommendationTableParameterHelper extends SingleSelectParameterHelper {
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName ,ResourceType dbType) {
		
		List<String> tableNames=new ArrayList<String>();  
 
		List<Object> inputList = parameter.getOperator().getOperatorInputList(); 
		if(inputList==null){
			return tableNames;
		}
		for(Object obj:inputList){
			if(obj instanceof OperatorInputTableInfo){
				String schemaName=((OperatorInputTableInfo)obj).getSchema();
				String tableName=((OperatorInputTableInfo)obj).getTable();
				String value =StringHandler.combinTableName(schemaName, tableName);
				//avoid null point and duplicate which will cause dojo store error...
				if(value!=null&&tableNames.contains(value)==false){
					tableNames.add(value) ;
				}
			}
		}
 		return tableNames; 
	}


}
