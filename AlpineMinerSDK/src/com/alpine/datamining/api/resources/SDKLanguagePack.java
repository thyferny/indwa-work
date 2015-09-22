/**
 * ClassName LanguagePack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.resources;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
/**
 * 
 * @author John Zhao
 *
 */
public class SDKLanguagePack {
	public static final String Bundle_Name="com.alpine.datamining.api.resources.SDKMessage" ;
 
	private static final MessageFormat formatter = new MessageFormat("");
	
	public static final List<Locale> Supported_Locales =Arrays.asList(new Locale[]{ 
			Locale.CHINA,Locale.CHINESE,Locale.ENGLISH,Locale.US,Locale.JAPAN ,Locale.JAPANESE}); 
 
	public static final Locale Default_Locale = Locale.ENGLISH; 

	private static final HashMap<Locale,ResourceBundle> resourceMap = new HashMap<Locale,ResourceBundle>();
	
	static{
		
		for(int i = 0;i<Supported_Locales.size();i++){
			Locale locale = Supported_Locales.get(i);
			 ResourceBundle rb =null;
			if(locale==Locale.US){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.ENGLISH  );
			} else if(locale==Locale.CHINA){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.CHINESE);
			}else if(locale==Locale.JAPAN){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.JAPANESE);
			}
			else{
			   rb = ResourceBundle.getBundle(Bundle_Name,locale  );
			 }
			 resourceMap.put(locale, rb);
			 
		}
		
	}
	
 	public static String getMessage(String key, Locale locale){
 		if(Supported_Locales.contains(locale)==false){
 			locale = Default_Locale;
 		} 
 		ResourceBundle rb =resourceMap.get(locale) ;
 		if(rb!=null){
 			return rb.getString(key);
 		}else{
 			return "";
 		}
 	}
 	
	public static String getMessage(String name, Locale locale,Object[] arguments) {		 
		String message = getMessage(name, locale);
		if(arguments!=null){
			try {
				formatter.applyPattern(message);
				String formatted = formatter.format(arguments);
				return formatted; 
			} catch (Throwable t) {
				return message;
			}
		}
		else{
			return message;
		}
		
	}
	public static final String Database_Connection = "Database_Connection"; 
	public static final String Table_Name = "Table_Name"; 
	public static final String Table_Name_oneline = "Table_Name_oneline"; 
	
	public static final String Table_Columns = "Table_Columns";
	public static final String DtatBase_URL = "DtatBase_URL";
	public static final String DtatBase_User_Name = "DtatBase_User_Name";
	public static final String CONNECTION_OK = "CONNECTION_OK";
	public static final String HISTOGRAM_NAME = "HISTOGRAM_NAME";
	public static final String HISTOGRAM_DESCRIPTION = "HISTOGRAM_DESCRIPTION";
	public static final String HISTOGRAM_COLUMN = "HISTOGRAM_COLUMN";
	public static final String CORRELATION_COLUMN = "CORRELATION_COLUMN";
	public static final String SCATTERMATRIX_NAME = "SCATTERMATRIX_NAME";
	public static final String SCATTERMATRIX_DESCRIPTION = "SCATTERMATRIX_DESCRIPTION";
	public static final String CORRELATION_NAME = "CORRELATION_NAME";
	public static final String CORRELATION_DESCRIPTION = "CORRELATION_DESCRIPTION";
	public static final String SQLEXECUTE_NAME = "SQLEXECUTE_NAME";
	public static final String SQLEXECUTE_DESCRIPTION = "SQLEXECUTE_DESCRIPTION";
	public static final String NORMALIZATION_NAME = "NORMALIZATION_NAME";
	public static final String NORMALIZATION_DESCRIPTION = "NORMALIZATION_DESCRIPTION";
	public static final String NORMALIZATION_MAXMIN = "NORMALIZATION_MAXMIN";
	public static final String NORMALIZATION_MAX = "NORMALIZATION_MAX";
	public static final String NORMALIZATION_Illegal_P = "NORMALIZATION_Illegal_P";
	public static final String NORMALIZATION_Illegal_Z = "NORMALIZATION_Illegal_Z";
	public static final String NORMALIZATION_Illegal_AVG = "NORMALIZATION_Illegal_AVG";
	public static final String AGGREGATE_NAME = "AGGREGATE_NAME";
	public static final String AGGREGATE_DESCRIPTION= "AGGREGATE_DESCRIPTION";
	public static final String ASSOCIATION_NAME= "ASSOCIATION_NAME";
	public static final String ASSOCIATION_DESCRIPTION= "ASSOCIATION_DESCRIPTION";
	public static final String KMEANS_NAME= "KMEANS_NAME";
	public static final String KMEANS_DESCRIPTION= "KMEANS_DESCRIPTION";
	public static final String DATA_FILTER_NAME= "DATA_FILTER_NAME";
	public static final String DATA_FILTER_DESCRIPTION= "DATA_FILTER_DESCRIPTION";
	public static final String REPLACE_NULL_NAME= "REPLACE_NULL_NAME";
	public static final String REPLACE_NULL_DESCRIPTION= "REPLACE_NULL_DESCRIPTION";
	public static final String REPLACE_NULL_COLUMN = "REPLACE_NULL_COLUMN";
	public static final String FREQUENCY_NAME = "FREQUENCY_NAME";
	public static final String FREQUENCY_DESCRIPTION = "FREQUENCY_DESCRIPTION";
	public static final String N2T_NAME = "N2T_NAME";
	public static final String N2T_DESCRIPTION = "N2T_DESCRIPTION";
	public static final String RANDOM_SAMPLEING_NAME = "RANDOM_SAMPLEING_NAME";
	public static final String RANDOM_SAMPLEING_DESCRIPTION = "RANDOM_SAMPLEING_DESCRIPTION";
	public static final String SAMPLEING_SELECTOR_NAME = "SAMPLEING_SELECTOR_NAME";
	public static final String SAMPLEING_SELECTOR_DESCRIPTION = "SAMPLEING_SELECTOR_DESCRIPTION";
	public static final String STRATIFIED_SAMPLEING_NAME = "STRATIFIED_SAMPLEING_NAME";
	public static final String STRATIFIED_SAMPLEING_DESCRIPTION = "STRATIFIED_SAMPLEING_DESCRIPTION";
	public static final String VALUE_ANALYSIS_NAME= "VALUE_ANALYSIS_NAME";
	public static final String VALUE_ANALYSIS_DESCRIPTION= "VALUE_ANALYSIS_DESCRIPTION";
	public static final String VARIABLE_ANALYSIS_NAME= "VARIABLE_ANALYSIS_NAME";
	public static final String VARIABLE_ANALYSIS_DESCRIPTION= "VARIABLE_ANALYSIS_DESCRIPTION";
	public static final String GOODNESSOFFit_NAME= "GOODNESSOFFit_NAME";
	public static final String GOODNESSOFFit_DESCRIPTION= "GOODNESSOFFit_DESCRIPTION";
	public static final String LIFT_NAME= "LIFT_NAME";
	public static final String LIFT_DESCRIPTION= "LIFT_DESCRIPTION";
	public static final String ROC_NAME= "ROC_NAME";
	public static final String ROC_DESCRIPTION= "ROC_DESCRIPTION";
    public static final String CONFUSION_NAME="CONFUSION_NAME";
    public static final String CONFUSION_DESCRIPTION="CONFUSION_DESCRIPTION";
	public static final String TREE_PREDICT_NAME= "TREE_PREDICT_NAME";
	public static final String TREE_PREDICT_DESCRIPTION= "TREE_PREDICT_DESCRIPTION";
	public static final String ASSOCIATION_PREDICT_NAME= "ASSOCIATION_PREDICT_NAME";
	public static final String ASSOCIATION_PREDICT_DESCRIPTION= "ASSOCIATION_PREDICT_DESCRIPTION";
	public static final String LINEAR_REGRESSION_PREDICT_NAME= "LINEAR_REGRESSION_PREDICT_NAME";
	public static final String LINEAR_REGRESSION_PREDICT_DESCRIPTION= "LINEAR_REGRESSION_PREDICT_DESCRIPTION";
	public static final String LOGISTIC_REGRESSION_PREDICT_NAME= "LOGISTIC_REGRESSION_PREDICT_NAME";
	public static final String LOGISTIC_REGRESSION_PREDICT_DESCRIPTION= "LOGISTIC_REGRESSION_PREDICT_DESCRIPTION";
	public static final String NAIVE_BAYES_PREDICT_NAME= "NAIVE_BAYES_PREDICT_NAME";
	public static final String NAIVE_BAYES_PREDICT_DESCRIPTION= "NAIVE_BAYES_PREDICT_DESCRIPTION";
	public static final String SVD_PREDICT_NAME= "SVD_PREDICT_NAME";
	public static final String SVD_PREDICT_DESCRIPTION= "SVD_PREDICT_DESCRIPTION";
	public static final String SVD_CALCULATOR_NAME= "SVD_CALCULATOR_NAME";
	public static final String SVD_CALCULATOR_DESCRIPTION= "SVD_CALCULATOR_DESCRIPTION";
	public static final String NEURALNETWORK_PREDICT_NAME = "NEURALNETWORK_PREDICT_NAME";
	public static final String NEURALNETWORK_PREDICT_DESCRIPTION= "NEURALNETWORK_PREDICT_DESCRIPTION";
	public static final String TABLE_JOIN_NAME = "TABLE_JOIN_NAME";
	public static final String TABLE_JOIN_DESCRIPTION = "TABLE_JOIN_DESCRIPTION";
	public static final String TABLE_SELECTOR_NAME = "TABLE_SELECTOR_NAME";
	public static final String TABLE_SELECTOR_DESCRIPTION = "TABLE_SELECTOR_DESCRIPTION";
	public static final String DECISION_TREE_TRAIN_NAME= "DECISION_TREE_TRAIN_NAME";
	public static final String DECISION_TREE_TRAIN_DESCRIPTION= "DECISION_TREE_TRAIN_DESCRIPTION";
	public static final String CART_TREE_TRAIN_NAME= "CART_TREE_TRAIN_NAME";
	public static final String CART_TREE_TRAIN_DESCRIPTION= "CART_TREE_TRAIN_DESCRIPTION";
	public static final String EngineModelWrapper_NAME= "EngineModelWrapper_NAME";
	public static final String EngineModelWrapper_DESCRIPTION= "EngineModelWrapper_DESCRIPTION";
	public static final String LINEAR_REGRESSION_TRAIN_NAME= "LINEAR_REGRESSION_TRAIN_NAME";
	public static final String LINEAR_REGRESSION_TRAIN_DESCRIPTION= "LINEAR_REGRESSION_TRAIN_DESCRIPTION";
	public static final String LOGISTIC_REGRESSION_TRAIN_NAME= "LOGISTIC_REGRESSION_TRAIN_NAME";
	public static final String LOGISTIC_REGRESSION_TRAIN_DESCRIPTION= "LOGISTIC_REGRESSION_TRAIN_DESCRIPTION";
	public static final String NAIVE_BAYES_TRAIN_NAME= "NAIVE_BAYES_TRAIN_NAME";
	public static final String NAIVE_BAYES_TRAIN_DESCRIPTION= "NAIVE_BAYES_TRAIN_DESCRIPTION";
	public static final String SVD_TRAIN_NAME= "SVD_TRAIN_NAME";
	public static final String SVD_TRAIN_DESCRIPTION= "SVD_TRAIN_DESCRIPTION";
	public static final String NEURALNETWORK_TRAIN_NAME = "NEURALNETWORK_TRAIN_NAME";
	public static final String NEURALNETWORK_TRAIN_DESCRIPTION= "NEURALNETWORK_TRAIN_DESCRIPTION";
	public static final String TOO_MANY_VALUES_WARNING= "TOO_MANY_VALUES_WARNING";
	public static final String STRATIFIED_OUT_BOUNDS= "STRATIFIED_OUT_BOUNDS";
	public static final String TYPE_CUSTIMZE_LABEL = "TYPE_CUSTIMZE_LABEL";
	public static final String TYPE_AVG_ASC_LABEL = "TYPE_AVG_ASC_LABEL";
	public static final String PIVOT_NAME = "PIVOT_NAME";
	public static final String PIVOT_DESRIPTION = "PIVOT_DESCRIPTION";
	public static final String ARIMA_TRAIN_NAME= "ARIMA_TRAIN_NAME";
	public static final String ARIMA_TRAIN_DESCRIPTION= "ARIMA_TRAIN_DESCRIPTION";
	public static final String ARIMA_PREDICT_NAME = "ARIMA_PREDICT_NAME";
	public static final String ARIMA_PREDICT_DESCRIPTION= "ARIMA_PREDICT_DESCRIPTION";
	public static final String UNIVARIATE_NAME= "UNIVARIATE_NAME";
	public static final String UNIVARIATE_DESCRIPTION= "UNIVARIATE_DESCRIPTION";
	public static final String BOX_PLOT_NAME= "BOX_PLOT_NAME";
	public static final String BOX_PLOT_DESCRIPTION= "BOX_PLOT_DESCRIPTION";
    public static final String BAR_CHART_NAME= "BAR_CHART_NAME";
    public static final String BAR_CHART_DESCRIPTION= "BAR_CHART_DESCRIPTION";
    public static final String INFORMATIONVALUE_NAME = "INFORMATIONVALUE_NAME";
	public static final String INFORMATIONVALUE_DESCRIPTION = "INFORMATIONVALUE_DESCRIPTION";
	public static final String RECOMMENDATION_NAME = "RECOMMENDATION_NAME";
	public static final String RECOMMENDATION_DESCRIPTION = "RECOMMENDATION_DESCRIPTION";
	public static final String RECOMMENDATION_EVALUATION_NAME = "RECOMMENDATION_EVALUATION_NAME";
	public static final String RECOMMENDATION_EVALUATION_DESCRIPTION = "RECOMMENDATION_EVALUATION_DESCRIPTION";
	public static final String SVM_CL_TRAIN_NAME= "SVM_CL_TRAIN_NAME";
	public static final String SVM_CL_TRAIN_DESCRIPTION= "SVM_CL_TRAIN_DESCRIPTION";
	public static final String SVM_RG_TRAIN_NAME= "SVM_RG_TRAIN_NAME";
	public static final String SVM_RG_TRAIN_DESCRIPTION= "SVM_RG_TRAIN_DESCRIPTION";
	public static final String SVM_ND_TRAIN_NAME= "SVM_ND_TRAIN_NAME";
	public static final String SVM_ND_TRAIN_DESCRIPTION= "SVM_ND_TRAIN_DESCRIPTION";
	public static final String SVM_PREDICT_NAME= "SVM_PREDICT_NAME";
	public static final String SVM_PREDICT_DESCRIPTION= "SVM_PREDICT_DESCRIPTION";
	public static final String VARIABLE_SELECTION_NAME = "VARIABLE_SELECTION_NAME";
	public static final String VARIABLE_SELECTION_DESCRIPTION = "VARIABLE_SELECTION_DESCRIPTION";
	public static final String VARIABLE_SELECTION_TRANSFORM_LABEL = "VARIABLE_SELECTION_TRANSFORM_LABEL"; 
	public static final String COLUMN_FILTER_NAME = "COLUMN_FILTER_NAME";
	public static final String COLUMN_FILTER_DESCRIPTION = "COLUMN_FILTER_DESCRIPTION";
	public static final String VARIABLE_SELECTION_SCORETYPE_INFO_GAIN =  "VARIABLE_SELECTION_SCORETYPE_INFO_GAIN";
	public static final String VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO =  "VARIABLE_SELECTION_SCORETYPE_INFO_GAIN_RATIO";
	public static final String VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN =  "VARIABLE_SELECTION_SCORETYPE_TRANSFORMED_INFO_GAIN";
	public static final String ADABOOST_PREDICT_NAME="ADABOOST_PREDICT_NAME";
	public static final String ADABOOST_PREDICT_DESCRIPTION="ADABOOST_PREDICT_DESCRIPTION";
	public static final String ADABOOST_TRAIN_NAME="ADABOOST_TRAIN_NAME";
	public static final String ADABOOST_TRAIN_DESCRIPTION="ADABOOST_TRAIN_DESCRIPTION";
	public static final String ADABOOST_SAMPLE_ERRINFO="ADABOOST_SAMPLE_ERRINFO";
	public static final String ADABOOST_SAMPLE_FAIL="ADABOOST_SAMPLE_FAIL";
	public static final String ADABOOST_MAX_DEPENDENT_COUNT_ERRINFO="ADABOOST_MAX_DEPENDENT_COUNT_ERRINFO";
	public static final String ADABOOST_TOO_MANY_TRAINER="ADABOOST_TOO_MANY_TRAINER";
	public static final String PCA_VALUES_ALL_SAME="PCA_VALUES_ALL_SAME";
	public static final String PCA_NUM_ZERO="PCA_NUM_ZERO";
	public static final String PCA_NAME="PCA_NAME";
	public static final String PCA_DESCRIPTION="PCA_DESCRIPTION";
	public static final String STEPWISE_LINEAR_SQUARE="STEPWISE_LINEAR_SQUARE";
	public static final String WOE_TRAIN_NAME="WOE_TRAIN_NAME";
	public static final String WOE_TRAIN_DESCRIPTION="WOE_TRAIN_DESCRIPTION";
	public static final String WOE_PREDICT_NAME="WOE_PREDICT_NAME";
	public static final String WOE_PREDICT_DESCRIPTION="WOE_PREDICT_DESCRIPTION";
	public static final String WOE_DEPENDENT_2_VALUE="WOE_DEPENDENT_2_VALUE";
	public static final String WOE_DEPENDENT_NULL="WOE_DEPENDENT_NULL";

	public static final String TABLESET_NAME = "TABLESET_NAME";
	public static final String TABLESET_DESCRIPTION = "TABLESET_DESCRIPTION";

	public static final String PLDA_TRAIN_NAME="PLDA_TRAIN_NAME";
	public static final String PLDA_TRAIN_DESCRIPTION="PLDA_TRAIN_DESCRIPTION";
	public static final String PLDA_PREDICT_NAME="PLDA_PREDICT_NAME";
	public static final String PLDA_PREDICT_DESCRIPTION="PLDA_PREDICT_DESCRIPTION";
	
	public static final String EM_TRAIN_NAME="EM_TRAIN_NAME";
	public static final String EM_TRAIN_DESCRIPTION="EM_TRAIN_DESCRIPTION";
	public static final String EM_PREDICT_NAME="PLDA_PREDICT_NAME";
	public static final String EM_PREDICT_DESCRIPTION="EM_PREDICT_DESCRIPTION";
	
	public static final String HD_FILE_NAME="HD_FILE_NAME";
	public static final String HD_FILE_NAME_DESCRIPTION="HD_FILE_NAME_DESCRIPTION";
	public static final String HD_FILE_PATH="HD_FILE_PATH";
	public static final String HD_ROWFILTER_NAME="HD_ROWFILTER_NAME";
	public static final String HD_ROWFILTER_DESCRIPTION="HD_ROWFILTER_DESCRIPTION";

	public static final String Append_Not_Supported = "Append_Not_Supported";
 
	public static final String RANDOME_FOREST_TRAIN_NAME = "RANDOME_FOREST_TRAIN_NAME";

	public static final String RANDOME_FOREST_TRAIN_DESCRIPTION = "RANDOME_FOREST_TRAIN_DESCRIPTION";

	public static final String LOCAL_MODE = "LOCAL_MODE"; 
}

