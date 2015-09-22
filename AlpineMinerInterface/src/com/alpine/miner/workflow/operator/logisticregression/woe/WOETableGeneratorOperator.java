package com.alpine.miner.workflow.operator.logisticregression.woe;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.db.attribute.woe.WOEDataSQL;
import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.model.UIOperatorModel;
import com.alpine.miner.workflow.operator.OperatorInputTableInfo;
import com.alpine.miner.workflow.operator.PredictOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.ParameterUtility;
import com.alpine.utility.file.StringUtil;

public class WOETableGeneratorOperator extends PredictOperator {

//	public static final List<String> parameterNames = Arrays
//			.asList(new String[] { OperatorParameter.NAME_outputSchema,
//					OperatorParameter.NAME_outputTable,
//					OperatorParameter.NAME_dropIfExist,
//					OperatorParameter.NAME_remainColumns });

	public WOETableGeneratorOperator() {
		super();
		addInputClass(EngineModel.MPDE_TYPE_WOE);
		addOutputClass(OperatorInputTableInfo.class.getName());
	}

	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.WOE_TABLE_GENERATOR_OPERATOR,locale);
	}


	@Override
	public List<Object> getOperatorOutputList() {
		List<Object> operatorInputList = new ArrayList<Object>();
		List<String[]> newFieldColumns = new ArrayList<String[]>();
		String dbType=null;
		if(getOperatorInputList()==null||getOperatorInputList().size()==0){
			return operatorInputList;
		}
		for (Object obj : getOperatorInputList()) {
			if (obj instanceof OperatorInputTableInfo) {
				OperatorInputTableInfo operatorInputTableInfo = (OperatorInputTableInfo) obj;
				dbType=operatorInputTableInfo.getSystem();
				List<String[]> oldFieldColumns = operatorInputTableInfo
						.getFieldColumns();
				newFieldColumns.addAll(oldFieldColumns);
				operatorInputTableInfo.setSchema((String) getOperatorParameter(
						OperatorParameter.NAME_outputSchema).getValue());
				operatorInputTableInfo.setTable((String) getOperatorParameter(
						OperatorParameter.NAME_outputTable).getValue());
				operatorInputTableInfo.setFieldColumns(newFieldColumns);
				operatorInputList.add(operatorInputTableInfo);
			}
		}
		List<UIOperatorModel> parentList = OperatorUtility.getParentList(getOperModel());
		for(UIOperatorModel opModel:parentList){
			if(opModel.getOperator() instanceof WOEOperator){
				Object obj=opModel.getOperator().getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				if(obj!=null){
					 String columnNames= (String)obj;
					if(!StringUtil.isEmpty(columnNames)){
						String[] ss=columnNames.split(",");
						for(String s:ss){
							String dataType = ParameterUtility.getDoubleType(dbType);
							newFieldColumns.add(new String[]{WOEDataSQL.addPre(s),dataType});
						}
					}
				}
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
}
