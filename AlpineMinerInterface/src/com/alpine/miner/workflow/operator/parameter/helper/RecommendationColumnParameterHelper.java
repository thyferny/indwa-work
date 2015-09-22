/**
 * ClassName RecommendationColumnParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.solutions.ProductRecommendationOperator;
import com.alpine.utility.tools.StringHandler;

/**
 * panrent could be table, it si simple
 * it also can be sampling 
 * or aggregate or sth could add columns...
 * @author zhaoyong
 *
 */
public class RecommendationColumnParameterHelper extends SingleSelectParameterHelper {
	private String columnType =null;
	//defrence column may comes form diffrent table
	private String tableParamName;

	List<String> canSelectNullValueParameters=Arrays.asList(new String[]{
			OperatorParameter.NAME_Customer_Product_Count_Column+ProductRecommendationOperator.class.getCanonicalName(),
	});
	
	//if(columnType==null) means all the column name...
	public RecommendationColumnParameterHelper(String tableParamName){
		this.tableParamName=tableParamName;
	}
	
	public RecommendationColumnParameterHelper(String columnType,String tableParamName){
		this.columnType=columnType;
		this.tableParamName = tableParamName;
	}
	
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType) throws Exception {
		List<String> result = new ArrayList<String>();
		String schemaTable=getParameterValueRecrusively(parameter.getOperator(), tableParamName);
		//not ready
		if(schemaTable==null||schemaTable.indexOf(".")<0){
			return result;
		}
		schemaTable=StringHandler.removeDoubleQ(schemaTable) ;
			
		String schemaName=schemaTable.substring(0,schemaTable.indexOf(".") -1);
		String tableName=schemaTable.substring(schemaTable.indexOf(".") +2,schemaTable.length());

		List<Object> inputList = parameter.getOperator().getOperatorInputList(); 
		if(inputList==null){
			return result;
		}
		String operatorClassName=parameter.getOperator().getClass().getCanonicalName();
		for(Object obj:inputList){
			if(obj instanceof OperatorInputTableInfo){
				if(schemaName.equals(((OperatorInputTableInfo)obj).getSchema())
						&&tableName.equals(((OperatorInputTableInfo)obj).getTable())){
					List<String[]> fieldColumns = ((OperatorInputTableInfo)obj).getFieldColumns();
					for(String[] ss:fieldColumns){
						if(isTypeOK(parameter.getOperator(), columnType, 
									ss[1], userName, dbType)&&result.contains(ss[0])==false){
							
								result.add(ss[0]);
						}						
					}
					break;
				}
			}
		}
	
		if(canSelectNullValueParameters.contains(parameter.getName()+operatorClassName)){
			result.add(0,"");
		}
		
		return result;
	
	}
 
}
