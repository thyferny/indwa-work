/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * PropertyUtil.java
 * 
 * Author sam_zang
 * Version 3.0
 * Date Aug 20, 2011
 */

package com.alpine.miner.impls.controller;

import com.alpine.miner.impls.controller.PropertyDTO.PropertyType;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.workflow.operator.VariableModel;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterFactory;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterHelper;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author sam_zang
 *
 */
public class PropertyUtil {

	static class PropertyDefault {
		PropertyDefault(String name, String diaplayName, PropertyType type, String[] selection, OperatorParameterHelper helper) {
			this.name = name;
			this.diaplayName = diaplayName;
			this.type = type;
			this.selection = selection;
			this.helper = helper;
		}
		
		String name;
		String diaplayName;
		PropertyType type;
		String[] selection;
		OperatorParameterHelper helper;				
	}
 
	
	private static HashMap<String, PropertyDefault> map = new HashMap<String, PropertyDefault>();
	

	/**
	 * @param propertyName
	 * @return
	 */
	public static PropertyDTO.PropertyType getType(String propertyName) {
		
		PropertyDefault prop = map.get(propertyName);
		if (prop != null) {
			return prop.type;
		}
		return PropertyDTO.PropertyType.PT_NOT_IN_LIST;
	}
	
	/**
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static String[] getSlection(String name) {
		PropertyDefault prop = map.get(name);

		if (prop == null) {
			return null;
		}

		return prop.selection;

	}

	public static String[] getDefaultValues(OperatorParameter parameter,String userName,ResourceType dbType,Locale locale) {
		String name = parameter.getName();
		PropertyDefault prop = map.get(name);
		
		if (prop == null) {
			return null;
		}
		if (prop.selection != null) {
			return prop.selection;
		}
		if (prop.helper != null   ) {
			try {
				 List<String> values = prop.helper.getAvaliableValues(parameter, userName, dbType,locale);
				 if(values!=null){
					 return values.toArray(new String[values.size()]);	 
				 }
				
			} catch (Exception e) {
				e.printStackTrace();
//				throw e;
			}
		}
		return null;
	}
	
	// the helper types.
	private static final OperatorParameterHelper dbconnection_helper = OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_dBConnectionName);
	private static final OperatorParameterHelper schema_helper = OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_schemaName);
	private static final OperatorParameterHelper inputSchema_helper = new InputSchemaHelper(schema_helper);
	private static final OperatorParameterHelper table_helper = OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_tableName);
	private static final OperatorParameterHelper column_helper = OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_columnNames);
	private static final OperatorParameterHelper number_column_helper = OperatorParameterFactory.singleNumericColumnParameterHelper;
	// Default values for selection, they are well defined.
	private static final String[] k_mean_distance = {"Euclidean", 
		"GeneralizedIDivergence", "KLDivergence", "CamberraNumerical",
		"Manhattan", "CosineSimilarity", "DiceNumericalSimilarity",
		"InnerProductSimilarity", "JaccardNumericalSimilarity"};
	
	private static final String[] normalization_methods = {
		"Proportion-Transformation", "Range-Transformation", 
		"Z-Transformation", "DevideByAverage-Transformation"};
	
	private static final String[] sample_type = {"Row", "Percentage"};
	private static final String[] yes_or_no = {"Yes" , "No"};
	private static final String[] true_or_false = {"true", "false"};
	private static final String[] table_or_view = {"TABLE", "VIEW"};
	private static final String[] adjust_per = {"ROW", "ALL"};
	private static final String[] expression = {"=", ">", "<", ">=", "<=", "<>"};
	private static final String[] feature_column = {"alpine_feature", "other_featyures"};
	private static final String[] score_type_list = {"Info gain", "Info gain ratio", "Transformed info gain"};
	private static final String[] kernel_type_list = {"dot product", "polynomial", "gaussian"};
	private static final String[] aggregate_tyypes = {"sum", "avg", "count", "max", "min"};
	
	// WARNING: This is the full list of all property
	// names used in ALL operators.
	// When new operator, new property is added
	// this list need to be updated.
	private static PropertyDefault[] default_property_array = {
		new PropertyDefault("adjust_per", null, PropertyType.PT_CHOICE, adjust_per, null),
		new PropertyDefault("aggregateColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("aggregateDataTypeList", null, PropertyType.PT_UNKNOWN, null, null),  
		new PropertyDefault("aggregateExpressionList", null, PropertyType.PT_UNKNOWN, null, null),  
		new PropertyDefault("aggregateType", null, PropertyType.PT_SINGLE_SELECT, aggregate_tyypes, null),
		new PropertyDefault("aheadNumber", "AheadNumber", PropertyType.PT_INT, null, null),
		new PropertyDefault("columns_bins", null, PropertyType.PT_HISTOGRAM, null, column_helper),  
		new PropertyDefault("calculateDeviance", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("categoryType", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("categoryDomain", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("clusterColumnName", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("columnNames", null, PropertyType.PT_MULTI_SELECT, null, column_helper),
		new PropertyDefault("columnValue", null, PropertyType.PT_STRING, null, null),
		new PropertyDefault("confidence", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("consistent", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("createSequenceID", null, PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("d", "Degree of differencing", PropertyType.PT_INT, null, null),
		new PropertyDefault("dataTypeList", null, PropertyType.PT_UNKNOWN, null, null), 
		new PropertyDefault("dbConnectionName", null, PropertyType.PT_SINGLE_SELECT, null, dbconnection_helper),
		new PropertyDefault("decay", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("degree", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("dependentColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("disjoint", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("distance", null, PropertyType.PT_SINGLE_SELECT, k_mean_distance, null),
		new PropertyDefault("dropIfExist", null, PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("epsilon", null, PropertyType.PT_DOUBLE , null, null),
		new PropertyDefault("error_epsilon", null, PropertyType.PT_DOUBLE , null, null),
		new PropertyDefault("eta", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("expression", null, PropertyType.PT_SINGLE_SELECT, expression, null),
		new PropertyDefault("expressionList", null, PropertyType.PT_UNKNOWN, null, null),  
		new PropertyDefault("fetchSize", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("forceRetrain", null, PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("gamma", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("good", "goodValue", PropertyType.PT_STRING, null, null),
		new PropertyDefault("goodValue", null, PropertyType.PT_STRING, null, null),
		new PropertyDefault("groupByFieldList", null, PropertyType.PT_UNKNOWN, null, null), 
		new PropertyDefault("idColumn", "Sequence Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("IDColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("k", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("kernelType", null, PropertyType.PT_SINGLE_SELECT, kernel_type_list, null),
		new PropertyDefault("keyColumnList", null, PropertyType.PT_MULTI_SELECT, null, column_helper),
		new PropertyDefault("learning_rate", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("local_random_seed", "random seed", PropertyType.PT_INT, null, null),
		new PropertyDefault("max_generations", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("maximal_depth", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("max_optimization_steps", "max optimization steps", PropertyType.PT_INT, null, null),
		new PropertyDefault("max_runs", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("method", null, PropertyType.PT_SINGLE_SELECT, normalization_methods, null),
		new PropertyDefault("minConfidence", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("minimal_gain", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("minimal_leaf_size", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("minimal_size_for_split", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("minSupport", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("modifyOriginTable", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("momentum", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("no_pre_pruning", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("no_pruning", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("normalize", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("nu", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("number_of_prepruning_alternatives", "Prepruning alternatives", PropertyType.PT_INT, null, null),
		new PropertyDefault("outputSchema", null, PropertyType.PT_SINGLE_SELECT, null, schema_helper),
		new PropertyDefault("outputTable", null, PropertyType.PT_STRING, null, null),
		new PropertyDefault("outputType", null, PropertyType.PT_CHOICE, table_or_view, null),
		new PropertyDefault("p", "AR Order", PropertyType.PT_INT, null, null),
		new PropertyDefault("parentFieldList", null, PropertyType.PT_UNKNOWN, null, null),  
		new PropertyDefault("pivotColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		

		new PropertyDefault("positiveValue", null, PropertyType.PT_STRING, null, null),  // TODO:
		new PropertyDefault("q", "MA Order", PropertyType.PT_INT, null, null),
		new PropertyDefault("randomSeed", null, PropertyType.PT_DOUBLE , null, null),
		new PropertyDefault("rangeMax", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("rangeMin", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("replacement", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("replacementNull", "replacement", PropertyType.PT_CUSTOM_REPLACEMENT, null, column_helper),
		new PropertyDefault("sampleCount", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("sampleSize", null, PropertyType.PT_CUSTOM_SAMPLE_SIZE, null, null),
		new PropertyDefault("sampleSizeType", null, PropertyType.PT_CHOICE, sample_type, null),
		new PropertyDefault("samplingColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("schemaName", null, PropertyType.PT_SINGLE_SELECT, null, inputSchema_helper),
		new PropertyDefault("scopeDomain", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("selectedFieldList", null, PropertyType.PT_UNKNOWN, null, null),  
		new PropertyDefault("selectedTable", null, PropertyType.PT_SINGLE_SELECT, null, null), 
		new PropertyDefault("size_threshold_load_data", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("slambda", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("split_Number", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("tableName", null, PropertyType.PT_SINGLE_SELECT, null, table_helper),
		new PropertyDefault("tableSizeThreshold", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("tableType", null, PropertyType.PT_CHOICE, table_or_view, null),
		new PropertyDefault("threshold", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("training_cycles", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("useArray", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("useModel", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("valueColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("valueDomain", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("whereClause", null, PropertyType.PT_CUSTOM_WHERECLAUSE, null, column_helper),
		new PropertyDefault("windowDataTypeList", null, PropertyType.PT_UNKNOWN, null, null),  
		new PropertyDefault("windowFunctionList", null, PropertyType.PT_UNKNOWN, null, null),   
		new PropertyDefault("windowSpecList", null, PropertyType.PT_UNKNOWN, null, null) , 
		
		// added SVD properties
		new PropertyDefault("colName", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("crossProduct", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("earlyTeminate", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("fastSpeedupConst", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("improvementReached", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("initValue", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("keyColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("keyValue", null, PropertyType.PT_STRING, null, null),
		new PropertyDefault("minImprovement", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("minNumIterations", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("numFeatures", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("numIterations", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("originalStep", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("rowName", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("slowdownConst", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("speedupConst", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("UdependentColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("UfeatureColumn", null, PropertyType.PT_SINGLE_SELECT, feature_column, null),
		new PropertyDefault("UmatrixDropIfExist", null, PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("UmatrixSchema", null, PropertyType.PT_SINGLE_SELECT, null, schema_helper),
		new PropertyDefault("UmatrixTable", null, PropertyType.PT_STRING, null, null),
		new PropertyDefault("VdependentColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("VfeatureColumn", null, PropertyType.PT_SINGLE_SELECT, feature_column, null),
		new PropertyDefault("VmatrixDropIfExist", null, PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("VmatrixSchema", null, PropertyType.PT_SINGLE_SELECT, null, schema_helper),
		new PropertyDefault("VmatrixTable", null, PropertyType.PT_STRING, null, null),
		
		new PropertyDefault("colNameF", "colName", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("rowNameF", "rowName", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("UdependentColumnF", "UdependentColumn", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("UmatrixTableF", "UmatrixTable", PropertyType.PT_STRING, null, null),
		new PropertyDefault("VdependentColumnF", "VdependentColumn", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("VmatrixTableF", "VmatrixTable", PropertyType.PT_STRING, null, null),
		new PropertyDefault("singularValuedependentColumnF", "Singular Value Dependent Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("singularValueTableF", "Singular Value Table", PropertyType.PT_STRING, null, null),
		
		// from variable selection
		new PropertyDefault("scoreType", null, PropertyType.PT_SINGLE_SELECT, score_type_list, null),
		
		// from new SVD classes
		new PropertyDefault("singularValuedependentColumn", "Singular Value Dependent Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("singularValueDropIfExist", "Singular Value DropIfExist", PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("singularValuefeatureColumn", "Singular Value Feature Column", PropertyType.PT_SINGLE_SELECT, feature_column, null),
		new PropertyDefault("singularValueSchema", "Singular Value Schema", PropertyType.PT_SINGLE_SELECT, null, schema_helper),
		new PropertyDefault("singularValueTable", "Singular Value Table", PropertyType.PT_STRING, null, null),
		
		// special popup support data.
		
		// 3 for aggregate operator
		new PropertyDefault("aggregateFieldList", null, PropertyType.PT_CUSTOM_AGG_COLUMN, null, column_helper),
		new PropertyDefault("windowFieldList", null, PropertyType.PT_CUSTOM_AGG_WINDOW, null, column_helper),
		new PropertyDefault("groupByColumn", null, PropertyType.PT_CUSTOM_AGG_GROUPBY, null, column_helper),
		new PropertyDefault("groupColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),
				
		// 2 for variable operator		
		new PropertyDefault(OperatorParameter.NAME_fieldList,
				null, PropertyType.PT_CUSTOM_VAR_FIELDLIST, null, column_helper),
		new PropertyDefault(OperatorParameter.NAME_quantileFieldList,
				null, PropertyType.PT_CUSTOM_VAR_QUANTILE, null, column_helper),
				
		// 1 for hidder_layers		
		new PropertyDefault(OperatorParameter.NAME_hidden_layers,
				null, PropertyType.PT_CUSTOM_NEURAL_HIDDEN_LAYERS, null, null),
				
		// 1 for table join
		new PropertyDefault(OperatorParameter.NAME_Set_Table_Join_Parameters,
				null, PropertyType.PT_CUSTOM_TABLEJOIN, null, null),
		
		new PropertyDefault(OperatorParameter.NAME_Interaction_Columns,
				null, PropertyType.PT_CUSTOM_INTERACTION_COLUMNS, null, null),	
		
		new PropertyDefault(OperatorParameter.NAME_WOEGROUP,
						null, PropertyType.PT_CUSTOM_WOE, null, null),	
		new PropertyDefault(OperatorParameter.NAME_SQL_Execute_Text,
								null, PropertyType.PT_TEXT, null, column_helper),
		// added for product recommendation.
		new PropertyDefault("customerTable", "Customer Table", PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("customerIDColumn", "Customer ID Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("customerValueColumn", "Customer Value Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("customerProductColumn", "Customer Product Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("customerProductCountColumn", "Customer Product Count Column", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("selectionTable", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("selectionIDColumn", null, PropertyType.PT_SINGLE_SELECT, null, column_helper),	 
		new PropertyDefault("maxRecords", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("minProductCount", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("cohortsAbove", null, PropertyType.PT_INT, null, null),
		new PropertyDefault("cohortsBelow", null, PropertyType.PT_INT, null, null),		
		new PropertyDefault("simThreshold", null, PropertyType.PT_STRING, null, null),
		new PropertyDefault("scoreThreshold", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("targetCohort", null, PropertyType.PT_DOUBLE , null, null),
		 
		new PropertyDefault("cohorts", "Cohorts", PropertyType.PT_CUSTOM_COHORTS, null, null),
		 
		new PropertyDefault("recommendationTable", "Recommendation Table", PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("recommendationIdColumn", "Recommendation Id Column", PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("recommendationProductColumn", "Recommendation Product Column", PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("preTable", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("preIdColumn", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("preValueColumn", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("postTable", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("postIdColumn", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("postProductColumn", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("postValueColumn", null, PropertyType.PT_SINGLE_SELECT, null, null),
		
		
		new PropertyDefault("adaboostUIModel", "Parameters Setting", PropertyType.PT_CUSTOM_ADABOOST, null, null),

		new PropertyDefault("analysisType", "Analysis Type", PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("PCAQoutputSchema", "resultOutputSchema", PropertyType.PT_SINGLE_SELECT, null, schema_helper),
		new PropertyDefault("PCAQDropIfExist", "resultDropIfExist", PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault("PCAQvalueOutputSchema", "valuesOutputSchema", PropertyType.PT_SINGLE_SELECT, null, schema_helper),
		new PropertyDefault("PCAQvalueDropIfExist", "valuesDropIfExist", PropertyType.PT_CHOICE, yes_or_no, null),

		new PropertyDefault("percent", "Percent", PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault("PCAQoutputTable", "resultOutputTable", PropertyType.PT_STRING, null, null),
		new PropertyDefault("PCAQvalueOutputTable", "valuesOutputTable", PropertyType.PT_STRING, null, null),
		//PCA
		new PropertyDefault("remainColumns", "IncludeSourceColumns", PropertyType.PT_MULTI_SELECT, null, column_helper),
		//UDF
		new PropertyDefault("includeSourceColumns", "IncludeSourceColumns", PropertyType.PT_MULTI_SELECT, null, column_helper),

		
		new PropertyDefault("isStepWise", null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault("stepWiseType", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("criterionType", null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault("checkValue", null, PropertyType.PT_DOUBLE, null, null),
		new PropertyDefault(OperatorParameter.NAME_groupByTrainerColumn, null, PropertyType.PT_SINGLE_SELECT, null, OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_groupByTrainerColumn)),
		new PropertyDefault(OperatorParameter.NAME_AddResidualPlot, null, PropertyType.PT_BOOLEAN, true_or_false,null),

		
		new PropertyDefault(OperatorParameter.NAME_tableSetConfig, null, PropertyType.PT_CUSTOM_TABLESET, null, null),
		
		//PLDA 
		new PropertyDefault("contentDocIndexColumn", "contentDocIndexColumn", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("contentWordColumn", "contentWordColumn", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("dictionarySchema", "dictionarySchema", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("dictionaryTable", "dictionaryTable", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("dicIndexColumn", "dicIndexColumn", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("dicContentColumn", "dicContentColumn", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("PLDADropIfExist", "PLDADropIfExist", PropertyType.PT_BOOLEAN, null, column_helper),
		new PropertyDefault("PLDAModelOutputSchema", "PLDAModelOutputSchema", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("PLDAModelOutputTable", "PLDAModelOutputTable", PropertyType.PT_STRING, null, column_helper),

		new PropertyDefault("Alpha", "Alpha", PropertyType.PT_DOUBLE, null, column_helper),
		new PropertyDefault("Beta", "Beta", PropertyType.PT_DOUBLE, null, column_helper),
		new PropertyDefault("iterationNumber", "iterationNumber", PropertyType.PT_INT, null, column_helper),
		new PropertyDefault("topicNumber", "topicNumber", PropertyType.PT_INT, null, column_helper),
		new PropertyDefault("topicOutTable", "topicOutTable", PropertyType.PT_STRING, null, column_helper),
		
		new PropertyDefault("topicOutSchema", "topicOutSchema", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("topicDropIfExist", "topicDropIfExist", PropertyType.PT_BOOLEAN, null, column_helper),
		new PropertyDefault("docTopicOutTable", "docTopicOutTable", PropertyType.PT_STRING, null, column_helper),
		new PropertyDefault("docTopicOutSchema", "docTopicOutSchema", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("docTopicDropIfExist", "docTopicDropIfExist", PropertyType.PT_BOOLEAN, null, column_helper),
		
		
		//PLDA Predictor
		new PropertyDefault("PLDADocTopicOutputTable", "PLDADocTopicOutputTable", PropertyType.PT_STRING, null, column_helper),
		new PropertyDefault("PLDADocTopicOutputSchema", "PLDADocTopicOutputSchema", PropertyType.PT_SINGLE_SELECT, null, column_helper),
		new PropertyDefault("PLDADocTopicDropIfExist", "PLDADocTopicDropIfExist", PropertyType.PT_BOOLEAN, null, column_helper),
		new PropertyDefault("IterationNumber", "IterationNumber", PropertyType.PT_INT, null, column_helper),
		
		new PropertyDefault(OperatorParameter.NAME_forestSize, null, PropertyType.PT_INT, null, null),
		new PropertyDefault(OperatorParameter.NAME_nodeColumnNumber , null, PropertyType.PT_INT, null, null),
		
	   //new PropertyDefault(OperatorParameter.NAME_outputTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null)
		//Sub-flow
		new PropertyDefault(OperatorParameter.NAME_subflowPath, "SubflowPath", PropertyType.PT_CUSTOM_SUBFLOWPATH, null, null),
		new PropertyDefault(OperatorParameter.NAME_tableMapping , "TableMapping", PropertyType.PT_CUSTOM_TABLEMAPING, null, null),
		new PropertyDefault(OperatorParameter.NAME_exitOperator, "ExitOperator", PropertyType.PT_CUSTOM_EXITOPERATOR, null, null),
		new PropertyDefault(OperatorParameter.NAME_subflowVariable, "SubflowVariable",PropertyType.PT_CUSTOM_SUBFLOWVARIABLE, null, null),
		
		new PropertyDefault(OperatorParameter.NAME_outputTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_UmatrixTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_VmatrixTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_singularValueTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_PCAQoutputTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_PCAQvalueOutputTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_PLDADocTopicOutputTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_PLDAModelOutputTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_topicOutTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		new PropertyDefault(OperatorParameter.NAME_docTopicOutTable_StorageParams, null, PropertyType.PT_OUTPUT_CREATION_PARAMETER, null, null),
		
		new PropertyDefault(OperatorParameter. NAME_X_Column, null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault(OperatorParameter.NAME_Y_Column, null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault(OperatorParameter. NAME_C_Column, null, PropertyType.PT_SINGLE_SELECT, null, null),
		
		new PropertyDefault(OperatorParameter. NAME_valueDomain_Column, null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault(OperatorParameter. NAME_typeDomain_Column, null, PropertyType.PT_SINGLE_SELECT, null, null),
		new PropertyDefault(OperatorParameter. NAME_seriesDomain_Column, null, PropertyType.PT_SINGLE_SELECT, null, null),
        new PropertyDefault(OperatorParameter. NAME_useApproximation, null, PropertyType.PT_BOOLEAN, yes_or_no, null),

		new PropertyDefault(OperatorParameter. NAME_UnivariateModel, null, PropertyType.PT_CUSTOM_UNIVARIATE_MODEL, null, null),

        //hadoopFileOperator
        new PropertyDefault(OperatorParameter.NAME_HD_connetionName,null,PropertyType.PT_CUSTOM_NAME_HD_CONNECTIONNAME,null,OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_HD_connetionName)),
        new PropertyDefault(OperatorParameter.NAME_HD_fileName,null,PropertyType.PT_CUSTOM_NAME_HD_FILENAME,null,OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_HD_fileName)),
        new PropertyDefault(OperatorParameter.NAME_HD_format,null,PropertyType.PT_CUSTOM_NAME_HD_FORMAT,null,OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_HD_format)),
        new PropertyDefault(OperatorParameter.NAME_HD_fileStructure,null,PropertyType.PT_CUSTOM_NAME_HD_CSVFILESTRUCTURE,null,OperatorParameterFactory.INSTANCE.getHelperByParamName(OperatorParameter.NAME_HD_fileStructure)),

		//hadoop Row Filter Operator
		new PropertyDefault(OperatorParameter.NAME_HD_Condition,null,PropertyType.PT_CUSTOM_WHERECLAUSE,null,column_helper),
		new PropertyDefault(OperatorParameter.NAME_HD_Override, null, PropertyType.PT_CHOICE, yes_or_no, null),
		new PropertyDefault(OperatorParameter.NAME_HD_StoreResults, null, PropertyType.PT_BOOLEAN, true_or_false, null),
		new PropertyDefault(OperatorParameter.NAME_HD_ResultsName, null, PropertyType.PT_STRING, null, null),
		new PropertyDefault(OperatorParameter.NAME_HD_ResultsLocation, null, PropertyType.PT_HD_FILE_EXPLORER, null, null),
        new PropertyDefault(OperatorParameter.NAME_HD_JOIN_MODEL,null,PropertyType.PT_CUSTOM_NAME_HD_JOIN,null,null),
        new PropertyDefault(OperatorParameter.NAME_HD_Union_Model,null,PropertyType.PT_CUSTOM_HD_TABLESET,null,null),
        new PropertyDefault(OperatorParameter.NAME_selectedFile,null,PropertyType.PT_SINGLE_SELECT,null,null),
        new PropertyDefault(OperatorParameter.NAME_HD_ifFileExists,null,PropertyType.PT_SINGLE_SELECT,null,null),
        new PropertyDefault(OperatorParameter.NAME_HD_ifDataExists,null,PropertyType.PT_SINGLE_SELECT,null,null),
        new PropertyDefault(OperatorParameter.NAME_note,null,PropertyType.PT_CUSTOM_NOTE,null,null),
        new PropertyDefault(OperatorParameter.NAME_HD_PigScript,null,PropertyType.PT_CUSTOM_PIG_EXEC_SCRIPT,null,null),
        new PropertyDefault(OperatorParameter.NAME_HD_PigExecute_fileStructure,null,PropertyType.PT_CUSTOM_PIG_EXEC_FILESTRUCTURE,null,null),

        //em clustering
        new PropertyDefault(OperatorParameter.NAME_clusterNumber,null,PropertyType.PT_INT,null,null),
        new PropertyDefault(OperatorParameter.NAME_initClusterSize,null,PropertyType.PT_INT,null,null),
        new PropertyDefault(OperatorParameter.NAME_maxIterationNumber,null,PropertyType.PT_INT,null,null),
		
		//random forest
        new PropertyDefault(OperatorParameter.NAME_sample_with_replacement,null,PropertyType.PT_SINGLE_SELECT,null,null),

		new PropertyDefault(OperatorParameter.NAME_TimeFormat,null,PropertyType.PT_SINGLE_SELECT,null,OperatorParameterFactory.timeFormatHelper),
		new PropertyDefault(OperatorParameter.NAME_LengthOfWindow,null,PropertyType.PT_INT,null,null),
		
		//hadoop decision tree
		new PropertyDefault(OperatorParameter.NAME_categoryLimit, null, PropertyType.PT_INT, null, null),
		new PropertyDefault(OperatorParameter.NAME_numericalGranularity, null, PropertyType.PT_INT, null, null)

	};

	static {
		for (PropertyDefault def : default_property_array) {
			if (map.get(def.name) != null) {
				throw new RuntimeException("Dup entry: " + def.name);
			}
			map.put(def.name, def);
		}
	}

	/**
	 * @param value2
	 * @return
	 */
	public static String kernel_type_number(String value2) {
		int idx = 1;
		for (String str : kernel_type_list) {
			if (str.equals(value2)) {
				return "" + idx;
			}
			idx ++;
		}
		return null;
	}

	/**
	 * @param value
	 * @return
	 */
	public static String kernel_type_label(String value) {
		int idx = 0;
		try {
			idx = Integer.parseInt(value) - 1;
		}
		catch (Exception e) {
			return value;
		}
		return kernel_type_list[idx];
	}

	/**
	 * @param value2
	 * @return
	 */
	public static String score_type_number(String value2) {
		int idx = 1;
		for (String str : score_type_list) {
			if (str.equals(value2)) {
				return "" + idx;
			}
			idx ++;
		}
		return null;
	}

	/**
	 * @param value
	 * @return
	 */
	public static String score_type_label(String value) {
		int idx = 0;
		try {
			idx = Integer.parseInt(value) - 1;
		}
		catch (Exception e) {
			return value;
		}
		return score_type_list[idx];
	}

	/**
	 * @param name
	 * @return
	 */
	public static String getDisplayName(String name,Locale locale) {
		OperatorParameterHelper helper = OperatorParameterFactory.INSTANCE.getHelperByParamName(name);
		if(helper!=null){
			String label = helper.getParameterLabel(name, locale) ;
			if (label != null ) {
				return label;
			}
		}
	
		return name;
	}
	

	public static Object getValueFromDTO(PropertyDTO p) {
		if(p==null){
			return null;
		}
		PropertyType propertyType = p.getType(); //== PropertyDTO.PropertyType.PT_UNKNOWN
		Object pValue;
		switch(propertyType){
			case  PT_CUSTOM_ADABOOST:
				pValue = p.getAdaboostPersistenceModel().getValue();
				break;
			case PT_CUSTOM_WOE:
				pValue = p.getWoeModel().getWoeTable();
				break;
			case PT_TEXT:
				pValue = p.getValue();
				break;
//			case PT_CUSTOM_COHORTS:
//				pValue = p.getgetColumnBinsModel();
//				break;	 
				
			case PT_HISTOGRAM:
				pValue = p.getColumnBinsModel();
				break;	 
				
			case PT_CUSTOM_REPLACEMENT:
				pValue = p.getNullReplacementModel();
				break;	 
				//legacy code...
//			case PT_CUSTOM_BIN:
//				pValue = p.getColumnBinsModel();
//				break;	 	
			case PT_CUSTOM_INTERACTION_COLUMNS:
				pValue = p.getInterActionModel();
				break;	 	
			case PT_CUSTOM_NEURAL_HIDDEN_LAYERS:
				pValue = p.getHiddenLayersModel();
				break;	 	
			case PT_CUSTOM_VAR_FIELDLIST:
				pValue = p.getDerivedFieldsModel();
				break;	 	
			case PT_CUSTOM_AGG_WINDOW:
				pValue = p.getWindowFieldsModel();
				break;	 	
			case PT_CUSTOM_VAR_QUANTILE:
				pValue = p.getQuantileFieldsModel().getValue();
				break;	 	
			case PT_CUSTOM_TABLEMAPING:
				pValue = p.getSubflowTableMappingModel();
 	            break;
			case PT_CUSTOM_AGG_COLUMN:
				pValue = p.getAggregateFieldsModel();
				break;	
			case PT_CUSTOM_TABLEJOIN:
				pValue = p.getTableJoinModel();
				break;	
			case PT_CUSTOM_WHERECLAUSE:
				pValue = p.getValue();
				break;	
			case PT_CUSTOM_TABLESET:
				pValue = p.getTableSetModel().reverse();
				break;
			case PT_OUTPUT_CREATION_PARAMETER:
				pValue = p.getOutputCreationParamModel().getOriginalModel();
				break;
			case PT_CUSTOM_UNIVARIATE_MODEL:
				pValue = p.getUnivariateModel();
				break;
            case PT_CUSTOM_NAME_HD_CSVFILESTRUCTURE:
                if(p.getCsvFileStructureModel() != null){
                    pValue = p.getCsvFileStructureModel();
                }else if(p.getXmlFileStructureModel()!=null){
                    pValue = p.getXmlFileStructureModel();
                }else if(p.getJsonFileStructureModel()!=null){
                    pValue = p.getJsonFileStructureModel();
                }else if(p.getAlpineLogFileStructureModel() != null){
                    pValue = p.getAlpineLogFileStructureModel();
                }else {
                    pValue = p.getCsvFileStructureModel();
                }
                break;
            case PT_CUSTOM_NAME_HD_JOIN:
                pValue = p.getHadoopJoinModel();
                break;
            case PT_CUSTOM_SAMPLE_SIZE:
                pValue = p.getSampleSizeModelUI().getRealModel();
                break;
            case PT_CUSTOM_HD_TABLESET:
                pValue = p.getHadoopUnionModel();
                break;
            case PT_CUSTOM_PIG_EXEC_SCRIPT:
            	pValue = p.getHadoopPigExecuteScriptModel().revertModel();
            	break;
            case PT_CUSTOM_PIG_EXEC_FILESTRUCTURE:
            	pValue = p.getCsvFileStructureModel();
            	break;
			default:
				pValue=p.getValue();
		} 
		return pValue;
	}
	private static class InputSchemaHelper implements OperatorParameterHelper{
		private OperatorParameterHelper proxy;
		
		public InputSchemaHelper(OperatorParameterHelper proxy){
			this.proxy = proxy;
		}

		public boolean doValidate(OperatorParameter arg0) {
			return proxy.doValidate(arg0);
		}

		public OperatorParameter fromXMLElement(Element arg0) {
			return proxy.fromXMLElement(arg0);
		}

		public List<String> getAvaliableValues(OperatorParameter arg0,
				String arg1, ResourceType arg2, Locale arg3) throws Exception {
			List<String> result = proxy.getAvaliableValues(arg0, arg1, arg2, arg3);
			List<String> resultFilter = new ArrayList<String>(result.size());
			for(String value: result){
				if(VariableModel.DEFAULT_SCHEMA.equals(value)){
					continue;
				}
				resultFilter.add(value);
			}
			return resultFilter;
		}

		public List<String> getAvaliableValues(OperatorParameter arg0,
				String arg1, ResourceType arg2) throws Exception {
			return proxy.getAvaliableValues(arg0, arg1, arg2);
		}

		public String getInputType(String arg0) {
			return proxy.getInputType(arg0);
		}

		public String getParameterDataType(String arg0) {
			return proxy.getParameterDataType(arg0);
		}

		public String getParameterLabel(String arg0, Locale arg1) {
			return proxy.getParameterLabel(arg0, arg1);
		}

		public String getParameterLabel(String arg0) {
			return proxy.getParameterLabel(arg0);
		}

		public Element toXMLElement(OperatorParameter arg0) {
			return proxy.toXMLElement(arg0);
		}
	}
}
