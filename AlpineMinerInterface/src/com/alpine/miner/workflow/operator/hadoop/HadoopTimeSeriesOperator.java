/**
 * ClassName HadoopTimeSeriesOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-11-7
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.NumberUtil;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public class HadoopTimeSeriesOperator extends HadoopLearnerOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_IDColumn_lower,
			OperatorParameter.NAME_ValueColumn,
			OperatorParameter.NAME_groupColumn,
			OperatorParameter.NAME_AR_Order,
			OperatorParameter.NAME_MA_Order	,
			OperatorParameter.NAME_Degree_of_differencing,
			OperatorParameter.NAME_LengthOfWindow,
			OperatorParameter.NAME_TimeFormat,
			
	});
	
	public HadoopTimeSeriesOperator() {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_HADOOP_ARIMA);
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String AR=null;
		String MA=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(paraName.equals(OperatorParameter.NAME_AR_Order)){
				AR=(String)para.getValue();
			}else if(paraName.equals(OperatorParameter.NAME_MA_Order)){
				MA=(String)para.getValue();
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_IDColumn_lower)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_ValueColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_groupColumn)){
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_AR_Order)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,false,10,false,variableModel);
				if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(AR)&&!StringUtil.isEmpty(AR)){
					if(NumberUtil.isInteger(AR)&&
							NumberUtil.isInteger(MA)){
						if(Integer.parseInt(AR)==0&&Integer.parseInt(MA)==0){
							invalidParameterList.add(paraName);
						}					
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_MA_Order)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,false,10,false,variableModel);
				if(!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(AR)&&!StringUtil.isEmpty(AR)){
					if(NumberUtil.isInteger(AR)&&
							NumberUtil.isInteger(MA)){
						if(Integer.parseInt(AR)==0&&Integer.parseInt(MA)==0){
							invalidParameterList.add(paraName);
						}					
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_Degree_of_differencing)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,false,10,false,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_LengthOfWindow)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,true,500000,true,variableModel);
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
		if(paraName.equals(OperatorParameter.NAME_AR_Order)){
			return "1";
		}else if(paraName.equals(OperatorParameter.NAME_MA_Order)){
			return "1";
		}else if(paraName.equals(OperatorParameter.NAME_Degree_of_differencing)){
			return "0";
		}else if(paraName.equals(OperatorParameter.NAME_LengthOfWindow)){
			return "1000";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TIMESERIES_OPERATOR,locale);
	}

}
