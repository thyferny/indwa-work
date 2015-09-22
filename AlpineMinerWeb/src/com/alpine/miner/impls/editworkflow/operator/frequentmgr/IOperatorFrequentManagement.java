/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * IOperatorFrequentManagement.java
 */
package com.alpine.miner.impls.editworkflow.operator.frequentmgr;

import java.util.List;

import com.alpine.miner.impls.editworkflow.operator.frequentmgr.impl.OperatorFrequentManagementFileImpl;

/**
 * @author Gary
 * Aug 22, 2012
 */
public interface IOperatorFrequentManagement {
	
	IOperatorFrequentManagement INSTANCE = new OperatorFrequentManagementFileImpl();

	void increaseFrequent(String operatorClassName, String userName);
	
	List<String> getFrequentOperatorNameList(int topSize, String userName);
}
