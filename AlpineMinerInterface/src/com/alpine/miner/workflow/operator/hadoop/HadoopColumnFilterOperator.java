package com.alpine.miner.workflow.operator.hadoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.inter.resources.LanguagePack;
import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModel;
import com.alpine.miner.workflow.operator.parameter.FileStructureModelUtility;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;
import com.alpine.utility.file.StringUtil;

public class HadoopColumnFilterOperator extends HadoopDataOperationOperator {

	public static final List<String> parameterNames = Arrays.asList(new String[]{
			OperatorParameter.NAME_columnNames,
			OperatorParameter.NAME_HD_StoreResults,
			OperatorParameter.NAME_HD_ResultsLocation,
			OperatorParameter.NAME_HD_ResultsName,
			OperatorParameter.NAME_HD_Override,
 
	});
	
	public HadoopColumnFilterOperator() {
		super(parameterNames);
	}
	
	@Override
	public boolean isVaild(VariableModel variableModel) {
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(this,
				false);
		
		List<String> invalidParameterList=new ArrayList<String>();
		List<OperatorParameter> paraList=getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			
			if(para.getValue()!=null&&false==(para.getValue() instanceof String)){
				continue;
			}
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_columnNames)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateColumnNames(fieldList,invalidParameterList, paraName, paraValue);
			}else {
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
		
		for (Object obj: getOperatorInputList()){
			if(obj instanceof OperatorInputFileInfo){
				OperatorInputFileInfo operatorInputFileInfo=(OperatorInputFileInfo)obj;
				operatorInputFileInfo=operatorInputFileInfo.clone();
                operatorInputFileInfo.setOperatorUUID(this.getOperModel().getUUID());

				operatorInputFileInfo.setHadoopFileName(getOutputFileName());
				
				String columnName=(String)getOperatorParameter(OperatorParameter.NAME_columnNames).getValue();
				
				List<String> newColumnNameList = new ArrayList<String>();
				List<String> newColumnTypeList = new ArrayList<String>();
				 //for xml -> csv...
                FileStructureModelUtility.switchFileStructureModel(operatorInputFileInfo) ;

				FileStructureModel columnInfo = operatorInputFileInfo.getColumnInfo();
				
				if(columnInfo==null){
					continue;
				}
				List<String> columnNameList = columnInfo.getColumnNameList();
				List<String> columnTypeList = columnInfo.getColumnTypeList();
				if(StringUtil.isEmpty(columnName)==false){
					String[] columnNames = columnName.split(",");
					for(String column:columnNames){
						newColumnNameList.add(column);
						for(int i=0;i<columnNameList.size();i++){
							if(column.equals(columnNameList.get(i))){
								newColumnTypeList.add(columnTypeList.get(i));
								break;
							}
						}
					}
				}
				
				columnInfo.setIsFirstLineHeader(Resources.FalseOpt);
				columnInfo.setColumnNameList(newColumnNameList);
				columnInfo.setColumnTypeList(newColumnTypeList);
				operatorInputFileInfo.setColumnInfo(columnInfo);
				operatorInputList.add(operatorInputFileInfo);
				break;
			}
		}
		return operatorInputList;
	}
	
	
	@Override
	public String getToolTipTypeName() {
		return LanguagePack.getMessage(LanguagePack.COLUMNFILTER_OPERATOR,locale);
	}

}
