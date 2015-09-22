/**
 * ClassName HadoopPredictOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2012-8-20
 *
 * COPYRIGHT (C) 2010-2012 Alpine Data Labs. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.hadoop.AbstractHadoopAnalyzer;
import com.alpine.datamining.api.impl.hadoop.models.LogisticRegressionHadoopModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;
import com.alpine.utility.tools.NLSUtility;

/**
 * @author Jeff Dong
 *
 */
public abstract class HadoopPredictOperator extends HadoopOperator {
	
	public static final String BAD_VALUE_DEFAULT = AbstractHadoopAnalyzer.BAD_VALUE_DEFAULT;
	public static final String PREDICT_SEP_CHAR = AbstractHadoopAnalyzer.PREDICT_SEP_CHAR;
	public static final String PREDICTION_NAME_P = AbstractHadoopAnalyzer.PREDICTION_NAME_P;
	public static final String PREDICTION_NAME_C = AbstractHadoopAnalyzer.PREDICTION_NAME_C;
	
	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,	
	});

	public HadoopPredictOperator() {
		this(true);
	}

	public HadoopPredictOperator(boolean needsInputFileInfo) {
		super(parameterNames);
        if (needsInputFileInfo) {
            addInputClass(OperatorInputFileInfo.class.getName());
        }
	}

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(paraName.equals(OperatorParameter.NAME_HD_ResultsLocation)){
				validateNull(invalidParameterList, paraName, (String)para.getValue());
			}else if(paraName.equals(OperatorParameter.NAME_HD_ResultsName)){
				validateNull(invalidParameterList, paraName, (String)para.getValue());
			}
		}
		
		if( this instanceof HadoopTimeSeriesPredictOperator ==false){
			boolean foundHadoopSource =false;
			List<Object> inputs = getOperatorInputList();
			if(inputs!=null){
				for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
					Object object = (Object) iterator.next();
					if(object instanceof OperatorInputFileInfo){
						foundHadoopSource= true;
						break;
					}
				}
				if(foundHadoopSource==false){
					invalidParameterList.add(OperatorParameter.NAME_HD_ResultsLocation);

				}
			}else{
				invalidParameterList.add(OperatorParameter.NAME_HD_ResultsLocation);
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
		List<Object> operatorInputList=getOperatorInputList();
		OperatorInputFileInfo operatorInputFileInfo=null;
		for (Object obj: operatorInputList){
			if(obj instanceof OperatorInputFileInfo){
				operatorInputFileInfo=(OperatorInputFileInfo)obj;
				operatorInputFileInfo=operatorInputFileInfo.clone();
				 //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());

				operatorInputFileInfo.setHadoopFileName(getOutputFileName());
				operatorInputFileInfo.getColumnInfo().setIsFirstLineHeader(Resources.FalseOpt);
				break;
			}
		}	
		if(operatorInputFileInfo!=null){
		
		List<UIOperatorModel> parentList = OperatorUtility.getParentList(getOperModel());
		for(UIOperatorModel opModel:parentList){
			if(!(opModel.getOperator() instanceof HadoopLinearRegressionOperator)
					&&!(opModel.getOperator() instanceof HadoopLogisticRegressionOperator)
					&&!(opModel.getOperator() instanceof HadoopDecisionTreeOperator)
					&&!(opModel.getOperator() instanceof ModelOperator)){
				continue;
			}
			String dependentColumn=null;
			String predictColumnType = HadoopDataType.DOUBLE;

			if(opModel.getOperator() instanceof ModelOperator){
				dependentColumn=((ModelOperator)opModel.getOperator()).getModel().getDependentColumn();		
				if(((ModelOperator)opModel.getOperator()).getModel().getModelType().equals(EngineModel.MPDE_TYPE_HADOOP_LOR)){
					predictColumnType = HadoopDataType.CHARARRAY;
				}
			}else{
				OperatorParameter operatorParameter=opModel.getOperator().getOperatorParameter(OperatorParameter.NAME_dependentColumn);
				if(operatorParameter!=null){
					dependentColumn=(String)operatorParameter.getValue();
				}
				if(opModel.getOperator() instanceof HadoopLogisticRegressionOperator){
					predictColumnType = HadoopDataType.CHARARRAY;

				}
			}
				
			if(!StringUtil.isEmpty(dependentColumn)){
					String newColumnName = PREDICTION_NAME_P+PREDICT_SEP_CHAR+dependentColumn ;
					operatorInputFileInfo.getColumnInfo().getColumnNameList().add(newColumnName);
					operatorInputFileInfo.getColumnInfo().getColumnTypeList().add(predictColumnType);
					if((opModel.getOperator() instanceof HadoopLogisticRegressionOperator)
							||(opModel.getOperator() instanceof ModelOperator
				&&((ModelOperator)opModel.getOperator()).getModel().getModelType().equals(EngineModel.MPDE_TYPE_HADOOP_LOR))){
						handleLorColumn(opModel,operatorInputFileInfo);
					}
					break;
					
			}
	
		}
		List<Object> newList = new ArrayList<Object>(); 
		newList.add(operatorInputFileInfo);
		return newList; 
		}else{
			return null;
		}
	}

	private void handleLorColumn(UIOperatorModel opModel, OperatorInputFileInfo operatorInputFileInfo) {
		if(opModel.getOperator() instanceof HadoopLogisticRegressionOperator
				){
		 
			OperatorParameter operatorParameter=opModel.getOperator().getOperatorParameter(OperatorParameter.NAME_goodValue);
			if(operatorParameter!=null){
				String goodValue = (String)operatorParameter.getValue();
				String newColumnName = PREDICTION_NAME_C+PREDICT_SEP_CHAR+goodValue ;
				operatorInputFileInfo.getColumnInfo().getColumnNameList().add(newColumnName);
				operatorInputFileInfo.getColumnInfo().getColumnTypeList().add(HadoopDataType.DOUBLE);
			}
			
			
		}else if (opModel.getOperator() instanceof ModelOperator
				&&((ModelOperator)opModel.getOperator()).getModel().getModelType().equals(EngineModel.MPDE_TYPE_HADOOP_LOR)){
			
			LogisticRegressionHadoopModel lorModel = (LogisticRegressionHadoopModel)((ModelOperator)opModel.getOperator()).getModel().getModel() ;
			String goodValue = (String)lorModel.getGood();
			String newColumnName = PREDICTION_NAME_C+PREDICT_SEP_CHAR+goodValue ;
			operatorInputFileInfo.getColumnInfo().getColumnNameList().add(newColumnName);
			operatorInputFileInfo.getColumnInfo().getColumnTypeList().add(HadoopDataType.DOUBLE);
			
		}
		
		String newColumnName =  BAD_VALUE_DEFAULT;
		operatorInputFileInfo.getColumnInfo().getColumnNameList().add(newColumnName);
		operatorInputFileInfo.getColumnInfo().getColumnTypeList().add(HadoopDataType.DOUBLE);
	}
	
	@Override
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputFileInfo> dbList = new ArrayList<OperatorInputFileInfo>();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof OperatorInputFileInfo){
					dbList.add((OperatorInputFileInfo)obj);
				}else if(obj instanceof EngineModel){
					modelList.add((EngineModel)obj);
				}
			}
		}
		if((dbList.size()==1) && (modelList.size()==1)){
			return true;
		}else{
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
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		if(StringUtil.isEmpty(message) ==true){ 
			message = super.validateStoreResult(precedingOperator);
		}
		
		
		if(message != null && !message.trim().equals("")){
			return message;
		}
		
		List<Object> list = precedingOperator.getOutputObjectList();
		boolean newModel = false;
		for(Object str:list){
			if(str instanceof EngineModel){
				newModel = true;
			}
		}
		boolean hasModel = false;
		
		List<UIOperatorModel> parentList = OperatorUtility.getParentList(getOperModel());
		for(UIOperatorModel om:parentList){
			for(Object str:om.getOperator().getOutputObjectList()){
				if(str instanceof EngineModel){
					hasModel = true;
					break;
				}
			}
		}
		
		if(newModel && hasModel){
			message =NLSUtility.bind(LanguagePack.getMessage(LanguagePack.CAN_NOT_LINK_MUTIL_MODEL,locale),this.getToolTipTypeName());
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
		
		return message;
	}
}
