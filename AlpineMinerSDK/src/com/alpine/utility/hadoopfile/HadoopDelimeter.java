package com.alpine.utility.hadoopfile;

import java.util.HashMap;
import java.util.Map;

public class HadoopDelimeter {
	private static final  Map<String,String> delimeters;
	static{
		delimeters=new HashMap<String,String>();
		delimeters.put("comma".toLowerCase(), ",");
		delimeters.put("tab".toLowerCase(), "\\t");
		delimeters.put("semicolon".toLowerCase(), ";");
		delimeters.put("space".toLowerCase(), "\\s");
		
		delimeters.put(",", "\\,");
		delimeters.put(".", "\\.");
		delimeters.put("|", "\\|");
		delimeters.put("", "\\s");
		delimeters.put(" ", "\\s");
		
		
		
	}
	
	public static String getDelimeter(String str){
		if (null == str)
			return null;
		String val = delimeters.get(str.toLowerCase());
		if (null == val)
			return str;
		return val;
	}
	
	
	
}
