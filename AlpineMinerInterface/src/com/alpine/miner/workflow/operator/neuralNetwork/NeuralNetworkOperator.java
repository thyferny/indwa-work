/**
 * NeuralNetworkOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 */
package com.alpine.miner.workflow.operator.neuralNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.NumberUtil;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.LearnerOperator;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayer;
import com.alpine.miner.workflow.operator.parameter.hiddenlayer.HiddenLayersModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class NeuralNetworkOperator extends LearnerOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_dependentColumn,
			OperatorParameter.NAME_forceRetrain,
			OperatorParameter.NAME_hidden_layers,
			OperatorParameter.NAME_training_cycles,
			OperatorParameter.NAME_learning_rate,
			OperatorParameter.NAME_momentum,
			OperatorParameter.NAME_decay,
			OperatorParameter.NAME_fetchsize,
			
			OperatorParameter.NAME_normalize,
			OperatorParameter.NAME_error_epsilon,
			OperatorParameter.NAME_local_random_seed,
			OperatorParameter.NAME_adjust_per,
			OperatorParameter.NAME_columnNames,
	});
		
	public NeuralNetworkOperator(){
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
		addOutputClass(EngineModel.MPDE_TYPE_NEU);
	}
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.NEURAL_NETWORK_OPERATOR,locale);
	}
 

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
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
			}else if(paraName.equals(OperatorParameter.NAME_training_cycles)){
//				validatNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_learning_rate)){
//				validatNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, true, 1, false,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_momentum)){
//				validatNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, true, 1, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_fetchsize)){
//				validatNull(invalidParameterList, paraName, paraValue);
				validateInteger(invalidParameterList, paraName, paraValue, 1, false, Integer.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_error_epsilon)){
//				validatNull(invalidParameterList, paraName, paraValue);
				validateNumber(invalidParameterList, paraName, paraValue, 0, true, Double.MAX_VALUE, true,variableModel);
			}else if(paraName.equals(OperatorParameter.NAME_local_random_seed)){
				paraValue=VariableModelUtility.getReplaceValue(variableModel, paraValue);
				if(!AlpineUtil.isInteger(paraValue)||(!StringUtil.isEmpty(paraValue)&&!NumberUtil.isInteger(paraValue,0,false
						,Integer.MAX_VALUE,true)&&Integer.parseInt(paraValue)!=-1
						&&!invalidParameterList.contains(paraName))){
					invalidParameterList.add(paraName);
				}
			}else if(paraName.equals(OperatorParameter.NAME_adjust_per)){
				validateNull(invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_hidden_layers)){
				if(para.getValue() instanceof HiddenLayersModel){
					HiddenLayersModel hiddenLayersModel = (HiddenLayersModel)para.getValue();
					if(hiddenLayersModel!=null){
						List<HiddenLayer> hiddenLayers = hiddenLayersModel.getHiddenLayers();
						if(hiddenLayers!=null){
							for(HiddenLayer hiddenLayer:hiddenLayers){
								String layerSize = hiddenLayer.getLayerSize();
								layerSize=VariableModelUtility.getReplaceValue(variableModel, layerSize);
								if(!StringUtil.isEmpty(layerSize)&&!AlpineUtil.isInteger(layerSize)){
									invalidParameterList.add(paraName);
									break;
								}
							}
						}
					}
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
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) {
		List<OperatorParameter> operatorParameters =super.fromXML(opTypeXmlManager, opNode);
		setOperatorParameterList(operatorParameters);
		
		ArrayList<Node> hiddenLayersNodeList=opTypeXmlManager.getNodeList(opNode, HiddenLayersModel.TAG_NAME);
		if(hiddenLayersNodeList!=null&&hiddenLayersNodeList.size()>0){
			Element hiddenElement=(Element)hiddenLayersNodeList.get(0);
			HiddenLayersModel hiddenModel=HiddenLayersModel.fromXMLElement(hiddenElement);
			getOperatorParameter(OperatorParameter.NAME_hidden_layers).setValue(hiddenModel);
		}	
		
		return operatorParameters;
	}

	@Override
	public void toXML(Document xmlDoc, Element element,boolean addSuffixToOutput) {
		super.toXML(xmlDoc, element,addSuffixToOutput);
		OperatorParameter nnModelParameter=ParameterUtility.getParameterByName(this,OperatorParameter.NAME_hidden_layers);
		
		setNNModel(xmlDoc, element, nnModelParameter);

	}
	
	@Override
	public String getOperatorParameterDefaultValue(String paraName) {
		 if(paraName.equals(OperatorParameter.NAME_training_cycles)){
			return "500";
		}else if(paraName.equals(OperatorParameter.NAME_learning_rate)){
			return "0.3";
		}else if(paraName.equals(OperatorParameter.NAME_momentum)){
			return "0.2";
		}else if(paraName.equals(OperatorParameter.NAME_decay)){
			return Resources.FalseOpt;
		}else if(paraName.equals(OperatorParameter.NAME_fetchsize)){
			return "10000";
		}else if(paraName.equals(OperatorParameter.NAME_normalize)){
			return Resources.TrueOpt;
		}else if(paraName.equals(OperatorParameter.NAME_error_epsilon)){
			return "0.00001";
		}else if(paraName.equals(OperatorParameter.NAME_local_random_seed)){
			return "-1";
		}else if(paraName.equals(OperatorParameter.NAME_adjust_per)){
			return "ALL";
		}else {
			return super.getOperatorParameterDefaultValue(paraName);
		}
	}

	private void setNNModel(Document xmlDoc, Element element, OperatorParameter nnModelParameter) {
		Object value=nnModelParameter.getValue();
		if (! (value instanceof HiddenLayersModel)) {
			return;
		}
		HiddenLayersModel hiddenLayersModel=(HiddenLayersModel)value;
		element.appendChild(hiddenLayersModel.toXMLElement(xmlDoc));
	}
}
