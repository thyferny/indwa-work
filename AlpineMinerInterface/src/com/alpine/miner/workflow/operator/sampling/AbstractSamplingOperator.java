/**
 * ClassName AbstarctSampling.java
 *
 * Version information:1.00
 *
 * Date:Jun 8, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.sampling;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.DataOperationOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.sampling.SampleSizeModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public abstract class AbstractSamplingOperator extends DataOperationOperator {
    private static final Logger itsLogger=Logger.getLogger(AbstractSamplingOperator.class);

    public AbstractSamplingOperator(List<String> parameternames) {
		super(parameternames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(SampleSelectorOperator.class.getName());   

	}

	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=new ArrayList<Object>();
		
		String outputSchema = (String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue();
		String outputTablePrefix = (String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue();
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
			if(obj instanceof OperatorInputTableInfo){
				for (int i=0; i<sampleCount; i++) {
					OperatorInputTableInfo operatorInputTableInfo=(OperatorInputTableInfo)obj;
					OperatorInputTableInfo newOperatorInputTableInfo;
					try {
						newOperatorInputTableInfo = operatorInputTableInfo.clone();
						newOperatorInputTableInfo.setSchema(outputSchema);
						newOperatorInputTableInfo.setTable(outputTablePrefix+"_"+i);
						operatorInputList.add(newOperatorInputTableInfo);
					} catch (CloneNotSupportedException e) {
						itsLogger.error(e.getMessage(),e);
					}
				}
				break;
			}
		}	
		return operatorInputList;
	}

	@Override
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
		// this is very special for new sample size model,will transform form old string to model 
		Object sampleSizeObj = ParameterUtility.getParameterValue(this, OperatorParameter.NAME_sampleSize);
		if(sampleSizeObj!=null&&sampleSizeObj instanceof String){
			List<String> sampleIdList=new ArrayList<String>();
			List<String> sampleSizeList=new ArrayList<String>();
			Object sampleCountObj = ParameterUtility.getParameterValue(this, OperatorParameter.NAME_sampleCount);
			if(sampleCountObj!=null){
				VariableModel variableModel = getWorkflow().getParentVariableModel();
				String sampleCount = (String)sampleCountObj;
				String sampleSize = (String)sampleSizeObj;
				sampleCount=VariableModelUtility.getReplaceValue(variableModel, sampleCount);
				sampleSize=VariableModelUtility.getReplaceValue(variableModel, sampleSize);
				if(AlpineUtil.isInteger(sampleCount)&&Integer.parseInt(sampleCount)>0
						&&AlpineUtil.isNumber(sampleSize)&&Double.parseDouble(sampleSize)>0){
					for(int i=0;i<Integer.parseInt(sampleCount);i++){
						sampleIdList.add(String.valueOf(i+1));
						sampleSizeList.add(String.valueOf(Double.parseDouble(sampleSize)/Integer.parseInt(sampleCount)));
					}
				}
			}
			SampleSizeModel sampleSizeModel = new SampleSizeModel(sampleIdList, sampleSizeList);
			getOperatorParameter(OperatorParameter.NAME_sampleSize).setValue(sampleSizeModel);
		}
		
		return operatorParameters;
	}
	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
		
	}

	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		if(paraName.equals(OperatorParameter.NAME_sampleSizeType)){
			return "ROW";
		}else if(paraName.equals(OperatorParameter.NAME_consistent)){
			return Resources.FalseOpt;
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}

}
