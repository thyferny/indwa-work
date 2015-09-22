/**
 * ClassName LanguagePack.getMessage(LanguagePack.java
 *
 * Version information: 1.00
 *
 * Data: 2011/04/07
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.inter.resources;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class LanguagePack {	
	public static final String Bundle_Name="com.alpine.miner.inter.resources.language";
    private static final Logger itsLogger=Logger.getLogger(LanguagePack.class);

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
 			try {
				return rb.getString(key);
			} catch (Exception e) {
				itsLogger.error(e.getMessage(),e);
				return key;
			}
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
			
	/**
	 * For each Operator 
	 */
	//Data Source
	public static final String DBTABLE_OPERATOR = "DBTABLE_OPERATOR";
	//Data Transformation
	public static final String AGGREGATE_OPERATOR = "AGGREGATE_OPERATOR";
	public static final String PIVOT_OPERATOR = "PIVOT_OPERATOR";
	public static final String TABLEJOIN_OPERATOR = "TABLEJOIN_OPERATOR";
	public static final String ROWFILTER_OPERATOR = "ROWFILTER_OPERATOR";
	public static final String COLUMNFILTER_OPERATOR = "COLUMNFILTER_OPERATOR";
	public static final String N2T_OPERATOR = "N2T_OPERATOR";
	public static final String REPLACENULL_OPERATOR = "REPLACENULL_OPERATOR";
	public static final String VARIABLE_OPERATOR = "VARIABLE_OPERATOR";
	public static final String NORMALIZATION_OPERATOR = "NORMALIZATION_OPERATOR";
	public static final String VARIABLE_SELECTION_OPERATOR = "VARIABLE_SELECTION_OPERATOR";
	public static final String PIG_EXECUTE_OPERATOR = "PIG_EXECUTE_OPERATOR";
	
	//Data Exploreration
	public static final String BARCHART_OPERATOR = "BARCHART_OPERATOR";
	public static final String CORRELATION_OPERATOR = "CORRELATION_OPERATOR";
	public static final String FREQUENCY_OPERATOR = "FREQUENCY_OPERATOR";
	public static final String HISTOGRAM_OPERATOR = "HISTOGRAM_OPERATOR";
	public static final String INFORMATIONVALUE_OPERATOR = "INFORMATIONVALUE_OPERATOR";
	public static final String VALUEANALYSIS_OPERATOR = "VALUEANALYSIS_OPERATOR";
	public static final String UNIVARIATE_OPERATOR = "UNIVARIATE_OPERATOR";
	public static final String SCATTERMATRIX_OPERATOR = "SCATTERMATRIX_OPERATOR";
	public static final String UNIVARIATEEXPLORER_OPERATOR = "UNIVARIATEEXPLORER_OPERATOR";
	public static final String SCATTERPLOT_OPERATOR = "SCATTERPLOT_OPERATOR";
	public static final String BOXANDWISKER_OPERATOR = "BOXANDWISKER_OPERATOR";
	//Association
	public static final String ASSOCIATION_OPERATOR = "ASSOCIATION_OPERATOR";
	public static final String ASSOCIATION_PREDICTION_OPERATOR = "ASSOCIATION_PREDICTION_OPERATOR";
	//Cluster
	public static final String KMEANS_OPERATOR = "KMEANS_OPERATOR";
	//Tree
	public static final String DTREE_OPERATOR = "DTREE_OPERATOR";
	public static final String CTREE_OPERATOR = "CTREE_OPERATOR";
	public static final String TREE_PREDICT_OPERATOR = "TREE_PREDICT_OPERATOR";
	//LinearRegression
	public static final String LINEARREGRESSION_OPERATOR = "LINEARREGRESSION_OPERATOR";
	public static final String LINEARREGRESSION_PREDICT_OPERATOR = "LINEARREGRESSION_PREDICT_OPERATOR";
	//LogisticRegression
	public static final String LOGISTICREGRESSION_OPERATOR = "LOGISTICREGRESSION_OPERATOR";
	public static final String LOGISTICREGRESSION_PREDICT_OPERATOR = "LOGISTICREGRESSION_PREDICT_OPERATOR";
	//Model
	public static final String MODEL_OPERATOR = "MODEL_OPERATOR";
	//Evaluate
	public static final String GOODNESSOFFIT_OPERATOR = "GOODNESSOFFIT_OPERATOR";
	public static final String LIFT_OPERATOR = "LIFT_OPERATOR";
	public static final String ROC_OPERATOR = "ROC_OPERATOR";
    public static final String CONFUSION_OPERATOR = "CONFUSION_OPERATOR";

    //NB
	public static final String NAIVE_BAYES_OPERATOR = "NAIVE_BAYES_OPERATOR";
	public static final String NAIVE_BAYES_PREDICTION_OPERATOR = "NAIVE_BAYES_PREDICTION_OPERATOR";
	//NN
	public static final String NEURAL_NETWORK_OPERATOR = "NEURAL_NETWORK_OPERATOR";
	public static final String NEURAL_NETWORK_PREDICTION_OPERATOR = "NEURAL_NETWORK_PREDICTION_OPERATOR";
	//Sampling
	public static final String RANDOM_SAMPLING_OPERATOR = "RANDOM_SAMPLING_OPERATOR";
	public static final String SAMPLE_SELECTOR_OPERATOR = "SAMPLE_SELECTOR_OPERATOR";
	public static final String STRATIFIED_SAMPLING_OPERATOR = "STRATIFIED_SAMPLING_OPERATOR";
	//SVM
	public static final String SVM_CLASSIFICATION_OPERATOR = "SVM_CLASSIFICATION_OPERATOR";
	public static final String SVM_NOVELTY_DETECTION_OPERATOR = "SVM_NOVELTY_DETECTION_OPERATOR";
	public static final String SVM_PREDICTION = "SVM_PREDICTION";
	public static final String SVM_REGRESSION_OPERATOR = "SVM_REGRESSION_OPERATOR";
	//PR
	public static final String PRODUCT_RECOMMONDATION_OPERATOR = "PRODUCT_RECOMMONDATION_OPERATOR";
	public static final String PRODUCT_RECOMMONDATION_EVALUATION_OPERATOR = "PRODUCT_RECOMMONDATION_EVALUATION_OPERATOR";
	//TimeSeries
	public static final String TIMESERIES_OPERATOR = "TIMESERIES_OPERATOR";
	public static final String TIMESERIES_PREDICTION_OPERATOR = "TIMESERIES_PREDICTION_OPERATOR";
	//Execute
	public static final String SQLEXECUTE_OPERATOR = "SQLEXECUTE_OPERATOR";
	public static final String SVD_OPERATOR = "SVD_OPERATOR";
	public static final String SVD_CALCULATOR = "SVD_CALCULATOR";
	//Adaboost
	public static final String ADABOOST_OPERATOR = "ADABOOST_OPERATOR";
	public static final String ADABOOST_PREDICTION_OPERATOR = "ADABOOST_PREDICTION_OPERATOR";
	//PCA
	public static final String PCA_OPERATOR = "PCA_OPERATOR";
	//table set
	public static final String TABLESET_OPERATOR = "TABLESET_OPERATOR";
	//WOE
	public static final String WOE_OPERATOR = "WOE_OPERATOR";
	public static final String WOE_TABLE_GENERATOR_OPERATOR = "WOE_TABLE_GENERATOR_OPERATOR";
	//EM
	public static final String EM_CLUSTERING_OPERATOR = "EM_CLUSTERING_OPERATOR";	
	//ERROR
	public static final String SQLEXECUTE_HAVE_NO_PRECEDING_OPERATOR = "SQLEXECUTE_HAVE_NO_PRECEDING_OPERATOR";
	public static final String MESSAGE_CHECK_LINK = "MESSAGE_CHECK_LINK";
	public static final String MESSAGE_CHECK_LINK_HADOOP = "MESSAGE_CHECK_LINK_HADOOP";
	public static final String MESSAGE_ALREADY_LINK = "MESSAGE_ALREADY_LINK";
	public static final String CANT_LINK_MUTIL_DATASOURCE = "CANT_LINK_MUTIL_DATASOURCE";
	public static final String CAN_NOT_LINK_MUTIL_MODEL = "CAN_NOT_LINK_MUTIL_MODEL";
	
	public static final String SVDC_CHECK_LINK_SVD_EXISTS = "SVDC_CHECK_LINK_SVD_EXISTS";
	public static final String ERROR_Configure_Operator = "ERROR_Configure_Operator";
	public static final String CANNOT_LINKMUTIL_TOOPERATOR = "CANNOT_LINKMUTIL_TOOPERATOR";
	
	public static final String SUBFLOW_INVALID = "SUBFLOW_INVALID";
	public static final String SUBFLOW_OR = "SUBFLOW_OR";

	public static final String PLDA_OPERATOR = "PLDA_OPERATOR" ;
	public static final String PLDA_OPERATOR_SHORT = "PLDA_OPERATOR_SHORT";

	public static final String PLDA_PREDICT_OPERATOR = "PLDA_PREDICT_OPERATOR";
	public static final String PLDA_PREDICT_OPERATOR_SHORT = "PLDA_PREDICT_OPERATOR_SHORT";

	public static final String SUBFLOW_OPERATOR =  "SUBFLOW_OPERATOR";
 
	public static final String HP_FILE_OPERATOR = "HP_FILE_OPERATOR";
	public static final String HP_FILE_OPERATOR_SHORT = "HP_FILE_OPERATOR_SHORT";
	public static final String HP_PR_OPERATOR = "HP_PR_OPERATOR";
	public static final String HP_PR_OPERATOR_SHORT = "HP_PR_OPERATOR_SHORT";
	public static final String HP_UNION_OPERATOR ="HP_UNION_OPERATOR";
	public static final String HP_UNION_OPERATOR_SHORT = "HP_UNION_OPERATOR_SHORT";
	
	public static final String HP_ROWFILTER_OPERATOR = "HP_ROWFILTER_OPERATOR";
	public static final String HP_ROWFILTER_OPERATOR_SHORT = "HP_ROWFILTER_OPERATOR_SHORT";

	public static final String TABLE_ALREADY_LINK = "TABLE_ALREADY_LINK";

	public static final String HP_AGGREGATE_OPERATOR = "HP_AGGEREGATE_OPERATOR";
	public static final String HP_AGGREGATE_OPERATOR_SHORT = "HP_AGGEREGATE_OPERATOR_SHORT";

	public static final String NOTE_OPERATOR = "NOTE_OPERATOR"; 
	public static final String NOTE_OPERATOR_SHORT = "NOTE_OPERATOR_SHORT";

	public static final String HP_COPYTO_OPERATOR = "HP_COPYTO_OPERATOR"; 
	public static final String CopyTo_Label = "CopyTo_Label" ;
	public static final String 	Destination_Lable = "Destination_Lable";

	public static final String HP_COPYTODB_OPERATOR = "HP_COPYTODB_OPERATOR";

	public static final String EM_CLUSTERING_PREDICTION_OPERATOR = "EM_CLUSTERING_PREDICTION_OPERATOR";  
}
