/**
 * ClassName SVMClassificationOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.svm;

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
import com.alpine.utility.file.StringUtil;

/**
 * @author zhao yong
 *
 */
public class SVMClassificationOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_kernel_type,
			
			OperatorParameter.NAME_degree,
			OperatorParameter.NAME_gamma,
			OperatorParameter.NAME_eta,
			
			OperatorParameter.NAME_nu,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_columnNames
	});
	
	public SVMClassificationOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_SVM_C);
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SVM_CLASSIFICATION_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		String kernelType=null;
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(paraName.equals(OperatorParameter.NAME_kernel_type)){
				kernelType=(String)para.getValue();
				break;
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_kernel_type)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_degree)){
				if(StringUtil.isEmpty(kernelType)
						||kernelType.equals("polynomial")){
					validateNull(invalidParameterList, paraName, paraValue);
					validateInteger(invalidParameterList, paraName, paraValue,0,true,Integer.MAX_VALUE,true,variableModel);
				}
			}else if(paraName.equals(OperatorParameter.NAME_gamma)){
				if(StringUtil.isEmpty(kernelType)
						||kernelType.equals("gaussian")){
					validateNull(invalidParameterList, paraName, paraValue);
					validateNumber(invalidParameterList, paraName, paraValue,0,true,Double.MAX_VALUE,true,variableModel);
				}
			}else if(paraName.equals(OperatorParameter.NAME_eta)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue,0,true,1,false,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_nu)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue,0,true,1,false,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
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
		if(paraName.equals(OperatorParameter.NAME_kernel_type)){
			return "dot product";
		}else if(paraName.equals(OperatorParameter.NAME_degree)){
			return "2";
		}else if(paraName.equals(OperatorParameter.NAME_gamma)){
			return "0.1";
		}else if(paraName.equals(OperatorParameter.NAME_eta)){
			return "0.05";
		}else if(paraName.equals(OperatorParameter.NAME_nu)){
			return "0.001";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
