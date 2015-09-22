/**
 * ClassName TreePredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.decisiontree;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.PredictOperator;

public class TreePredictOperator extends PredictOperator {

	public TreePredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_TREE_CLASSIFICATION);
		addInputClass(EngineModel.MPDE_TYPE_TREE_REGRESSION);
	}
 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TREE_PREDICT_OPERATOR,locale);
	}

	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputTableInfo> dbList = new ArrayList<OperatorInputTableInfo>();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof OperatorInputTableInfo){
					dbList.add((OperatorInputTableInfo)obj);
				}else if(obj instanceof EngineModel){
					modelList.add((EngineModel)obj);
				}
			}
		}
		if((dbList.size()==1) && (modelList.size() ==1)){
			return true;
		}else{
			return false;
		}
	}
	
	
}