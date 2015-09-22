/**
 * ClassName SelectedFileParameterHelper.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-10
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.hadoop.HadoopRandomSamplingOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopSampleSelectorOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;

/**
 * @author Jeff Dong
 *
 */
public class SelectedFileParameterHelper extends SingleSelectParameterHelper {

	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName,ResourceType dbType) {
	
		List<String> fileNames= new ArrayList<String>();
		
		HadoopSampleSelectorOperator samplingOperator= (HadoopSampleSelectorOperator) parameter.getOperator();
		VariableModel variableModel = samplingOperator.getWorkflow().getParentVariableModel();
		
		List<Operator> parents = samplingOperator.getParentOperators();
		
		if(parents!=null){
			for (Iterator<Operator> iterator = parents.iterator(); iterator.hasNext();) {
				Operator operator = iterator.next();
				if(operator instanceof HadoopRandomSamplingOperator){
					String folderName = (String)operator.getOperatorParameter(OperatorParameter.NAME_HD_ResultsLocation).getValue();
					String prefix=(String)operator.getOperatorParameter(OperatorParameter.NAME_HD_ResultsName).getValue();
					String sampleCountStr=(String)operator.getOperatorParameter(OperatorParameter.NAME_sampleCount).getValue();
					if(StringUtil.isEmpty(folderName)
							||StringUtil.isEmpty(prefix)
							||StringUtil.isEmpty(sampleCountStr)){
						continue;
					}
					sampleCountStr=VariableModelUtility.getReplaceValue(variableModel, sampleCountStr);
					int sampleCount =Integer.parseInt(sampleCountStr);
					for (int i = 0; i < sampleCount; i++) {
						fileNames.add(folderName+"/"+prefix+"_"+String.valueOf(i+1));
					}
				}
			}
		}
		
		return fileNames;
	}
}
