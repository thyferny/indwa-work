
package com.alpine.miner.workflow.operator.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldItem;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItem;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.xml.XmlDocManager;


public class VariableOperator extends DataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			 
			OperatorParameter.NAME_fieldList,
			OperatorParameter.NAME_quantileFieldList,
			OperatorParameter.NAME_outputType,
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
	});

	public VariableOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

 

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.VARIABLE_OPERATOR,locale);
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		DerivedFieldsModel fieldModel=null;
		QuantileFieldsModel quantileModel=null;
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			if(para.getValue() instanceof String)continue;	
			Object paraObj=para.getValue();
			if(paraObj instanceof DerivedFieldsModel){
				fieldModel=(DerivedFieldsModel)paraObj;
			}else if(paraObj instanceof QuantileFieldsModel){
				quantileModel=(QuantileFieldsModel)paraObj;
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}			
			if(paraName.equals(OperatorParameter.NAME_fieldList)){
				if(fieldModel==null&&quantileModel==null){
					invalidParameterList.add(paraName);
					continue;
				}
				List<DerivedFieldItem>  derivedFieldList=null;
				List<QuantileItem>  quanFieldList=null;
				if(fieldModel!=null){
					derivedFieldList=fieldModel.getDerivedFieldsList();
				}
				if(quantileModel!=null){
					quanFieldList=quantileModel.getQuantileItems();
				}
				if((derivedFieldList==null||derivedFieldList.size()==0)
						&&(quanFieldList==null||quanFieldList.size()==0)){
					invalidParameterList.add(paraName);
					continue;
				}
				validateSelectedColumn(fieldList,invalidParameterList,paraName,fieldModel);
			}else if(paraName.equals(OperatorParameter.NAME_quantileFieldList)){
				if(fieldModel==null&&quantileModel==null){
					invalidParameterList.add(paraName);
					continue;
				}
				List<DerivedFieldItem>  derivedFieldList=null;
				List<QuantileItem>  quanFieldList=null;
				if(fieldModel!=null){
					derivedFieldList=fieldModel.getDerivedFieldsList();
				}
				if(quantileModel!=null){
					quanFieldList=quantileModel.getQuantileItems();
				}
				if((derivedFieldList==null||derivedFieldList.size()==0)
						&&(quanFieldList==null||quanFieldList.size()==0)){
					invalidParameterList.add(paraName);
					continue;
				}

				if(quanFieldList!=null){
					for(QuantileItem item:quanFieldList){
						if(!fieldList.contains(item.getColumnName())){
							invalidParameterList.add(paraName);
							continue;
						}
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
			}else if(paraName.equals(OperatorParameter.NAME_outputType)){
				validateNull(invalidParameterList, paraName, paraValue);
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
				
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo.setTableType((String)getOperatorParameter(OperatorParameter.NAME_outputType).getValue());
				
				DerivedFieldsModel derivedFieldsModel=(DerivedFieldsModel)getOperatorParameter(OperatorParameter.NAME_fieldList).getValue();
				QuantileFieldsModel quantileFieldsModel=(QuantileFieldsModel)getOperatorParameter(OperatorParameter.NAME_quantileFieldList).getValue();
				
				List<String[]> newFieldColumns=new ArrayList<String[]>();
				List<String[]> fieldColumns=operatorInputTableInfo.getFieldColumns();
				Map<String,String> dataTypeMap=new HashMap<String,String>();
				for(String[] fieldColumn:fieldColumns){
					dataTypeMap.put(fieldColumn[0], fieldColumn[1]);
				}
				if(derivedFieldsModel!=null){
					List<String> selectedList=derivedFieldsModel.getSelectedFieldList();
					for(String s:selectedList){
						if(!StringUtil.isEmpty(s)){
							newFieldColumns.add(new String[]{s,dataTypeMap.get(s)});
						}		
					}
					
					List<DerivedFieldItem> derivedFieldItemlist=derivedFieldsModel.getDerivedFieldsList();
					for(DerivedFieldItem item:derivedFieldItemlist){
						newFieldColumns.add(new String[]{item.getResultColumnName(),item.getDataType()});
					}
				}
				if(quantileFieldsModel!=null){
					List<QuantileItem>  quantileItemList=quantileFieldsModel.getQuantileItems();
					for(QuantileItem item:quantileItemList){
						if(!item.isCreateNewColumn())continue;
						newFieldColumns.add(new String[]{item.getNewColumnName(),ParameterUtility.getIntegerType(operatorInputTableInfo.getSystem())});
					}
				}
				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(operatorInputTableInfo);
				break;
			}
		}
		return operatorInputList;
	}

	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> derivedNodeList = opTypeXmlManager.getNodeList(opNode, DerivedFieldsModel.TAG_NAME);	
		if(derivedNodeList!=null&&derivedNodeList.size()>0){
			DerivedFieldsModel derivedModel=DerivedFieldsModel.fromXMLElement((Element)derivedNodeList.get(0));		
			getOperatorParameter(OperatorParameter.NAME_fieldList).setValue(derivedModel);
		}
		
		ArrayList<Node> quantileNodeList = opTypeXmlManager.getNodeList(opNode, QuantileFieldsModel.TAG_NAME);	
		if(quantileNodeList!=null&&quantileNodeList.size()>0){
			Element quantileElement=(Element)quantileNodeList.get(0);
			QuantileFieldsModel quantileModel=QuantileFieldsModel.fromXMLElement(quantileElement);
			getOperatorParameter(OperatorParameter.NAME_quantileFieldList).setValue(quantileModel);
		}
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		
		OperatorParameter quantileModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_quantileFieldList);
	 	
		if(quantileModelParameter!=null&&quantileModelParameter.getValue()!=null){
			element.appendChild(((QuantileFieldsModel)quantileModelParameter.getValue()).toXMLElement(xmlDoc));
		}
		OperatorParameter derivedFieldsModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_fieldList);
		
		setDeriveFieldsModel(xmlDoc, element,
				derivedFieldsModelParameter);

	}

	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}

	private void validateSelectedColumn(List<String> fieldList, List<String> invalidParameterList, String paraName,DerivedFieldsModel derivedModel) {
		if (!invalidParameterList.contains(paraName)) {
			List<String> seletectList = derivedModel.getSelectedFieldList();

			List<String> newSelectedList = new ArrayList<String>();
			
			for (String s : seletectList) {
				if (fieldList.contains(s)) {
					newSelectedList.add(s);
				}
			}
			if(newSelectedList.size()!=seletectList.size()){
				invalidParameterList.add(paraName);
			}
		}		
	}



	private void setDeriveFieldsModel(Document xmlDoc, Element element,
			OperatorParameter derivedFieldsModelParameter) {
		Object value=derivedFieldsModelParameter.getValue();
		if (! (value instanceof DerivedFieldsModel)) {
			return;
		}
		DerivedFieldsModel dfModel=(DerivedFieldsModel)value;
		element.appendChild(dfModel.toXMLElement(xmlDoc));
	}

}
