/**
 * ClassName SelectedTableParamterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.sampling.AbstractSamplingOperator;
import com.alpine.miner.workflow.operator.sampling.SampleSelectorOperator;
import com.alpine.utility.file.StringUtil;

/**
 * This is used for sampleing
 * @author zhaoyong
 *
 */

public class SelectedTableParamterHelper extends SingleSelectParameterHelper {
 
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName,ResourceType dbType) {
	
		return getAvaliableSamplingTables(parameter);
	}

	private List<String> getAvaliableSamplingTables(OperatorParameter parameter) {
		// TODO find the possible table name from the smapling operator...
		
		List<String> tableNames= new ArrayList<String>();
		
		SampleSelectorOperator samplingOperator= (SampleSelectorOperator) parameter.getOperator();
		VariableModel variableModel = samplingOperator.getWorkflow().getParentVariableModel();
		
		List<Operator> parents = samplingOperator.getParentOperators();
		
		if(parents!=null){
			for (Iterator<Operator> iterator = parents.iterator(); iterator.hasNext();) {
				Operator operator = iterator.next();
				if(operator instanceof AbstractSamplingOperator){
					String schema = (String)operator.getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue();
					String prefix=(String)operator.getOperatorParameter(OperatorParameter.NAME_outputTable).getValue();
					String sampleCountStr=(String)operator.getOperatorParameter(OperatorParameter.NAME_sampleCount).getValue();
					if(StringUtil.isEmpty(schema)
							||StringUtil.isEmpty(prefix)
							||StringUtil.isEmpty(sampleCountStr)){
						continue;
					}
					sampleCountStr=VariableModelUtility.getReplaceValue(variableModel, sampleCountStr);
					int sampleCount =Integer.parseInt(sampleCountStr);
					for (int i = 0; i < sampleCount; i++) {
						tableNames.add(schema+"."+prefix+"_"+String.valueOf(i));
					}
				}
			}
		}
		
		return tableNames;
	}



}
