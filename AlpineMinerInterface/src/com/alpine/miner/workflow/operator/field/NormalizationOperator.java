/**
 * ClassName NormalizationOperator.java
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
import com.alpine.miner.util.NumberUtil;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.DataSourceType;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhao yong
 *
 */
public class NormalizationOperator extends DataOperationOperator {
	
	public static final String RangeTransformation = "Range-Transformation";
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_method,
			OperatorParameter.NAME_rangeMin,
			OperatorParameter.NAME_rangeMax,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			OperatorParameter.NAME_columnNames,
	});
	

	public NormalizationOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NORMALIZATION_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableNumColumnsList(this, false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String method=null;
		String min=null;
		String max=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(paraName.equals(OperatorParameter.NAME_method)){
				method=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_rangeMin)){
				min=(String)para.getValue();
				min = VariableModelUtility.getReplaceValue(variableModel, min);
			}else if(paraName.equals(OperatorParameter.NAME_rangeMax)){
				max=(String)para.getValue();
				max = VariableModelUtility.getReplaceValue(variableModel, max);
			}
		}
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
			}else if(paraName.equals(OperatorParameter.NAME_method)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_rangeMin)){
				if(!StringUtil.isEmpty(method)&&method.equals(RangeTransformation)){
					if(!StringUtil.isEmpty(min)&&NumberUtil.isNumber(min)){
						if(!StringUtil.isEmpty(max)&&NumberUtil.isNumber(max)){
							double minD = Double.parseDouble(min);
							double maxD = Double.parseDouble(max);
							if (minD>=maxD) {
								invalidParameterList.add(paraName);
								continue;
							}
						}
					}else{
						invalidParameterList.add(paraName);
						continue;
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_rangeMax)){
				if(!StringUtil.isEmpty(method)&&method.equals(RangeTransformation)){
					if(!StringUtil.isEmpty(max)&&NumberUtil.isNumber(max)){
						if(!StringUtil.isEmpty(min)&&NumberUtil.isNumber(min)){
							double minD = Double.parseDouble(min);
							double maxD = Double.parseDouble(max);
							if (minD>=maxD) {
								invalidParameterList.add(paraName);
								continue;
							}
						}
					}else{
						invalidParameterList.add(paraName);
						continue;
					}
				}
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
		List<Object> operatorInputList=getOperatorInputList();
		
		for (Object obj: operatorInputList){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				
				List<String[]> fieldColumns = operatorInputTableInfo.getFieldColumns();
				String columnName=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				
				if(StringUtil.isEmpty(columnName)==false){
					String[] columnNames = columnName.split(",");
					for(int i=0;i<columnNames.length;i++){
						for(String[] ss:fieldColumns){
							if(ss[0].equals(columnNames[i])){
								ss[1]=DataSourceType.getDataSourceType(operatorInputTableInfo.getSystem()).getDoubleType();
								break;
							}
						}
					}
				}
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
		if(paraName.equals(OperatorParameter.NAME_method)){
			return RangeTransformation;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
		
	}
}
