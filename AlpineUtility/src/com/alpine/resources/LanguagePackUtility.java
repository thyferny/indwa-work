package com.alpine.resources;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguagePackUtility {

	
	private static LanguagePackUtility language; 
	public static LanguagePackUtility getInstance(){
		if(language == null){
			language = new LanguagePackUtility();
		}
		return language;
	}
	public static Locale locale = Locale.getDefault();
	public static ResourceBundle rb = ResourceBundle.getBundle("com.alpine.resources.language",locale);
	public static final String INVALID_LICENSE_KEY =  rb.getString("INVALID_LICENSE_KEY");
	public static final String MACHINE_UNMATCHED =  rb.getString("MACHINE_UNMATCHED");
	public static final String EXPIRED_LICENSE =  rb.getString("EXPIRED_LICENSE");
	
	public static final String USER_UNMATCH =  rb.getString("USER_UNMATCH");
	public static final String COMPANY_UNMATCH =  rb.getString("COMPANY_UNMATCH");
}

