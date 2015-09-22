/**
 * ClassName HadoopPigExecuteOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-10-8
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

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelFactory;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.pigexe.PigExecutableModel;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author Jeff Dong
 *
 */
public class HadoopPigExecuteOperator extends HadoopOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_PigScript,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
			OperatorParameter.NAME_HD_PigExecute_fileStructure,
	});
	
	public HadoopPigExecuteOperator() {
		super(parameterNames);
		addInputClass(OperatorInputFileInfo.class.getName());
		addOutputClass(OperatorInputFileInfo.class.getName());
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList = new ArrayList<String>();
		List<OperatorParameter> paraList = getOperatorParameterList();
		FileStructureModel fileModel = null;
		for (OperatorParameter para : paraList) {
			if (para.getValue() instanceof String)
				continue;
			Object paraObj = para.getValue();
			if (paraObj instanceof FileStructureModel) {
				fileModel = (FileStructureModel) paraObj;
			}  
		}
		
		for(OperatorParameter para:paraList){
			String paraName = para.getName();
			String paraValue = null;
			if (para.getValue() instanceof String) {
				paraValue = (String) para.getValue();
			}
			if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
				validateNull(invalidParameterList, paraName, paraValue);	
			}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsName)){
				validateNull(invalidParameterList, paraName, paraValue);
			}
			else if(paraName.equals(OperatorParameter.NAME_HD_PigScript)){
				if(para.getValue() == null){
					invalidParameterList.add(paraName);
				}else{
					validateNull(invalidParameterList, paraName, ((PigExecutableModel)para.getValue()).getPigScript());
				}
				//check preceding operators are coming from same connection
				if(invalidParameterList.contains(paraName)==false){
					if(OperatorUtility.isComingFromSameHadoopConnetion(this)==false){
						invalidParameterList.add(paraName);
					}
				}
            }
			if (paraName.equals(OperatorParameter.NAME_HD_PigExecute_fileStructure)) {
				if(fileModel==null){
					invalidParameterList.add(paraName);
				}
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
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputFileInfo) {
				OperatorInputFileInfo operatorInputFileInfo = (OperatorInputFileInfo) obj;
				operatorInputFileInfo =operatorInputFileInfo.clone();
				String hadoopFileName = getOutputFileName();
				operatorInputFileInfo.setHadoopFileName(hadoopFileName);
				operatorInputFileInfo.setIsDir(true) ;
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());

                FileStructureModel columnInfo = (FileStructureModel)getOperatorParameter(OperatorParameter.NAME_HD_PigExecute_fileStructure).getValue();
                operatorInputFileInfo.setColumnInfo(columnInfo);
							
                operatorInputList.add(operatorInputFileInfo);
				break;
			}
		}
		return operatorInputList;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,
			boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element, addSuffixToOutput);
		 
		OperatorParameter fileStructureModelParameter = ParameterUtility
		.getParameterByName(this,
				OperatorParameter.NAME_HD_PigExecute_fileStructure);
		
		setFileStructureModel(xmlDoc, element, fileStructureModelParameter);
		
		OperatorParameter pigScriptModelParameter = ParameterUtility
		.getParameterByName(this,
				OperatorParameter.NAME_HD_PigScript);
		if(pigScriptModelParameter!=null&&pigScriptModelParameter.getValue()!=null){
			PigExecutableModel model = ((PigExecutableModel)pigScriptModelParameter.getValue());
			element.appendChild(model.toXMLElement(xmlDoc));
		}
	}
	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
	 
		List<OperatorParameter> operatorParameters = super.fromXML(
				opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		FileStructureModel fileStructureModel=FileStructureModelFactory.
				createFileStructureModelByXML(opTypeXmlManager,opNode);
		getOperatorParameter(OperatorParameter.NAME_HD_PigExecute_fileStructure)
		.setValue(fileStructureModel);
		
		List<Node> scriptModelNodeList = opTypeXmlManager.getNodeList(opNode,
				PigExecutableModel.TAG_NAME);
		if (scriptModelNodeList != null && scriptModelNodeList.size() > 0) {
			PigExecutableModel pigExecutableModel = PigExecutableModel
					.fromXMLElement((Element) scriptModelNodeList.get(0));
			getOperatorParameter(OperatorParameter.NAME_HD_PigScript)
					.setValue(pigExecutableModel);
		}
		
		return operatorParameters;
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator) {
		 return super.validateInputLink(precedingOperator, true);
	}
	
	private void setFileStructureModel(Document xmlDoc, Element element,
			OperatorParameter fileStructureModelParameter) {
		Object value = fileStructureModelParameter.getValue();
		if (!(value instanceof FileStructureModel)) {
			return;
		}
		FileStructureModel fileStructureModel = (FileStructureModel) value;
		element.appendChild(fileStructureModel.toXMLElement(xmlDoc));	
	}
	
	@Override
	public List<Object> getOutputObjectList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputFileInfo());
		return list;
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.AGGREGATE_OPERATOR,locale);
	}

}
