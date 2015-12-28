
package com.alpine.miner.workflow.operator.association;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.association.ExpressionModel;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.xml.XmlDocManager;


public class AssociationOperator extends LearnerOperator {
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_minSupport,
			OperatorParameter.NAME_tableSizeThreshold,
			OperatorParameter.NAME_minConfidence,
//			OperatorParameter.NAME_positiveValue,
			OperatorParameter.NAME_expression,
			OperatorParameter.NAME_Use_Array,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			OperatorParameter.NAME_columnNames
	});
	
	

	public AssociationOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.ASSOCIATION_OPERATOR,locale);
	}

	@Override
	public String[] getInvalidParameters() {
		return invalidParameters;
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		ExpressionModel expressionModel=null;
		for(OperatorParameter para:paraList){
			if(para.getValue() instanceof ExpressionModel){
				expressionModel=(ExpressionModel)para.getValue();
				break;
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				 paraValue=(String)para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_minSupport)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue,0,true,1,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_minConfidence)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue,0,true,1,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_tableSizeThreshold)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,false,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_expression)){
				if(expressionModel==null){
					invalidParameterList.add(paraName);
				}else{
					if(StringUtil.isEmpty(expressionModel.getExpression())
							||StringUtil.isEmpty(expressionModel.getPositiveValue())){
						invalidParameterList.add(paraName);
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_outputSchema)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateTableName(invalidParameterList, paraName, paraValue,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
				validateNull(invalidParameterList, paraName, paraValue);
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
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_minSupport)){
			return "0.1";
		}else if (paraName.equals(OperatorParameter.NAME_tableSizeThreshold)){
			return "10000000";
		}else if (paraName.equals(OperatorParameter.NAME_minConfidence)){
			return "0.8";
		}else if (paraName.equals(OperatorParameter.NAME_Use_Array)){
			return Resources.FalseOpt;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
		
	}

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		ArrayList<Node> expressionNodeList=opTypeXmlManager.getNodeList(opNode, ExpressionModel.TAG_NAME);
		if(expressionNodeList!=null&&expressionNodeList.size()>0){
			Element nrElement=(Element)expressionNodeList.get(0);
			ExpressionModel expressionModel=ExpressionModel.fromXMLElement(nrElement);
			getOperatorParameter(OperatorParameter.NAME_expression).setValue(expressionModel);
		}			
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		
		OperatorParameter expressionModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_expression);
	 	
		if(expressionModelParameter!=null&&expressionModelParameter.getValue()!=null){
			element.appendChild(((ExpressionModel)expressionModelParameter.getValue()).toXMLElement(xmlDoc));
		}
		
	}
	
	
}
