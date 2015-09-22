/**
 * ClassName :ErrorNLS.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.error;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author zhaoyong
 *
 */
public class ErrorNLS {

	public static final String Bundle_Name="com.alpine.miner.error.AnalysisErrorMessage" ;
	
	private static final MessageFormat formatter = new MessageFormat("");

	
	public static final List<Locale> Supported_Locales =Arrays.asList(new Locale[]{ 
			Locale.CHINA,Locale.CHINESE,Locale.ENGLISH,Locale.US,Locale.JAPAN ,Locale.JAPANESE}); 
 
	public static final Locale Default_Locale = Locale.ENGLISH; 

	private static final HashMap<Locale,ResourceBundle> resourceMap = new HashMap<Locale,ResourceBundle>();

	static{
		
		for(int i = 0;i<Supported_Locales.size();i++){
			Locale locale = Supported_Locales.get(i);
			 ResourceBundle rb =null;
			if(locale == Locale.ENGLISH || locale==Locale.US){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.ENGLISH  );
			}else if(locale==Locale.CHINA || locale == Locale.CHINESE){
				   rb = ResourceBundle.getBundle(Bundle_Name,Locale.CHINESE);
			}else if(locale==Locale.JAPAN || locale == Locale.JAPANESE){
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
 
 
	public static final String UDF_MODEL_NOT_FOUND = "UDF_MODEL_NOT_FOUND";

	public static final String FILE_NOT_EXISTS = "FILE_NOT_EXISTS";

	public static final String Can_Not_Replace_Itself = "Can_Not_Replace_Itself"; 
	public static final String Timeseries_ID_Not_Dicsinct = "timeseries_ID_not_dicsinct" ;
	public static final String Can_Not_Drop = "Can_Not_Drop";

	public static final String CAN_NOT_OPEN_FILE = "CAN_NOT_OPEN_FILE"; 	 
 

	public static final String Group_already_exist = "Group_already_exist";

	public static final String Group_used_by_user = "Group_used_by_user";

	public static final String User_already_exist = "User_already_exist";

	public static final String User_not_exists = "User_not_exists";

	public static final String Group_not_found = "Group_not_found";
	
	public static final String Message_Unknow_Error = "message_unknow_error";
	
	public static final String SUBFLOW_CYCLE = "subflow_cycle";

    public static final String HadoopFileOperator_Connection_Error = "Hadoop_File_Operator_Connection_error";
    
    public static final String BADCOLUMN_IDX_PIG = "BADCOLUMN_IDX_PIG";
	public static final String BADDATA_COUNT_PIG = "BADDATA_COUNT_PIG";
	public static final String BADDATA_COUNT_MR = "BADDATA_COUNT_MR";
	



//    public static final String HadoopFileOperator_File_Error = "HadoopFileOperator_File_Error";

}
