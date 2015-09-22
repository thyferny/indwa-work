/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * SvdOperatorUtility.java
 */
package com.alpine.miner.impls.editworkflow.operator;

import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.tools.StringHandler;

/**
 * @author Gary
 * Nov 22, 2012
 */
public class SvdOperatorUtility {

	/**
	 * copy property's values from SVD operator to SVD Calculator operator
	 * this function should be called when update SVD operator's properties and connect SVD operator to SVD calculator.
	 * @param svdOperator
	 * @param calculatorOperator
	 */
	public static void syncSVDParams2SVDCalculator(Operator svdOperator, Operator calculatorOperator){
		final int svdOperatorIdx = 1,
				  svdCalculatorOperatorIdx = 0;
		final String[][] syncParamsNameMapping = {
				{OperatorParameter.NAME_UmatrixFullTable, OperatorParameter.NAME_UmatrixTable},
				{OperatorParameter.NAME_RowNameF, OperatorParameter.NAME_RowName},
				{OperatorParameter.NAME_UdependentColumn, OperatorParameter.NAME_dependentColumn},
				{OperatorParameter.NAME_VmatrixFullTable, OperatorParameter.NAME_VmatrixTable},
				{OperatorParameter.NAME_ColNameF, OperatorParameter.NAME_ColName},
				{OperatorParameter.NAME_VdependentColumn, OperatorParameter.NAME_dependentColumn},
				{OperatorParameter.NAME_SmatrixFullTable, OperatorParameter.NAME_singularValueTable},
				{OperatorParameter.NAME_SdependentColumn, OperatorParameter.NAME_dependentColumn}
		};
		for(String[] syncParamNameMapping : syncParamsNameMapping){
			Object value = svdOperator.getOperatorParameter(syncParamNameMapping[svdOperatorIdx]).getValue();
			boolean isTable = false;
			String schemaName = null;
			if(syncParamNameMapping[svdOperatorIdx] == OperatorParameter.NAME_UmatrixTable){
				schemaName = StringHandler.doubleQ((String) svdOperator.getOperatorParameter(OperatorParameter.NAME_UmatrixSchema).getValue());
				isTable |= true;
			}else if(syncParamNameMapping[svdOperatorIdx] == OperatorParameter.NAME_VmatrixTable){
				schemaName = StringHandler.doubleQ((String) svdOperator.getOperatorParameter(OperatorParameter.NAME_VmatrixSchema).getValue());
				isTable |= true;
			}else if(syncParamNameMapping[svdOperatorIdx] == OperatorParameter.NAME_singularValueTable){
				schemaName = StringHandler.doubleQ((String) svdOperator.getOperatorParameter(OperatorParameter.NAME_singularValueSchema).getValue());
				isTable |= true;
			}
			if(isTable){
				value = schemaName + "." + StringHandler.doubleQ((String) value);
			}
			calculatorOperator.getOperatorParameter(syncParamNameMapping[svdCalculatorOperatorIdx]).setValue(value);
		}
	}
}
