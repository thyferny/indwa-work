/**
 * ClassName SampleSelector.java
 *
 * Version information:1.00
 *
 * Date:Jun 9, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */

package com.alpine.miner.workflow.operator.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;

/**
 * @author Richie Lo
 *
 */
public class SampleSelectorOperator extends AbstractOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_selectedTable,
	});
		
	public SampleSelectorOperator() {
		super(parameterNames);
		addInputClass(SampleSelectorOperator.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SAMPLE_SELECTOR_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_selectedTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSelectedTable(invalidParameterList, paraName, paraValue,variableModel);
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
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=new ArrayList<Object>();
		
		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputTableInfo){
				OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
				String outputSchema = operatorInputTableInfo.getSchema();
				String outputTable = operatorInputTableInfo.getTable();
				String outputTableName = outputSchema+"."+outputTable;
				String selectedTable = (String)getOperatorParameter(OperatorParameter.NAME_selectedTable).getValue();
				if (outputTableName.equals(selectedTable)) {
					operatorInputList.add(operatorInputTableInfo);
					break;
				}
			}
		}
		return operatorInputList;
	}
	
	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
		
	}

	private void validateSelectedTable(List<String> invalidParameterList, String paraName, String paraValue, VariableModel variableModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		OperatorInputTableInfo operatorInputTableInfo=null;
		List<Object> operatorInputList = getOperatorInputList();
		List<String> tableList=new ArrayList<String>();
		String schema=null;
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputTableInfo) {
				operatorInputTableInfo = (OperatorInputTableInfo) obj;
				String table = operatorInputTableInfo.getTable();
				table=VariableModelUtility.getReplaceValue(variableModel, table);
				tableList.add(table);
				schema=operatorInputTableInfo.getSchema();
				schema=VariableModelUtility.getReplaceValue(variableModel, schema);
			}
		}
		if(!StringUtil.isEmpty(paraValue)){
			String[] temp=paraValue.split("\\.");
			if(!temp[0].equals(schema)||!tableList.contains(temp[1])){
				invalidParameterList.add(paraName);
			}
		}
	}

	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(message == null || message.trim().equals("")){
			if (!(precedingOperator instanceof RandomSamplingOperator) && !(precedingOperator instanceof StratifiedSamplingOperator)) {
				message =  NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
			}
		}
		
		if(message == null || message.trim().equals("")){
			List<UIOperatorModel> parentList = OperatorUtility.getParentList(getOperModel());
			if(parentList != null && parentList.size()>0){
				message =  NLSUtility.bind(LanguagePack.getMessage(LanguagePack.CANNOT_LINKMUTIL_TOOPERATOR,locale),this.getToolTipTypeName());
			}
		}
		return message;
	}
}
