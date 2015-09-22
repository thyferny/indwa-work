/**
 * ClassName BoxAndWhiskerOperator.java
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

public class BoxAndWhiskerOperator extends AbstractOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_valueDomain_Column,
			OperatorParameter.NAME_seriesDomain_Column,
			OperatorParameter.NAME_typeDomain_Column,
	});

	public BoxAndWhiskerOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.BOXANDWISKER_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		String columnValue=null;
		String columnSeries=null;
		String columnType=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue() instanceof String){
				if(paraName.equals(OperatorParameter.NAME_valueDomain_Column)){
					columnValue=(String)para.getValue();
				}else if(paraName.equals(OperatorParameter.NAME_seriesDomain_Column)){
					columnSeries=(String)para.getValue();
				}else if(paraName.equals(OperatorParameter.NAME_typeDomain_Column)){
					columnType=(String)para.getValue();
				}
			}
		}
		for(OperatorParameter para:paraList){
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if (paraName.equals(OperatorParameter.NAME_valueDomain_Column)) {
				validateNull(invalidParameterList, paraName, paraValue);
				if(!invalidParameterList.contains(paraName)){
					if(!StringUtil.isEmpty(columnValue)
							&&!StringUtil.isEmpty(columnSeries)
							&&columnValue.equals(columnSeries)){
						invalidParameterList.add(paraName);
					}else if(!StringUtil.isEmpty(columnValue)
							&&!StringUtil.isEmpty(columnType)
							&&columnValue.equals(columnType)){
						invalidParameterList.add(paraName);
					}else if(!fieldList.contains(paraValue)){
						invalidParameterList.add(paraName);
					}
				}	
			}else if(paraName.equals(OperatorParameter.NAME_seriesDomain_Column)){
				if(!StringUtil.isEmpty(paraValue)&&!fieldList.contains(paraValue)){
					invalidParameterList.add(paraName);
				}else if(!StringUtil.isEmpty(columnValue)
						&&!StringUtil.isEmpty(columnSeries)
						&&columnValue.equals(columnSeries)){
					invalidParameterList.add(paraName);
				}else if(!StringUtil.isEmpty(columnSeries)
						&&!StringUtil.isEmpty(columnType)
						&&columnSeries.equals(columnType)){
					invalidParameterList.add(paraName);
				}
			}else if(paraName.equals(OperatorParameter.NAME_typeDomain_Column)){
				if(!StringUtil.isEmpty(paraValue)&&!fieldList.contains(paraValue)){
					invalidParameterList.add(paraName);
				}else if(!StringUtil.isEmpty(columnValue)
						&&!StringUtil.isEmpty(columnType)
						&&columnValue.equals(columnType)){
					invalidParameterList.add(paraName);
				}else if(!StringUtil.isEmpty(columnSeries)
						&&!StringUtil.isEmpty(columnType)
						&&columnSeries.equals(columnType)){
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
	
	@Override
	public List<Object> getOperatorOutputList() {
		return null;
	}

}
