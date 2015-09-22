/**
 * ClassName HadoopRandomSamplingOperator.java
 *
 * Version information: 1.00
 *
 * Data: Aug 1, 2012
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
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author Jeff Dong
 *
 */
public class HadoopRandomSamplingOperator extends HadoopDataOperationOperator {
	
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_sampleCount,
			OperatorParameter.NAME_sampleSizeType,
			OperatorParameter.NAME_sampleSize,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
	});

	public HadoopRandomSamplingOperator() {
		super(parameterNames);
		getOutputClassList().clear();
		addOutputClass(HadoopSampleSelectorOperator.class.getName());
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {	
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();

		String sampleSizeType=null;
		
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()==null){
				continue;
			}
			if(para.getValue() instanceof String){
				String paraValue=(String)para.getValue();
				if(paraName.equals(OperatorParameter.NAME_sampleSizeType)){
					sampleSizeType=paraValue;
					break;
				}
			}
		}
		
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=null;
			if(para.getValue() instanceof String){
				paraValue=(String)para.getValue();
			}
			
			if(paraName.equals(OperatorParameter.NAME_sampleCount)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue,0,true,Integer.MAX_VALUE,true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_sampleSizeType)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_sampleSize)){
				SampleSizeModel sampleSizeModel = (SampleSizeModel)para.getValue();
				if(sampleSizeModel==null
						||sampleSizeModel.getSampleIdList()==null
						||sampleSizeModel.getSampleIdList().size()==0){
					invalidParameterList.add(paraName);
				}else if(sampleSizeType.equals(com.alpine.utility.db.Resources.PercentageType)){
					for(int i=0;i<sampleSizeModel.getSampleIdList().size();i++){
						String sampleSize = sampleSizeModel.getSampleSizeList().get(i);
						sampleSize=VariableModelUtility.getReplaceValue(variableModel, sampleSize);
						if(StringUtil.isEmpty(sampleSize)==true||AlpineUtil.isNumber(sampleSize)==false
								||(AlpineUtil.isNumber(sampleSize)==true&&Double.parseDouble(sampleSize)>100)){
							if(!invalidParameterList.contains(paraName)){
								invalidParameterList.add(paraName);
							}
						}
					}
				}
			}else{
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
		
		String folder = (String)getOperatorParameter(OperatorParameter.NAME_HD_ResultsLocation).getValue();
		if(folder!=null&&folder.endsWith(HadoopFile.SEPARATOR)==false){
			folder =folder+HadoopFile.SEPARATOR;
		}
		String outputFilePrefix = (String)getOperatorParameter(OperatorParameter.NAME_HD_ResultsName).getValue();
		String sampleCountStr = (String)getOperatorParameter(OperatorParameter.NAME_sampleCount).getValue();

		if(StringUtil.isEmpty(sampleCountStr)){
			return operatorInputList;
		}
	 
		if(getWorkflow()!=null){
			VariableModel variableModel = getWorkflow().getParentVariableModel();
			sampleCountStr=VariableModelUtility.getReplaceValue(variableModel, sampleCountStr);
		}
		int sampleCount = Integer.parseInt(sampleCountStr);
		
		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputFileInfo){
				for (int i=0; i<sampleCount; i++) {
					OperatorInputFileInfo operatorInputFileInfo=(OperatorInputFileInfo)obj;
					OperatorInputFileInfo newOperatorInputFileInfo;
					newOperatorInputFileInfo = operatorInputFileInfo.clone();
					newOperatorInputFileInfo.setHadoopFileName((null==folder?"/":folder)+outputFilePrefix+"_"+(i+1));
					 //for xml -> csv...
	                FileStructureModelUtility.switchFileStructureModel(newOperatorInputFileInfo) ;

					if(newOperatorInputFileInfo.getColumnInfo() != null)
						newOperatorInputFileInfo.getColumnInfo().setIsFirstLineHeader(Resources.FalseOpt);
					operatorInputList.add(newOperatorInputFileInfo);
				}
				break;
			}
		}	
		return operatorInputList;
	}
	
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		
		OperatorParameter sampleSizeModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_sampleSize);
	 	
		if(sampleSizeModelParameter!=null&&sampleSizeModelParameter.getValue()!=null){
			element.appendChild(((SampleSizeModel)sampleSizeModelParameter.getValue()).toXMLElement(xmlDoc));
		}
	}
	
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters = super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> sampleSizeNodeList = opTypeXmlManager.getNodeList(opNode, SampleSizeModel.TAG_NAME);	
		if(sampleSizeNodeList!=null&&sampleSizeNodeList.size()>0){
			SampleSizeModel sampleSizeModel = SampleSizeModel.fromXMLElement((Element)sampleSizeNodeList.get(0));		
			getOperatorParameter(OperatorParameter.NAME_sampleSize).setValue(sampleSizeModel);
		}
		
		return operatorParameters;
	}
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.RANDOM_SAMPLING_OPERATOR,locale);
	}
	
	@Override
	protected void validateHadoopStorageParameter(String paraName,String paraValue,List<String> invalidParameterList) {	
		if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
				validateNull(invalidParameterList, paraName, paraValue);
		}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsName)){
				validateNull(invalidParameterList, paraName, paraValue);
		}	
	}
}
