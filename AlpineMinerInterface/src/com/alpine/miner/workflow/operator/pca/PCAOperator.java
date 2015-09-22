package com.alpine.miner.workflow.operator.pca;

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

public class PCAOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			
			
			
			OperatorParameter.NAME_analysisType,
			OperatorParameter.NAME_percent,
			
			OperatorParameter.NAME_PCAQoutputSchema,
			OperatorParameter.NAME_PCAQoutputTable,
			OperatorParameter.NAME_PCAQoutputTable_StorageParams,
			OperatorParameter.NAME_PCAQDropIfExist,
			
			OperatorParameter.NAME_PCAQvalueOutputSchema,
			OperatorParameter.NAME_PCAQvalueOutputTable,
			OperatorParameter.NAME_PCAQvalueOutputTable_StorageParams,
			OperatorParameter.NAME_PCAQvalueDropIfExist,
				
			OperatorParameter.NAME_columnNames,
			OperatorParameter.NAME_remainColumns///same as NAME_columnNames
	});

	public PCAOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.PCA_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();

		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_PCAQDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_PCAQoutputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_PCAQoutputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_PCAQvalueDropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_PCAQvalueOutputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_PCAQvalueOutputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_remainColumns)){
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_percent)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, true, 1, true,variableModel);
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
		if (paraName.equals(OperatorParameter.NAME_analysisType)){
			return "COV-POP";
		}else if(paraName.equals(OperatorParameter.NAME_percent)){
			return "0.95";
		}else if(paraName.equals(OperatorParameter.NAME_PCAQDropIfExist)){
			return Resources.YesOpt;
		}else if(paraName.equals(OperatorParameter.NAME_PCAQvalueDropIfExist)){
			return Resources.YesOpt;
		}else if(paraName.equals(OperatorParameter.NAME_PCAQoutputSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else if(paraName.equals(OperatorParameter.NAME_PCAQvalueOutputSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
