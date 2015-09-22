/**
 * ClassName WrongUsedException.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.exception;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.alpine.datamining.operator.Operator;
import com.alpine.resources.AlpineThreadLocal;


/**
 * Exception class which are thrown due to a user error. <br>
 * In order to create a WrongUsedException, do the following:
 * 
 * @author Eason
 */
public class WrongUsedException extends OperatorException implements WrongUsed {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2499108522410365963L;
	private static final MessageFormat formatter = new MessageFormat("");

	public static final String BundleName="com.alpine.datamining.resources.AlpineDataAnalysisUserErrorMessages" ;
	
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

	public static String getMessage(String key){
		return getMessage(key, AlpineThreadLocal.getLocale());
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
	private String name;
	
	private String errorMessage;

	private transient Operator operator;

	/**
	 * Creates a new WrongUsedException.
	 * 
	 * @param operator
	 *            The {@link Operator} in which the exception occured.
	 * @param cause
	 *            The exception that caused the user error. May be null. Using
	 *            this makes debugging a lot easier.
	 * @param name
	 *            The error name referring to a message in the file
	 *            <name>UserErrorMessages.properties</name>
	 * @param arguments
	 *            Arguments for the short message.
	 */
	public WrongUsedException(Operator operator, Throwable cause, String name, Object ... arguments) {
		super(getErrorMessage(name, arguments), cause);
		this.errorMessage = getErrorMessage(name, arguments);
		this.name = name;
		this.operator = operator;
	}

	public WrongUsedException(Operator operator, String name, Object ... arguments) {
		this(operator, null, name, arguments);
	}

	/** Convenience constructor for messages with no arguments. */
	public WrongUsedException(Operator operator, String name) {
		this(operator, null, name, new Object[0]);
	}

	public String getMessage() {
		return getResourceString(name,"Message missing.");
//		return super.getMessage();
	}


	public String getName() {
		return name;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	public String getErrorMessage() {
		return errorMessage;
	}

	public static String getErrorMessage(String name, Object[] arguments) {
		String message = getResourceString(name, "No message.");
		try {
			formatter.applyPattern(message);
			String formatted = formatter.format(arguments);
			return formatted;
		} catch (Throwable t) {
			return message;
		}
	}

	/**
	 * Returns a resource message for the given error name.
	 * 
	 * @param key
	 *            one out of &quot;name&quot;, &quot;short&quot;,
	 *            &quot;long&quot;
	 */
	public static String getResourceString(String name, String deflt) {
		try {
			return getMessage(name);
		} catch (java.util.MissingResourceException e) {
			return deflt;
		}
	}


}
