/**
 * ClassName SubFlowOperator
 *
 * Version information: 1.00
 *
 * Data: 2012-4-8
 *
 * COPYRIGHT   2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.structual;

import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;

/**
 * @author zhao yong
 *
 */
public class NoteOperator extends AbstractOperator {
 
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_note, 
		 
	});
 

	public NoteOperator() {
		super(parameterNames);
		 
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NOTE_OPERATOR,locale);
	}
 

	@Override
	public boolean isVaild(VariableModel variableModel) {
		 
			return true;
 
	}
	
	@Override
	public boolean isInputObjectsReady() {
	 
			return true;
	 
	}
 

}
