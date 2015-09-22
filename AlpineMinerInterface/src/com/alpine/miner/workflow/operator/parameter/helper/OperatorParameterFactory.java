/**
 * ClassName OperatorParameterFactory.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter.helper;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.alpine.datamining.api.impl.db.attribute.model.customized.CustomizedException;
import com.alpine.miner.workflow.operator.Operator;
import com.alpine.miner.workflow.operator.hadoop.HadoopOperator;
import com.alpine.miner.workflow.operator.parameter.OperatorParameter;
import com.alpine.miner.workflow.operator.parameter.OperatorParameterImpl;
import com.alpine.miner.workflow.operator.parameter.ParameterDataType;

/**
 * @author zhaoyong
 *
 */
public class OperatorParameterFactory {

	
	
	
	public static final String VALUE_YES="Yes" ;
	public static final String VALUE_NO="No" ;
	
	public static final String VALUE_TRUE="true" ;
	public static final String VALUE_FALSE="false" ;
	
	public static final String VALUE_TABLE="TABLE" ;
	public static final String VALUE_VIEW="VIEW" ;
	
	public static final String VALUE_PERCENTAGE = "Percentage";
	public static final String VALUE_ROW = "ROW";
	public static final String VALUE_ALL = "ALL";
	public static final String VALUE_DOT_PRODUCT="dot product";
	public static final String VALUE_POLYNOMINAL="polynomial";
	public static final String VALUE_GAUSSIAN="gaussian";
	
	public static final String VALUE_sum = "sum";
	public static final String VALUE_avg = "avg";
	public static final String VALUE_count = "count";
	public static final String VALUE_max = "max";
	public static final String VALUE_min = "min";
	
	public static final String VALUE_CamberraNumerical="CamberraNumerical";
	public static final String VALUE_CosineSimilarity="CosineSimilarity";
	public static final String VALUE_DiceNumericalSimilarity="DiceNumericalSimilarity" ;
	public static final String VALUE_Euclidean="Euclidean";
	public static final String VALUE_GeneralizedIDivergence="GeneralizedIDivergence";
	
	public static final String VALUE_InnerProductSimilarity ="InnerProductSimilarity";
	public static final String VALUE_JaccardNumericalSimilarity="JaccardNumericalSimilarity";
	public static final String VALUE_KLDivergence="KLDivergence";
	public static final String VALUE_Manhattan="Manhattan";
	
	
	public static final String VARIABLE_SELECTION_SCORETYPE_INFO_GAIN="Info gain";
	public static final String VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO="Info gain ratio";
	public static final String VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN="Transformed info gain";
	
	public static final String STEP_WISE_FORWORD="FORWARD";
	public static final String STEP_WISE_BACKWORD="BACKWARD";
	public static final String STEP_WISE_STEPWISE="STEPWISE";
	
	public static final String CRITERION_TYPE_SBC="SBC";
	public static final String CRITERION_TYPE_AIC="AIC";
	
	private static final OperatorParameterHelper schemaNameHelper = new DBSchemaNameParamterHelper(); 
	private static final OperatorParameterHelper tableNameHelper = new DBTableNameParamterHelper();
	private static List<String> ifFileExistingOptions = Arrays.asList(new String[]{
			 "Drop", "Append", "Skip", "Error"
	}); 
	private static List<String> ifTableExistingOptions = Arrays.asList(new String[]{
			 "Drop", "Append", "Skip", "Error"
	});
	
	public static final OperatorParameterHelper timeFormatHelper = new SingleSelectParameterHelper(Arrays.asList(new String[]{	
			"Integer",
			"yyyy-MM-dd",
			"dd/MM/yyyy hh:mm a",
			"MM/dd/yy hh:mm:ss",
			"dd-MM-yyyy",
			"MM/dd/yy",
			"hh:mm",
			"hh:mm a",
			"hh:mm:ss",		
			}));
	PLDADictTableNameParamterHelper pldaDictTableNameHelper = new PLDADictTableNameParamterHelper();
	public static final OperatorParameterHelper columnNamesParameterHelper= new ColumnNamesParameterHelper();

	public static final OperatorParameterHelper pldaContentColumnParameterHelper= new PLDAContentColumnParameterHelper();
 
	public static final OperatorParameterHelper singleColumnParameterHelper= new SingleColumnNameParamterHelper();
	public static final OperatorParameterHelper singleNoFloatColumnParameterHelper= new SingleColumnNameParamterHelper(OperatorParameter.Column_Type_NoFloat);
	public static final OperatorParameterHelper singleNumericColumnParameterHelper= new SingleColumnNameParamterHelper(OperatorParameter.Column_Type_Numeric);
	private static final OperatorParameterHelper singleCategoryColumnParameterHelper= new SingleColumnNameParamterHelper(OperatorParameter.Column_Type_Category);
	private static final OperatorParameterHelper singleNoNumberColumnParameterHelper= new SingleColumnNameParamterHelper(OperatorParameter.Column_Type_NoNumeric);

	private static final OperatorParameterHelper customizePrameterHelper= new CustomizeParameterHelper();
	
	
	private static final OperatorParameterHelper recColumnHelper_numeric_custom_table
		= new RecommendationColumnParameterHelper(OperatorParameter.Column_Type_Numeric,OperatorParameter.NAME_Customer_Table_Name);
	
	private static final OperatorParameterHelper recColumnHelper_all_custom_table
	= new RecommendationColumnParameterHelper(OperatorParameter.NAME_Customer_Table_Name);
	
	private static final OperatorParameterHelper recColumnHelper_all_selection_table
		= new RecommendationColumnParameterHelper(OperatorParameter.NAME_Selection_Table_Name);
	
	private static final OperatorParameterHelper recColumnHelper_all_recomendation_table
		= new RecommendationColumnParameterHelper(OperatorParameter.NAME_Recommendataion_Table);

	private static final OperatorParameterHelper recColumnHelper_all_pre_rec_table
		= new RecommendationColumnParameterHelper(OperatorParameter.NAME_Pre_Recommendataion_Table);
	
	private static final OperatorParameterHelper recColumnHelper_numeric_pre_rec_table
		= new RecommendationColumnParameterHelper(OperatorParameter.Column_Type_Numeric,OperatorParameter.NAME_Pre_Recommendataion_Table);

	private static final OperatorParameterHelper recColumnHelper_all_post_rec_table
		= new RecommendationColumnParameterHelper(OperatorParameter.NAME_Post_Recommendataion_Table);
	
	private static final OperatorParameterHelper recColumnHelper_numeric_post_rec_table
		= new RecommendationColumnParameterHelper(OperatorParameter.Column_Type_Numeric,OperatorParameter.NAME_Post_Recommendataion_Table);

	private static final OperatorParameterHelper recColumnHelper_svd_uTable
	= new RecommendationColumnParameterHelper(OperatorParameter.NAME_UmatrixFullTable);

	private static final OperatorParameterHelper recColumnHelper_svd_vTable
	= new RecommendationColumnParameterHelper(OperatorParameter.NAME_VmatrixFullTable);
	
	private static final OperatorParameterHelper recColumnHelper_svd_sTable
	= new RecommendationColumnParameterHelper(OperatorParameter.NAME_SmatrixFullTable);
	
	private static final OperatorParameterHelper recomendationTableHelper= new RecommendationTableParameterHelper();
 
	 	 	
	public static final OperatorParameterHelper simpleStringHelper
						= new SimpleInputParameterHelper(ParameterDataType.STRING);
	
	private static final OperatorParameterHelper simpleTextHelper
						= new SimpleTextInputParameterHelper( );
	private static final OperatorParameterHelper ifFileExistsHelper
	= new SingleSelectParameterHelper(ifFileExistingOptions );
	private static final OperatorParameterHelper ifTableExistsHelper
	= new SingleSelectParameterHelper(ifTableExistingOptions );
	
	
	private static final OperatorParameterHelper simpleIntHelper
						= new SimpleInputParameterHelper(ParameterDataType.INT);
	private static final OperatorParameterHelper simplePercentIntHelper
						= new SimpleInputParameterHelper(ParameterDataType.PERCENT);
	
	
	//if is simple boolean , UI may use checkbox
	private static final OperatorParameterHelper simpleBooleanHelper
						= new SimpleInputParameterHelper(ParameterDataType.BOOLEAN);
	
	private static final OperatorParameterHelper simpleDoubleHelper
						= new SimpleInputParameterHelper(ParameterDataType.DOUBLE);
	
	private static final OperatorParameterHelper yesNoHelper
						= new SingleSelectParameterHelper(Arrays.asList(new String[]{VALUE_YES,VALUE_NO}));
	
	public static final OperatorParameterHelper trueFalseHelper
						= new SingleSelectParameterHelper(Arrays.asList(new String[]{VALUE_TRUE,VALUE_FALSE}));
	
	private static final OperatorParameterHelper outPutTypeHelper
						= new SingleSelectParameterHelper(Arrays.asList(new String[]{VALUE_TABLE,VALUE_VIEW}));
	
 	private static final OperatorParameterHelper adjustPerHelper 
 						= new SingleSelectParameterHelper(Arrays.asList(new String[]{VALUE_ALL,VALUE_ROW}));
	
	private static final OperatorParameterHelper sampleSizeTypeHelper
						= new SingleSelectParameterHelper(Arrays.asList(new String[]{VALUE_PERCENTAGE,VALUE_ROW}));
	
	private static final OperatorParameterHelper dependentColumnHelper 
						= new DependentColumnParamterHelper();
	private static final OperatorParameterHelper groupByColumnHelper = new GroupByColumnParamterHelper();
	
	private static final OperatorParameterHelper svdCalKeyColumnHelper
						= new 	SvdCalKeyColumnHelper();
	
 
	 
	private static HashMap<String,OperatorParameterHelper> helperMap=new HashMap <String,OperatorParameterHelper> ();

	public static final OperatorParameterFactory INSTANCE= new OperatorParameterFactory();
	private OperatorParameterHelper expressionHelper = new SingleSelectParameterHelper(
			Arrays.asList(new String[]{"=",">","<",">=","<=","<>"}));
	 
	private OperatorParameterHelper pcaAnalysiTypeHelper = new SingleSelectParameterHelper(
			Arrays.asList(new String[]{"COV-POP","COV-SAM","CORR"}));
	
	OperatorParameterHelper pldaDictIndexColumnHelper = new PLDADictColumnParameterHelper(OperatorParameter.Column_Type_Int);
	OperatorParameterHelper pldaDictContentColumnHelper = new PLDADictColumnParameterHelper();
	
	private OperatorParameterFactory() {
		
		//PLDA
		helperMap.put(OperatorParameter.NAME_contentDocIndexColumn,pldaContentColumnParameterHelper) ;			
		helperMap.put(OperatorParameter.NAME_contentWordColumn ,pldaContentColumnParameterHelper);
		
		//input config
		helperMap.put(OperatorParameter.NAME_dictionarySchema ,schemaNameHelper);
		helperMap.put(OperatorParameter.NAME_dictionaryTable ,pldaDictTableNameHelper);
		 
		helperMap.put(OperatorParameter.NAME_dicIndexColumn ,pldaDictIndexColumnHelper);
		helperMap.put(OperatorParameter.NAME_dicContentColumn ,pldaDictContentColumnHelper);
		
		
		
		//outputconfig
		helperMap.put(OperatorParameter.NAME_PLDAModelOutputSchema ,schemaNameHelper);
		helperMap.put(OperatorParameter.NAME_PLDADropIfExist ,yesNoHelper);
		helperMap.put(OperatorParameter.NAME_PLDAModelOutputTable ,simpleStringHelper);
		
		//parameters
		helperMap.put(OperatorParameter.NAME_Alpha,simpleDoubleHelper);
		helperMap.put(OperatorParameter.NAME_Beta ,simpleDoubleHelper);
		//topic out
		helperMap.put(OperatorParameter.NAME_topicNumber ,simpleIntHelper);
		helperMap.put(OperatorParameter.NAME_topicOutTable ,simpleStringHelper);
		helperMap.put(OperatorParameter.NAME_topicOutSchema ,schemaNameHelper);
		helperMap.put(OperatorParameter.NAME_topicDropIfExist ,yesNoHelper);
		
		helperMap.put(OperatorParameter.NAME_docTopicOutTable,simpleStringHelper);
		helperMap.put(OperatorParameter.NAME_docTopicOutSchema ,schemaNameHelper);
		helperMap.put(OperatorParameter.NAME_docTopicDropIfExist ,yesNoHelper);
		
		//PLDA Predictor

		helperMap.put(OperatorParameter.NAME_PLDADocTopicOutputTable,simpleStringHelper);
		helperMap.put(OperatorParameter.NAME_PLDADocTopicOutputSchema,schemaNameHelper);
		helperMap.put(OperatorParameter.NAME_PLDADocTopicDropIfExist ,yesNoHelper);
		helperMap.put(OperatorParameter.NAME_IterationNumber ,simpleIntHelper);

		
		
		

		helperMap.put(OperatorParameter.NAME_dBConnectionName, new DBConnectionParamterHelper());		
 
		helperMap.put(OperatorParameter.NAME_schemaName, schemaNameHelper);
		helperMap.put(OperatorParameter.NAME_tableName,  tableNameHelper);
		
		helperMap.put(OperatorParameter.NAME_columnNames, columnNamesParameterHelper);
		
		helperMap.put( OperatorParameter.NAME_dropIfExist,yesNoHelper);
		helperMap.put( OperatorParameter.NAME_outputTable,simpleTextHelper);
		helperMap.put( OperatorParameter.NAME_outputTable_StorageParams,customizePrameterHelper);
		
		
		helperMap.put( OperatorParameter.NAME_subflowVariable,customizePrameterHelper);
		helperMap.put( OperatorParameter.NAME_tableMapping,customizePrameterHelper);
		
		
		helperMap.put( OperatorParameter.NAME_outputSchema,schemaNameHelper  ) ;
		helperMap.put( OperatorParameter.NAME_outputType,outPutTypeHelper);
		
		helperMap.put( OperatorParameter.NAME_dependentColumn,dependentColumnHelper );
		helperMap.put( OperatorParameter.NAME_groupByTrainerColumn,groupByColumnHelper );
		
		
		helperMap.put( OperatorParameter.NAME_useModel,simpleStringHelper);
		helperMap.put( OperatorParameter.NAME_columnValue,simpleStringHelper);
		helperMap.put( OperatorParameter.NAME_goodValue,simpleStringHelper);
		helperMap.put( OperatorParameter.NAME_forceRetrain,yesNoHelper) ;
		
		//Tableset
		helperMap.put( OperatorParameter.NAME_tableSetConfig,customizePrameterHelper);
		
		//IV
		helperMap.put( OperatorParameter.NAME_good,simpleStringHelper);
		//aggregate
		helperMap.put( OperatorParameter.NAME_aggregateFieldList,customizePrameterHelper);
		helperMap.put( OperatorParameter.NAME_windowFieldList ,customizePrameterHelper);
		//row filter
			helperMap.put( OperatorParameter.NAME_whereClause,simpleTextHelper);
		//varaiable
		helperMap.put( OperatorParameter.NAME_fieldList,customizePrameterHelper);
		helperMap.put( OperatorParameter.NAME_quantileFieldList,customizePrameterHelper);
		
		//sampling
		helperMap.put( OperatorParameter.NAME_samplingColumn,singleColumnParameterHelper);
		
		helperMap.put( OperatorParameter.NAME_sampleCount,simpleIntHelper);
		
		helperMap.put( OperatorParameter.NAME_sampleSizeType,sampleSizeTypeHelper );
		helperMap.put( OperatorParameter.NAME_sampleSize,customizePrameterHelper);
		helperMap.put( OperatorParameter.NAME_randomSeed,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_consistent ,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_replacement ,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_disjoint ,simpleBooleanHelper);
		
		
		helperMap.put( OperatorParameter.NAME_replacement_config ,columnNamesParameterHelper);
	
		helperMap.put( OperatorParameter.NAME_keyColumnList,columnNamesParameterHelper );
//		helperMap.put( OperatorParameter.NAME_outputTable ,simpleStringHelper) ;
		//sample Selector
		OperatorParameterHelper selectedTableHelper= new SelectedTableParamterHelper();
		helperMap.put( OperatorParameter.NAME_selectedTable,selectedTableHelper);
		OperatorParameterHelper selectedFileHelper= new SelectedFileParameterHelper();
		helperMap.put( OperatorParameter.NAME_selectedFile,selectedFileHelper);
		//adaboost
		helperMap.put( OperatorParameter.NAME_adaboostUIModel,customizePrameterHelper);
		
		//lr
		helperMap.put( OperatorParameter.NAME_max_generations,simpleIntHelper) ;
		helperMap.put( OperatorParameter.NAME_epislon,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_Interaction_Columns,columnNamesParameterHelper);
		helperMap.put( OperatorParameter.NAME_isStepWise,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_stepWiseType,
				new SingleSelectParameterHelper(Arrays.asList(new String[]{STEP_WISE_FORWORD,STEP_WISE_BACKWORD,STEP_WISE_STEPWISE})));
		helperMap.put( OperatorParameter.NAME_criterionType,
				new SingleSelectParameterHelper(Arrays.asList(new String[]{CRITERION_TYPE_SBC,CRITERION_TYPE_AIC,""})));
		helperMap.put( OperatorParameter.NAME_checkValue,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_AddResidualPlot,trueFalseHelper);
		//carttree
		helperMap.put( OperatorParameter.NAME_maximal_depth,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_confidence,simpleDoubleHelper);
		
		
		helperMap.put( OperatorParameter.NAME_number_of_prepruning_alternatives,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_minimal_size_for_split,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_no_pruning ,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_no_pre_pruning ,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_size_threshold_load_data,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_minimal_leaf_size ,simpleIntHelper);
		
		//DT
		helperMap.put( OperatorParameter.NAME_minimal_gain ,simpleDoubleHelper);
		
		//nb
		helperMap.put( OperatorParameter.NAME_isCalculateDeviance,simpleBooleanHelper);
		
		//nn
		helperMap.put( OperatorParameter.NAME_hidden_layers ,customizePrameterHelper);
		helperMap.put( OperatorParameter.NAME_training_cycles ,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_learning_rate,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_momentum ,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_decay,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_fetchsize ,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_normalize,simpleBooleanHelper);
		helperMap.put( OperatorParameter.NAME_error_epsilon,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_local_random_seed,simpleDoubleHelper);
		
		helperMap.put( OperatorParameter.NAME_adjust_per,adjustPerHelper );
		//columns bins 
		helperMap.put( OperatorParameter.NAME_Columns_Bins,columnNamesParameterHelper);
		//barchart
		helperMap.put( OperatorParameter.NAME_valueDomain,singleNumericColumnParameterHelper);
		helperMap.put( OperatorParameter.NAME_scopeDomain,singleColumnParameterHelper );
		helperMap.put( OperatorParameter.NAME_categoryType,singleColumnParameterHelper );
		
		 
	
		//Normalization
 		helperMap.put( OperatorParameter.NAME_method,new SingleSelectParameterHelper(
				Arrays.asList(new String[]{"Proportion-Transformation","Range-Transformation","Z-Transformation","DivideByAverage-Transformation"})) ); 
		helperMap.put( OperatorParameter.NAME_rangeMin,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_rangeMax,simpleDoubleHelper);
		
		
		
		//table join
		
		helperMap.put( OperatorParameter.NAME_createSequenceID,yesNoHelper);
		helperMap.put( OperatorParameter.NAME_Set_Table_Join_Parameters,customizePrameterHelper);
		 
		//svm classfication
		helperMap.put( OperatorParameter.NAME_kernel_type ,new SingleSelectParameterHelper(Arrays.asList(
				new String[]{VALUE_DOT_PRODUCT, VALUE_POLYNOMINAL,VALUE_GAUSSIAN})) ); 
		helperMap.put( OperatorParameter.NAME_degree,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_gamma,simpleDoubleHelper);
		
		    
		helperMap.put( OperatorParameter.NAME_eta,simpleDoubleHelper);
		helperMap.put( OperatorParameter.NAME_nu,simpleDoubleHelper);
		   //svm novelty detection --no 
		
		//svm regression
		helperMap.put( OperatorParameter.NAME_lambda,simpleDoubleHelper);		  
   
		//Timeseries 
		helperMap.put( OperatorParameter.NAME_groupColumn,singleNoFloatColumnParameterHelper );
		helperMap.put( OperatorParameter.NAME_IDColumn,singleColumnParameterHelper );
		helperMap.put( OperatorParameter.NAME_ValueColumn,singleNumericColumnParameterHelper);
		helperMap.put( OperatorParameter.NAME_groupByColumn,singleColumnParameterHelper);   	   
		helperMap.put( OperatorParameter.NAME_AR_Order,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_MA_Order,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_Degree_of_differencing,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_Load_Data_Threshhold,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_LengthOfWindow,simpleTextHelper);	
		helperMap.put( OperatorParameter.NAME_TimeFormat,timeFormatHelper);

		//kmeans
		helperMap.put( OperatorParameter.NAME_IDColumn_lower,singleColumnParameterHelper );
		

		
		
		//association
		
		helperMap.put( OperatorParameter.NAME_expression,expressionHelper);	
		helperMap.put( OperatorParameter.NAME_minSupport,simpleDoubleHelper);		
		helperMap.put( OperatorParameter.NAME_tableSizeThreshold,simpleIntHelper);		
		helperMap.put( OperatorParameter.NAME_minConfidence,simpleDoubleHelper);		   
		helperMap.put( OperatorParameter.NAME_positiveValue,simpleStringHelper);		
		helperMap.put( OperatorParameter.NAME_Use_Array,simpleStringHelper);
		
		//kmeans
		helperMap.put( OperatorParameter.NAME_k,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_split_Number,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_max_runs,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_max_optimization_steps,simpleIntHelper);
		helperMap.put( OperatorParameter.NAME_distanse ,new SingleSelectParameterHelper(
				Arrays.asList(new String[]{	
					VALUE_Euclidean,
					VALUE_GeneralizedIDivergence,
					VALUE_KLDivergence,
					VALUE_CamberraNumerical,
					VALUE_Manhattan,
					VALUE_CosineSimilarity,
					VALUE_DiceNumericalSimilarity,		
					VALUE_InnerProductSimilarity,
					VALUE_JaccardNumericalSimilarity,
					})));

		//numeric to text
		helperMap.put( OperatorParameter.NAME_modifyOriginTable,simpleStringHelper);
		//pvoit
		helperMap.put( OperatorParameter.NAME_pivotColumn,singleNoNumberColumnParameterHelper);
		helperMap.put( OperatorParameter.NAME_groupByColumn,singleColumnParameterHelper);
		helperMap.put( OperatorParameter.NAME_aggregateColumn,singleNumericColumnParameterHelper);
 
		
		helperMap.put( OperatorParameter.NAME_analysisType ,new SingleSelectParameterHelper(
				Arrays.asList(new String[]{		
						"COV-POP","COV-SAM","CORR"})));
		
		//pca
		 helperMap.put(OperatorParameter.NAME_aggregateType ,new SingleSelectParameterHelper(
					Arrays.asList(new String[]{		
							VALUE_sum,
							VALUE_avg,
							VALUE_count,
							VALUE_max,
							VALUE_min		
							})));
		//svd
		 helperMap.put(OperatorParameter.NAME_dependentColumn ,dependentColumnHelper);
		 helperMap.put(OperatorParameter.NAME_forceRetrain ,yesNoHelper);
		 helperMap.put(OperatorParameter.NAME_ColName,singleNoFloatColumnParameterHelper); 
		 helperMap.put(OperatorParameter.NAME_RowName,singleNoFloatColumnParameterHelper);
		 helperMap.put(OperatorParameter.NAME_NumFeatures,simpleIntHelper);
		 helperMap.put(OperatorParameter.NAME_UmatrixTable,simpleTextHelper);
		 helperMap.put(OperatorParameter.NAME_UmatrixSchema,schemaNameHelper);
		 helperMap.put(OperatorParameter.NAME_UmatrixDropIfExist,yesNoHelper);
		 helperMap.put(OperatorParameter.NAME_VmatrixTable,simpleTextHelper);
		 helperMap.put(OperatorParameter.NAME_VmatrixSchema,schemaNameHelper);
		 helperMap.put(OperatorParameter.NAME_VmatrixDropIfExist,yesNoHelper);
		 helperMap.put(OperatorParameter.NAME_singularValueTable,simpleTextHelper);
		 helperMap.put(OperatorParameter.NAME_singularValueSchema,schemaNameHelper);
		 helperMap.put(OperatorParameter.NAME_singularValueDropIfExist,yesNoHelper);
		 
		 //svd Calculator
		 //this is not editable in the calculator 
		 //need add special Helper for following 3 parameters
		 helperMap.put(OperatorParameter.NAME_UmatrixFullTable,recomendationTableHelper);		 		
		 helperMap.put(OperatorParameter.NAME_VmatrixFullTable,recomendationTableHelper);
		 helperMap.put(OperatorParameter.NAME_SmatrixFullTable,recomendationTableHelper);
		 
		 helperMap.put(OperatorParameter.NAME_ColNameF,recColumnHelper_svd_vTable);
		 helperMap.put(OperatorParameter.NAME_RowNameF,recColumnHelper_svd_uTable);
		 helperMap.put(OperatorParameter.NAME_VfeatureColumn,recColumnHelper_svd_vTable);
		 helperMap.put(OperatorParameter.NAME_VdependentColumn,recColumnHelper_svd_vTable);
		 helperMap.put(OperatorParameter.NAME_UfeatureColumn,recColumnHelper_svd_uTable);
		 helperMap.put(OperatorParameter.NAME_UdependentColumn,recColumnHelper_svd_uTable);
		 helperMap.put(OperatorParameter.NAME_SfeatureColumn,recColumnHelper_svd_sTable);
		 helperMap.put(OperatorParameter.NAME_SdependentColumn,recColumnHelper_svd_sTable);
		 //these 8 are got from parent (svd),after table seleted.
		 
		 //----------------------none editable end-------------
		 helperMap.put(OperatorParameter.NAME_CrossProduct,simpleBooleanHelper);
		 helperMap.put(OperatorParameter.NAME_KeyColumn,svdCalKeyColumnHelper);
		 helperMap.put(OperatorParameter.NAME_KeyValue,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_outputSchema,schemaNameHelper);
//		 helperMap.put(OperatorParameter.NAME_outputTable,tableNameHelper);
		 helperMap.put(OperatorParameter.NAME_dropIfExist,yesNoHelper);
		 
		//product recommendation
		 helperMap.put(OperatorParameter.NAME_Recommendataion_Table ,recomendationTableHelper);
		 helperMap.put(OperatorParameter.NAME_Recommendataion_ID_Column ,recColumnHelper_all_recomendation_table);
		 helperMap.put(OperatorParameter.NAME_Recommendataion_Product_Column ,recColumnHelper_all_recomendation_table);
		 helperMap.put(OperatorParameter.NAME_Pre_Recommendataion_Table ,recomendationTableHelper);
		 helperMap.put(OperatorParameter.NAME_Pre_Recommendataion_ID_Column ,recColumnHelper_all_pre_rec_table);
		 helperMap.put(OperatorParameter.NAME_Pre_Recommendataion_Value_Column,recColumnHelper_numeric_pre_rec_table);
		 helperMap.put(OperatorParameter.NAME_Post_Recommendataion_Table,recomendationTableHelper);
		 helperMap.put(OperatorParameter.NAME_Post_Recommendataion_ID_Column,recColumnHelper_all_post_rec_table);
		 helperMap.put(OperatorParameter.NAME_Post_Recommendataion_Product_Column,recColumnHelper_all_post_rec_table);
		 helperMap.put(OperatorParameter.NAME_Post_Recommendataion_Value_Column ,recColumnHelper_numeric_post_rec_table);
		 helperMap.put(OperatorParameter.NAME_Customer_Table_Name ,recomendationTableHelper);
		 helperMap.put(OperatorParameter.NAME_Customer_ID_Column ,recColumnHelper_all_custom_table);
		 helperMap.put(OperatorParameter.NAME_Customer_Value_Column,recColumnHelper_numeric_custom_table);
		 helperMap.put(OperatorParameter.NAME_Customer_Product_Column ,recColumnHelper_all_custom_table);
		 helperMap.put(OperatorParameter.NAME_Customer_Product_Count_Column ,recColumnHelper_numeric_custom_table);
		 helperMap.put(OperatorParameter.NAME_Selection_Table_Name ,recomendationTableHelper);
		 helperMap.put(OperatorParameter.NAME_Selection_ID_Column ,recColumnHelper_all_selection_table);
		 helperMap.put(OperatorParameter.NAME_SimThreshold ,simpleDoubleHelper);
		 helperMap.put(OperatorParameter.NAME_Max_Record ,simpleIntHelper);
		 helperMap.put(OperatorParameter.NAME_Min_Product_Count ,simpleIntHelper);
		 helperMap.put(OperatorParameter.NAME_Score_Threshold,simpleDoubleHelper);
		 helperMap.put(OperatorParameter.NAME_Cohorts ,customizePrameterHelper);
		 helperMap.put(OperatorParameter.NAME_Above_Cohort ,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_Below_Cohort ,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_TargetCohorts ,simpleStringHelper);

		 helperMap.put(OperatorParameter.NAME_SQL_Execute_Text ,simpleTextHelper);
		 helperMap.put(OperatorParameter.NAME_note ,simpleTextHelper);
		 
		 //variableSelection
		 helperMap.put(OperatorParameter.NAME_scoreType ,new SingleSelectParameterHelper(
					Arrays.asList(new String[]{		
							VARIABLE_SELECTION_SCORETYPE_INFO_GAIN,
							VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO,
							VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN})));
		 
		 
		  
		 helperMap.put(OperatorParameter.NAME_Ahead_Number ,simpleIntHelper);
		 
		 //for PCA:
		 helperMap.put(OperatorParameter.NAME_analysisType ,pcaAnalysiTypeHelper);
		 helperMap.put(OperatorParameter.NAME_percent ,simpleDoubleHelper);
		 helperMap.put(OperatorParameter.NAME_PCAQoutputSchema ,schemaNameHelper);
		 helperMap.put(OperatorParameter.NAME_PCAQoutputTable ,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_PCAQDropIfExist ,yesNoHelper);
		 
		 helperMap.put(OperatorParameter.NAME_PCAQvalueOutputSchema ,schemaNameHelper);
		 helperMap.put(OperatorParameter.NAME_PCAQvalueOutputTable ,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_PCAQvalueDropIfExist ,yesNoHelper);
		 
		 helperMap.put(OperatorParameter.NAME_remainColumns ,columnNamesParameterHelper);
		 helperMap.put(OperatorParameter.NAME_IncludeSourceColumn ,columnNamesParameterHelper);
		 
		 //for adaboost
		 //depenedent column, column names and foreRetrain are already defined
		 //and parameter setting is an object, so need not helper
		 //for WOE
		 helperMap.put(OperatorParameter.NAME_WOEGROUP,customizePrameterHelper);
		 
		 helperMap.put(OperatorParameter.NAME_UnivariateModel,customizePrameterHelper);
		 helperMap.put(OperatorParameter.NAME_Y_Column,singleNumericColumnParameterHelper);
		 helperMap.put(OperatorParameter.NAME_X_Column,singleNumericColumnParameterHelper);
		 helperMap.put(OperatorParameter.NAME_C_Column,singleNoNumberColumnParameterHelper);
		 helperMap.put(OperatorParameter.NAME_valueDomain_Column,singleNumericColumnParameterHelper);
		 helperMap.put(OperatorParameter.NAME_typeDomain_Column,singleColumnParameterHelper);
		 helperMap.put(OperatorParameter.NAME_seriesDomain_Column,singleColumnParameterHelper);
         helperMap.put(OperatorParameter.NAME_useApproximation,simpleBooleanHelper);

        //Hadoop
		 helperMap.put(OperatorParameter.NAME_HD_productColumn,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_idColumn,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_timeColumn,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_productCountPerUserThreshold,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_cooccurenceThreshold,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_split,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_interval,simpleStringHelper);
		 helperMap.put(OperatorParameter.NAME_HD_ouputfile,simpleStringHelper);	 
		 
		 helperMap.put(OperatorParameter.NAME_HD_fileName,simpleTextHelper);
		  
		 helperMap.put(OperatorParameter.NAME_HD_ifFileExists,ifFileExistsHelper);
		 helperMap.put(OperatorParameter.NAME_HD_copyToFileName,simpleTextHelper);
		 helperMap.put(OperatorParameter.NAME_HD_ifDataExists,ifTableExistsHelper);
		 helperMap.put(OperatorParameter.NAME_HD_copyToTableName,simpleTextHelper);
		 
		 helperMap.put(OperatorParameter.NAME_HD_connetionName,new HadoopConnetionParameterhelper());	 
		 helperMap.put(OperatorParameter.NAME_HD_format,new SingleSelectParameterHelper(HadoopOperator.File_Formats)); 
		 helperMap.put(OperatorParameter.NAME_HD_fileStructure,customizePrameterHelper); 
		 
		 helperMap.put(OperatorParameter.NAME_HD_Condition,simpleTextHelper);
		 helperMap.put(OperatorParameter.NAME_HD_StoreResults,simpleBooleanHelper); 
		 helperMap.put(OperatorParameter.NAME_HD_ResultsLocation,simpleTextHelper); 
		 helperMap.put(OperatorParameter.NAME_Model_File_Path,simpleTextHelper); 
		 helperMap.put(OperatorParameter.NAME_HD_ResultsName,simpleTextHelper); 
		 helperMap.put(OperatorParameter.NAME_HD_Override,yesNoHelper); 
		 helperMap.put(OperatorParameter.NAME_HD_JOIN_MODEL,customizePrameterHelper); 
		 
		 helperMap.put(OperatorParameter.NAME_HD_PigScript,customizePrameterHelper);
		 helperMap.put(OperatorParameter.NAME_HD_PigExecute_fileStructure,customizePrameterHelper);
		 
		helperMap.put( OperatorParameter.NAME_sample_with_replacement,trueFalseHelper);

		 
		 //random forest
		 
		//carttree
			helperMap.put( OperatorParameter.NAME_forestSize,simpleIntHelper);

			helperMap.put( OperatorParameter.NAME_nodeColumnNumber,simpleIntHelper);

		 
	}

	public static OperatorParameter newParameter(Operator operator,String parameterName){
		return new OperatorParameterImpl(operator,parameterName);
	}
	
	//this is for the customized parameters
	public static OperatorParameter newParameter(Operator operator,String parameterName,ParameterDataType dataType){
		return new OperatorParameterImpl(operator,parameterName,dataType);
	}
	
	public   OperatorParameterHelper getHelper(OperatorParameter parameter){
		return getHelperByParamName(parameter.getName());
	}

	public   OperatorParameterHelper getHelperByParamName(
			String parameterName) {
		 
		if(helperMap.keySet().contains(parameterName)){
			return helperMap.get(parameterName);
		}else{ //that we don't know is customized
			return CustomizeParameterHelper.INSTANCE;
		}
	}
	//this is for udf
	public void registryHelper(String paramterName,OperatorParameterHelper helper) throws Exception{
		if(helperMap.containsKey(paramterName)){
			throw new CustomizedException("parameter name already existed:"+paramterName,CustomizedException.PARA_ALREADY_EXISTS);
		}
		helperMap.put(paramterName, helper) ;
	}
	
	public void unRegistryHelper(String paramterName) {
		if(helperMap.containsKey(paramterName)){
			helperMap.remove(paramterName) ;
		}
		
	}
	

}
