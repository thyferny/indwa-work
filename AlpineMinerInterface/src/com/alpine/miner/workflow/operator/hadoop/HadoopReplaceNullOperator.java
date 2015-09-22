/**
 * ClassName HadoopReplaceNullOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-01
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.ParameterValidateUtility;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementItem;
import com.alpine.miner.workflow.operator.parameter.nullreplacement.NullReplacementModel;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 * 
 */
public class HadoopReplaceNullOperator extends HadoopDataOperationOperator {

	public static final List<String> parameterNames = Arrays
			.asList(new String[] {


					// this is special for the UI...
					OperatorParameter.NAME_replacement_config, 
					
					OperatorParameter.NAME_HD_StoreResults,
					OperatorParameter.NAME_HD_ResultsLocation,
					OperatorParameter.NAME_HD_ResultsName,
					OperatorParameter.NAME_HD_Override		
			});
	
	

	public HadoopReplaceNullOperator() {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(OperatorInputFileInfo.class.getName());
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
			  if (paraName
					.equals(OperatorParameter.NAME_replacement_config)) {
				if (model == null || model.getNullReplacements() == null
						|| model.getNullReplacements().isEmpty()) {
					invalidParameterList.add(paraName);
					continue;
				}
				validateReplacementModel(fieldList,invalidParameterList,paraName,model,variableModel);
			}else {
			 
				validateHadoopStorageParameter(paraName,paraValue,invalidParameterList);
			 
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
		list.add(new OperatorInputFileInfo());
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

	private void validateReplacementModel(List<String> fieldList, List<String> invalidParameterList, String paraName,
			NullReplacementModel nrModel, VariableModel variableModel) {
		if(invalidParameterList.contains(paraName)){
			return;
		}
		List<NullReplacementItem> nrList = nrModel.getNullReplacements();
		List<NullReplacementItem> needRemoveList=new ArrayList<NullReplacementItem>();
		for(NullReplacementItem item:nrList){
			if(!fieldList.contains(item.getColumnName())){
				needRemoveList.add(item);
			}else{ 
				String value = item.getValue();
                String type = item.getType();
				value=VariableModelUtility.getReplaceValue(variableModel, value);
				if(true==ParameterValidateUtility.validateNumberColumn(item.getColumnName(), this, paraName) && type.equals("value") ){
					try{
						Double.parseDouble(value);
					}catch(Exception e){
						invalidParameterList.add(paraName);
						return;
					}
                } else if (type.equals("agg")) {
                    if (true==ParameterValidateUtility.validateNumberColumn(item.getColumnName(), this, paraName)) {
                        return;
                    } else {
                        invalidParameterList.add(paraName);
                        return;
                    }
				} else {
					if(value.startsWith("'")==false||value.endsWith("'")==false||value.length()<2){
						invalidParameterList.add(paraName);
						return;
					}
				}
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


	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> outputFileList =  super.getOperatorOutputList();
		if(outputFileList == null || outputFileList.size() == 0){
			return null;
		}
		OperatorInputFileInfo outputFileInfo = (OperatorInputFileInfo) outputFileList.get(0);// there is must be only one output.
		OperatorParameter replacementConfigParameter = getOperatorParameter(OperatorParameter.NAME_replacement_config);
		NullReplacementModel model = (NullReplacementModel) replacementConfigParameter.getValue();
		for(NullReplacementItem replacementItem : model.getNullReplacements()){
			// replace column type to DOUBLE if replace type is AVG. 
			if("AVG".equals(replacementItem.getValue())){
				String columnName = replacementItem.getColumnName();
				List<String> columnNameList = outputFileInfo.getColumnInfo().getColumnNameList();
				List<String> columnTypeList = outputFileInfo.getColumnInfo().getColumnTypeList();
				for(int i = 0;i < columnNameList.size();i++){
					if(columnNameList.get(i).equals(columnName)){
						columnTypeList.set(i, HadoopDataType.DOUBLE);
					}
				}
			}
		}
		return outputFileList;
	}
}
