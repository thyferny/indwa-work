/**
 * ClassName HadoopBarChartOperator.java
 *
 * Version information: 1.00
 *
 * Data: 2011-4-11
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.workflow.operator.parameter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.miner.util.OperatorUtility;
import com.alpine.miner.util.VariableModelUtility;
import com.alpine.miner.workflow.operator.AbstractOperator;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.OperatorInputFileInfo;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.field.BarChartAnalysisOperator;
import com.alpine.miner.workflow.operator.field.FrequencyAnalysisOperator;
import com.alpine.miner.workflow.operator.field.HistogramOperator;
import com.alpine.miner.workflow.operator.field.ValueAnalysisOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopBarChartOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopFrequencyAnalysisOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopHistogramOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopValueAnalysisOperator;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBin;
import com.alpine.miner.workflow.operator.parameter.columnbins.ColumnBinsModel;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopDataType;

/**
 * @author zhao yong
 *
 */
public class ParameterValidateUtility {
	private static final Logger itsLogger=Logger.getLogger(ParameterValidateUtility.class);
	public static String[] validate(Operator operator,VariableModel variableModel){
		List<String> fieldList = OperatorUtility.getAvailableColumnsList(operator,
				false);
	
		List<String> invalidParameterList=new ArrayList<String>();
		if(operator instanceof BarChartAnalysisOperator
				||operator instanceof HadoopBarChartOperator){
		
			validateBarChartParameter(operator, fieldList, invalidParameterList);
		 }else if(operator instanceof HistogramOperator
					||operator instanceof HadoopHistogramOperator){
				
			 validateHistogramParameter(operator, fieldList, invalidParameterList,variableModel);
			 }
		 else if(operator instanceof ValueAnalysisOperator 
					||operator instanceof HadoopValueAnalysisOperator
					||operator instanceof FrequencyAnalysisOperator 
					||operator instanceof HadoopFrequencyAnalysisOperator
					){
				
			 validateColumnNamesParameter(operator, fieldList, invalidParameterList,variableModel);
			 }
		return invalidParameterList.toArray(new String[invalidParameterList.size()]);
	}

	private static void validateColumnNamesParameter(Operator operator,
			List<String> fieldList, List<String> invalidParameterList,
			VariableModel variableModel) {
 
		 
			String paraValue=(String)ParameterUtility.getParameterValue(operator, OperatorParameter.NAME_columnNames);
		 
				validateNull(invalidParameterList, OperatorParameter.NAME_columnNames, paraValue);
				AbstractOperator.validateColumnNames(fieldList,invalidParameterList, OperatorParameter.NAME_columnNames, paraValue);
	 	
		 
		
	}

	private static void validateHistogramParameter(Operator operator,
			List<String> fieldList, List<String> invalidParameterList,VariableModel variableModel){
 
	 
			List<OperatorParameter> paraList=operator.getOperatorParameterList();
			ColumnBinsModel model=null;
			for(OperatorParameter para:paraList){
				if(para.getValue() instanceof ColumnBinsModel){
					model=(ColumnBinsModel)para.getValue();
					break;
				}
			}
			for(OperatorParameter para:paraList){
				String paraName=para.getName();
				if(paraName.equals(OperatorParameter.NAME_Columns_Bins)){
					if(model==null||model.getColumnBins()==null||model.getColumnBins().isEmpty()){
					invalidParameterList.add(paraName);
					continue;
					}else{
						List<ColumnBin> columnBins = model.getColumnBins();
						for(ColumnBin columnBin:columnBins){
							if(columnBin.getType()==0){
								String bin = VariableModelUtility.getReplaceValue(variableModel, columnBin.getBin());
								if(bin==null||!AlpineUtil.isInteger(bin)||Integer.parseInt(bin)>100||Integer.parseInt(bin)<2){
									if(!invalidParameterList.contains(paraName)){
										invalidParameterList.add(paraName);
										break;
									}
								}
							}else if(columnBin.getType()==1){
								String width = VariableModelUtility.getReplaceValue(variableModel, columnBin.getWidth());
								if(!AlpineUtil.isNumber(width)||Double.parseDouble(width)<=0.0){
									if(!invalidParameterList.contains(paraName)){
										invalidParameterList.add(paraName);
										break;
									}
								}
							}
							if(columnBin.isMax()){
								String max = VariableModelUtility.getReplaceValue(variableModel, columnBin.getMax());
								if(!AlpineUtil.isNumber(max)){
									if(!invalidParameterList.contains(paraName)){
										invalidParameterList.add(paraName);
										break;
									}
								}
							}
							if(columnBin.isMin()){
								String min = VariableModelUtility.getReplaceValue(variableModel, columnBin.getMin());
								if(!AlpineUtil.isNumber(min)){
									if(!invalidParameterList.contains(paraName)){
										invalidParameterList.add(paraName);
										break;
									}
								}
							}
						}
					}
					validateColumnBins(fieldList,invalidParameterList,paraName,model,operator);
				}		
			}
		 
 
	}
	

	private static void validateColumnBins(List<String> fieldList, List<String> invalidParameterList, String paraName, ColumnBinsModel columnBinsModel,  Operator operator) {
		if (!invalidParameterList.contains(paraName)) {
			List<ColumnBin> needRemovedColumnBins=new ArrayList<ColumnBin>();
			List<ColumnBin> columnBins = columnBinsModel.getColumnBins();
			for(ColumnBin columnBin:columnBins){
				if (false ==fieldList.contains(columnBin.getColumnName())){
						 	needRemovedColumnBins.add(columnBin);
				}else if (operator instanceof HadoopOperator){
					if(false==validateNumberColumn(columnBin.getColumnName(),(HadoopOperator)operator,OperatorParameter.NAME_valueDomain) ){
						invalidParameterList.add(paraName);
							return ;
					}
						 
				}
			}
			if(needRemovedColumnBins.size()>0){
				invalidParameterList.add(paraName);
			}
		}
	}
	
	private static void validateBarChartParameter(Operator operator,
			List<String> fieldList, List<String> invalidParameterList) {
		List<OperatorParameter> paraList=operator.getOperatorParameterList();
		for(OperatorParameter para:paraList){
			String paraName=para.getName();
			String paraValue=(String)para.getValue();
			if(paraName.equals(OperatorParameter.NAME_valueDomain)){
				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
				if(operator instanceof HadoopOperator){
					if(!validateNumberColumn(paraValue, (HadoopOperator)operator,OperatorParameter.NAME_valueDomain)){
						invalidParameterList.add(paraName);
						return;
					}
				}
			}else if(paraName.equals(OperatorParameter.NAME_scopeDomain)){
//				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}else if(paraName.equals(OperatorParameter.NAME_categoryType)){
//				validateNull(invalidParameterList, paraName, paraValue);
				validateContainColumns(fieldList,invalidParameterList, paraName, paraValue);
			}		
		}
		//type :value can only be number type ...
	}
	
	public static boolean validateNumberColumn(String columnName, HadoopOperator operator, String paraName ) {
		if (operator.getParentHadoopFileInputs().size()==0){
			return false;
		}	
		String columnType = getInputColumnType(columnName, operator);
			if(HadoopDataType.isNumberType(columnType)==false){
				return false;
			}
		 return true;
		
	}

	public static String getInputColumnType(String columnName,
			HadoopOperator operator) {
		OperatorInputFileInfo fileInfo = operator.getParentHadoopFileInputs().get(0);
		return getInputColumnType(columnName, fileInfo);
	}

	public static String getInputColumnType(String columnName,
			OperatorInputFileInfo fileInfo) {
		if(null==fileInfo||null==fileInfo.getColumnInfo()){
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("There is no column so there is no need for type etc");
			}
			return "";
		}
		List<String> nameList = fileInfo.getColumnInfo().getColumnNameList();
		int index = nameList.indexOf(columnName);
		if(-1==index){
			if(itsLogger.isDebugEnabled()){
				itsLogger.debug("Column is not assigned to a type yet["+columnName+"]does not exist");
			}
			return "";
		}
		String columnType = fileInfo.getColumnInfo().getColumnTypeList().get(index) ;
		return columnType;
	}

	protected static void validateNull(List<String> invalidParameterList,
			String paraName, String paraValue) {
		if (StringUtil.isEmpty(paraValue)
				&& !invalidParameterList.contains(paraName)) {
			invalidParameterList.add(paraName);
		}
	}

	protected  static void validateContainColumns(List<String> fieldList, List<String> invalidParameterList, String paraName, String paraValue) {
		if (!invalidParameterList.contains(paraName)&&!StringUtil.isEmpty(paraValue)) {
			if (!fieldList.contains(paraValue)) {
				invalidParameterList.add(paraName);
			} 
		}
	}
	

}
