/**
 * ClassName UnivariateExplorerOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-4-24
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
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
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.univariate.UnivariateModel;
import com.alpine.utility.xml.XmlDocManager;

public class UnivariateExplorerOperator extends AbstractOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_UnivariateModel,
	});

	public UnivariateExplorerOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}
	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		ArrayList<Node> univariateNodeList=opTypeXmlManager.getNodeList(opNode, UnivariateModel.TAG_NAME);
		if(univariateNodeList!=null&&univariateNodeList.size()>0){
			Element interActionElement=(Element)univariateNodeList.get(0);
			UnivariateModel univariateModel = UnivariateModel.fromXMLElement(interActionElement);
			getOperatorParameter(OperatorParameter.NAME_UnivariateModel).setValue(univariateModel);
		}			
		return operatorParameters;
	}
	
	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter univariateModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_UnivariateModel);
	 	
		if(univariateModelParameter!=null&&univariateModelParameter.getValue()!=null){
			element.appendChild(((UnivariateModel)univariateModelParameter.getValue()).toXMLElement(xmlDoc));
		}
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.UNIVARIATEEXPLORER_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue() instanceof UnivariateModel){
				UnivariateModel model = (UnivariateModel)para.getValue();
				if(model==null){
					invalidParameterList.add(paraName);
					break;
				}else{
					validateNull(invalidParameterList, paraName, model.getReferenceColumn());
					validateListNull(invalidParameterList, paraName, model.getAnalysisColumns());
					if(!invalidParameterList.contains(paraName)
							&&!fieldList.contains(model.getReferenceColumn())){
						invalidParameterList.add(paraName);
					}
					if(!invalidParameterList.contains(paraName)){
						for(String columnName:model.getAnalysisColumns()){
							if(!fieldList.contains(columnName)){
								invalidParameterList.add(paraName);
								break;
							}
						}
					}
				}
			}
		}
		invalidParameters=invalidParameterList.toArray(new String[invalidParameterList.size()]);
		if(invalidParameterList.size()==0){
			return true;
		}else{
			return false;
		}
	}



}
