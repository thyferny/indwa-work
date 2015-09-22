/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OutputCreationParameterWebModel
 * Apr 5, 2012
 */
package com.alpine.miner.impls.web.resource.operator.ordinary;

import java.util.List;

import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.storageparam.StorageParameterModel;

/**
 * @author Gary
 *
 */
public class OutputCreationParameterWebModel {

	private StorageParameterModel originalModel;
	private String databaseType;
	
	/**
	 * 
	 */
	public OutputCreationParameterWebModel(StorageParameterModel originalModel, List<Object> operatorInputList) {
		this.originalModel = originalModel;
		buildDatabaseType(operatorInputList);
	}
	
	private void buildDatabaseType(List<Object> operatorInputList){
		if(operatorInputList == null){
			return;
		}
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo tableInfo = ((OperatorInputTableInfo) obj);
				databaseType = tableInfo.getSystem();
				return;
			}
		}
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public StorageParameterModel getOriginalModel() {
		return originalModel;
	}
}
