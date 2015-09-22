/**
 * ClassName HistogramOperator.java
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
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class HistogramOperator extends AbstractOperator {
	 
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_Columns_Bins,
	});
	
	public HistogramOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.HISTOGRAM_OPERATOR,locale);
	}
 
	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		ArrayList<Node> columnBinNodeList=opTypeXmlManager.getNodeList(opNode, ColumnBinsModel.TAG_NAME);
		if(columnBinNodeList!=null&&columnBinNodeList.size()>0){
			Element interActionElement=(Element)columnBinNodeList.get(0);
			ColumnBinsModel columnBinsModel=ColumnBinsModel.fromXMLElement(interActionElement);
			getOperatorParameter(OperatorParameter.NAME_Columns_Bins).setValue(columnBinsModel);
		}			
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter columnBinsModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_Columns_Bins);
	 	
		if(columnBinsModelParameter!=null&&columnBinsModelParameter.getValue()!=null){
			element.appendChild(((ColumnBinsModel)columnBinsModelParameter.getValue()).toXMLElement(xmlDoc));
		}
	}



	@Override
	public List<Object> getOperatorOutputList() {
		return null;
	}

}
