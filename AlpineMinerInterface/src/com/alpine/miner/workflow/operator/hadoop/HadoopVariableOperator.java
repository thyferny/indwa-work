package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldItem;
import com.alpine.miner.workflow.operator.parameter.variable.DerivedFieldsModel;
import com.alpine.miner.workflow.operator.parameter.variable.QuantileItem;
import com.alpine.utility.db.Resources;
import com.alpine.utility.xml.XmlDocManager;

public class HadoopVariableOperator extends HadoopDataOperationOperator {
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_fieldList,
			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
	});
	
	public HadoopVariableOperator() {
		super(parameterNames);
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		DerivedFieldsModel fieldModel=null;
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			Object paraObj=para.getValue();
			if(paraObj==null){
				continue;
			}
			if(paraObj instanceof DerivedFieldsModel){
				fieldModel=(DerivedFieldsModel)paraObj;
				break;
			}
		}
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}			
			if(paraName.equals(OperatorParameter.NAME_fieldList)){
				if(fieldModel==null){
					invalidParameterList.add(paraName);
					continue;
				}
				List<DerivedFieldItem>  derivedFieldList=null;
				List<QuantileItem>  quanFieldList=null;
				if(fieldModel!=null){
					derivedFieldList=fieldModel.getDerivedFieldsList();
				}
				if((derivedFieldList==null||derivedFieldList.size()==0)
						&&(quanFieldList==null||quanFieldList.size()==0)){
					invalidParameterList.add(paraName);
					continue;
				}
				validateSelectedColumn(fieldList,invalidParameterList,paraName,fieldModel);
			}else {
				validateHadoopStorageParameter(paraName,paraValue,invalidParameterList);
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
			if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo=(OperatorInputFileInfo)obj;
				operatorInputFileInfo=operatorInputFileInfo.clone();
				 //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

                
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());

				operatorInputFileInfo.setHadoopFileName(getOutputFileName());
				
				DerivedFieldsModel derivedModel=(DerivedFieldsModel)getOperatorParameter(OperatorParameter.NAME_fieldList).getValue();
				
				List<String> newColumnNameList = new ArrayList<String>();
				List<String> newColumnTypeList = new ArrayList<String>();
				
				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				if(columnInfo==null){
					continue;
				}
				
				if(derivedModel!=null&&derivedModel.getDerivedFieldsList()!=null){
					List<DerivedFieldItem> derivedFields = derivedModel.getDerivedFieldsList();
					for(DerivedFieldItem derivedField:derivedFields){
						newColumnNameList.add(derivedField.getResultColumnName());
						newColumnTypeList.add(derivedField.getDataType());
					}
				}
				if(derivedModel!=null&&derivedModel.getSelectedFieldList()!=null){
					List<String> selectedFields = derivedModel.getSelectedFieldList();
					for(String selectedField:selectedFields){
						newColumnNameList.add(selectedField);
						newColumnTypeList.add(getOldColumnType(columnInfo, selectedField));
					}
				}
				columnInfo.setIsFirstLineHeader(Resources.FalseOpt);
				columnInfo.setColumnNameList(newColumnNameList);
				columnInfo.setColumnTypeList(newColumnTypeList);
				operatorInputFileInfo.setColumnInfo(columnInfo);
				operatorInputList.add(operatorInputFileInfo);
				break;
			}
		}
		return operatorInputList;
	}
	
	@Override
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		
		OperatorParameter derivedFieldsModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_fieldList);
		
		setDeriveFieldsModel(xmlDoc, element,
				derivedFieldsModelParameter);
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
		return operatorParameters;
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.VARIABLE_OPERATOR,locale);
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
