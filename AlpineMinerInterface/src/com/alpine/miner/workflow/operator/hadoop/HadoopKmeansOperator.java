/**
 * ClassName HadoopKmeansOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-8
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Jeff Dong
 *
 */
public class HadoopKmeansOperator extends HadoopOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
            OperatorParameter.NAME_IDColumn_lower,
            OperatorParameter.NAME_k,
			OperatorParameter.NAME_distanse,
			// OperatorParameter.NAME_clusterColumnName,
			OperatorParameter.NAME_split_Number,
//			OperatorParameter.NAME_max_runs,
			OperatorParameter.NAME_max_optimization_steps,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
			OperatorParameter.NAME_columnNames,
	});
	public HadoopKmeansOperator() {
		super(parameterNames);
		this.addInputClass(OperatorInputFileInfo.class.getName());
		this.addOutputClass(OperatorInputFileInfo.class.getName());
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
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsName)){
				validateNull(invalidParameterList, paraName, paraValue);
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
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputFileInfo());
		return list;
	}
	
	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorOutputList=new ArrayList<Object>();
		for(Object obj:getOperatorInputList()){
			if(obj instanceof OperatorInputFileInfo){
                OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
                operatorInputFileInfo = operatorInputFileInfo.clone();
                //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

                String hadoopFileName = getOutputFileName();
                operatorInputFileInfo.setHadoopFileName(hadoopFileName);
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());
			
				List<String> newColumnNameList = new ArrayList<String>();
				List<String> newColumnTypeList = new ArrayList<String>();
				
				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				if(columnInfo==null){
					continue;
				}
				
				try {
					columnInfo = (FileStructureModel)columnInfo.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
				columnInfo.setIsFirstLineHeader(Resources.FalseOpt);
				List<String> columnNameList = columnInfo.getColumnNameList();
				List<String> columnTypeList = columnInfo.getColumnTypeList();
				
				String columnNames=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				if(!StringUtil.isEmpty(columnNames)){
					String[] columns =columnNames.split(",");
					for(String s:columns){
						newColumnNameList.add(s);
						for(int i=0;i<columnNameList.size();i++){
							if(s.equals(columnNameList.get(i))){
								newColumnTypeList.add(columnTypeList.get(i));
							}
						}
					}
				}

                String id=(String)getOperatorParameter(OperatorParameter.NAME_IDColumn_lower).getValue();
                if(!StringUtil.isEmpty(id)){
                    newColumnNameList.add(id);
                    for(int i=0;i<columnNameList.size();i++){
                        if(id.equals(columnNameList.get(i))){
                            newColumnTypeList.add(columnTypeList.get(i));
                        }
                    }
                }

				String clusterNo = AlpineUtil.generateClusterName(columnNameList);
				newColumnNameList.add(clusterNo);
				newColumnTypeList.add(HadoopDataType.INT);
				
				columnInfo.setColumnNameList(newColumnNameList);
				columnInfo.setColumnTypeList(newColumnTypeList);
				operatorInputFileInfo.setColumnInfo(columnInfo);
				operatorOutputList.add(operatorInputFileInfo);

				break;
			}
		}
		
		return operatorOutputList;
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(StringUtil.isEmpty(message) ==true){ 
			message = super.validateStoreResult(precedingOperator);
		}
		return message;
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
		}else{
			return super.getOperatorParameterDefaultValue(paraName);
		}	
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.KMEANS_OPERATOR,locale);
	}

}
