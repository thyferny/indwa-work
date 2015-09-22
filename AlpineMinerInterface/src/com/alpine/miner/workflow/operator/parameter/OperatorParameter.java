/**
 * ClassName OperatorParameter.java
 *
 * Version information:3.00
 *
 * Date:Aug 10, 2011
 *
 * COPYRIGHT (C) 2011 Alpine Solution. All rights Reserved
 */
package com.alpine.miner.workflow.operator.parameter;

import java.util.Locale;

import com.alpine.datamining.api.impl.algoconf.PLDAConfig;
import com.alpine.datamining.api.impl.algoconf.PLDAPredictConfig;
import com.alpine.datamining.api.impl.algoconf.hadoop.HadoopFileSelectorConfig;
import com.alpine.miner.workflow.operator.Operator;

/**
 * @author zhaoyong
 *
 */
public interface OperatorParameter {

	public abstract void setDataType(ParameterDataType type);

	public abstract ParameterDataType getDataType();

	public abstract void setValue(Object value);
		//mostly it is a string ,object must implement the parameterobject interface later... 
	public abstract Object getValue();

	public abstract void setName(String name);

	public abstract String getName();

	public abstract void setOperator(Operator operator);
	public abstract Operator getOperator();
	
	public abstract String getParameterLabel(Operator operator);
 
	public String getParameterLabel(Locale locale,Operator operator);  

	//PLDA
	
	//contentSchema ,contentTable is from input link 
	public static final String NAME_contentDocIndexColumn =PLDAConfig.ConstContentDocIndexColumn;
	public static final String NAME_contentWordColumn =PLDAConfig.ConstContentWordColumn;
	
	//input config
	public static final String NAME_dictionarySchema =PLDAConfig.ConstDictionarySchema;
	public static final String NAME_dictionaryTable =PLDAConfig.ConstDictionaryTable;
	public static final String NAME_dicIndexColumn =PLDAConfig.ConstDicIndexColumn;
	public static final String NAME_dicContentColumn =PLDAConfig.ConstDicContentColumn;
	
	
	
	//outputconfig
	public static final String NAME_PLDAModelOutputSchema =PLDAConfig.ConstPLDAModelOutputSchema;
	public static final String NAME_PLDADropIfExist =PLDAConfig.ConstPLDADropIfExist;
	public static final String NAME_PLDAModelOutputTable =PLDAConfig.ConstPLDAModelOutputTable;
	
	//parameters
	public static final String NAME_Alpha =PLDAConfig.ConstAlpha;
	public static final String NAME_Beta =PLDAConfig.ConstBeta;
	public static final String NAME_IterationNumber =PLDAConfig.ConstIterationNumber;
	//topic out
	public static final String NAME_topicNumber =PLDAConfig.ConstTopicNumber;
	public static final String NAME_topicOutTable =PLDAConfig.ConstTopicOutTable;
	public static final String NAME_topicOutSchema =PLDAConfig.ConstTopicOutSchema;
	public static final String NAME_topicDropIfExist =PLDAConfig.ConstTopicDropIfExist;
	
	public static final String NAME_docTopicOutTable =PLDAConfig.ConstDocTopicOutTable;
	public static final String NAME_docTopicOutSchema =PLDAConfig.ConstDocTopicOutSchema;
	public static final String NAME_docTopicDropIfExist =PLDAConfig.ConstDocTopicDropIfExist;
	
	//PLDA Predictor

	public static final String NAME_PLDADocTopicOutputTable = PLDAPredictConfig.ConstPLDADocTopicOutputTable;
	public static final String NAME_PLDADocTopicOutputSchema= PLDAPredictConfig.ConstPLDADocTopicOutputSchema;
	public static final String NAME_PLDADocTopicDropIfExist= PLDAPredictConfig.ConstPLDADocTopicDropIfExist;

	//dbtable...
	public static final String NAME_dBConnectionName="dbConnectionName";
	public static final String NAME_schemaName="schemaName";
	public static final String NAME_tableName="tableName";
	public static final String NAME_System = "system";
	public static final String NAME_URL = "url";
	public static final String NAME_UserName = "userName";
	public static final String NAME_Password = "password";
	public static final String NAME_tableType= "tableType";
	
	//common...
	public static final String NAME_columnNames="columnNames";
	public static final String NAME_dropIfExist="dropIfExist";
	
	public static final String NAME_outputTable="outputTable" ; 

	
	
	
	
	
	
	
	
	
	public static final String NAME_outputSchema="outputSchema" ;
	public static final String NAME_outputType="outputType";
	public static final String NAME_dependentColumn="dependentColumn";
	public static final String NAME_useModel="useModel";
	public static final String NAME_columnValue="columnValue";
	public static final String NAME_goodValue="goodValue" ;
	public static final String NAME_forceRetrain="forceRetrain" ;
	
	//aggregate
	public static final String NAME_aggregateFieldList="aggregateFieldList";
	public static final String NAME_windowFieldList ="windowFieldList" ;
	//row filter
	public static final String NAME_whereClause="whereClause";
	//varaiable
	public static final String NAME_fieldList="fieldList";
	public static final String NAME_quantileFieldList="quantileFieldList" ;
	
	//informationValue
	public static final String NAME_good="good" ;
	//
	
	public static final String NAME_sampleCount="sampleCount";
	public static final String NAME_sampleSizeType="sampleSizeType" ;
	public static final String NAME_sampleSize ="sampleSize" ;
	public static final String NAME_randomSeed ="randomSeed";
	public static final String NAME_consistent = "consistent" ;
	public static final String NAME_replacement ="replacement" ;
	public static final String NAME_disjoint ="disjoint" ;
	public static final String NAME_keyColumnList="keyColumnList";
	public static final String NAME_samplingColumn = "samplingColumn";

	//sample Selector
	public static final String NAME_selectedTable="selectedTable";
	public static final String NAME_selectedFile="selectedFile";
	//null value replacement
	public static final String NAME_replacement_config = "replacementNull";
	
	//lr
	public static final String NAME_max_generations="max_generations" ;
	public static final String NAME_epislon="epsilon";
	public final static String ConstEpsilon_LR = "epsilon";
	public final static String NAME_isStepWise = "isStepWise";
	public final static String NAME_stepWiseType = "stepWiseType";
	public final static String NAME_criterionType = "criterionType";
	public final static String NAME_checkValue = "checkValue";
	//this is saved as "InteractionModel"
	//<InterActionModel>
	//<InterActionItem firstColumn="residual sugar" id="0" interactionType="*" secondColumn="sulphates"/>
	//<InterActionItem firstColumn="residual sugar" id="1" interactionType="*" secondColumn="pH"/>
	//</InterActionModel>
	public static final String NAME_Interaction_Columns="interActionModel" ;
	
	//carttree
	public static final String NAME_maximal_depth="maximal_depth" ;
	public static final String NAME_confidence="confidence" ;
	public static final String NAME_number_of_prepruning_alternatives="number_of_prepruning_alternatives";
	public static final String NAME_minimal_size_for_split="minimal_size_for_split" ;
	public static final String NAME_no_pruning ="no_pruning" ;
	public static final String NAME_no_pre_pruning ="no_pre_pruning" ;
	public static final String NAME_size_threshold_load_data ="size_threshold_load_data" ; 
	public static final String NAME_minimal_leaf_size ="minimal_leaf_size" ;
	
	//decision tree
	public static final String NAME_minimal_gain ="minimal_gain" ;
	public static final String NAME_categoryLimit ="categoryLimit" ;
	public static final String NAME_numericalGranularity ="numericalGranularity" ;
	
	
	
	
	//nb
	public static final String NAME_isCalculateDeviance="calculateDeviance" ;
	
	//nn
	public static final String NAME_hidden_layers ="hidden_layers" ;
	public static final String NAME_training_cycles ="training_cycles" ;
	public static final String NAME_learning_rate="learning_rate" ;
	public static final String NAME_momentum ="momentum";
	public static final String NAME_decay="decay";
	public static final String NAME_fetchsize="fetchSize" ;
	public static final String NAME_normalize ="normalize" ;
	public static final String NAME_error_epsilon="error_epsilon";
	public static final String NAME_local_random_seed ="local_random_seed";
	public static final String NAME_adjust_per="adjust_per";
	//histogram--please be careful ,now we are not using the model
	public static final String NAME_Columns_Bins = "columns_bins";
 
 	 
	public static final String NAME_valueDomain = "valueDomain"; 
	public static final String NAME_scopeDomain = "scopeDomain";
	//special for barchart
	public static final String NAME_categoryType = "categoryType";
	public static final String NAME_categoryDomain = "categoryDomain";
	public static final String NAME_method = "method";
	public static final String NAME_rangeMin = "rangeMin";
	public static final String NAME_rangeMax = "rangeMax";
	public static final String NAME_createSequenceID = "createSequenceID";
	//this is not use for save, because we save a model ofr them
	public static final String NAME_Set_Table_Join_Parameters = "set_table_join_parameters";
	public static final String NAME_kernel_type = "kernelType";
	public static final String NAME_degree = "degree";
	public static final String NAME_gamma = "gamma";
	public static final String NAME_eta = "eta";
	public static final String NAME_nu = "nu";
	public static final String NAME_lambda = "slambda";
	//TODO:this is speical will refine the flow file later
	//seems only for svm and time ser
	public static final String NAME_IDColumn = "IDColumn";
	public static final String NAME_IDColumn_lower = "idColumn";
	
	public static final String NAME_ValueColumn = "valueColumn";
	public static final String NAME_AR_Order ="p";// "AR Order";
	public static final String NAME_MA_Order ="q";// "MA Order";
	public static final String NAME_Degree_of_differencing = "d";//"Degree of differencing";
	public static final String NAME_Load_Data_Threshhold = "threshold";//"Load Data Threshhold"; 
	public static final String NAME_LengthOfWindow = "lengthOfWindow";//For hadoop
	public static final String NAME_TimeFormat = "timeFormat";//For hadoop
		
	//association
	public static final String NAME_minSupport = "minSupport";
	public static final String NAME_expression = "expression";
	public static final String NAME_tableSizeThreshold = "tableSizeThreshold";
	public static final String NAME_minConfidence = "minConfidence";
	public static final String NAME_positiveValue = "positiveValue";
	public static final String NAME_Use_Array = "useArray";
	//kmeans
	public static final String NAME_k = "k";
	public static final String NAME_distanse = "distance";
	public static final String NAME_clusterColumnName="clusterColumnName";
	
	public static final String NAME_split_Number = "split_Number";
	public static final String NAME_max_runs = "max_runs";
	public static final String NAME_max_optimization_steps = "max_optimization_steps";
	public static final String NAME_modifyOriginTable = "modifyOriginTable";
	//pivot
	public static final String NAME_pivotColumn = "pivotColumn";
	public static final String NAME_groupByColumn = "groupByColumn";
	public static final String NAME_aggregateColumn = "aggregateColumn";
	public static final String NAME_aggregateType = "aggregateType";

	
	//product recommendation
	public static final String NAME_Recommendataion_Table = "recommendationTable";//"Recommendataion Table";
	public static final String NAME_Recommendataion_ID_Column ="recommendationIdColumn";// "Recommendataion ID Column";
	public static final String NAME_Recommendataion_Product_Column = "recommendationProductColumn";
	public static final String NAME_Pre_Recommendataion_Table = "preTable";//"Pre_Recommendataion Table";
	public static final String NAME_Pre_Recommendataion_ID_Column = "preIdColumn";
	public static final String NAME_Pre_Recommendataion_Value_Column ="preValueColumn";// "Pre Recommendataion Product Column";
	
	public static final String NAME_Post_Recommendataion_Table = "postTable";
	public static final String NAME_Post_Recommendataion_ID_Column = "postIdColumn";
	public static final String NAME_Post_Recommendataion_Product_Column = "postProductColumn";
	public static final String NAME_Post_Recommendataion_Value_Column = "postValueColumn";
	
	public static final String NAME_Customer_Table_Name = "customerTable";//"Customer Table Name";
	public static final String NAME_Customer_ID_Column = "customerIDColumn";//"Customer ID Column";
	public static final String NAME_Customer_Value_Column = "customerValueColumn";
	public static final String NAME_Customer_Product_Column = "customerProductColumn";
	public static final String NAME_Customer_Product_Count_Column = "customerProductCountColumn";
	public static final String NAME_Selection_Table_Name = "selectionTable";
	public static final String NAME_Selection_ID_Column = "selectionIDColumn";
	public static final String NAME_SimThreshold = "simThreshold";
	public static final String NAME_Max_Record = "maxRecords";
	public static final String NAME_Min_Product_Count = "minProductCount";
	public static final String NAME_Score_Threshold = "scoreThreshold";
	public static final String NAME_Cohorts = "cohorts";
	public static final String NAME_TargetCohorts = "targetCohort";
	public static final String NAME_Above_Cohort = "cohortsAbove";
	public static final String NAME_Below_Cohort = "cohortsBelow";
	//
	public static final String NAME_SQL_Execute_Text = "sqlClause";
	
	//time series prediction
	public static final String NAME_Ahead_Number = "aheadNumber";  
	
	//svd
	public static final String NAME_ColName = "colName";
    public static final String NAME_RowName = "rowName";
    public static final String NAME_NumFeatures = "numFeatures";
    public static final String NAME_OriginalStep = "originalStep";
    public static final String NAME_SpeedupConst = "speedupConst";
    public static final String NAME_FastSpeedupConst = "fastSpeedupConst";
    public static final String NAME_SlowdownConst = "slowdownConst";
    public static final String NAME_NumIterations = "numIterations";
    public static final String NAME_MinNumIterations = "minNumIterations";
    public static final String NAME_MinImprovement = "minImprovement";
    public static final String NAME_ImprovementReached = "improvementReached";
    public static final String NAME_InitValue = "initValue";
    public static final String NAME_EarlyTeminate = "earlyTeminate";
    public static final String NAME_UmatrixTable = "UmatrixTable";
	public static final String NAME_UmatrixSchema = "UmatrixSchema";
	public static final String NAME_UmatrixDropIfExist = "UmatrixDropIfExist";
    public static final String NAME_VmatrixTable = "VmatrixTable";
	public static final String NAME_VmatrixSchema = "VmatrixSchema";
	public static final String NAME_VmatrixDropIfExist = "VmatrixDropIfExist";
	public static final String NAME_singularValueSchema ="singularValueSchema";
	public static final String NAME_singularValueTable ="singularValueTable";
	public static final String NAME_singularValueDropIfExist = "singularValueDropIfExist";
	
	//svd Calculator
	public static final String NAME_ColNameF = "colNameF";
    public static final String NAME_RowNameF = "rowNameF";
	public static final String NAME_UmatrixFullTable = "UmatrixTableF";
	public static final String NAME_VmatrixFullTable = "VmatrixTableF";
	public static final String NAME_SmatrixFullTable = "singularValueTableF";
	public static final String NAME_UdependentColumn = "UdependentColumnF";
    public static final String NAME_VdependentColumn = "VdependentColumnF";
    public static final String NAME_SdependentColumn = "singularValuedependentColumnF";
	public static final String NAME_UfeatureColumn = "UfeatureColumn";
    public static final String NAME_VfeatureColumn = "VfeatureColumn";
    public static final String NAME_SfeatureColumn = "singularValuefeatureColumn";
    
	public static final String NAME_CrossProduct = "crossProduct";
    public static final String NAME_KeyColumn = "keyColumn";
    public static final String NAME_KeyValue = "keyValue";
    
    //varaibleSelection
    public static final String NAME_scoreType = "scoreType";
    
    //PCA
    public static final String NAME_RemainColumn = "remainColumns";
    
    //Adaboost
	public static final String NAME_AdaBoostModel = "adaboostUIModel";
    
    //CustomizedOperator
    public static final String NAME_IncludeSourceColumn = "includeSourceColumns";
	//WOE
    public static final String NAME_WOEGROUP = "WOETableInfor";
	 
	//others are all ...
	public static final String Column_Type_Numeric="Numeric";
	public static final String Column_Type_Category="String";
	public static final String Column_Type_Int="Int";
	public static final String Column_Type_CategoryAndInt="StringInt";
	public static final String Column_Type_ALL = "ALL";
	public static final String Column_Type_NoFloat="NoFloat";
	public static final String Column_Type_NoNumeric="NoNumeric";
	public static final String Column_Type_DateAndTime="DateAndTime";
	public static final String Column_Type_NoDateAndTime="NoDateAndTime";
	public static final String Column_Type_NoAllArray="NoAllArray";
	public static final String Column_Type_AllArray="AllArray";
	
	public static final String NAME_groupColumn = "groupColumn";

	//PCA
	public static final String NAME_PCAQDropIfExist = "PCAQDropIfExist";
	public static final String NAME_PCAQvalueOutputSchema = "PCAQvalueOutputSchema";
	public static final String NAME_percent = "percent";
	public static final String NAME_PCAQoutputSchema = "PCAQoutputSchema";
	public static final String NAME_PCAQvalueOutputTable = "PCAQvalueOutputTable";
	public static final String NAME_remainColumns = "remainColumns";
	public static final String NAME_analysisType = "analysisType";
	public static final String NAME_PCAQvalueDropIfExist = "PCAQvalueDropIfExist";
	public static final String NAME_PCAQoutputTable = "PCAQoutputTable";
	
	//adaboost, this is customized parameter...
	public static final String NAME_adaboostUIModel = "adaboostUIModel";
	public static final String NAME_Target_Cohort =  "targetCohort";
	public static final String NAME_tableSetConfig = "tableSetConfig";
	 
	
	public static final String NAME_outputTable_StorageParams="StorageParameters" ;
	
	public static final String NAME_UmatrixTable_StorageParams=NAME_UmatrixTable+NAME_outputTable_StorageParams;
	public static final String NAME_VmatrixTable_StorageParams=NAME_VmatrixTable+NAME_outputTable_StorageParams;
	public static final String NAME_singularValueTable_StorageParams=NAME_singularValueTable+NAME_outputTable_StorageParams;
	public static final String NAME_PCAQoutputTable_StorageParams=NAME_PCAQoutputTable+NAME_outputTable_StorageParams;
	public static final String NAME_PCAQvalueOutputTable_StorageParams=NAME_PCAQvalueOutputTable+NAME_outputTable_StorageParams;
	public static final String NAME_PLDADocTopicOutputTable_StorageParams=NAME_PLDADocTopicOutputTable+NAME_outputTable_StorageParams;
	public static final String NAME_PLDAModelOutputTable_StorageParams=NAME_PLDAModelOutputTable+NAME_outputTable_StorageParams;
	public static final String NAME_topicOutTable_StorageParams=NAME_topicOutTable+NAME_outputTable_StorageParams;
	public static final String NAME_docTopicOutTable_StorageParams=NAME_docTopicOutTable+NAME_outputTable_StorageParams;

	public static final String NAME_subflowPath = "subflowPath"; 
	public static final String NAME_tableMapping = "tableMapping";
	public static final String NAME_exitOperator = "exitOperator"; //uuid		
	public static final String NAME_subflowVariable = "subflowVariable"; 

	//UnivariateExplorer
	public static final String NAME_UnivariateModel = "univariateModel"; 
	//ScatterPlot
	public static final String NAME_Y_Column = "columnY"; 
	public static final String NAME_X_Column = "columnX";
	public static final String NAME_C_Column = "categoryColumn";
	//BoxAndWisker
	public static final String NAME_valueDomain_Column = "analysisValueDomain"; 
	public static final String NAME_typeDomain_Column = "typeDomain";
	public static final String NAME_seriesDomain_Column = "seriesDomain";

    public static final String NAME_useApproximation = "useApproximation";
	
	public static final String NAME_USESSL = "useSSL";
	
	//Hadoop
	public static final String NAME_HD_productColumn = "productIndex";
	public static final String NAME_HD_idColumn = "useridIndex";
	public static final String NAME_HD_timeColumn = "timeIndex";
	public static final String NAME_HD_productCountPerUserThreshold = "productCountPerUserThreshold";
	public static final String NAME_HD_cooccurenceThreshold = "cooccurenceThreshold";
	public static final String NAME_HD_split = "delimiter";
	public static final String NAME_HD_interval = "interval";
	public static final String NAME_HD_ouputfile ="outputDir";
	
	public static final String NAME_HD_connetionName =HadoopFileSelectorConfig.NAME_HD_connetionName ;
	public static final String NAME_HD_fileName =HadoopFileSelectorConfig.NAME_HD_fileName ;
	
	public static final String NAME_HD_format =HadoopFileSelectorConfig.NAME_HD_format ;
	public static final String NAME_HD_fileStructure =HadoopFileSelectorConfig.NAME_HD_fileStructure ;
	
	//Hadoop Row Filter
	public static final String NAME_HD_Condition ="filterCondition";
	public static final String NAME_HD_StoreResults ="storeResults";
	public static final String NAME_HD_ResultsLocation ="resultsLocation";
	public static final String NAME_HD_ResultsName ="resultsName";
	public static final String NAME_HD_Override ="override";
	//Hadoop Aggregate
	
	//Hadoop Join
	public static final String NAME_HD_JOIN_MODEL ="hadoopJoinModel";
	
	//whether add QQ plot and Residual plot
	public static final String NAME_AddResidualPlot = "addResidualPlot";
	//split model
	public static final String NAME_groupByTrainerColumn = "splitModelGroupByColumn";
	public static final String NAME_note = "note"; 
	public static final String NAME_HD_Union_Model = "hadoopUnionModel";
	
	public static final String NAME_HD_copyToTableName = "copyToTableName";
	public static final String NAME_HD_ifDataExists = "ifDataExists";
//	public static final String NAME_HD_destination = "destination";
 	public static final String NAME_HD_copyToFileName = "copyToFileName";
	public static final String NAME_HD_ifFileExists = "ifFileExists";
	public static final String NAME_Model_File_Path = "modelFilePath";
	public static final String NAME_HD_PigScript = "pigScript";
	  
	public static final String NAME_HD_PigExecute_fileStructure = "pigExecuteFileStructure";
	public static final String NAME_forestSize = "forestSize"; 
	public static final String NAME_nodeColumnNumber = "nodeColumnNumber";
	
	//EM Clustering
	public static final String NAME_clusterNumber = "clusterNumber";
	public static final String NAME_maxIterationNumber = "maxIterationNumber";
	public static final String NAME_sample_with_replacement = "sampleWithReplacement";
	public static final String NAME_initClusterSize="initClusterSize";
	
}
