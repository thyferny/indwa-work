/**
 * ClassName HadoopLinearRegressionOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-8-17
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.interaction.InterActionColumnsModel;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author Jeff Dong
 *
 */
public class HadoopLinearRegressionOperator extends HadoopLearnerOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_Interaction_Columns,
			OperatorParameter.NAME_columnNames,
	});

	public HadoopLinearRegressionOperator() {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_HADOOP_LIR);
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.LINEARREGRESSION_OPERATOR,locale);
	}


	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableNumColumnsList(this,
                false);

		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		InterActionColumnsModel interModel=null;
		String columnName=null;
		for(OperatorParameter para:paraList){
			if(para.getName().equals(OperatorParameter.NAME_columnNames)){
				columnName=(String)para.getValue();
			}else if(para.getValue() instanceof InterActionColumnsModel){
				interModel=(InterActionColumnsModel)para.getValue();
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
			}else if(paraName.equals(OperatorParameter.NAME_Interaction_Columns)){
				if((interModel==null||interModel.getInterActionItems().size()==0)
						&&StringUtil.isEmpty(columnName)){
					invalidParameterList.add(paraName);
					continue;
				}
				validateInteractionColumns(fieldList,invalidParameterList, paraName, interModel);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				if((interModel==null||interModel.getInterActionItems().size()==0)
						&&StringUtil.isEmpty(columnName)){
					invalidParameterList.add(paraName);
					continue;
				}
 				validateColumnNames(OperatorUtility.getAvailableColumnsList(this, false),invalidParameterList, paraName, paraValue);
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
		
		ArrayList<Node> interActionNodeList=opTypeXmlManager.getNodeList(opNode, InterActionColumnsModel.TAG_NAME);
		if(interActionNodeList!=null&&interActionNodeList.size()>0){
			Element interActionElement=(Element)interActionNodeList.get(0);
			InterActionColumnsModel interActionModel=InterActionColumnsModel.fromXMLElement(interActionElement);
			getOperatorParameter(OperatorParameter.NAME_Interaction_Columns).setValue(interActionModel);
		}
		
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		
		OperatorParameter interModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_Interaction_Columns);
		
		Object value = interModelParameter.getValue();
		if (! (value instanceof InterActionColumnsModel)) {
			return;
		}
		
		InterActionColumnsModel interActionModel=(InterActionColumnsModel)value;
		if(interActionModel!=null
				&&interActionModel.getInterActionItems()!=null
				&&interActionModel.getInterActionItems().size()>0){
			element.appendChild(interActionModel.toXMLElement(xmlDoc));	
		}
	}
	

}
