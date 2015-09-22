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

import java.util.Arrays;
import java.util.List;

import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.adaboost.AdaboostOperator;
import com.alpine.miner.workflow.operator.decisiontree.DecisionTreeOperator;
import com.alpine.miner.workflow.operator.field.InformationValueAnalysisOperator;
import com.alpine.miner.workflow.operator.field.UnivariateOperator;
import com.alpine.miner.workflow.operator.field.variableselection.VariableSelectionAnalysisOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopDecisionTreeOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopLinearRegressionOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopLogisticRegressionOperator;
import com.alpine.miner.workflow.operator.hadoop.HadoopVariableSelectionAnalysisOperator;
import com.alpine.miner.workflow.operator.linearregression.LinearRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.LogisticRegressionOperator;
import com.alpine.miner.workflow.operator.logisticregression.woe.WOEOperator;
import com.alpine.miner.workflow.operator.naivebayes.NaiveBayesOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.svd.SVDLanczosOperator;
import com.alpine.miner.workflow.operator.svm.SVMClassificationOperator;
import com.alpine.miner.workflow.operator.svm.SVMRegressionOperator;

/**
 * couln could comes from table, sampling and some like aggregate, table join ...
 * @author zhaoyong
 *
 */
public class DependentColumnParamterHelper extends SingleSelectParameterHelper {
	
	//if not appear in the numeric and category, it means all the column  
	List<String> numericALLDCOperators=Arrays.asList(new String[]{
			LinearRegressionOperator.class.getCanonicalName(),
            HadoopVariableSelectionAnalysisOperator.class.getCanonicalName(),
			HadoopLinearRegressionOperator.class.getCanonicalName(),
            //HadoopLogisticRegressionOperator.class.getCanonicalName(),
			SVMRegressionOperator.class.getCanonicalName(),
			SVDLanczosOperator.class.getCanonicalName(),
	}) ;
	List<String> noFloatDCOperators=Arrays.asList(new String[]{
			HadoopDecisionTreeOperator.class.getCanonicalName(),
			DecisionTreeOperator.class.getCanonicalName(),
			InformationValueAnalysisOperator.class.getCanonicalName(),
			UnivariateOperator.class.getCanonicalName(),
			LogisticRegressionOperator.class.getCanonicalName(),
			HadoopLogisticRegressionOperator.class.getCanonicalName(),
			VariableSelectionAnalysisOperator.class.getCanonicalName(),
            HadoopVariableSelectionAnalysisOperator.class.getCanonicalName(),
			AdaboostOperator.class.getCanonicalName(),
			SVMClassificationOperator.class.getCanonicalName(),
			WOEOperator.class.getCanonicalName(),
			NaiveBayesOperator.class.getCanonicalName(),
	}) ;
	
	//all:nn carttree csvm nb
	 

	public DependentColumnParamterHelper(){
		
	}
	
	 
	public List<String> getAvaliableValues(OperatorParameter parameter,String userName ,ResourceType dbType ) throws Exception {
		//String schemaName=getSchemaNameInOperator(parameter.getOperator());//schemaName or outputSchema
		//String dbConnName=getDBConnNameInOperator(parameter.getOperator());

		//DBUtil.getColumnList(user, conn, schema, table) ;
		// TODO :it will dynamiclly make numeric or category or both column names based on the operator (get from operator parameter)
		String operatorClassName=parameter.getOperator().getClass().getCanonicalName();
		if(numericALLDCOperators.contains(operatorClassName)){
			return getColumnNames(parameter.getOperator(), OperatorParameter.Column_Type_Numeric, userName,dbType);
			
		}else if(noFloatDCOperators.contains(operatorClassName)){
			return getColumnNames(parameter.getOperator(), OperatorParameter.Column_Type_NoFloat, userName,dbType);	
		}
		else{
			return getColumnNames(parameter.getOperator(), OperatorParameter.Column_Type_ALL, userName,dbType);
		}
	}

 

}
