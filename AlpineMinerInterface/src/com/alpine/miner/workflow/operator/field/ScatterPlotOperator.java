/**
 * ClassName ScatterPlotOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-26
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

public class ScatterPlotOperator extends AbstractOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_Y_Column,
			OperatorParameter.NAME_X_Column,
			OperatorParameter.NAME_C_Column,
	});

	public ScatterPlotOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SCATTERPLOT_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		String columnY=null;
		String columnX=null;
		String columnC=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue() instanceof String){
				if(paraName.equals(OperatorParameter.NAME_Y_Column)){
					columnY=(String)para.getValue();
				}else if(paraName.equals(OperatorParameter.NAME_X_Column)){
					columnX=(String)para.getValue();
				}else if(paraName.equals(OperatorParameter.NAME_C_Column)){
					columnC=(String)para.getValue();
				}
			}
		}
		for(OperatorParameter para:paraList){
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if (paraName.equals(OperatorParameter.NAME_Y_Column)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)){
					if(!StringUtil.isEmpty(columnY)
							&&!StringUtil.isEmpty(columnX)
							&&columnY.equals(columnX)){
						invalidParameterList.add(paraName);
					}else if(!StringUtil.isEmpty(columnY)
							&&!StringUtil.isEmpty(columnC)
							&&columnY.equals(columnC)){
						invalidParameterList.add(paraName);
					}else if(!fieldList.contains(paraValue)){
						invalidParameterList.add(paraName);
					}
				}	
			}else if(paraName.equals(OperatorParameter.NAME_X_Column)){
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)){
					if(!StringUtil.isEmpty(columnY)
							&&!StringUtil.isEmpty(columnX)
							&&columnY.equals(columnX)){
						invalidParameterList.add(paraName);
					}else if(!StringUtil.isEmpty(columnX)
							&&!StringUtil.isEmpty(columnC)
							&&columnX.equals(columnC)){
						invalidParameterList.add(paraName);
					}else if(!fieldList.contains(paraValue)){
						invalidParameterList.add(paraName);
					}
				}	
			}else if(paraName.equals(OperatorParameter.NAME_C_Column)){
				if(!StringUtil.isEmpty(paraValue)&&!fieldList.contains(paraValue)){
					invalidParameterList.add(paraName);
				}else if(!StringUtil.isEmpty(columnY)
						&&!StringUtil.isEmpty(columnC)
						&&columnY.equals(columnC)){
					invalidParameterList.add(paraName);
				}else if(!StringUtil.isEmpty(columnX)
						&&!StringUtil.isEmpty(columnC)
						&&columnX.equals(columnC)){
					invalidParameterList.add(paraName);
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

}
