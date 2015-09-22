/**
 * ClassName LanguagePack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual.resource;
        
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
/**
 * 
 * @author jimmy
 *
 */
public class VisualLanguagePack {

public static final String Bundle_Name="com.alpine.datamining.api.impl.visual.resource.language" ; 
	
	public static final List<Locale> Supported_Locales =Arrays.asList(new Locale[]{ 
			Locale.CHINA,Locale.CHINESE,Locale.ENGLISH,Locale.US,Locale.JAPAN ,Locale.JAPANESE}); 
 
	public static final Locale Default_Locale = Locale.ENGLISH; 

	private static final HashMap<Locale,ResourceBundle> resourceMap = new HashMap<Locale,ResourceBundle>();

	static{
		
		for(int i = 0;i<Supported_Locales.size();i++){
			Locale locale = Supported_Locales.get(i);
			 ResourceBundle rb =null;
			if(locale==Locale.US){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.ENGLISH ) ;
			} else if(locale==Locale.CHINA){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.CHINESE);
			}else if(locale==Locale.JAPAN){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.JAPANESE);
			}
			else{
			   rb = ResourceBundle.getBundle(Bundle_Name,locale ) ;
			 }
			 resourceMap.put(locale, rb);
			 
		}
		
	}
	
	public static String getMessage(String key, Locale locale){
 		if(locale==null||Supported_Locales.contains(locale)==false){
 			locale = Default_Locale;
 		} 
 		ResourceBundle rb =resourceMap.get(locale) ;
 		if(rb!=null){
 			return  rb.getString(key);
 		}else{
 			return "";
 		}
 	}
 
	
 
	
	public static final String CONFIDENCE =  "CONFIDENCE";
	public static final String PREMISE =  "PREMISE";
	public static final String CONCLUSION =  "CONCLUSION";
	public static final String SUPPORT =  "SUPPORT";
	
	public static final String CLUSTER_COLUMN_NAME =  "CLUSTER_COLUMN_NAME";
	public static final String CLUSTER_COUNT =  "CLUSTER_COUNT";
	public static final String AVG_MEASUREMENT =  "AVG_MEASUREMENT";
	public static final String NOT_SPECIFIED =  "NOT_SPECIFIED";
	
	public static final String SELECTED =  "SELECTED";
	public static final String FIELD_NAME =  "FIELD_NAME";
	
	public static final String COUNT =  "COUNT";
	public static final String PERCENTAGE =  "PERCENTAGE";
	
	public static final String BIN =  "BIN";
	public static final String BEGIN =  "BEGIN";
	public static final String END =  "END";
	public static final String ACC_COUNT =  "ACC_COUNT";
	public static final String ACC_PERCENTAGE =  "ACC_PERCENTAGE";
	public static final String SUMMARY =  "SUMMARY";
	
	public static final String NO_OF_BIN =  "NO_OF_BIN";
	public static final String FIELD_VALUE_NOT_VALID =  "FIELD_VALUE_NOT_VALID";
	
	public static final String TOTAL_VALUE_COUNT =  "TOTAL_VALUE_COUNT";
	public static final String UNIQUE_VALUE_COUNT =  "UNIQUE_VALUE_COUNT";
	public static final String NULL_COUNT =  "NULL_COUNT";
	public static final String EMPTY_COUNT =  "EMPTY_COUNT";
	public static final String ZERO_COUNT =  "ZERO_COUNT";
	public static final String POS_VALUE_COUNT =  "POS_VALUE_COUNT";
	public static final String NEG_VALUE_COUNT =  "NEG_VALUE_COUNT";
	public static final String VALUE_SHAPE_ANALYSIS =  "VALUE_SHAPE_ANALYSIS";
	public static final String COUNT_SHAPE_ANALYSIS =  "COUNT_SHAPE_ANALYSIS";
	
	
	public static final String COLUMN_NAME =  "COLUMN_NAME";
	public static final String VALUE =  "VALUE";
	public static final String DATA_TYPE =  "DATA_TYPE";
	public static final String VARIABLES=  "VARIABLES";
	public static final String STATES =  "STATES";
	public static final String POPULATION =  "POPULATION";
	public static final String CLUSTER = "CLUSTER";
	public static final String MESSAGE_TITLE =  "MESSAGE_TITLE";
	public static final String DATA_TITLE =  "DATA_TITLE";
	public static final String WARING_MESSAGE_TITLE =  "WARING_MESSAGE_TITLE";
	public static final String CLUSTER_PROFILES =  "CLUSTER_PROFILES";
	public static final String SHAPE_ANALYSIS_TITLE =  "SHAPE_ANALYSIS_TITLE";
	public static final String MIN_VALUE =  "MIN_VALUE";
	public static final String MAX_VALUE =  "MAX_VALUE";
	public static final String TOP_N =  "TOP_N";
	public static final String STANDARD_DEVIATION =  "STANDARD_DEVIATION";
	public static final String AVERAGE =  "AVERAGE";
	
	public static final String DENSITY =  "DENSITY";

	public static final String FALSE_POSITIVE_RATE =  "FALSE_POSITIVE_RATE";
	public static final String SENSITIVITY =  "SENSITIVITY";
	
	public static final String ROC_CURVE =  "ROC_CURVE";
	public static final String RANDOM =  "RANDOM";
	public static final String LIFT_CURVE =  "LIFT_CURVE";
	public static final String TYPE =  "TYPE";
	public static final String BOX_AND_WHISKER_SHOW =  "BOX_AND_WHISKER_SHOW";
	public static final String SCATTER_POINT =  "SCATTER_POINT";
	public static final String CENTER_POINT =  "CENTER_POINT";
	public static final String CHARACTER_FONT =  "CHARACTER_FONT";
	
	public static final String PERCENTAGE_UPPER =  "PERCENTAGE_UPPER";	
	
	public static final String UNITS =  "UNITS";	
	
	public static final String TIMESERIES_PREDICTION_SHARP =  "TIMESERIES_PREDICTION_SHARP";
	public static final String TIMESERIES_PREDICTION_ORIGIN =  "TIMESERIES_PREDICTION_ORIGIN";
	public static final String TIMESERIES_PREDICTION_PREDICT =  "TIMESERIES_PREDICTION_PREDICT";
	public static final String TIMESERIES =  "TIMESERIES";	
	
	public static final String PRE_TRN =  "PRE_TRN";
	public static final String PRE_TRAPEV =  "PRE_TRAPEV";	
	public static final String PRE_TRAPOV =  "PRE_TRAPOV";	
	public static final String PRE_TRIVP =  "PRE_TRIVP";	
	public static final String PRE_NTRN =  "PRE_NTRN";	
	public static final String PRE_NTRAPEV =  "PRE_NTRAPEV";	
	public static final String PRE_NTRAPOV =  "PRE_NTRAPOV";	
	public static final String PRE_NTRIVP =  "PRE_NTRIVP";	
	
	public static final String SVM_NSVS=   "SVM_NSVS";
	public static final String SVM_IND_DIM=   "SVM_IND_DIM";
	public static final String VS_THRESHOLD_CATEGORY= "VS_THRESHOLD_CATEGORY";
	public static final String VS_THRESHOLD_NUMBER= "VS_THRESHOLD_NUMBER";
	public static final String VS_COLUMN_NAME= "VS_COLUMN_NAME";
	public static final String VS_SCORE= "VS_SCORE";	
	
	public static final String PCA_OUTPUTTABLE =  "PCA_OUTPUTTABLE";
	public static final String PCA_VALUEOUTPUTTABLE =  "PCA_VALUEOUTPUTTABLE";


	public static final String ADABOOST_SUMMARY =  "ADABOOST_SUMMARY";
	public static final String ADABOOST_NAME =  "ADABOOST_NAME";
	public static final String ADABOOST_WEIGHT =  "ADABOOST_WEIGHT";
	
	public static final String SCATTER_MATRIX_CORRELATION =  "SCATTER_MATRIX_CORRELATION";
	public static final String SCATTER_MATRIX_IMAGE =  "SCATTER_MATRIX_IMAGE";

	public static final String RESIDUALPLOT_TITLE = "RESIDUALPLOT_TITLE";
	public static final String Q_Q_PLOT_TITLE = "Q_Q_PLOT_TITLE";
	

	public static final String RESIDUAL = "RESIDUAL";
	public static final String FITVALUE = "FITVALUE";
	public static final String SAMPLE_QUANTILE = "SAMPLE_QUANTILE";
	public static final String THEORY_QUANTILE = "THEORY_QUANTILE";
	
	
	public static final String PredictTable  = "PredictTable";
	public static final String PLDADocTopicOutputTable  = "PLDADocTopicOutputTable";
	public static final String PLDAModelOutputTable = "PLDAModelOutputTable";
	public static final String topicOutTable  = "TopicOutTable";
	public static final String docTopicOutTable = "DocTopicOutTable";
	
	public static final String SCATTERMATRIX_VALUE = "SCATTERMATRIX_VALUE";
	public static final String SCATTERMATRIX_LINE = "SCATTERMATRIX_LINE";
	
	public static final String NB_TOOMANY_DISTINCT = "NB_TOOMANY_DISTINCT";
	
	public static final String SPLITMODEL_SUMMARY = "SPLITMODEL_SUMMARY";
	public static final String SPLITMODEL_GROUP = "SPLITMODEL_GROUP";
	public static final String SPLITMODEL_ITERATION = "SPLITMODEL_ITERATION";
	public static final String SPLITMODEL_IS_CONVERGE = "SPLITMODEL_IS_CONVERGE";
}




