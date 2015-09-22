package com.alpine.miner.workflow.operator.logisticregression.woe;

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
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.woe.WOEInforList;
import com.alpine.miner.workflow.operator.parameter.woe.WOETable;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.xml.XmlDocManager;

public class WOEOperator extends LearnerOperator {

	public static final List<String> parameterNames = Arrays
			.asList(new String[] { OperatorParameter.NAME_dependentColumn,
					OperatorParameter.NAME_goodValue,
					OperatorParameter.NAME_WOEGROUP,
					OperatorParameter.NAME_columnNames,});

	public WOEOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_WOE);
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.WOE_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_goodValue)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_dependentColumn)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_WOEGROUP)){
				validateWOETable(fieldList,invalidParameterList, paraName,(WOETable)para.getValue());
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
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(
				opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		List<Node> woeTableNodeList = opTypeXmlManager.getNodeList(opNode,
				WOETable.TAG_NAME);
		if (woeTableNodeList != null && woeTableNodeList.size() > 0) {
			WOETable aggModel = WOETable
					.fromXMLElement((Element) woeTableNodeList.get(0));
			getOperatorParameter(OperatorParameter.NAME_WOEGROUP)
					.setValue(aggModel);
		}
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter woeGroupModelParameter = ParameterUtility
		.getParameterByName(this,
				OperatorParameter.NAME_WOEGROUP);
		
		setWOEModel(xmlDoc, element, woeGroupModelParameter);
	}

	private void setWOEModel(Document xmlDoc, Element element,
			OperatorParameter woeGroupModelParameter) {
		Object value = woeGroupModelParameter.getValue();
		if (!(value instanceof WOETable)) {
			return;
		}
		WOETable woeModel = (WOETable) value;
		element.appendChild(woeModel.toXMLElement(xmlDoc));
	}

	private void validateWOETable(List<String> fieldList, List<String> invalidParameterList, String paraName,WOETable woeTable) {
		if(!invalidParameterList.contains(paraName)){
			List<WOEInforList> removeList=new ArrayList<WOEInforList>();
			if(woeTable!=null){
				for(WOEInforList woeInfo:woeTable.getDataTableWOE()){
					if(!fieldList.contains(woeInfo.getColumnName())){
						removeList.add(woeInfo);
					}
				}
				if(removeList.size()>0){
					invalidParameterList.add(paraName);
				}
			}
		}
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator   ) {
		if(precedingOperator instanceof WOETableGeneratorOperator){
			return NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
					precedingOperator.getToolTipTypeName(), this
							.getToolTipTypeName());		
		}
		else{
			return super.validateInputLink(  precedingOperator,   false);
		}
	}

}
