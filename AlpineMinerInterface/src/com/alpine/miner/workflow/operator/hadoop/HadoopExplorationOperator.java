/**
 * ClassName HadoopBarChartOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.List;

import com.alpine.miner.workflow.operator.OperatorInputFileInfo;

/**
 * @author zhao yong
 *
 */
public abstract class HadoopExplorationOperator extends HadoopOperator {
	
 
	public HadoopExplorationOperator(List<String> parameterNames) {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
	}
  
	
	@Override
	public List<Object> getOperatorOutputList() {
		return null;
	}
}
