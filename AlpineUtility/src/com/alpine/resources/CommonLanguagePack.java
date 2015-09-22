/**
 * ClassName LanguagePack.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.resources;

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
public class CommonLanguagePack {
	public static final String Bundle_Name="com.alpine.resources.CommonMessage";
	
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
	public static final String Unsupported_Database_type = "Unsupported_Database_type"; 

	public static final String JDBC_Driver_Not_Found = "JDBC_Driver_Not_Found";

	public static final String Database_connection_error = "Database_connection_error";

	public static final String Network_Interface_error = "Network_Interface_error";
	
	public static final String JDBC_LOAD_ERROR= "JDBC_LOAD_ERROR";
	
	public static final String Unknown_Database_type= "Unknown_Database_type";
	
	public static final String Network_Interface_PORT_error = "Network_Interface_PORT_error";
	
	public static final String Connection_TIME_OUT = "Connection_TIME_OUT";
 
	public static final String RESOURCE_NOT_NULL = "RESOURCE_NOT_NULL";
	
	public static final String CHARACTOR_NOT_SUPPORT = "CHARACTOR_NOT_SUPPORT";
}

