/**
 * ClassName RandomSamplingOperator.java
 *
 * Version information:1.00
 *
 * Date:Jun 8, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.miner.workflow.operator.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhao yong
 *
 */
public class StratifiedSamplingOperator extends AbstractSamplingOperator {
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_samplingColumn,
			OperatorParameter.NAME_sampleCount,
			OperatorParameter.NAME_sampleSizeType,
			OperatorParameter.NAME_sampleSize,
			OperatorParameter.NAME_randomSeed,
			OperatorParameter.NAME_consistent,
			OperatorParameter.NAME_disjoint,
			OperatorParameter.NAME_keyColumnList,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			
	});
		
	public StratifiedSamplingOperator() {
		super(parameterNames);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.STRATIFIED_SAMPLING_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String sampleSizeType=null;
		String consistent=null;
		String disjoint = null;
		SampleSizeModel sampleSizeModel = null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()==null){
				continue;
			}
			
			if(para.getValue() instanceof String){
				String paraValue=(String)para.getValue();
				if(paraName.equals(OperatorParameter.NAME_sampleSizeType)){
					sampleSizeType=paraValue;
				}else if(paraName.equals(OperatorParameter.NAME_consistent)){
					consistent=paraValue;
				}else if(paraName.equals(OperatorParameter.NAME_disjoint)){
					disjoint=paraValue;
				}
			}else if(para.getValue() instanceof SampleSizeModel){
				sampleSizeModel=(SampleSizeModel)para.getValue();
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_sampleCount)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,true,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_sampleSizeType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_sampleSize)){
				if(sampleSizeModel==null
						||sampleSizeModel.getSampleIdList()==null
						||sampleSizeModel.getSampleIdList().size()==0){
					invalidParameterList.add(paraName);
				}else{
					boolean validPercentage=false;
					if(StringUtil.isEmpty(disjoint)==false&&disjoint.equals(com.alpine.utility.db.Resources.TrueOpt)
								&&com.alpine.utility.db.Resources.PercentageType.equals(sampleSizeType)){
							validPercentage=true;
					}
					double sum=0;
					for(int i=0;i<sampleSizeModel.getSampleIdList().size();i++){
						String sampleSize = sampleSizeModel.getSampleSizeList().get(i);
						sampleSize=VariableModelUtility.getReplaceValue(variableModel, sampleSize);
						if(StringUtil.isEmpty(sampleSize)==true||AlpineUtil.isNumber(sampleSize)==false){
							if(!invalidParameterList.contains(paraName)){
								invalidParameterList.add(paraName);
							}
						}
						if(!StringUtil.isEmpty(sampleSizeType)){
							if(sampleSizeType
									.equals(com.alpine.utility.db.Resources.PercentageType)){
									validateNumber(invalidParameterList, paraName, sampleSize.toString(),0,true,100,false,variableModel);	
									sum=sum+Double.parseDouble(sampleSize);
							}else{
								if(new Double(sampleSize).doubleValue()==new Double(sampleSize).intValue()){//judge whether int type
									validateInteger(invalidParameterList, paraName, String.valueOf(new Double(sampleSize).intValue()),0,true,Integer.MAX_VALUE,true,variableModel);
								}else{
									if(!invalidParameterList.contains(paraName)){
										invalidParameterList.add(paraName);
									}
								}	
							}	
						}
					}
					if(!invalidParameterList.contains(paraName)&&validPercentage&&sum>100){
						invalidParameterList.add(paraName);
					}
				}		
			}else if(paraName.equals(OperatorParameter.NAME_randomSeed)){
				if(!StringUtil.isEmpty(consistent)
						&&consistent.equalsIgnoreCase("true")){
					validateNull(invalidParameterList, paraName, paraValue);
					validateNumber(invalidParameterList, paraName, paraValue,0,false,1,false,variableModel);
				}else{
					validateNumber(invalidParameterList, paraName, paraValue,0,false,1,false,variableModel);
				}
			}else if(paraName.equals(OperatorParameter.NAME_keyColumnList)){
				if(!StringUtil.isEmpty(consistent)
						&&consistent.equalsIgnoreCase("true")){
					validateNull(invalidParameterList, paraName, paraValue);
					validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
				}
			}else if(paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_samplingColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
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
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_disjoint)){
			return Resources.FalseOpt;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
