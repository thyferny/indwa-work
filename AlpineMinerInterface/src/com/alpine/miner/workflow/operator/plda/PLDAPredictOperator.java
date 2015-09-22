/**
 * ClassName LinearRegressionPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.plda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.PredictOperator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;

public class PLDAPredictOperator extends PredictOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
						
			OperatorParameter.NAME_IterationNumber,
			
			OperatorParameter.NAME_PLDADocTopicOutputSchema ,
			OperatorParameter.NAME_PLDADocTopicOutputTable ,
			OperatorParameter.NAME_PLDADocTopicOutputTable_StorageParams,
			OperatorParameter.NAME_PLDADocTopicDropIfExist 
			  
			
	});
	
	public PLDAPredictOperator() {
		super(parameterNames);
		addInputClass(EngineModel.MPDE_TYPE_PLDA);
	}
 
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.PLDA_PREDICT_OPERATOR,locale);
	}

	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		
		super.isVaild(variableModel);
		List<String> invalidParameterList= new ArrayList<String>();//Arrays.asList(super.invalidParameters);
		if(super.invalidParameters!=null&&super.invalidParameters.length>0){
			for(int i=0;i<super.invalidParameters.length;i++){
				invalidParameterList.add(super.invalidParameters[i]);
			}
		}
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
		  if(paraName.equals(OperatorParameter.NAME_PLDADocTopicOutputSchema)){
					validateNull(invalidParameterList, paraName, paraValue);
					validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_PLDADocTopicOutputTable)){
					validateNull(invalidParameterList, paraName, paraValue);
					validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_PLDADocTopicDropIfExist)){
					validateNull(invalidParameterList, paraName, paraValue);
			}			else if(paraName.equals(OperatorParameter.NAME_IterationNumber)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,true,Integer.MAX_VALUE,true,variableModel);
				
			}
			
		}
		
		validateDuplicateTableName(invalidParameterList,  				 
				  OperatorParameter.NAME_outputSchema, OperatorParameter.NAME_outputTable ,
				  OperatorParameter.NAME_PLDADocTopicOutputSchema,OperatorParameter.NAME_PLDADocTopicOutputTable  );

		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}	
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if (paraName.equals(OperatorParameter.NAME_IterationNumber )){
			return "20";
		}else if (paraName.equals(OperatorParameter.NAME_PLDADocTopicDropIfExist)){
			return Resources.YesOpt;
		}else if (paraName.equals(OperatorParameter.NAME_PLDADocTopicOutputSchema)){
			return VariableModel.DEFAULT_SCHEMA;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}
}
