package com.alpine.miner.framework;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
/**   
 * ClassName:SqlUtil  
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29    
 *  
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.    
 */
public class SqlUtil {

	public static String getIncrement(){
		return UUID.randomUUID().toString();
	}
	public static String getCurrentDate(){
		Calendar ca = Calendar.getInstance();   
		ca.setTime(new java.util.Date());   
		SimpleDateFormat simpledate = new SimpleDateFormat("yyyy-MM-dd");   
		String date = simpledate.format(ca.getTime());
		return date;
	}
	
	public static String getCurrentDateTime(){
		Calendar ca = Calendar.getInstance();   
		ca.setTime(new java.util.Date());   
		SimpleDateFormat simpledate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		String date = simpledate.format(ca.getTime());
		return date;
	}
}
