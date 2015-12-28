
package com.alpine.datamining.resources;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AlpineDataAnalysisLanguagePack {
	public static final String BundleName="com.alpine.datamining.resources.alpineDataAnalysis" ;
	
	private static final MessageFormat formatter = new MessageFormat("");

	
	public static final List<Locale> SupportedLocales =Arrays.asList(new Locale[]{ 
			Locale.CHINA,Locale.CHINESE,Locale.ENGLISH,Locale.US,Locale.JAPAN ,Locale.JAPANESE}); 
 
	public static final Locale DefaultLocale = Locale.ENGLISH; 

 
	private static final HashMap<Locale,ResourceBundle> resourceMap = new HashMap<Locale,ResourceBundle>();

	static{
		
		for(int i = 0;i<SupportedLocales.size();i++){
			Locale locale = SupportedLocales.get(i);
			 ResourceBundle rb =null;
		  if(locale==Locale.CHINESE){
				   rb = ResourceBundle.getBundle(BundleName,Locale.CHINA);
			}else if(locale==Locale.JAPANESE){
				   rb = ResourceBundle.getBundle(BundleName,Locale.JAPAN);
			}else if(locale==Locale.ENGLISH){
				   rb = ResourceBundle.getBundle(BundleName,Locale.US);
			}
			else{
			   rb = ResourceBundle.getBundle(BundleName,locale  );
			 }
			 resourceMap.put(locale, rb);
		}
		
	}
 
 	public static String getMessage(String key, Locale locale){
 		if(SupportedLocales.contains(locale)==false){
 			locale = DefaultLocale;
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
//	static Locale locale = Locale.getDefault();
//	public static ResourceBundle rb = ResourceBundle.getBundle("com.alpine.datamining.resources.alpineDataAnalysis",locale);
	public static final String CREATE_OPERATOR_FAILED = "CREATE_OPERATOR_FAILED";
	public static final String KMEANS_NOTSTABLE = "KMEANS_NOTSTABLE";
	public static final String KMEANS_LESS_K = "KMEANS_LESS_K";
	public static final String ALGORITHM_DID_NOT_CONVERGE = "ALGORITHM_DID_NOT_CONVERGE";
	public static final String CORRELATION_COEFFICIENT = "CORRELATION_COEFFICIENT";
	public static final String CORRELATION_NAME = "CORRELATION_NAME";
	public static final String CORRELATION_COLUMN = "CORRELATION_COLUMN";
	public static final String HISTOGRAM_NAME = "HISTOGRAM_NAME";
	public static final String HISTOGRAM_COLUMN = "HISTOGRAM_COLUMN";
	public static final String GOOD_VALUE = "GOOD_VALUE";
	public static final String DEPENDENT_COLUMN = "DEPENDENT_COLUMN";
	public static final String NORMALIZATION_EXIST = "NORMALIZATION_EXIST";
	public static final String DEPENDENT_COLUMN_NONUMERIC = "DEPENDENT_COLUMN_NONUMERIC";
	public static final String DEPENDENT_COLUMN_MORETHAN2 = "DEPENDENT_COLUMN_MORETHAN2";
	public static final String TABLE_EXIST_NULL = "TABLE_EXIST_NULL";
	public static final String ITERATION = "ITERATION";
	public static final String MATRIX_IS_SIGULAR="MATRIX_IS_SIGULAR";
	public static final String LR_NAME="LR_NAME";
	public static final String LR_DEPENDENT_2_VALUE="LR_DEPENDENT_2_VALUE";
	public static final String GOOD_VALUE_NOT_EXIST="GOOD_VALUE_NOT_EXIST";
	public static final String NOT_SUPPORT_ARRAY_ASSOCIATION="NOT_SUPPORT_ARRAY_ASSOCIATION";
	public static final String NOT_SUPPORT_TYPE_ASSOCIATION="NOT_SUPPORT_TYPE_ASSOCIATION";
	public static final String NOT_SUPPORT_DIFF_TYPE_ASSOCIATION="NOT_SUPPORT_DIFF_TYPE_ASSOCIATION";
	public static final String DB2_PREDICT_WRONG_DEPENDENTCOLUMN = "DB2_PREDICT_WRONG_DEPENDENTCOLUMN";
}

