/**
 * ClassName ReplaceNullOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementItem;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 * 
 */
public class ReplaceNullOperator extends DataOperationOperator {

	public static final List<String> parameterNames = Arrays
			.asList(new String[] {

			OperatorParameter.NAME_outputType,
					OperatorParameter.NAME_outputSchema,
					OperatorParameter.NAME_outputTable,
					OperatorParameter.NAME_outputTable_StorageParams,
					OperatorParameter.NAME_dropIfExist,
					// this is special for the UI...
					OperatorParameter.NAME_replacement_config, });

	public ReplaceNullOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.REPLACENULL_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		NullReplacementModel model = null;
		for (OperatorParameter para : paraList) {
			if (para.getValue() instanceof NullReplacementModel) {
				model = (NullReplacementModel) para.getValue();
				break;
			}
		}

		for (OperatorParameter para : paraList) {
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if (paraName.equals(OperatorParameter.NAME_outputType)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName.equals(OperatorParameter.NAME_outputSchema)) {
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			} else if (paraName.equals(OperatorParameter.NAME_outputTable)) {
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			} else if (paraName.equals(OperatorParameter.NAME_dropIfExist)) {
				validateNull(invalidParameterList, paraName, paraValue);
			} else if (paraName
					.equals(OperatorParameter.NAME_replacement_config)) {
				if (model == null || model.getNullReplacements() == null
						|| model.getNullReplacements().isEmpty()) {
					invalidParameterList.add(paraName);
					continue;
				}
				validateReplacementModel(fieldList,invalidParameterList,paraName,model);
			}
		}
		invalidParameters = invalidParameterList
				.toArray(new String[invalidParameterList.size()]);
		if (invalidParameterList.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {

		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter nullReplacementModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_replacement_config);
	 	
		if(nullReplacementModelParameter!=null&&nullReplacementModelParameter.getValue()!=null){
			element.appendChild(((NullReplacementModel)nullReplacementModelParameter.getValue()).toXMLElement(xmlDoc));
		}
	}

	private void validateReplacementModel(List<String> fieldList, List<String> invalidParameterList, String paraName,NullReplacementModel nrModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		List<NullReplacementItem> nrList = nrModel.getNullReplacements();
		List<NullReplacementItem> needRemoveList=new ArrayList<NullReplacementItem>();
		for(NullReplacementItem item:nrList){
			if(!fieldList.contains(item.getColumnName())){
				needRemoveList.add(item);
			}
		}
		if(needRemoveList.size()>0){
			invalidParameterList.add(paraName);
		}
	}


	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		ArrayList<Node> nullReNodeList=opTypeXmlManager.getNodeList(opNode, NullReplacementModel.TAG_NAME);
		if(nullReNodeList!=null&&nullReNodeList.size()>0){
			Element nrElement=(Element)nullReNodeList.get(0);
			NullReplacementModel columnBinsModel=NullReplacementModel.fromXMLElement(nrElement);
			getOperatorParameter(OperatorParameter.NAME_replacement_config).setValue(columnBinsModel);
		}			
		return operatorParameters;
	}
	
	
}
