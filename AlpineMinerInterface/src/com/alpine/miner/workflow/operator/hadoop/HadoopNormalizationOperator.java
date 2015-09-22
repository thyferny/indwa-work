/**
 * ClassName HadoopNormalizationOperator.java
 *
 * Version information: 1.00
 *
 * Data: Aug 1, 2012
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.NumberUtil;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author Jeff Dong
 *
 */
public class HadoopNormalizationOperator extends HadoopDataOperationOperator {
	
	public static final String RangeTransformation = "Range-Transformation";
	
	public HadoopNormalizationOperator() {
		super(parameterNames);
	}

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_method,
			OperatorParameter.NAME_rangeMin,
			OperatorParameter.NAME_rangeMax,
			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
			OperatorParameter.NAME_columnNames,
	});

	
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
			if(paraName.equals(OperatorParameter.NAME_columnNames)){
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
			}else {
				validateHadoopStorageParameter(paraName,paraValue,invalidParameterList);
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
			if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo=(OperatorInputFileInfo)obj;
				operatorInputFileInfo=operatorInputFileInfo.clone();
				
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());

				operatorInputFileInfo.setHadoopFileName(getOutputFileName());
				
				String columnName=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				 //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				if(columnInfo==null){
					continue;
				}
				List<String> columnNameList = columnInfo.getColumnNameList();
				List<String> columnTypeList = columnInfo.getColumnTypeList();
				if(StringUtil.isEmpty(columnName)==false){
					String[] columnNames = columnName.split(",");
					for(int i=0;i<columnNames.length;i++){
						int columnNameIdx = columnNameList.indexOf(columnNames[i]);
						if(columnNameIdx == -1){//fix Pivotal 41661923
							continue;
						}
						columnTypeList.set(columnNameIdx, HadoopDataType.DOUBLE);
					}
				}
				columnInfo.setIsFirstLineHeader(Resources.FalseOpt);
				operatorInputFileInfo.setColumnInfo(columnInfo);
				operatorInputList.add(operatorInputFileInfo);
				break;
			}
		}
		return operatorInputList;
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NORMALIZATION_OPERATOR,locale);
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
