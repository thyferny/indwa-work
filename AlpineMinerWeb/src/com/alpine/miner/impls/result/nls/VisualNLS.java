/**
 * ClassName VisualNLS.java
 *
 * Version information: 3.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.result.nls;
        
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
/**
 * 
 * @author john
 *
 */
public class VisualNLS {
	public static final List<Locale> Supported_Locales =Arrays.asList(new Locale[]{ 
			Locale.CHINA,Locale.CHINESE,Locale.ENGLISH,Locale.US,Locale.JAPAN ,Locale.JAPANESE}); 
 
	private static final MessageFormat formatter = new MessageFormat("");
	public static final Locale Default_Locale =Locale.ENGLISH; 

	private static final HashMap<Locale,ResourceBundle> resourceMap = new HashMap<Locale,ResourceBundle>();

	private static final String NLS_BUNDLE = "com.alpine.miner.impls.result.nls.language"; 
	static{
		
		for(int i = 0;i<Supported_Locales.size();i++){
			Locale locale = Supported_Locales.get(i);
			 ResourceBundle rb =null;
			if(locale==Locale.US){
				   rb = ResourceBundle.getBundle(NLS_BUNDLE,Locale.ENGLISH  );
			} else if(locale==Locale.CHINA){
				   rb = ResourceBundle.getBundle(NLS_BUNDLE,Locale.CHINESE);
			}else if(locale==Locale.JAPAN){
				   rb = ResourceBundle.getBundle(NLS_BUNDLE,Locale.JAPANESE);
			}
			else{
			   rb = ResourceBundle.getBundle(NLS_BUNDLE,locale  );
			 }
			 resourceMap.put(locale, rb);
			 
		}
		
	}
	 	
 	public static String getMessage(String key, Locale locale){
 		if(Supported_Locales.contains(locale)==false){
 			locale = Default_Locale;
 		} 
 		ResourceBundle rb =resourceMap.get(locale) ;
 		
 		return rb.getString(key);
 	}
 
 	public static String getMessage(String key, Locale locale,Object ... arguments){
 		if(Supported_Locales.contains(locale)==false){
 			locale = Default_Locale;
 		} 
 		ResourceBundle rb =resourceMap.get(locale) ;
 		
 		String message = rb.getString(key);
 		 
 		 
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
 	
//	public static final String CONFIDENCE ="CONFIDENCE";
//	public static final String PREMISE ="PREMISE";
//	public static final String CONCLUSION ="CONCLUSION";
//	public static final String SUPPORT ="SUPPORT";
	
	public static final String CLUSTER_COLUMN_NAME ="CLUSTER_COLUMN_NAME";
	public static final String CLUSTER_COUNT ="CLUSTER_COUNT";
	public static final String AVG_MEASUREMENT ="AVG_MEASUREMENT";
//	public static final String NOT_SPECIFIED ="NOT_SPECIFIED";
	
//	public static final String SELECTED ="SELECTED";
//	public static final String FIELD_NAME ="FIELD_NAME";
	
	public static final String COUNT ="COUNT";
	public static final String PERCENTAGE ="PERCENTAGE";
	
	public static final String BIN ="BIN";
	public static final String BEGIN ="BEGIN";
	public static final String END ="END";
	public static final String ACC_COUNT ="ACC_COUNT";
	public static final String ACC_PERCENTAGE ="ACC_PERCENTAGE";
	public static final String SUMMARY ="SUMMARY";
	public static final String KMeans ="KMeans";

//	public static final String NO_OF_BIN ="NO_OF_BIN";
//	public static final String FIELD_VALUE_NOT_VALID ="FIELD_VALUE_NOT_VALID";
//
//	public static final String TOTAL_VALUE_COUNT ="TOTAL_VALUE_COUNT";
	public static final String UNIQUE_VALUE_COUNT ="UNIQUE_VALUE_COUNT";
	public static final String NULL_COUNT ="NULL_COUNT";
	public static final String EMPTY_COUNT ="EMPTY_COUNT";
	public static final String ZERO_COUNT ="ZERO_COUNT";
	public static final String POS_VALUE_COUNT ="POS_VALUE_COUNT";
	public static final String NEG_VALUE_COUNT ="NEG_VALUE_COUNT";
	public static final String VALUE_SHAPE_ANALYSIS ="VALUE_SHAPE_ANALYSIS";
	public static final String COUNT_SHAPE_ANALYSIS ="COUNT_SHAPE_ANALYSIS";
	
	
	public static final String COLUMN_NAME ="COLUMN_NAME";
	public static final String VALUE ="VALUE";
	public static final String DATA_TYPE ="DATA_TYPE";
	public static final String VARIABLES="VARIABLES";
	public static final String STATES ="STATES";
	public static final String POPULATION ="POPULATION";
	public static final String CLUSTER ="CLUSTER";
	public static final String MESSAGE_TITLE ="MESSAGE_TITLE";
	public static final String DATA_TITLE ="DATA_TITLE";
    public static final String DATA_GROUP = "DATA_GROUP";
	public static final String WARING_MESSAGE_TITLE ="WARING_MESSAGE_TITLE";
	public static final String CLUSTER_PROFILES ="CLUSTER_PROFILES";
//	public static final String SHAPE_ANALYSIS_TITLE ="SHAPE_ANALYSIS_TITLE";
	public static final String MIN_VALUE = "MIN_VALUE";
	public static final String Q1_VALUE = "Q1_VALUE";
	public static final String MEDIAN_VALUE = "MEDIAN_VALUE";
	public static final String Q3_VALUE = "Q3_VALUE";
	public static final String TOP_01_VALUE = "TOP_01_VALUE";
	public static final String TOP_01_PERCENT = "TOP_01_PERCENT";
	public static final String TOP_02_VALUE = "TOP_02_VALUE";
	public static final String TOP_02_PERCENT = "TOP_02_PERCENT";
	public static final String TOP_03_VALUE = "TOP_03_VALUE";
	public static final String TOP_03_PERCENT = "TOP_03_PERCENT";
	public static final String TOP_04_VALUE = "TOP_04_VALUE";
	public static final String TOP_04_PERCENT = "TOP_04_PERCENT";
	public static final String TOP_05_VALUE = "TOP_05_VALUE";
	public static final String TOP_05_PERCENT = "TOP_05_PERCENT";
	public static final String TOP_06_VALUE = "TOP_06_VALUE";
	public static final String TOP_06_PERCENT = "TOP_06_PERCENT";
	public static final String TOP_07_VALUE = "TOP_07_VALUE";
	public static final String TOP_07_PERCENT = "TOP_07_PERCENT";
	public static final String TOP_08_VALUE = "TOP_08_VALUE";
	public static final String TOP_08_PERCENT = "TOP_08_PERCENT";
	public static final String TOP_09_VALUE = "TOP_09_VALUE";
	public static final String TOP_09_PERCENT = "TOP_09_PERCENT";
	public static final String TOP_10_VALUE = "TOP_10_VALUE";
	public static final String TOP_10_PERCENT = "TOP_10_PERCENT";
	public static final String MAX_VALUE ="MAX_VALUE";
//	public static final String TOP_N ="TOP_N";
	public static final String STANDARD_DEVIATION ="STANDARD_DEVIATION";
	public static final String AVERAGE ="AVERAGE";
	
//	public static final String DENSITY ="DENSITY";

	public static final String FALSE_POSITIVE_RATE ="FALSE_POSITIVE_RATE";
	public static final String SENSITIVITY ="SENSITIVITY";
	
	public static final String ROC_CURVE ="ROC_CURVE";
	public static final String RANDOM ="RANDOM";
	public static final String LIFT_CURVE ="LIFT_CURVE";
	public static final String TYPE ="TYPE";
//	public static final String BOX_AND_WHISKER_SHOW ="BOX_AND_WHISKER_SHOW";
//	public static final String SCATTER_POINT ="SCATTER_POINT";
	public static final String CENTER_POINT ="CENTER_POINT";
//	public static final String CHARACTER_FONT ="CHARACTER_FONT";
	
	public static final String PERCENTAGE_UPPER ="PERCENTAGE_UPPER";	
	
//	public static final String UNITS ="UNITS";
	
	public static final String TIMESERIES_PREDICTION_SHARP ="TIMESERIES_PREDICTION_SHARP";
	public static final String TIMESERIES_PREDICTION_ORIGIN ="TIMESERIES_PREDICTION_ORIGIN";
	public static final String TIMESERIES_PREDICTION_PREDICT ="TIMESERIES_PREDICTION_PREDICT";
//	public static final String TIMESERIES ="TIMESERIES";
	
	public static final String PRE_TRN ="PRE_TRN";
	public static final String PRE_TRAPEV ="PRE_TRAPEV";	
	public static final String PRE_TRAPOV ="PRE_TRAPOV";	
	public static final String PRE_TRIVP ="PRE_TRIVP";	
	public static final String PRE_NTRN ="PRE_NTRN";	
	public static final String PRE_NTRAPEV ="PRE_NTRAPEV";	
	public static final String PRE_NTRAPOV ="PRE_NTRAPOV";	
	public static final String PRE_NTRIVP ="PRE_NTRIVP";	
	
	public static final String SVM_NSVS= "SVM_NSVS";
	public static final String SVM_IND_DIM= "SVM_IND_DIM";
	public static final String VS_THRESHOLD_CATEGORY="VS_THRESHOLD_CATEGORY";
	public static final String VS_THRESHOLD_NUMBER="VS_THRESHOLD_NUMBER";
	public static final String VS_COLUMN_NAME="VS_COLUMN_NAME";
	public static final String VS_SCORE="VS_SCORE";	
	
	public static final String PCA_OUTPUTTABLE ="PCA_OUTPUTTABLE";
	public static final String PCA_VALUEOUTPUTTABLE ="PCA_VALUEOUTPUTTABLE";


	public static final String ADABOOST_SUMMARY ="ADABOOST_SUMMARY";
	public static final String ADABOOST_NAME ="ADABOOST_NAME";
	public static final String ADABOOST_WEIGHT ="ADABOOST_WEIGHT";
	
	public static final String BARS_EXCEED_LIMIT = "BARS_EXCEED_LIMIT";
	public static final String FREQUENCY_EXCEED_LIMIT = "FREQUENCY_EXCEED_LIMIT";
	public static final String BARS_EXCEED_LIMIT_SHOW_ORDER_DESCENDING = "BARS_EXCEED_LIMIT_SHOW_ORDER_DESCENDING";

    public static final String APPROXIMATE_VALUES = "APPROXIMATE_VALUES";
	
	public static final String ALGORITHM_DID_NOT_CONVERGE = "ALGORITHM_DID_NOT_CONVERGE";
	public static final String ITERATION = "ITERATION";

	public static final String MODEL = "MODEL";

	public static final String Output_Image = "Output_Image";

	public static final String Node_Description = "Node_Description";

	public static final String Node_Name = "Node_Name";
 

	public static final String QuantileModel = "QuantileModel";

	public static final String Result_Column = "Result_Column";

	public static final String Data_Type = "Data_Type";

	public static final String Window_Function = "Window_Function";

	public static final String Window_Specification = "Window_Specification";

	public static final String Alias = "Alias";

	public static final String Expression = "Expression";

	public static final String Groupby_Fields = "Groupby_Fields";

	public static final String Parent_Fields = "Parent_Fields";

	public static final String Column_Name = "Column_Name";

	public static final String Selected_Columns = "Selected_Columns";

	public static final String OPERATOR_IS_START = "operator_start";

	public static final String PROCESS_FINISHED = "process_finished";

	public static final String PROCESS_STOP = "process_stop";

	public static final String PROCESS_ERROR = "process_error";

	public static final String OPERATOR_FINISHED = "operator_finished";

	public static final String PROCESS_START = "process_start";

	public static final String Axis = "Axis";

	public static final String Cetner_Point = "Cetner_Point"; 

	public static final String Cluster_Point = "Cluster_Point";  

	public static final String Naive_Bayes = "Naive_Bayes" ;
	public static final String Decision_Tree = "Decision_Tree";
	public static final String Logistic_Regression = "Logistic_Regression";
	public static final String SVM_Classification = "SVM_Classification";
	public static final String Cart_Tree = "Cart_Tree";
	public static final String Neural_Network = "Neural_Network";

	public static final String THRESHOLD = "THRESHOLD";

	public static final String SCORE = "SCORE";

	public static final String ID = "ID";

	public static final String WOE_VALUE = "WOE_VALUE";

	public static final String WOE_OPTION_VALUE = "WOE_OPTION_VALUE";

	public static final String WOE_Bottom = "WOE_Bottom";

	public static final String WOE_Upper = "WOE_Upper";
	
	public static final String HISTOGRAM_BY_NUMBER = "histogram_by_number";
	public static final String HISTOGRAM_BY_WIDTH = "histogram_by_width";
	public static final String HISTOGRAM_MAXIMUM = "histogram_maximum";
	public static final String HISTOGRAM_MINIMUM = "histogram_minimum";
	
	public static final String REPLACE_SUCCESS = "Replace_Succeful";
	public static final String REPLACE_OPERATOR_NUMBER= "Replace_Operator_Number";

    public static final  String Logistic_Regression_Group_By_Value = "Logistic_Regression_Group_By_Value";

    public static final String Hadoop_KMEANS_NOTSTABLE = "KMEANS_NOTSTABLE";

	public static final String OOB_VALUE_LOSS = "OOB_VALUE_LOSS";

	public static final String OOB_Estimate_Error = "OOB_Estimate_Error";
	
	public static final String EM_CLUSTER_INFO ="EM_CLUSTER_INFO";
	public static final String EM_CLUSTER_VALUE ="EM_CLUSTER_VALUE";
}



