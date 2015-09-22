/**
 * ClassName N2TOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
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
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhao yong
 *
 */
public class IntegerToTextOperator extends DataOperationOperator {
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_modifyOriginTable,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			OperatorParameter.NAME_columnNames
	});

	public IntegerToTextOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.N2T_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableNumColumnsList(this, false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		
		String modifyOriginTable=null;
		for(OperatorParameter para:paraList){
			if(para.getName().equals(OperatorParameter.NAME_modifyOriginTable)){
				modifyOriginTable=(String)para.getValue();
				break;
			}
		}
		if(StringUtil.isEmpty(modifyOriginTable)){
			modifyOriginTable=Resources.FalseOpt;
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			if(modifyOriginTable.equals(Resources.FalseOpt)&&paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(modifyOriginTable.equals(Resources.FalseOpt)&&paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(modifyOriginTable.equals(Resources.FalseOpt)&&paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(modifyOriginTable.equals(Resources.FalseOpt)&&paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_modifyOriginTable)){
				validateNull(invalidParameterList, paraName, paraValue);
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

		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				String modifyOriginTable=(String)getOperatorParameter(OperatorParameter.NAME_modifyOriginTable).getValue();
				if(!StringUtil.isEmpty(modifyOriginTable)&&modifyOriginTable.equals("false")){		
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				}
				
				
				String columnNames=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				if(StringUtil.isEmpty(columnNames)){continue;}
				
				String[] columnArray=columnNames.split(",");
				List<String[]> fieldColumns=operatorInputTableInfo.getFieldColumns();
				for(String s:columnArray){
					for(String[] fieldColumn:fieldColumns){
						if(s.equals(fieldColumn[0])){
							fieldColumn[1]=ParameterUtility.getTextType(operatorInputTableInfo.getSystem());
							break;
						}
					}
				}
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
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_modifyOriginTable)){
			return Resources.FalseOpt;
		}else{
			return super.getOperatorParameterDefaultValue(paraName);
		}	
	}
}
