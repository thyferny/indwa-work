/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * OperatorHelpConvertion
 * Nov 21, 2011
 */
package com.alpine.miner.impls.onlinehelp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Gary
 *
 */
public class OperatorHelpConvertion {
    private static Logger itsLogger = Logger.getLogger(OperatorHelpConvertion.class);

    private static Properties prop = initializeResources();
	
	private static String PATH_SIGN = "/";
	
	private Locale locale;
	
	private static Properties initializeResources(){
		Properties prop = new Properties();
		InputStream is = null;
		String root = null;
		try {
			root = OperatorHelpConvertion.class.getResource("/").toURI().getPath();
			is = new FileInputStream(root + "onlineHelp.inf");
			prop.load(is);
		} catch (Exception e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				//ignore
			}
		}
		return prop;
	}
	
	private OperatorHelpConvertion(Locale locale){
		this.locale = locale;
	}
	
	public static OperatorHelpConvertion getInstance(Locale locale){
		return new OperatorHelpConvertion(locale);
	}
	
	public String getHelpVal(String operatorKey){
		return locale.getLanguage() + PATH_SIGN + prop.getProperty(operatorKey);
//		return "en/" + operatorKey + ".html";
	}
}
