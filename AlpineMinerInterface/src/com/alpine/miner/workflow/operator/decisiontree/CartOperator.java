/**
 * ClassName CartOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.decisiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.NumberUtil;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhao yong
 *
 */
public class CartOperator extends LearnerOperator {
 
		public static final List<String> parameterNames = Arrays.asList(new String[]{
				OperatorParameter.NAME_dependentColumn,
                OperatorParameter.NAME_forceRetrain,
                OperatorParameter.NAME_columnNames,
                OperatorParameter.NAME_maximal_depth,
				OperatorParameter.NAME_confidence,
				OperatorParameter.NAME_number_of_prepruning_alternatives,
				OperatorParameter.NAME_minimal_size_for_split,
				OperatorParameter.NAME_no_pruning,
				OperatorParameter.NAME_no_pre_pruning,
				OperatorParameter.NAME_size_threshold_load_data,
				OperatorParameter.NAME_minimal_leaf_size
		});
	 	
	public CartOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_TREE_CLASSIFICATION);
		addOutputClass(EngineModel.MPDE_TYPE_TREE_REGRESSION);
	}
 
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.CTREE_OPERATOR,locale);
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
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_maximal_depth)){
				validateNull(invalidParameterList, paraName, paraValue);
				paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
				if (!StringUtil.isEmpty(paraValue)
						&& !NumberUtil.isInteger(paraValue, 0, true,
								Integer.MAX_VALUE, true)
								&&!NumberUtil.isInteger(paraValue, -1, false,
										-1, false)
						&& !invalidParameterList.contains(paraName)) {
					invalidParameterList.add(paraName);
				}
			}else if(paraName.equals(OperatorParameter.NAME_confidence)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue,0.0000001,true,0.5,false,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_number_of_prepruning_alternatives)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_minimal_size_for_split)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_size_threshold_load_data)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_minimal_leaf_size)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,1,false,Integer.MAX_VALUE,true,variableModel);
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
		if(paraName.equals(OperatorParameter.NAME_maximal_depth)){
			return "5";
		}else if(paraName.equals(OperatorParameter.NAME_confidence)){
			return "0.25";
		}else if(paraName.equals(OperatorParameter.NAME_number_of_prepruning_alternatives)){
			return "3";
		}else if(paraName.equals(OperatorParameter.NAME_minimal_size_for_split)){
			return "4";
		}else if(paraName.equals(OperatorParameter.NAME_no_pruning)){
			return Resources.FalseOpt;
		}else if(paraName.equals(OperatorParameter.NAME_no_pre_pruning)){
			return Resources.FalseOpt;
		}else if(paraName.equals(OperatorParameter.NAME_size_threshold_load_data)){
			return "10000";
		}else if(paraName.equals(OperatorParameter.NAME_minimal_leaf_size)){
			return "2";
		}else{
			return super.getOperatorParameterDefaultValue(paraName);
		}	
	}
	
}
