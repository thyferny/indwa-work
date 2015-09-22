/**
 * ClassName EMClusteringOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-19
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author Jeff Dong
 *
 */
public class EMClusteringOperator extends LearnerOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_clusterNumber,
			OperatorParameter.NAME_epislon,
			OperatorParameter.NAME_maxIterationNumber,
			OperatorParameter.NAME_columnNames,
			OperatorParameter.NAME_initClusterSize
	});
	
	public EMClusteringOperator() {
		super(parameterNames);
		this.addInputClass(OperatorInputTableInfo.class.getName());
		this.addOutputClass(EngineModel.MPDE_TYPE_EMCLUSTER);
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_clusterNumber)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,true,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_epislon)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, true, Double.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_maxIterationNumber)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_initClusterSize)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
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
		if(paraName.equals(OperatorParameter.NAME_clusterNumber)){
			return "3";
		}else if(paraName.equals(OperatorParameter.NAME_epislon)){
			return "0.00001";
		}else if(paraName.equals(OperatorParameter.NAME_maxIterationNumber)){
			return "20";
		}else if(paraName.equals(OperatorParameter.NAME_initClusterSize))
		{
			return "10";
		}else{
			return super.getOperatorParameterDefaultValue(paraName);
		}	
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.EM_CLUSTERING_OPERATOR,locale);
	}

}
