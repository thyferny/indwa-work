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

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.DataAnalyzer;
import com.alpine.datamining.exception.WrongUsedException;
/**
 * 
 * @author John Zhao
 *
 */
public class AnalysisError extends AnalysisException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6990356729035084609L;

	public static final String Bundle_Name="com.alpine.datamining.api.resources.AnalysisErrorMessage";
	
	public String name;

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
	
	public String message;//include resolution...
	
	DataAnalyzer analyzer;

	//this is for web nls use...
	private Object[] arguments; 

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	//can support the arguments in messages
	public AnalysisError(DataAnalyzer analyzer,String name, Locale locale, Object ... arguments){
		super(getErrorMessage(name,locale, arguments));
		this.arguments=arguments;
		this.analyzer=analyzer;
		setName(name);
//		setErrorName(getResourceString(errorName,""));
		setMessage(getErrorMessage(name,locale, arguments));
	}

	public AnalysisError(DataAnalyzer analyzer, Locale locale,String name){
		super(getResourceString(name,"",locale));
		this.analyzer=analyzer;
		setName(name);
//		setErrorName(getResourceString(errorCode,""));	 
		setMessage(getResourceString(name,"",locale));
	}
	//mostly for dataanalysis
	public AnalysisError(DataAnalyzer analyzer,WrongUsedException wrongUsedException){
		super(wrongUsedException.getErrorMessage(),wrongUsedException);
		this.analyzer=analyzer;
		setName(wrongUsedException.getName());
//		setErrorName(userError.getErrorName());
 		setMessage(wrongUsedException.getErrorMessage());
	}

	//who cause this error
	public String getSource(){
		return analyzer.getName();
	}
 
    public String getFullMessage() {
    	StringBuffer sb =new StringBuffer();
    	sb.append("AnalysisError:\n" +
    			  "Error Source: "+getSource()+"\n");
//    	sb.append("Error Name:   "+getErrorName()+"\n");
    	sb.append("Error Name:   "+getName()+"\n");    	
    	sb.append("Error Message:"+getErrorMsg() +"\n");
//    	sb.append("Error Detail: "+getDetail()+"\n");
        return sb.toString();
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public String getErrorName() {
//		return errorName;
//	}
//
//	public void setErrorName(String errorName) {
//		this.errorName = errorName;
//	}

 
	public String getMessage() {
		return message; 
	}

	public void setMessage(String message) {
		this.message = message;
	}
	public static String getResourceString(String name, String defaultValue,Locale locale) {
 		if(Supported_Locales.contains(locale)==false){
 			locale = Default_Locale;
 		} 
 		ResourceBundle rb =resourceMap.get(locale);
 		if(rb!=null){
 			try {
 				return rb.getString(name);
 			} catch (java.util.MissingResourceException e) {
 				return defaultValue;
 			}
 		}else{
 			return defaultValue;
 		}
	}

	public void printStackTrace(PrintStream s) {
		s.print("AnalysisError: \n" +
				"Error Source: "+getSource()+"\n");
		s.print("Error Name:   "+getName()+"\n");
		s.print("Error Message:"+ getErrorMsg()+"\n");
	}
	
	public static String getErrorMessage(String name,Locale locale, Object[] arguments) {
	
		String message = getResourceString(name, "",locale);
		try {
			formatter.applyPattern(message);
			String formatted = formatter.format(arguments);
			return formatted; 
		} catch (Throwable t) {
			return message;
		}
	}
}

