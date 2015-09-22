/**
 * ClassName KmeansOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhao yong
 *
 */
public class KMeansOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_IDColumn_lower,
			OperatorParameter.NAME_k,
			OperatorParameter.NAME_distanse,
			// OperatorParameter.NAME_clusterColumnName,
			OperatorParameter.NAME_split_Number,
			OperatorParameter.NAME_max_runs,
			OperatorParameter.NAME_max_optimization_steps,
			OperatorParameter.NAME_Use_Array,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			OperatorParameter.NAME_columnNames
			
  
	});
	
	public KMeansOperator() {
		super(parameterNames);
		this.addInputClass(OperatorInputTableInfo.class.getName());
		this.addOutputClass(OperatorInputTableInfo.class.getName());
	}
 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.KMEANS_OPERATOR,locale);
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
			if(paraName.equals(OperatorParameter.NAME_k)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,2,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_distanse)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_split_Number)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,2,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_max_runs)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_max_optimization_steps)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
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
			}else if(paraName.equals(OperatorParameter.NAME_IDColumn_lower)){
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
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
		for(Object obj:getOperatorInputList()){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
			
				String clusterNo = getClusterNo(operatorInputTableInfo.getFieldColumns());
				
				ArrayList<String> outList = new ArrayList<String>();
				
				String columnNames=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				if(!StringUtil.isEmpty(columnNames)){
					String[] columns =columnNames.split(",");
					for(String s:columns){
						outList.add(s);
					}
				}
				
				String id=(String)getOperatorParameter(OperatorParameter.NAME_IDColumn_lower).getValue();
				if(!StringUtil.isEmpty(id)){
					outList.add(id);
				}
				List<String[]> newFieldColumns=new ArrayList<String[]>();
				for(String s:outList){
					for(String[] fieldColumn:operatorInputTableInfo.getFieldColumns()){
						if(s.equals(fieldColumn[0])){
							newFieldColumns.add(new String[]{s,fieldColumn[1]});
							break;
						}
					}
				}
				newFieldColumns.add(new String[]{clusterNo,"INTEGER"});
				
				
				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(operatorInputTableInfo);
				break;
			}
		}
		
		return operatorInputList;
	}


	private String getClusterNo(List<String[]> fieldColumns) {
		List<String> list=new ArrayList<String>();
		for(String[] ss:fieldColumns){
			list.add(ss[0]);
		}
		String cluster = "alpine_cluster";
		if(fieldColumns == null){
			return cluster;
		}
		int id = 1;
		if(!list.contains(cluster)){
			return cluster;
		}
		while(true){
			if(list.contains(cluster+"_"+id)){
				id++;
			}else{
				break;
			}
		}
		return cluster+"_"+id;
	}


	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
		
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_k)){
			return "3";
		}else if(paraName.equals(OperatorParameter.NAME_distanse)){
			return "Euclidean";
		}else if(paraName.equals(OperatorParameter.NAME_split_Number)){
			return "5";
		}else if(paraName.equals(OperatorParameter.NAME_max_runs)){
			return "1";
		}else if(paraName.equals(OperatorParameter.NAME_max_optimization_steps)){
			return "10";
		}else if(paraName.equals(OperatorParameter.NAME_Use_Array)){
			return Resources.FalseOpt;
		}else{
			return super.getOperatorParameterDefaultValue(paraName);
		}	
	}
	
}
