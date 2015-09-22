/**
 * ClassName ColumnFilterOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-9-3
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

public class ColumnFilterOperator extends DataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_columnNames,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist
 
	});
	
	public ColumnFilterOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}
 
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.COLUMNFILTER_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}		
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}	
	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		List<Object> inputList = getOperatorInputList();
		if(inputList==null){
			return operatorInputList;	
		}
		for (Object obj: inputList){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo.setTableType((String)getOperatorParameter(OperatorParameter.NAME_outputType).getValue());
				
				List<String[]> newFieldColumns=new ArrayList<String[]>();
				
				String columnNames=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				
				if(!StringUtil.isEmpty(columnNames)){
					String[] columnArray=columnNames.split(",");
					List<String[]> fieldColumns=operatorInputTableInfo.getFieldColumns();	
					for(String s:columnArray){
						for(String[] fieldColumn:fieldColumns){
							if(fieldColumn[0].equals(s)){
								newFieldColumns.add(new String[]{s,fieldColumn[1]});
								break;
							}
						}
					}
				}

				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(operatorInputTableInfo);
				break;
			}
		}	
		
		return operatorInputList;
	}

	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
}
