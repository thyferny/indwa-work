package com.alpine.miner.workflow.operator.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIConnectionModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class SQLExecuteOperator extends AbstractOperator {
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_dBConnectionName,
			OperatorParameter.NAME_SQL_Execute_Text
		 
			
	});
	
	public SQLExecuteOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(ModelOperator.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.SQLEXECUTE_OPERATOR,locale);
	}
 
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameterList = super.fromXML(opTypeXmlManager, opNode);
		
		attachConn(opTypeXmlManager, opNode, operatorParameterList);
		
		return operatorParameterList;
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_SQL_Execute_Text)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_dBConnectionName)){
				List<UIConnectionModel> connModelList=getOperModel().getSourceConnection();
				if(connModelList!=null&&connModelList.size()==0){
					validateNull(invalidParameterList, paraName, paraValue);
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
	
	@Override
	public boolean isInputObjectsReady() {
		return true;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);

		Map<String, String> paraMap = OperatorUtility.refreshTableInfo(this,userName,resourceType);
		if (paraMap == null)
			return;
		createSimpleElements(xmlDoc, element, paraMap.get("userName"),
				"userName");
		createSimpleElements(xmlDoc, element, paraMap.get("url"), "url");
		createSimpleElements(xmlDoc, element, paraMap.get("password"),
				"password");
		createSimpleElements(xmlDoc, element, paraMap.get("system"), "system");
		createSimpleElements(xmlDoc, element, paraMap.get("useSSL"), "useSSL");
	}
	
	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=getOperatorInputList();
		//do nothing,just pass parent to child
		return operatorInputList;
	}
	
	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
	
	public Map<String, String> refreshTableInfo() {
		Map<String, String> paraMap = OperatorUtility.refreshTableInfo(this,userName,resourceType);
		return paraMap;
	}
}
