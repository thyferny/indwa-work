/**
 * ClassName PivotOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoOracle;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.GPSqlType;
import com.alpine.utility.db.OraSqlType;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class PivotOperator extends DataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_pivotColumn,
			OperatorParameter.NAME_groupByColumn,
			OperatorParameter.NAME_aggregateColumn,
			OperatorParameter.NAME_aggregateType,
			OperatorParameter.NAME_Use_Array,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			
	});
	
	public PivotOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.PIVOT_OPERATOR,locale);
	}
 
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String pivotColumn=null;
		String groupByColumn=null;
		for(OperatorParameter para:paraList){
			if(para.getName().equals(OperatorParameter.NAME_pivotColumn)){
				pivotColumn=(String)para.getValue();
			}else if(para.getName().equals(OperatorParameter.NAME_groupByColumn)){
				groupByColumn=(String)para.getValue();
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
			}else if(paraName.equals(OperatorParameter.NAME_aggregateColumn)){
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_pivotColumn)){
				if(!StringUtil.isEmpty(pivotColumn)&&!StringUtil.isEmpty(groupByColumn)
						&&pivotColumn.equals(groupByColumn)&&!invalidParameterList.contains(paraName)){
					invalidParameterList.add(paraName);
				}
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_groupByColumn)){
				if(!StringUtil.isEmpty(pivotColumn)&&!StringUtil.isEmpty(groupByColumn)
						&&pivotColumn.equals(groupByColumn)&&!invalidParameterList.contains(paraName)){
					invalidParameterList.add(paraName);
				}
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_aggregateType)){
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
		List<Object> operatorInputList=new ArrayList<Object>();
		
		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				
				String pivotColumn=(String)getOperatorParameter(OperatorParameter.NAME_pivotColumn).getValue();
				String groupColumn=(String)getOperatorParameter(OperatorParameter.NAME_groupByColumn).getValue();
				String useArray=(String)getOperatorParameter(OperatorParameter.NAME_Use_Array).getValue();
				
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				
				List<String[]> fieldColumns=operatorInputTableInfo.getFieldColumns();
				Map<String,String> dataTypeMap=new HashMap<String,String>();
				List<String[]> newFieldColumns=new ArrayList<String[]>();
				String[] pivotColumnArray=null;
				for(String[] fieldColumn:fieldColumns){
					if(!StringUtil.isEmpty(pivotColumn)&&pivotColumn.equals(fieldColumn[0])){
						pivotColumnArray=new String[]{pivotColumn,fieldColumn[1]};
						newFieldColumns.add(pivotColumnArray);
						dataTypeMap.put(pivotColumn, fieldColumn[1]);
					}else if(!StringUtil.isEmpty(groupColumn)&&groupColumn.equals(fieldColumn[0])){
						newFieldColumns.add(new String[]{groupColumn,fieldColumn[1]});
						dataTypeMap.put(groupColumn, fieldColumn[1]);
					}
				}

				if(!StringUtil.isEmpty(useArray)
						&&useArray.equalsIgnoreCase(Resources.TrueOpt)){
					if(dataTypeMap.containsKey(pivotColumn)){
						if(operatorInputTableInfo.getSystem().equals(DataSourceInfoOracle.dBType)){
							newFieldColumns.remove(pivotColumnArray);
							pivotColumnArray[1]=OraSqlType.FLOATARRAYARRAY;
							newFieldColumns.add(pivotColumnArray);
						}else if(operatorInputTableInfo.getSystem().equals(DataSourceInfoPostgres.dBType)
						||operatorInputTableInfo.getSystem().equals(DataSourceInfoGreenplum.dBType)){
							newFieldColumns.remove(pivotColumnArray);
							pivotColumnArray[1]=GPSqlType.ARRAY;
							newFieldColumns.add(pivotColumnArray);
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
	
	
	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> paraList = super.fromXML(opTypeXmlManager, opNode);
		OperatorParameter operatorParameter=null;
		for (OperatorParameter para : paraList) {
			if (para.getName().equals(OperatorParameter.NAME_Use_Array)) {
				operatorParameter=para;
				break;
			}
		}
		String useArray=(String)operatorParameter.getValue();
		if(!StringUtil.isEmpty(useArray)
				&&useArray.equals(Resources.TrueOpt)){
			addOutputClass(OperatorInputTableInfo.class.getName());
		}else{
			if(getOutputClassList()!=null&&getOutputClassList().contains(OperatorInputTableInfo.class.getName())){
				getOutputClassList().remove(OperatorInputTableInfo.class.getName());
			}
		}
		return paraList;
	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_aggregateType)){
			return "sum";
		}else if(paraName.equals(OperatorParameter.NAME_Use_Array)){
			return Resources.FalseOpt;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
