package com.alpine.miner.workflow.operator.adaboost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.xml.XmlDocManager;

public class AdaboostOperator extends LearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_columnNames,
			OperatorParameter.NAME_adaboostUIModel
	});

	public AdaboostOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_ADABOOST);
	}

 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ADABOOST_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		AdaboostPersistenceModel interModel=null;
		for(OperatorParameter para:paraList){
			if(para.getValue() instanceof String){
				
			}else{
				interModel=(AdaboostPersistenceModel)para.getValue();
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_forceRetrain)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_adaboostUIModel)){
				if((interModel==null||interModel.getAdaboostUIItems().size()==0)){
					invalidParameterList.add(paraName);
					continue;
				}
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
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
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> adaboostNodeList=opTypeXmlManager.getNodeList(opNode, AdaboostPersistenceModel.TAG_NAME);
		if(adaboostNodeList!=null&&adaboostNodeList.size()>0){
			Element adaboostElement=(Element)adaboostNodeList.get(0);
			AdaboostPersistenceModel adaboostModel=AdaboostPersistenceModel.fromXMLElement(adaboostElement);
			getOperatorParameter(OperatorParameter.NAME_adaboostUIModel).setValue(adaboostModel);
		}		
		
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter adaboostModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_adaboostUIModel);
		
		Object value = adaboostModelParameter.getValue();
		if (! (value instanceof AdaboostPersistenceModel)) {
			return;
		}
		AdaboostPersistenceModel adaboostModel=(AdaboostPersistenceModel)value;
		element.appendChild(adaboostModel.toXMLElement(xmlDoc));	
	}

	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new EngineModel());
		return list;
	}


}
