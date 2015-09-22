/**
 * ClassName HadoopSampleSelectorOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-10
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;

/**
 * @author Jeff Dong
 *
 */
public class HadoopSampleSelectorOperator extends HadoopDataOperationOperator {
	private static final Logger itsLogger=Logger.getLogger(HadoopSampleSelectorOperator.class);
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_selectedFile,
	});
	
	public HadoopSampleSelectorOperator() {
		super(parameterNames);
		getInputClassList().clear();
		addInputClass(HadoopSampleSelectorOperator.class.getName());
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
			if(paraName.equals(OperatorParameter.NAME_selectedFile)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSelectedFile(invalidParameterList, paraName, paraValue,variableModel);
			}
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}
	}

	private void validateSelectedFile(List<String> invalidParameterList, String paraName, String paraValue, VariableModel variableModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
		OperatorInputFileInfo operatorInputFileInfo=null;
		List<Object> operatorInputList = getOperatorInputList();
		List<String> fileList=new ArrayList<String>();
		for (Object obj : operatorInputList) {
			if (obj instanceof OperatorInputFileInfo) {
				operatorInputFileInfo = (OperatorInputFileInfo) obj;
				String fileName = operatorInputFileInfo.getHadoopFileName();
				fileName=VariableModelUtility.getReplaceValue(variableModel, fileName);
				fileList.add(fileName);
			}
		}
		if(!StringUtil.isEmpty(paraValue)){
			if(fileList.contains(paraValue)==false){
				invalidParameterList.add(paraName);
			}
		}
	}
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SAMPLE_SELECTOR_OPERATOR,locale);
	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=new ArrayList<Object>();
		
		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputTableInfo=(OperatorInputFileInfo)obj;
				operatorInputTableInfo = operatorInputTableInfo.clone();
                operatorInputTableInfo.setOperatorUUID(this.getOperModel().getUUID());
				String fileName = operatorInputTableInfo.getHadoopFileName();
				String selectedFile = (String)getOperatorParameter(OperatorParameter.NAME_selectedFile).getValue();
				if(null==selectedFile){
					itsLogger.error("There is no selected file");
					continue;
				}
				operatorInputTableInfo.getColumnInfo().setIsFirstLineHeader(Resources.FalseOpt);
				if (selectedFile.equals(fileName)) {
					operatorInputList.add(operatorInputTableInfo);
					break;
				}
			}
		}
		return operatorInputList;
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(message == null || message.trim().equals("")){
			if (!(precedingOperator instanceof HadoopRandomSamplingOperator)) {
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
