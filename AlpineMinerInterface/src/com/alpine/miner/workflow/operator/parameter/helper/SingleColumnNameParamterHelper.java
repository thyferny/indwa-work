/**
 * ClassName DBTableNameParamterHelper.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.clustering.KMeansOperator;
import com.alpine.miner.workflow.operator.field.BarChartAnalysisOperator;
import com.alpine.miner.workflow.operator.field.BoxAndWhiskerOperator;
import com.alpine.miner.workflow.operator.field.PivotOperator;
import com.alpine.miner.workflow.operator.field.ScatterPlotOperator;
import com.alpine.miner.workflow.operator.hadoop.*;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.timeseries.TimeSeriesOperator;
import com.alpine.utility.file.StringUtil;

/**
 * panrent could be table, it si simple
 * it also can be sampling 
 * or aggregate or sth could add columns...
 * @author zhaoyong
 *
 */
public class SingleColumnNameParamterHelper extends SingleSelectParameterHelper {
	private String columnType =null;
	
	List<String> numericALLDCOperators=Arrays.asList(new String[]{
			
	});
	
	List<String> canSelectNullValueParameters=Arrays.asList(new String[]{
			OperatorParameter.NAME_aggregateColumn+PivotOperator.class.getCanonicalName(),
			OperatorParameter.NAME_aggregateColumn+HadoopPivotOperator.class.getCanonicalName(),
			OperatorParameter.NAME_IDColumn_lower+KMeansOperator.class.getCanonicalName(),
			OperatorParameter.NAME_IDColumn_lower+ HadoopKmeansOperator.class.getCanonicalName(),
			OperatorParameter.NAME_groupColumn+TimeSeriesOperator.class.getCanonicalName(),
			OperatorParameter.NAME_groupColumn+HadoopTimeSeriesOperator.class.getCanonicalName(),
			OperatorParameter.NAME_C_Column+ScatterPlotOperator.class.getCanonicalName(),
			OperatorParameter.NAME_typeDomain_Column+BoxAndWhiskerOperator.class.getCanonicalName(),
			OperatorParameter.NAME_seriesDomain_Column+BoxAndWhiskerOperator.class.getCanonicalName(),
            OperatorParameter.NAME_typeDomain_Column+ HadoopBoxAndWiskerOperator.class.getCanonicalName(),
            OperatorParameter.NAME_seriesDomain_Column+HadoopBoxAndWiskerOperator.class.getCanonicalName(),
            OperatorParameter.NAME_categoryType+BarChartAnalysisOperator.class.getCanonicalName(),
			OperatorParameter.NAME_categoryType+HadoopBarChartOperator.class.getCanonicalName(),
			OperatorParameter.NAME_scopeDomain+BarChartAnalysisOperator.class.getCanonicalName(),
			OperatorParameter.NAME_scopeDomain+HadoopBarChartOperator.class.getCanonicalName(),
	});

	public SingleColumnNameParamterHelper(){
		
	}
	
	public SingleColumnNameParamterHelper(String columnType){
		this.columnType=columnType;
	}
	
	@Override
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName, ResourceType dbType) throws Exception {
		List<String> avaliableValues=new ArrayList<String>();
		String operatorClassName=parameter.getOperator().getClass().getCanonicalName();
		if(!StringUtil.isEmpty(columnType)){
			avaliableValues = getColumnNames(parameter.getOperator(),columnType,  userName, dbType) ;
		}else if(numericALLDCOperators.contains(operatorClassName)){
			avaliableValues = getColumnNames(parameter.getOperator(), OperatorParameter.Column_Type_Numeric, userName ,dbType);		
		}else{
			avaliableValues= getColumnNames(parameter.getOperator(), OperatorParameter.Column_Type_ALL, userName ,dbType);
		}
		
		if(canSelectNullValueParameters.contains(parameter.getName()+operatorClassName)){
			avaliableValues.add(0,"");
		}
		return avaliableValues;
	}
 
}
