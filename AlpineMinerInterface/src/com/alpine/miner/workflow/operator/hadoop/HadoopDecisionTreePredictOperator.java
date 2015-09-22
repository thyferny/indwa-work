/**
 * ClassName HadoopDecisionTreePredictOperator.java
 *
 * Version information: 1.00
 *
 * Date: 2012-10-08
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;

public class HadoopDecisionTreePredictOperator extends HadoopPredictOperator {

	public HadoopDecisionTreePredictOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_TREE_CLASSIFICATION);
//		addInputClass(EngineModel.MPDE_TYPE_HADOOP_TREE_REGRESSION);
	}
 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.TREE_PREDICT_OPERATOR,locale);
	}

	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputFileInfo> dbList = new ArrayList<OperatorInputFileInfo>();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof OperatorInputFileInfo){
					dbList.add((OperatorInputFileInfo)obj);
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
