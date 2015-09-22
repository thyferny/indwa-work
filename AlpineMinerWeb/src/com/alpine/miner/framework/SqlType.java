package com.alpine.miner.framework;
/**   
 * ClassName:SqlType   
 *   
 * Author   kemp zhang   
 *
 * Version  Ver 1.0
 *   
 * Date     2011-3-29    
 *  
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.    
 */
public class SqlType {

 	public static boolean isVarchar(int type){
		if(type == java.sql.Types.VARCHAR){
			return true;
		}
		return false;
	}
	public static boolean isNumber(int type){
		if(type == java.sql.Types.NUMERIC||type == java.sql.Types.DECIMAL){
			return true;
		}
		return false;
	}
	
	public static boolean isDateTime(int type){
		if(type == java.sql.Types.TIME){
			return true;
		}
		return false;
	}
	
	public static boolean isDate(int type){
		if(type == java.sql.Types.DATE){
			return true;
		}
		return false;
	}
}
