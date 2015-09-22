/**
 * ClassName SvdCalKeyColumnHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterInputType;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 *
 */
public class SvdCalKeyColumnHelper extends SingleSelectParameterHelper {
	
 	/**Tell the user this is a text input.
 	 * */
	@Override
	public String getInputType(String parameterName) {
		return ParameterInputType.SINGLE_SELECT;
	}
	//this is special ,need get the parant's 2 parameter: colName, rowName
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType) {
		List<String> result= new ArrayList<String>();
		if(isFromSVD(parameter.getOperator())){
			List<Operator> parents = parameter.getOperator().getParentOperators();
			if(parents!=null&&!parents.isEmpty()){
				Operator parent = parents.get(0); 
				List<OperatorParameter> paramList = parent.getOperatorParameterList();
				for(int i=0;i<paramList.size();i++){
					OperatorParameter param = paramList.get(i);
					if(param.getName().equals(OperatorParameter.NAME_RowName) 
							||param.getName().equals(OperatorParameter.NAME_ColName)){
						if(param.getValue()!=null&&result.contains(param.getValue())==false){
							result.add((String)param.getValue()) ;
						}				
					}
				}
			}
		}else{
			String rowName=(String)parameter.getOperator().getOperatorParameter(OperatorParameter.NAME_RowNameF).getValue();
			String colName=(String)parameter.getOperator().getOperatorParameter(OperatorParameter.NAME_ColNameF).getValue();
			
			if(!StringUtil.isEmpty(rowName)&&result.contains(rowName)==false){
				result.add(rowName);
			}
			if(!StringUtil.isEmpty(colName)&&result.contains(colName)==false){
				result.add(colName);
			}
		}	
		return result ;
	
	}
	
	private boolean isFromSVD(Operator operator) {
		boolean isFromSVD = false;
		List<UIOperatorModel> opModels = OperatorUtility
				.getParentList(operator.getOperModel());
		for (UIOperatorModel opModel : opModels) {
			if (opModel.getOperator() instanceof SVDLanczosOperator) {
				isFromSVD = true;
				break;
			}
		}
		return isFromSVD;
	}
 
}
