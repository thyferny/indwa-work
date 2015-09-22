/**
 * ClassName ColumnNamesParameterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.association.AssociationOperator;
import com.alpine.miner.workflow.operator.field.CorrelationAnalysisOperator;
import com.alpine.miner.workflow.operator.field.HistogramOperator;
import com.alpine.miner.workflow.operator.field.InformationValueAnalysisOperator;
import com.alpine.miner.workflow.operator.field.IntegerToTextOperator;
import com.alpine.miner.workflow.operator.field.NormalizationOperator;
import com.alpine.miner.workflow.operator.field.ScatterMatrixOperator;
import com.alpine.miner.workflow.operator.field.ValueAnalysisOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopHistogramOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopKmeansOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopLinearRegressionOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopLogisticRegressionOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopNormalizationOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopScatterPlotMatrixOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.utility.db.Resources;

/**
 * parent could be table, it is simple it also can be sampling or aggregate or
 * sth could add columns...
 * 
 * @author zhaoyong
 * 
 */
public class ColumnNamesParameterHelper extends MutilpleSelectParameterHelper {

	List<String> numericALLDCOperators = Arrays.asList(new String[] {
			CorrelationAnalysisOperator.class.getCanonicalName(),
			HistogramOperator.class.getCanonicalName(),
			HadoopHistogramOperator.class.getCanonicalName(),
			NormalizationOperator.class.getCanonicalName(),
			IntegerToTextOperator.class.getCanonicalName(), 
			ScatterMatrixOperator.class.getCanonicalName(), 
			HadoopScatterPlotMatrixOperator.class.getCanonicalName(),
			HadoopNormalizationOperator.class.getCanonicalName(),
			HadoopKmeansOperator.class.getCanonicalName(),
			//HadoopLinearRegressionOperator.class.getCanonicalName(),
			//HadoopLogisticRegressionOperator.class.getCanonicalName()
			});
	
	List<String> noFloatDCOperators=Arrays.asList(new String[]{
			InformationValueAnalysisOperator.class.getCanonicalName(),
	}) ;
	
	List<String> noArrayDCOperators=Arrays.asList(new String[]{
			ValueAnalysisOperator.class.getCanonicalName(),
	}) ;

	List<String> intALLDCOperators = Arrays.asList(new String[] {

	});

	private String columnType = null;

	public ColumnNamesParameterHelper() {

	}

	public ColumnNamesParameterHelper(String columnType) {
		this.columnType = columnType;
	}

	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType,Locale locale) throws Exception {
		return getAvaliableValues(parameter, userName,   dbType);
	}
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,
			String userName, ResourceType dbType) throws Exception {
		String operatorClassName = parameter.getOperator().getClass()
				.getCanonicalName();
		List<String>  columnNames=null;
		if (numericALLDCOperators.contains(operatorClassName)) {
			columnNames= getColumnNames(parameter.getOperator(),
					OperatorParameter.Column_Type_Numeric, userName, dbType);
		} else if (noFloatDCOperators.contains(operatorClassName)) {
			columnNames= getColumnNames(parameter.getOperator(),
					OperatorParameter.Column_Type_NoFloat, userName, dbType);
		} else if (intALLDCOperators.contains(operatorClassName)) {
			columnNames= getColumnNames(parameter.getOperator(),
					OperatorParameter.Column_Type_Int, userName, dbType);
		} else if(parameter.getOperator() instanceof AssociationOperator){
			OperatorParameter useArrayPara=parameter.getOperator().getOperatorParameter(OperatorParameter.NAME_Use_Array);
			if(useArrayPara!=null
					&&Resources.TrueOpt.equals(useArrayPara.getValue())){
				columnNames=getColumnNames(parameter.getOperator(), OperatorParameter.Column_Type_AllArray,
						userName, dbType);
			}else{
				columnNames= getColumnNames(parameter.getOperator(), columnType,
						userName, dbType);
			}
		}else if (noArrayDCOperators.contains(operatorClassName)) {
			columnNames= getColumnNames(parameter.getOperator(),
					OperatorParameter.Column_Type_NoAllArray, userName, dbType);
		}
//		else if(parameter.getOperator() instanceof HadoopOperator){
//			columnNames = OperatorUtility.getAvailableColumnsList(parameter.getOperator(), false);
//			
//		}
		else{
			columnNames= getColumnNames(parameter.getOperator(), columnType,
					userName, dbType);
		}
		
		
		return columnNames;
	}
}
