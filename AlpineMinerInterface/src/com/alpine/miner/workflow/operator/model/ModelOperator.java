/**
 * ClassName ModelOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-12
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.execute.SQLExecuteOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.common.ListUtility;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;
import com.alpine.utility.xml.XmlDocManager;

/**
 * @author zhao yong
 *
 */
public class ModelOperator extends AbstractOperator {

	private EngineModel model;
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_Model_File_Path
	});
	public ModelOperator() {
		super(parameterNames);
		addInputClass(EngineModel.MPDE_TYPE_PLDA);
		addInputClass(EngineModel.MPDE_TYPE_LOR);
		addInputClass(EngineModel.MPDE_TYPE_LIR);
		addInputClass(EngineModel.MPDE_TYPE_NB);
		addInputClass(EngineModel.MPDE_TYPE_NEU);
		addInputClass(EngineModel.MPDE_TYPE_TREE_CLASSIFICATION);
		addInputClass(EngineModel.MPDE_TYPE_TREE_REGRESSION);
		addInputClass(EngineModel.MPDE_TYPE_TIMESERIES);
		addInputClass(SQLExecuteOperator.class.getName());
		addInputClass(EngineModel.MPDE_TYPE_SVM_C);
		addInputClass(EngineModel.MPDE_TYPE_SVM_ND);
		addInputClass(EngineModel.MPDE_TYPE_SVM_R);
		addInputClass(EngineModel.MPDE_TYPE_ADABOOST);
		addInputClass(EngineModel.MPDE_TYPE_WOE);
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_LIR);
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_LOR);		
		addInputClass(EngineModel.MPDE_TYPE_HADOOP_TREE_CLASSIFICATION);
        addInputClass(EngineModel.MPDE_TYPE_HADOOP_NB);

		addInputClass(EngineModel.MPDE_TYPE_TREE_RANDOM_FOREST);
		
	}
	
	private boolean saveModelFromCache = true; 
	
	public boolean isSaveModelFromCache() {
		return saveModelFromCache;
	}

	public void setSaveModelFromCache(boolean saveModelFromCache) {
		this.saveModelFromCache = saveModelFromCache;
	}


	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.MODEL_OPERATOR,locale);
	}

	public EngineModel getModel() {
		return model;
	}

	public void setModel(EngineModel model) {
 
		this.model = model;
		if(getOutputClassList() != null){
			getOutputClassList().clear();
		}
		if(model != null){
			addOutputClass(model.getModelType());
		}

	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		Object pathParameter = ParameterUtility.getParameterValue(this, OperatorParameter.NAME_Model_File_Path);
		if(ListUtility.isEmpty(getParentOperatorList())==false){
			return true;
		}
		else if (pathParameter!=null&&StringUtil.isEmpty(pathParameter.toString())==false){
			if(getModel()==null){
				invalidParameters = new String[]{OperatorParameter.NAME_Model_File_Path} ;
				return false;
			}else{
				return true;
			}
		}
		else if(getModel()==null&&ParameterUtility.getParameterValue(this, OperatorParameter.NAME_Model_File_Path)==null){
			invalidParameters = new String[]{OperatorParameter.NAME_Model_File_Path} ;
			return false;
		}else{
			return true;
		}
	}

	@Override
	public boolean isInputObjectsReady() {
		List<UIOperatorModel> childList=OperatorUtility.getChildList(this.getOperModel());
		if((childList != null) && (childList.size()>0)){
			return true;
		}else {
			List<UIOperatorModel> parentList=OperatorUtility.getParentList(this.getOperModel());
			if((parentList != null) && (parentList.size()>0)){
				List<Object> list = getParentOutputClassList();
				List<EngineModel> modelList = new ArrayList<EngineModel>();
				if(list != null){
					for(Object obj :list){
						if(obj instanceof EngineModel){
							modelList.add((EngineModel)obj);
						}
					}
				}
				if(modelList.size()==1){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(message == null || message.trim().equals("")){
			if(OperatorUtility.getParentList(getOperModel()) != null && OperatorUtility.getParentList(getOperModel()).size()>0){
				message = NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
						precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
				return message;
			}
			if(OperatorUtility.getParentList(precedingOperator.getOperModel())!=null
					&&!OperatorUtility.getParentList(precedingOperator.getOperModel()).isEmpty()
					&&OperatorUtility.getParentList(precedingOperator.getOperModel()).size()==1){
				UIOperatorModel opModel=OperatorUtility.getParentList(precedingOperator.getOperModel()).get(0);
				if(opModel.getOperator() instanceof AdaboostOperator){
					message = NLSUtility.bind(LanguagePack.getMessage(LanguagePack.MESSAGE_CHECK_LINK,locale),
							precedingOperator.getToolTipTypeName(),this.getToolTipTypeName());
					return message;
				}
			}
		}
		return message;
	}

	@Override
	public List<Object> getOperatorOutputList() {
		ArrayList<Object> list = new ArrayList<Object>();
		if(model != null){
			list.add(model);
		}
		return list;
	}

//	@Override
//	public void toXML(Document xmlDoc, Element element,
//			boolean addSuffixToOutput) {
//		return;
//	}


	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new EngineModel());
		return list;
	}
	@Override
	public List<OperatorParameter> fromXML(XmlDocManager opTypeXmlManager,
			Node opNode) { 
		List<OperatorParameter> list = super.fromXML(opTypeXmlManager, opNode);
		if(list!=null){
			for (OperatorParameter operatorParameter : list) {
				if(operatorParameter.getName().equals(OperatorParameter.NAME_Model_File_Path)){
					if(operatorParameter.getValue()!=null
							&&StringUtil.isEmpty(operatorParameter.getValue().toString())==false){
						loadOutputClassFromModelFile(operatorParameter.getValue().toString());
					}
				}
			}
		}
 
		  return list;
			
			}
	public void loadOutputClassFromModelFile(){
		Object value = ParameterUtility.getParameterValue(this, OperatorParameter.NAME_Model_File_Path) ;
		if(value!=null){
			String modelFilePath =value.toString();
			loadOutputClassFromModelFile(modelFilePath) ;
		}
	}
	
	private void loadOutputClassFromModelFile(String modelFilePath){
		 
		if(StringUtil.isEmpty(modelFilePath)==false){
			  model = loadModelFromFile(modelFilePath) ;
		 
			  
			if(model != null){ 
				if(getOutputClassList() != null){
					getOutputClassList().clear();
				}
				addOutputClass(model.getModelType());
			}
		}
	}


	public boolean isHadoopModel() { 
		 if(this.getOutputClassList()!=null){
			 for (String className : getOutputClassList()) {
				if(EngineModel.MPDE_TYPE_HADOOP_LIR.equals(className)
						||EngineModel.MPDE_TYPE_HADOOP_LOR.equals(className)){
					return true;
				}
			}
 		 }
		return false;
	}
}
