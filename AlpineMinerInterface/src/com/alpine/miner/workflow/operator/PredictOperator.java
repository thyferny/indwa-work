
package com.alpine.miner.workflow.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionOperator;
import com.alpine.miner.workflow.operator.model.ModelOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.tools.NLSUtility;

public abstract class PredictOperator extends AbstractOperator {
	
	private static final String NUMERIC = "NUMERIC";
	private static final String PREDICTION_NAME = "P";

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_outputSchema,
			OperatorParameter.NAME_outputTable,
			OperatorParameter.NAME_outputTable_StorageParams,
			OperatorParameter.NAME_dropIfExist,
			
	});
		
	public PredictOperator() {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}

	public PredictOperator(List<String> parameterNames) {
		super(parameterNames);
		addInputClass(OperatorInputTableInfo.class.getName());
	}
 

	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			if(para.getValue() instanceof String ){
				String paraValue=(String)para.getValue();
				if(paraName.equals(OperatorParameter.NAME_outputSchema)){
					validateNull(invalidParameterList, paraName, paraValue);
					validateSchemaName(invalidParameterList, paraName, paraValue,variableModel);
				}else if(paraName.equals(OperatorParameter.NAME_outputTable)){
					validateNull(invalidParameterList, paraName, paraValue);
					validateTableName(invalidParameterList, paraName, paraValue,variableModel);
				}else if(paraName.equals(OperatorParameter.NAME_dropIfExist)){
					validateNull(invalidParameterList, paraName, paraValue);
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
	public boolean isInputObjectsReady() {
		List<Object> list = getParentOutputClassList();
		List<OperatorInputTableInfo> dbList = new ArrayList<OperatorInputTableInfo>();
		List<EngineModel> modelList = new ArrayList<EngineModel>();
		if(list != null){
			for(Object obj :list){
				if(obj instanceof OperatorInputTableInfo){
					dbList.add((OperatorInputTableInfo)obj);
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
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList=getOperatorInputList();
		OperatorInputTableInfo operatorInputTableInfo=null;
		for (Object obj: operatorInputList){
			if(obj instanceof OperatorInputTableInfo){
				operatorInputTableInfo=(OperatorInputTableInfo)obj;
				operatorInputTableInfo.setSchema((String)getOperatorParameter(OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String)getOperatorParameter(OperatorParameter.NAME_outputTable).getValue());
				break;
			}
		}	
		
		
		List<UIOperatorModel> parentList = OperatorUtility.getParentList(getOperModel());
		for(UIOperatorModel opModel:parentList){
			if(!(opModel.getOperator() instanceof LinearRegressionOperator)
					&&!(opModel.getOperator() instanceof ModelOperator)){
				continue;
			}
			String dependentColumn=null;
			if(opModel.getOperator() instanceof ModelOperator){
				dependentColumn=((ModelOperator)opModel.getOperator()).getModel().getDependentColumn();		
			}else{
				OperatorParameter operatorParameter=opModel.getOperator().getOperatorParameter(OperatorParameter.NAME_dependentColumn);
				if(operatorParameter!=null){
					dependentColumn=(String)operatorParameter.getValue();
				}
			}
				if(!StringUtil.isEmpty(dependentColumn)){
					String[] newColumn=new String[2];
					newColumn[0]=PREDICTION_NAME+"("+dependentColumn+")";
					newColumn[1]=NUMERIC;
					operatorInputTableInfo.addFieldColumns(newColumn);	
					break;
				}
		}
		
		return operatorInputList;
	}

	@Override
	public ArrayList<Object> getOutputObjectList() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(new OperatorInputTableInfo());
		return list;
	}
	
	@Override
	public String validateInputLink(Operator precedingOperator) {
		String message = super.validateInputLink(precedingOperator);
		
		List<Object> list = precedingOperator.getOutputObjectList();
		if(message != null && !message.trim().equals("")){
			return message;
		}
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
